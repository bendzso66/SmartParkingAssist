package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            readUrl(serverIpAddress+"sendFreeLot?&lat="+lat+"&lon="+lon+"&avail=free&id=1");
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

    public static String readUrl(String mapsApiDirectionsUrl) throws Exception {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            Log.d("[LotManager]:", mapsApiDirectionsUrl);
            URL url = new URL(mapsApiDirectionsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("[LotManager]:",data);
            br.close();
        } catch (Exception e) {
            Log.d("Exception reading url", e.toString());
            throw e;
        } finally {
            if(iStream != null)
                iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
