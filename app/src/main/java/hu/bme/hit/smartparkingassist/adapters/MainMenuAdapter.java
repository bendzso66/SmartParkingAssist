package hu.bme.hit.smartparkingassist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.items.MainMenuItem;

public class MainMenuAdapter extends BaseAdapter {

    private final List<MainMenuItem> mainMenuItems;

    public MainMenuAdapter(final Context context, final ArrayList<MainMenuItem> aMainMenuItems) {
        mainMenuItems = aMainMenuItems;
    }

    @Override
    public int getCount() {
        return mainMenuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mainMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sor megjelenítésének beállítása
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        final MainMenuItem item = mainMenuItems.get(position);
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.main_menu_item_row, parent, false);
        }

        TextView textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
        textViewTitle.setText(item.getTitle());

        return itemView;
    }
}
