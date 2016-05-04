package hu.bme.hit.smartparkingassist.communication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class GetRoadPointsTask extends AsyncTask<WayItem, Void, Road> {

    public static final String GET_ROAD_POINTS_FILTER = "GET_ROAD_POINTS_FILTER";
    public static final String GET_ROAD_POINTS_RESULT_KEY = "GET_ROAD_POINTS_RESULT_KEY";
    public static final String GET_ROAD_COLOR_RESULT_KEY = "GET_ROAD_COLOR_RESULT_KEY";

    private Context ctx;
    private int lineColor;

    public GetRoadPointsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Road doInBackground(WayItem... params) {
        WayItem wayItem = params[0];

        double latitude1 = wayItem.getLatitude1();
        double longitude1 = wayItem.getLongitude1();
        double latitude2 = wayItem.getLatitude2();
        double longitude2 = wayItem.getLongitude2();
        GeoPoint startPoint = new GeoPoint(latitude1, longitude1);
        GeoPoint endPoint = new GeoPoint(latitude2, longitude2);
        ArrayList<GeoPoint> wayPoints = new ArrayList<>();
        wayPoints.add(startPoint);
        wayPoints.add(endPoint);

        double freeSpaces = wayItem.getFreeSpaces().doubleValue();
        double allSpaces = wayItem.getAllSpaces().doubleValue();
        Double saturation = 1.0 - (freeSpaces / allSpaces);
        if (saturation < 0.7) {
            lineColor = Color.GREEN;
        } else if (saturation < 0.9) {
            lineColor = Color.YELLOW;
        } else {
            lineColor = Color.RED;
        }

        RoadManager roadManager = new OSRMRoadManager(ctx);
        Road road = roadManager.getRoad(wayPoints);

        return road;
    }

    @Override
    protected void onPostExecute(Road result) {
        if (result != null) {
            Intent intent = new Intent(GET_ROAD_POINTS_FILTER);
            intent.putExtra(GET_ROAD_POINTS_RESULT_KEY, result);
            intent.putExtra(GET_ROAD_COLOR_RESULT_KEY, lineColor);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        }
    }
}
