package hu.bme.hit.smartparkingassist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.Utility;
import hu.bme.hit.smartparkingassist.adapters.FreeLotAdapter;
import hu.bme.hit.smartparkingassist.communication.FindFreeLotFromAddressTask;
import hu.bme.hit.smartparkingassist.communication.LogInTask;

public class LogInFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_log_in, container, false);

        final EditText mail = (EditText) rootView.findViewById(R.id.get_email_field);
        final EditText password = (EditText) rootView.findViewById(R.id.get_password_field);

        Button logInButton = (Button) rootView.findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ASDASD", mail.getText().toString());
                Log.d("ASDASD", password.getText().toString());
                new LogInTask(getActivity()).execute(mail.getText().toString(), password.getText().toString());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LogInTask.LOG_IN_FILTER);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(LogInTask.LOG_IN_RESULT_KEY);
            String sessionId = intent.getStringExtra(LogInTask.LOG_IN_SESSION_ID_KEY);
            if (result.equals("Got result.")) {
                Snackbar.make(rootView, "Result is: " + sessionId, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(rootView, result, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };

}
