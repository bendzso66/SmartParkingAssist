package hu.bme.hit.smartparkingassist.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.items.WayItem;

public class WayAdapter extends BaseAdapter implements View.OnClickListener {

    private final List<WayItem> wayItems;
    private Context context;

    public static final String SHOW_A_WAY_FILTER = "SHOW_A_WAY_FILTER";
    public static final String WAY_FILTER_POSITION_KEY = "WAY_FILTER_POSITION_KEY";

    public WayAdapter(final Context aContext, final ArrayList<WayItem> aWayItems) {
        context = aContext;
        wayItems = aWayItems;
    }

    @Override
    public int getCount() {
        return wayItems.size();
    }

    @Override
    public Object getItem(int position) {
        return wayItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sor megjelenítésének beállítása
     */
    public View getView(final int position, View convertView, ViewGroup parent) {

        final WayItem item = wayItems.get(position);
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.way_item_row, parent, false);
        }

        TextView addressTextView = (TextView) itemView.findViewById(R.id.address_item);
        addressTextView.setText(item.getNameOfWay());

        TextView distanceTextView = (TextView) itemView.findViewById(R.id.distance_item);
        Integer distance = (int) Math.floor(item.getDistance() + 0.5d);
        distanceTextView.setText("Walk distance: " + distance.toString() + " m");

        ImageButton showWayOnMapBtn = ((ImageButton) itemView.findViewById(R.id.show_way));
        showWayOnMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SHOW_A_WAY_FILTER);
                intent.putExtra(WAY_FILTER_POSITION_KEY, position);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        ImageButton navigateToFreeBtn = ((ImageButton) itemView.findViewById(R.id.navigate_free_lot));
        navigateToFreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("navigateToFreeBtn", ((Integer) position).toString());
            }
        });

        return itemView;
    }

    @Override
    public void onClick(View v) {
    }
}