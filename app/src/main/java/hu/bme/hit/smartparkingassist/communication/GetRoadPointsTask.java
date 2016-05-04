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

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.net.URLEncoder;
import java.util.ArrayList;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.items.WayItem;

public class GetRoadPointsTask extends AsyncTask<ArrayList<GeoPoint>, Void, Road> {

    public static final String GET_ROAD_POINTS_FILTER = "GET_ROAD_POINTS_FILTER";
    public static final String GET_ROAD_POINTS_RESULT_KEY = "GET_ROAD_POINTS_RESULT_KEY";

    private Context ctx;

    public GetRoadPointsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Road doInBackground(ArrayList<GeoPoint>... params) {
        ArrayList<GeoPoint> wayPoints = params[0];
        RoadManager roadManager = new OSRMRoadManager(ctx);
        Road road = roadManager.getRoad(wayPoints);

        return road;
    }

    @Override
    protected void onPostExecute(Road result) {
        if (result != null) {
            Intent intent = new Intent(GET_ROAD_POINTS_FILTER);
            intent.putExtra(GET_ROAD_POINTS_RESULT_KEY, result);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        }
    }
}
