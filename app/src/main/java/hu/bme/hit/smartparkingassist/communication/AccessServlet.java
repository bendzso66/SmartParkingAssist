package hu.bme.hit.smartparkingassist.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccessServlet {

    private static String data = "";
    private static InputStream iStream = null;
    private static HttpURLConnection urlConnection = null;

    public static String readUrl(String mapsApiDirectionsUrl) throws Exception {

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
            while ((line = br.readLine()) != null) {	// read response
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
        return data;	// return the response
    }

}
