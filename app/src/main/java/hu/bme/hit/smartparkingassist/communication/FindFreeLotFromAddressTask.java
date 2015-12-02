package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import hu.bme.hit.smartparkingassist.R;

public class FindFreeLotFromAddressTask extends AsyncTask<String, Void, String> {

    public static final String FIND_FREE_LOT_FROM_ADDRESS_FILTER = "FIND_FREE_LOT_FROM_ADDRESS_FILTER";
    public static final String FIND_FREE_LOT_FROM_ADDRESS_KEY = "FIND_FREE_LOT_FROM_ADDRESS_KEY";
    private Context ctx;

    public FindFreeLotFromAddressTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String address = params[0];
            String url = serverIpAddress+"findFreeLotFromAddress?address=Budapest+" + URLEncoder.encode(address, "UTF-8");
            Log.d("[Communicator]parameterezett url findFreelot-nal: ", url);
            final String freeLots = AccessServlet.readUrl(url);
            Log.d("[Communicator]findFreelotra servertol kapott valasz: ",freeLots);

            JSONArray jArray = null;
            if(freeLots == null || freeLots.equalsIgnoreCase("[]") || freeLots.equalsIgnoreCase("UNSUCCESSFULL_REQUEST") ) {
                Log.d("mylog", "Empty array, returning");
                return "No Places Available";
            }
            try {
                jArray = new JSONArray(freeLots);
            } catch (JSONException e) {
                e.printStackTrace();
                return "Unexpected response from the server";
            }

            String addresses = "";

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject curr = jArray.getJSONObject(i);
                    addresses += curr.getString("address") + "\n";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Unexpected response from the server";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Free lot finder error.";
        }

        return "Free lot was found.";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("[SendFreeLot]", result);
        Intent intent = new Intent(FIND_FREE_LOT_FROM_ADDRESS_FILTER);
        intent.putExtra(FIND_FREE_LOT_FROM_ADDRESS_KEY, result);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
