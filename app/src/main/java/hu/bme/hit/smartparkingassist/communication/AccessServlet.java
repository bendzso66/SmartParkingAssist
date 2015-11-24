package hu.bme.hit.smartparkingassist.communication;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AccessServlet {

    String apiurl = "http://152.66.248.87:4567/"; //iparking.hit.bme.hu
    //String apiurl = "http://192.168.1.4:4567/";
    Activity display;
    TextView view;

    public AccessServlet(Activity iDisplay) {
        display = iDisplay;
    }

    public AccessServlet(Activity iDisplay, TextView iView) {
        display = iDisplay;
        view = iView;
    }

    public boolean findFreeLotFromAddress(String address) {
        //String url = apiurl+"findFreeLot?lat="+lat+"&lon="+lon+"&id="+id+"&rad="+walkdist;
        String url = null;
        try {
            url = apiurl+"findFreeLotFromAddress?address=Budapest+" + URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new AsyncTask<Object, Void, Void>() {
            private String url;

            public AsyncTask<Object, Void, Void> setData(String curl) {
                url = curl;

                return this;
            }

            @Override
            protected Void doInBackground(Object... param) {
                try {
                    Log.d("[Communicator]parameterezett url findFreelot-nal: ",url);
                    final String ret = readUrl(url);
                    Log.d("[Communicator]findFreelotra servertol kapott valasz: ",ret);

                    JSONArray jArray = null;
                    if(ret == null || ret.equalsIgnoreCase("[]") || ret.equalsIgnoreCase("UNSUCCESSFULL_REQUEST") ) {
                        Log.d("mylog","Empty array, returning");
                        display.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(display, "No Places Available", Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                    try {
                        jArray = new JSONArray(ret);
                    } catch (JSONException e) {
                        display.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(display, "Unexpected response from the server", Toast.LENGTH_LONG).show();
                            }
                        });
                        e.printStackTrace();
                        return null;
                    }

                    String addresses = "";

                    for (int i = 0; i < jArray.length(); i++) {
                        try {
                            JSONObject curr = jArray.getJSONObject(i);
                            addresses += curr.getString("address") + "\n";
                        } catch (JSONException e) {
                            display.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(display, "Unexpected response from the server", Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                            return null;
                        }
                    }

                    final String result = addresses;

                    display.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(display, "Free lot was found.", Toast.LENGTH_LONG).show();
                            view.setText(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    display.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(display, "Free lot finder error.", Toast.LENGTH_LONG).show();

                        }
                    });
                }

                return null;
            }
        }.setData(url).execute();
        return true; //new LatLng(lat, lon);
    }

    public boolean findFreeLot(double lat, double lon) {
        //String url = apiurl+"findFreeLot?lat="+lat+"&lon="+lon+"&id="+id+"&rad="+walkdist;
        String url = apiurl+"findFreeLot?lat="+lat+"&lon="+lon;
        new AsyncTask<Object, Void, Void>() {
            private String url;

            public AsyncTask<Object, Void, Void> setData(String curl) {
                url = curl;

                return this;
            }

            @Override
            protected Void doInBackground(Object... param) {
                try {
                    Log.d("[Communicator]parameterezett url findFreelot-nal: ",url);
                    final String ret = readUrl(url);
                    Log.d("[Communicator]findFreelotra servertol kapott valasz: ",ret);
                    display.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(display, "Free lot was found.", Toast.LENGTH_LONG).show();
                            view.setText(ret);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    display.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(display, "Free lot finder error.", Toast.LENGTH_LONG).show();

                        }
                    });
                }

                return null;
            }
        }.setData(url).execute();
        return true; //new LatLng(lat, lon);
    }

    public boolean sendFreeLot(double lat, double lon) {
        // Use asynctask to handle this in background - dont freeze the gui
        new AsyncTask<Object, Void, Void>() {
            private double lat,lon;
            private String url;

            // Catch the parameters
            public AsyncTask<Object, Void, Void> setData(String iurl, double ilat, double ilon) {
                url = iurl;
                lat = ilat;
                lon = ilon;
                return this;	// Return myself, for the simple syntax (.execute)
            }

            // Main task
            @Override
            protected Void doInBackground(Object... param) {
                String ret = null;
                try {
                    // Log.d("smartparkingassist",url);
                    ret = readUrl(url+"sendFreeLot?&lat="+lat+"&lon="+lon+"&avail=free&id=1");
                    // Log.d("smartparkingassist", ret);
                } catch (Exception e) {	// Handle exceptions, eg network error
                    display.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(display, "Free lot sender error.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();	// Logcat
                }

                // After succeed, display toast on the GUI thread (because of security)
                display.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(display, "Free lot sent", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        }.setData(apiurl, lat, lon).execute();	// Pass the parameters then run
        return true;
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
