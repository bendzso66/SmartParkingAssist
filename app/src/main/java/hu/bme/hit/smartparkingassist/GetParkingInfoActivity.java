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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.bme.hit.smartparkingassist.adapters.WayAdapter;
import hu.bme.hit.smartparkingassist.communication.SendLotAvailabilityTask;
import hu.bme.hit.smartparkingassist.communication.SendParkingConditionTask;
import hu.bme.hit.smartparkingassist.communication.SendParkingLaneTask;
import hu.bme.hit.smartparkingassist.service.LocationService;
import hu.bme.hit.smartparkingassist.service.ObdService;

public class GetParkingInfoActivity extends AppCompatActivity {

    private Location currentLocation = null;
    private Intent i = null;
    private boolean isBound = false;
    private LocationService myLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_parking_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String wayId = intent.getStringExtra(WayAdapter.WAY_ID_KEY);
        final String nameOfWay = intent.getStringExtra(WayAdapter.NAME_OF_WAY_KEY);

        TextView nameOfWayText = (TextView) findViewById(R.id.name_of_way);
        nameOfWayText.setText(nameOfWay);

        List<String> directions = new ArrayList<>();
        directions.add("Unknown");
        directions.add("Parallel");
        directions.add("Diagonal");
        directions.add("Perpendicular");
        directions.add("No parking");
        directions.add("No stopping");
        ArrayAdapter<String> directionsAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, directions);
        directionsAdapter.setDropDownViewResource(R.layout.spinner_item);

        List<String> conditions = new ArrayList<>();
        conditions.add("Unknown");
        conditions.add("Free");
        conditions.add("Ticket");
        ArrayAdapter<String> conditionsAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, conditions);
        conditionsAdapter.setDropDownViewResource(R.layout.spinner_item);

        final Spinner rightDirectionSpinner = (Spinner) findViewById(R.id.right_side_direction);
        rightDirectionSpinner.setAdapter(directionsAdapter);

        final Spinner rightConditionSpinner = (Spinner) findViewById(R.id.right_side_condition);
        rightConditionSpinner.setAdapter(conditionsAdapter);

        final Spinner leftDirectionSpinner = (Spinner) findViewById(R.id.left_side_direction);
        leftDirectionSpinner.setAdapter(directionsAdapter);

        final Spinner leftConditionSpinner = (Spinner) findViewById(R.id.left_side_condition);
        leftConditionSpinner.setAdapter(conditionsAdapter);

        Button sendButton = (Button) findViewById(R.id.send_parking_info_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rightDirection = rightDirectionSpinner.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                String rightCondition = rightConditionSpinner.getSelectedItem().toString().toLowerCase();
                String leftDirection = leftDirectionSpinner.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                String leftCondition = leftConditionSpinner.getSelectedItem().toString().toLowerCase();

                if (rightDirection.equals(leftDirection)) {
                    if (!rightDirection.equals("unknown")) {
                        new SendParkingLaneTask(getApplicationContext()).execute(wayId, "both", rightDirection);
                    }
                } else {
                    if (!rightDirection.equals("unknown")) {
                        new SendParkingLaneTask(getApplicationContext()).execute(wayId, "right", rightDirection);
                    }
                    if (!leftDirection.equals("unknown")) {
                        new SendParkingLaneTask(getApplicationContext()).execute(wayId, "left", leftDirection);
                    }
                }

                if (rightCondition.equals(leftCondition)) {
                    if (!rightCondition.equals("unknown")) {
                        new SendParkingConditionTask(getApplicationContext()).execute(wayId, "both", rightCondition);
                    }
                } else {
                    if (!rightCondition.equals("unknown")) {
                        new SendParkingConditionTask(getApplicationContext()).execute(wayId, "right", rightCondition);
                    }
                    if (!leftCondition.equals("unknown")) {
                        new SendParkingConditionTask(getApplicationContext()).execute(wayId, "left", leftCondition);
                    }
                }

                onBackPressed();
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
