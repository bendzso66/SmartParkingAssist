package hu.bme.hit.smartparkingassist.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.Utility;
import hu.bme.hit.smartparkingassist.items.FreeLotItem;
import hu.bme.hit.smartparkingassist.items.MainMenuItem;

public class FreeLotAdapter extends BaseAdapter {

    private final List<FreeLotItem> freeLotItems;

    public FreeLotAdapter(final Context context, final ArrayList<FreeLotItem> aFreeLotItems) {
        freeLotItems = aFreeLotItems;
    }

    @Override
    public int getCount() {
        return freeLotItems.size();
    }

    @Override
    public Object getItem(int position) {
        return freeLotItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sor megjelenítésének beállítása
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        final FreeLotItem item = freeLotItems.get(position);
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.free_lot_item_row, parent, false);
        }

        TextView addressTextView = (TextView) itemView.findViewById(R.id.address_item);
        addressTextView.setText(item.getAddress());

        TextView distanceTextView = (TextView) itemView.findViewById(R.id.distance_item);
        Integer distance = (int) Math.floor(item.getDistance() * 1000 + 0.5d);
        distanceTextView.setText("Walk distance: " + distance.toString() + " m");

        return itemView;
    }
}