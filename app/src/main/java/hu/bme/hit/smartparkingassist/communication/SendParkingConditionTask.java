package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import hu.bme.hit.smartparkingassist.R;

public class SendParkingConditionTask extends AsyncTask<String, Void, Void> {

    private Context ctx;

    public SendParkingConditionTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected Void doInBackground(String... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String wayId = params[0];
            String side = params[1];
            String condition = params[2];
            AccessServlet.readUrl(serverIpAddress
                    + "sendParkingCondition?&wayid="
                    + wayId
                    + "&side="
                    + side
                    + "&condition="
                    + condition);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}