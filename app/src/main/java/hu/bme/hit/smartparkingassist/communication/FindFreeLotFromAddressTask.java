package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.util.ArrayList;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.items.FreeLotItem;

public class FindFreeLotFromAddressTask extends AsyncTask<String, Void, String> {

    public static final String FIND_FREE_LOT_FROM_ADDRESS_FILTER = "FIND_FREE_LOT_FROM_ADDRESS_FILTER";
    public static final String FIND_FREE_LOT_FROM_ADDRESS_RESULT_KEY = "FIND_FREE_LOT_FROM_ADDRESS_RESULT_KEY";
    public static final String FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY = "FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY";

    ArrayList<FreeLotItem> freeLotItems;
    private Context ctx;

    public FindFreeLotFromAddressTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String address = params[0];
            String distanceInMeter = params[1];
            Double distanceInKilometer = Double.parseDouble(distanceInMeter) / 1000;
            String url = serverIpAddress +
                        "findFreeLotFromAddress?address=Budapest+" +
                        URLEncoder.encode(address, "UTF-8") +
                        "&rad=" +
                        distanceInKilometer.toString();
            if (params.length == 3) {
                String sessionId = params[2];
                url += "&id=" + sessionId;
            }
            Log.d("[Communicator]parameterezett url findFreelot-nal: ", url);
            final String result = AccessServlet.readUrl(url);
            Log.d("[Communicator]findFreelotra servertol kapott valasz: ",result);

            if(result == null || result.equalsIgnoreCase("[]") || result.equalsIgnoreCase("UNSUCCESSFULL_REQUEST") ) {
                Log.d("mylog", "Empty array, returning");
                return "No Places Available";
            }

            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray jArray = parser.parse(result).getAsJsonArray();
            freeLotItems = new ArrayList<FreeLotItem>();
            for(JsonElement obj : jArray )
            {
                FreeLotItem freeLotItem = gson.fromJson(obj, FreeLotItem.class);
                freeLotItems.add(freeLotItem);
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
        intent.putExtra(FIND_FREE_LOT_FROM_ADDRESS_RESULT_KEY, result);
        if (result.equals("Free lot was found.")) {
            intent.putParcelableArrayListExtra(FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY, freeLotItems);
        }
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
