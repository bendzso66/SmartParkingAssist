package hu.bme.hit.smartparkingassist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import hu.bme.hit.smartparkingassist.FindFreeLotActivity;
import hu.bme.hit.smartparkingassist.MainMenuActivity;
import hu.bme.hit.smartparkingassist.MapActivity;
import hu.bme.hit.smartparkingassist.OsmActivity;
import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.Utility;
import hu.bme.hit.smartparkingassist.adapters.WayAdapter;
import hu.bme.hit.smartparkingassist.communication.FindFreeLotFromAddressTask;
import hu.bme.hit.smartparkingassist.items.WayItem;
import hu.bme.hit.smartparkingassist.items.MainMenuItem;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link MainMenuActivity}
 * in two-pane mode (on tablets) or a {@link FindFreeLotActivity}
 * on handsets.
 */
public class FindFreeLotFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String TAG = "FindFreeLotFragment";

    public static final String KEY_TITLE_DESCRIPTION_QUERY = "uniqueItemTitle";

    private static MainMenuItem selectedItem;

    private String findFreeLotResponse = null;

    private View rootView;

    private Button viewAllOnMapButton;

    public static ArrayList<WayItem> wayItems = new ArrayList<WayItem>();

    private ListView freeLotListView;

    private WayAdapter freeLotAdapter = null;

    private String SAVE_FREE_LOT_ITEMS_KEY = "SAVE_FREE_LOT_ITEMS_KEY";

    public static FindFreeLotFragment newInstance(String itemDesc) {
        FindFreeLotFragment result = new FindFreeLotFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TITLE_DESCRIPTION_QUERY, itemDesc);
        result.setArguments(args);

        return result;
    }

    public static FindFreeLotFragment newInstance(Bundle args) {
        FindFreeLotFragment result = new FindFreeLotFragment();
        result.setArguments(args);

        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (getArguments() != null) {
                selectedItem = new MainMenuItem(getArguments().getString(KEY_TITLE_DESCRIPTION_QUERY));
                Activity activity = this.getActivity();
                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null) {
                    appBarLayout.setTitle(selectedItem.getTitle());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_find_free_lot, container, false);

        freeLotListView = (ListView) rootView.findViewById(R.id.free_lot_list);
        ImageButton findFreeLotButton = (ImageButton) rootView.findViewById(R.id.find_free_lot_button);
        viewAllOnMapButton = (Button) rootView.findViewById(R.id.view_all_on_map_button);
        if (savedInstanceState == null) {
            viewAllOnMapButton.setVisibility(View.INVISIBLE);
        } else {
            wayItems = savedInstanceState.getParcelableArrayList(SAVE_FREE_LOT_ITEMS_KEY);
            freeLotAdapter = new WayAdapter(getActivity().getApplicationContext(), wayItems);
            freeLotListView.setAdapter(freeLotAdapter);
            Utility.setListViewHeightBasedOnChildren(freeLotListView);
        }

        final EditText address = (EditText) rootView.findViewById(R.id.get_address_field);

        findFreeLotButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String distanceInMeter = myPrefs.getString("max_walk_distance_preference", "500");
                SharedPreferences sp =
                        getActivity().getSharedPreferences("SESSION_ID_PREF_KEY", getActivity().MODE_PRIVATE);
                String sessionId = sp.getString("LAST_SESSION_ID", "NaN");
                if (sessionId.equals("NaN")) {
                    new FindFreeLotFromAddressTask(getActivity()).execute(address.getText().toString(), distanceInMeter);
                } else {
                    new FindFreeLotFromAddressTask(getActivity()).execute(address.getText().toString(), distanceInMeter, sessionId);
                }
            }
        });

        viewAllOnMapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY, wayItems);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FILTER);
        intentFilter.addAction(WayAdapter.SHOW_A_WAY_FILTER);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_FREE_LOT_ITEMS_KEY, wayItems);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FILTER)) {
                findFreeLotResponse = intent.getStringExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_RESULT_KEY);
                Snackbar.make(rootView, findFreeLotResponse, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (findFreeLotResponse.equals("Free lot was found.")) {
                    wayItems = intent.getParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY);

                    freeLotAdapter = new WayAdapter(getActivity().getApplicationContext(), wayItems);
                    freeLotListView.setAdapter(freeLotAdapter);
                    Utility.setListViewHeightBasedOnChildren(freeLotListView);

                    viewAllOnMapButton.setVisibility(TextView.VISIBLE);
                }
            } else if (intent.getAction().equals(WayAdapter.SHOW_A_WAY_FILTER)) {
                int position = intent.getIntExtra(WayAdapter.WAY_FILTER_POSITION_KEY, 0);
                ArrayList<WayItem> aWayItem = new ArrayList<WayItem>();
                aWayItem.add(wayItems.get(position));
                Intent intentForMap = new Intent(getActivity(), OsmActivity.class);
                intentForMap.putParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY, aWayItem);
                startActivity(intentForMap);
            }
        }
    };

}
