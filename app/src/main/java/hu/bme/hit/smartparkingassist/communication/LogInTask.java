package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import hu.bme.hit.smartparkingassist.R;

public class LogInTask extends AsyncTask<String, Void, String> {

    public static final String LOG_IN_FILTER = "LOG_IN_FILTER";
    public static final String LOG_IN_RESULT_KEY = "LOG_IN_RESULT_KEY";
    public static final String LOG_IN_SESSION_ID_KEY = "LOG_IN_SESSION_ID_KEY";
    String sessionId;
    private Context ctx;

    public LogInTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String mail = params[0];
            String password = params[1];
            //TODO check the returned parameter of readUrl
            sessionId = AccessServlet.readUrl(serverIpAddress+"login?&mail="+mail+"&pass="+password);
        } catch (Exception e) {
            e.printStackTrace();
            return "Free lot sender error.";
        }
        return "Got result.";
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(LOG_IN_FILTER);
        intent.putExtra(LOG_IN_RESULT_KEY, result);
        intent.putExtra(LOG_IN_SESSION_ID_KEY, sessionId);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

}
