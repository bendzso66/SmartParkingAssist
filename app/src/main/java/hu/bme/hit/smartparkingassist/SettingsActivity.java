package hu.bme.hit.smartparkingassist;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

import java.util.Date;

import hu.bme.hit.smartparkingassist.communication.SendLotAvailabilityTask;
import hu.bme.hit.smartparkingassist.fragment.SettingsFragment;
import hu.bme.hit.smartparkingassist.service.LocationService;
import hu.bme.hit.smartparkingassist.service.ObdService;

public class SettingsActivity extends AppCompatActivity {

    private Location currentLocation = null;
    private Intent i = null;
    private boolean isBound = false;
    private LocationService myLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
        intentFilter.addAction(LocationService.BR_NEW_LOCATION);
        intentFilter.addAction(SendLotAvailabilityTask.SEND_FREE_LOT_FILTER);
        intentFilter.addAction(ObdService.BR_PARKING_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
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
            if (intent.getAction().equals(LocationService.BR_NEW_LOCATION)) {
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
