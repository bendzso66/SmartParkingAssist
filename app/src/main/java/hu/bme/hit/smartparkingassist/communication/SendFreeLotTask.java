package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import hu.bme.hit.smartparkingassist.R;

public class SendFreeLotTask extends AsyncTask<Double, Void, String> {

    public static final String SEND_FREE_LOT_FILTER = "SEND_FREE_LOT_FILTER";
    public static final String SEND_FREE_LOT_RESULT_KEY = "SEND_FREE_LOT_RESULT_KEY";
    private Context ctx;

    public SendFreeLotTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected String doInBackground(Double... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String lat = params[0].toString();
            String lon = params[1].toString();
            //TODO check the returned parameter of readUrl
            AccessServlet.readUrl(serverIpAddress+"sendLotAvailability?&lat="+lat+"&lon="+lon+"&avail=free");
        } catch (Exception e) {
            e.printStackTrace();
            return "Free lot sender error.";
        }

        return "Free lot is sent successfully!";
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(SEND_FREE_LOT_FILTER);
        intent.putExtra(SEND_FREE_LOT_RESULT_KEY, result);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
