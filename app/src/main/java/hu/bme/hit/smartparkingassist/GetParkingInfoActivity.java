package hu.bme.hit.smartparkingassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import java.util.List;

import hu.bme.hit.smartparkingassist.adapters.WayAdapter;
import hu.bme.hit.smartparkingassist.communication.SendParkingConditionTask;
import hu.bme.hit.smartparkingassist.communication.SendParkingLaneTask;

public class GetParkingInfoActivity extends AppCompatActivity {

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

}
