package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.commons.lang3.text.WordUtils;

import hu.bme.hit.smartparkingassist.R;

public class SendFreeLotTask extends AsyncTask<String, Void, String> {

    public static final String SEND_FREE_LOT_FILTER = "SEND_FREE_LOT_FILTER";
    public static final String SEND_FREE_LOT_RESULT_KEY = "SEND_FREE_LOT_RESULT_KEY";
    private Context ctx;

    public SendFreeLotTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected String doInBackground(String... params) {
        String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
        String lat = params[0];
        String lon = params[1];
        String availability = params[2];

        try {

            //TODO check the returned parameter of readUrl
            AccessServlet.readUrl(serverIpAddress
                    +"sendLotAvailability?&lat="
                    +lat
                    +"&lon="
                    +lon
                    +"&avail="
                    + availability);
        } catch (Exception e) {
            e.printStackTrace();
            return "Couldn't send " + availability + " lot availability.";
        }

        return WordUtils.capitalize(availability)
                + " lot availability was sent successfully!";
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(SEND_FREE_LOT_FILTER);
        intent.putExtra(SEND_FREE_LOT_RESULT_KEY, result);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
