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

public class GetRoadPointsTask extends AsyncTask<String, Void, String> {

    public static final String GET_ROAD_POINTS_FILTER = "GET_ROAD_POINTS_FILTER";
    public static final String GET_ROAD_POINTS_RESULT_KEY = "GET_ROAD_POINTS_RESULT_KEY";
    public static final String GET_ROAD_POINTS_ROADS_KEY = "GET_ROAD_POINTS_ROADS_KEY";

    Road road;
    private Context ctx;

    public GetRoadPointsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        GeoPoint startPoint = new GeoPoint(47.4986276, 19.0700189);
        GeoPoint endPoint = new GeoPoint(47.4971664, 19.070468);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint);

        RoadManager roadManager = new OSRMRoadManager(ctx);
        road = roadManager.getRoad(waypoints);

        return "Road points were found.";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("[GetRoadPoints]", result);
        Intent intent = new Intent(GET_ROAD_POINTS_FILTER);
        intent.putExtra(GET_ROAD_POINTS_RESULT_KEY, result);
        if (result.equals("Road points were found.")) {
            intent.putExtra(GET_ROAD_POINTS_ROADS_KEY, road);
        }
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }
}
