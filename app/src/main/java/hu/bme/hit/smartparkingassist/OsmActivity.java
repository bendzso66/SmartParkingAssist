package hu.bme.hit.smartparkingassist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Date;

import hu.bme.hit.smartparkingassist.adapters.WayAdapter;
import hu.bme.hit.smartparkingassist.communication.FindFreeLotFromAddressTask;
import hu.bme.hit.smartparkingassist.communication.GetRoadPointsTask;
import hu.bme.hit.smartparkingassist.communication.SendLotAvailabilityTask;
import hu.bme.hit.smartparkingassist.items.WayItem;
import hu.bme.hit.smartparkingassist.service.LocationService;
import hu.bme.hit.smartparkingassist.service.ObdService;

public class OsmActivity extends Activity {

    private MapView map;
    private Location currentLocation = null;
    private Intent i = null;
    private boolean isBound = false;
    private LocationService myLocationService;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm);
        map = (MapView) findViewById(R.id.osm_map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Intent intent = getIntent();
        ArrayList<WayItem> wayItems = intent.getParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY);

        double sumLatitude = 0;
        double sumLongitude = 0;
        for (WayItem wayItem : wayItems) {
            sumLatitude += wayItem.getLatitude1() + wayItem.getLatitude2();
            sumLongitude += wayItem.getLongitude1() + wayItem.getLongitude2();

            GeoPoint centerPoint = new GeoPoint(wayItem.getCenterLatitude(), wayItem.getCenterLongitude());
            Marker startMarker = new Marker(map);
            startMarker.setPosition(centerPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle("Num of spaces: " + wayItem.getFreeSpaces());
            map.getOverlays().add(startMarker);

            new GetRoadPointsTask(this).execute(wayItem);
        }

        IMapController mapController = map.getController();
        mapController.setZoom(18);
        int numOfCoords = 2 * wayItems.size();
        GeoPoint centerPoint = new GeoPoint(sumLatitude / numOfCoords, sumLongitude / numOfCoords);
        mapController.setCenter(centerPoint);

        map.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
        i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

        @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetRoadPointsTask.GET_ROAD_POINTS_FILTER);
            intentFilter.addAction(LocationService.BR_NEW_LOCATION);
            intentFilter.addAction(SendLotAvailabilityTask.SEND_FREE_LOT_FILTER);
            intentFilter.addAction(ObdService.BR_PARKING_STATUS);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(locationServiceConnection);
        isBound = false;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GetRoadPointsTask.GET_ROAD_POINTS_FILTER)) {
                Road road = intent.getParcelableExtra(GetRoadPointsTask.GET_ROAD_POINTS_RESULT_KEY);
                int lineColor = intent.getIntExtra(GetRoadPointsTask.GET_ROAD_COLOR_RESULT_KEY, Color.GRAY);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, lineColor, 8, context);
                map.getOverlays().add(roadOverlay);
                map.invalidate();
            } else if (intent.getAction().equals(LocationService.BR_NEW_LOCATION)) {
                currentLocation = intent.getParcelableExtra(LocationService.KEY_LOCATION);
                Log.d("[LOCATION] latitude: ", ((Double) currentLocation.getLatitude()).toString());
                Log.d("[LOCATION] longitude: ", ((Double) currentLocation.getLongitude()).toString());
                Log.d("[LOCATION] altitude: ", ((Double) currentLocation.getAltitude()).toString());
                Log.d("[LOCATION] speed: ", ((Float) currentLocation.getSpeed()).toString());
                Log.d("[LOCATION] provider: ", currentLocation.getProvider());
                Log.d("[LOCATION] time: ", new Date(currentLocation.getTime()).toString());
            } else if (intent.getAction().equals(SendLotAvailabilityTask.SEND_FREE_LOT_FILTER)) {
                String result = intent.getStringExtra(SendLotAvailabilityTask.SEND_FREE_LOT_RESULT_KEY);
                Snackbar.make(findViewById(android.R.id.content).getRootView(), result, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (intent.getAction().equals(ObdService.BR_PARKING_STATUS)) {
                String availability = intent.getStringExtra(ObdService.PARKING_STATUS_KEY);
                Log.d("[OBDService]", "Broadcast message is received: " + availability);

                if (currentLocation != null) {
                    Log.d("[SendFreeLot] GPS time: ", ((Long) currentLocation.getTime()).toString());
                    Log.d("[SendFreeLot] current millis: ", ((Long) System.currentTimeMillis()).toString());
                    if (currentLocation.getTime() + MainMenuActivity.THREE_MINUTE > System.currentTimeMillis()) {
                        new SendLotAvailabilityTask(getApplicationContext()).execute(String.valueOf(currentLocation.getLatitude()),
                                String.valueOf(currentLocation.getLongitude()),
                                availability);
                    }
                }
            }
        }
    };

    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            myLocationService = binder.getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    };

}
