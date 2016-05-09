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
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;

import hu.bme.hit.smartparkingassist.communication.SendLotAvailabilityTask;
import hu.bme.hit.smartparkingassist.fragment.FindFreeLotFragment;
import hu.bme.hit.smartparkingassist.service.LocationService;
import hu.bme.hit.smartparkingassist.service.ObdService;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainMenuActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link FindFreeLotFragment}.
 */
public class FindFreeLotActivity extends AppCompatActivity
        implements FindFreeLotFragment.IFindFreeLotFragment {

    Location currentLocation = null;

    Intent i = null;
    LocationService myLocationService;
    boolean isBound = false;

    @Override
    public double getLatitude() {
        if (currentLocation != null &&
                currentLocation.getTime() + MainMenuActivity.THREE_MINUTE > System.currentTimeMillis()) {
            return currentLocation.getLatitude();
        } else {
            return 0;
        }
    }

    @Override
    public double getLongitude() {
        if (currentLocation != null &&
                currentLocation.getTime() + MainMenuActivity.THREE_MINUTE > System.currentTimeMillis()) {
            return currentLocation.getLongitude();
        } else {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_free_lot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            FindFreeLotFragment fragment = FindFreeLotFragment.newInstance(getIntent().getStringExtra(FindFreeLotFragment.KEY_TITLE_DESCRIPTION_QUERY));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.find_free_lot_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        i = new Intent(getApplicationContext(),LocationService.class);
        bindService(i, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, MainMenuActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
