package hu.bme.hit.smartparkingassist;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import hu.bme.hit.smartparkingassist.communication.SendLotAvailabilityTask;
import hu.bme.hit.smartparkingassist.fragment.FindFreeLotFragment;
import hu.bme.hit.smartparkingassist.fragment.LogInFragment;
import hu.bme.hit.smartparkingassist.fragment.MainMenuFragment;
import hu.bme.hit.smartparkingassist.fragment.RegistrationFragment;
import hu.bme.hit.smartparkingassist.items.MainMenuItem;
import hu.bme.hit.smartparkingassist.service.LocationService;
import hu.bme.hit.smartparkingassist.service.ObdService;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FindFreeLotActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MainMenuFragment} and the item details
 * (if present) is a {@link FindFreeLotFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MainMenuFragment.IMainMenuFragment} interface
 * to listen for item selections.
 */
public class MainMenuActivity extends AppCompatActivity
        implements MainMenuFragment.IMainMenuFragment, FindFreeLotFragment.IFindFreeLotFragment {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    Intent i = null;
    Location currentLocation = null;
    LocationService myLocationService;
    boolean isBound = false;
    public static final int THREE_MINUTE = 3 * 60 * 1000;
    public static final String SELECTED_BLUETOOTH_DEVICE_KEY = "SELECTED_BLUETOOTH_DEVICE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.find_free_lot_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((MainMenuFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onStart() {
        super.onStart();
        i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Callback method from {@link MainMenuFragment.IMainMenuFragment}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(MainMenuItem aItem) {

        String selectedItemTitle = aItem.getTitle();

        if (mTwoPane) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (selectedItemTitle.equals("Find free lot")) {
                FindFreeLotFragment fragment = FindFreeLotFragment.newInstance(aItem.getTitle());
                fragmentTransaction.replace(R.id.find_free_lot_container, fragment);
            } else if (selectedItemTitle.equals("Log in")) {
                LogInFragment fragment = new LogInFragment();
                fragmentTransaction.replace(R.id.find_free_lot_container, fragment);
            } else if (selectedItemTitle.equals("Registration")) {
                RegistrationFragment fragment = new RegistrationFragment();
                fragmentTransaction.replace(R.id.find_free_lot_container, fragment);
            } else if (selectedItemTitle.equals("Settings")) {
                Intent detailIntent = new Intent(this, SettingsActivity.class);
                startActivity(detailIntent);
            } else if (selectedItemTitle.equals("Start OBD monitoring")) {
                showBluetoothDevices();
            }

            fragmentTransaction.commit();

        } else {

            if (selectedItemTitle.equals("Find free lot")) {
                Intent detailIntent = new Intent(this, FindFreeLotActivity.class);
                detailIntent.putExtra(FindFreeLotFragment.KEY_TITLE_DESCRIPTION_QUERY, aItem.getTitle());
                startActivity(detailIntent);
            } else if (selectedItemTitle.equals("Log in")) {
                Intent detailIntent = new Intent(this, LogInActivity.class);
                startActivity(detailIntent);
            } else if (selectedItemTitle.equals("Registration")) {
                Intent detailIntent = new Intent(this, RegistrationActivity.class);
                startActivity(detailIntent);
            } else if (selectedItemTitle.equals("Settings")) {
                Intent detailIntent = new Intent(this, SettingsActivity.class);
                startActivity(detailIntent);
            } else if (selectedItemTitle.equals("Start OBD monitoring")) {
                showBluetoothDevices();
            }
        }
    }

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
                    if (currentLocation.getTime() + THREE_MINUTE > System.currentTimeMillis()) {
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

    private void showBluetoothDevices() {
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isObdEnabled = myPrefs.getBoolean("obd_switch", false);

        if (isObdEnabled) {
            final ArrayList<String> deviceStrs = new ArrayList();
            final ArrayList<String> devices = new ArrayList();
            ArrayList<String> deviceNames = new ArrayList();

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                    deviceNames.add(device.getName());
                }
            }

            CharSequence[] deviceNamesCh = deviceNames.toArray(new CharSequence[deviceNames.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a bluetooth device");
            builder.setItems(deviceNamesCh, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), ObdService.class);
                    intent.putExtra(SELECTED_BLUETOOTH_DEVICE_KEY, devices.get(which));
                    getApplicationContext().startService(intent);
                }
            });
            builder.show();
        } else {
            Snackbar.make(findViewById(android.R.id.content).getRootView(),
                    "Please turn on OBD monitoring!",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

}
