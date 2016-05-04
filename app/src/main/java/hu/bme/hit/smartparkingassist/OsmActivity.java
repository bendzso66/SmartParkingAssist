package hu.bme.hit.smartparkingassist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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

import hu.bme.hit.smartparkingassist.adapters.WayAdapter;
import hu.bme.hit.smartparkingassist.communication.FindFreeLotFromAddressTask;
import hu.bme.hit.smartparkingassist.communication.GetRoadPointsTask;
import hu.bme.hit.smartparkingassist.items.WayItem;

public class OsmActivity extends Activity {

    private MapView map;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm);
        map = (MapView) findViewById(R.id.osm_map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        new GetRoadPointsTask(this).execute();

        GeoPoint startPoint = new GeoPoint(47.4986276, 19.0700189);
        IMapController mapController = map.getController();
        mapController.setZoom(20);
        mapController.setCenter(startPoint);

        map.invalidate();
    }

        @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetRoadPointsTask.GET_ROAD_POINTS_FILTER);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GetRoadPointsTask.GET_ROAD_POINTS_FILTER)) {
                Road road = intent.getParcelableExtra(GetRoadPointsTask.GET_ROAD_POINTS_ROADS_KEY);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8, context);
                map.getOverlays().add(roadOverlay);
                map.invalidate();
            }
        }
    };

}
