package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import hu.bme.hit.smartparkingassist.R;

public class RegistrationTask extends AsyncTask<String, Void, String> {

    public static final String REGISTRATION_FILTER = "REGISTRATION_FILTER";
    public static final String REGISTRATION_RESULT_KEY = "REGISTRATION_RESULT_KEY";
    public static final String REGISTRATION_SESSION_ID_KEY = "REGISTRATION_SESSION_ID_KEY";
    String sessionId;
    private Context ctx;

    public RegistrationTask(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            String serverIpAddress = ctx.getResources().getString(R.string.server_ip_address);
            String mail = params[0];
            String password = params[1];
            Double distance = Double.parseDouble(params[2]) / 1000;
            sessionId = AccessServlet.readUrl(serverIpAddress +
                                            "registration?&mail=" + mail +
                                            "&pass=" + password +
                                            "&rad=" + distance);
        } catch (Exception e) {
            e.printStackTrace();
            return "Free lot sender error.";
        }
        return "Got result.";
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(REGISTRATION_FILTER);
        intent.putExtra(REGISTRATION_RESULT_KEY, result);
        intent.putExtra(REGISTRATION_SESSION_ID_KEY, sessionId);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
    
}
