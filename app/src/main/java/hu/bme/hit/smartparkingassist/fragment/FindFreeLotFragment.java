package hu.bme.hit.smartparkingassist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import hu.bme.hit.smartparkingassist.FindFreeLotActivity;
import hu.bme.hit.smartparkingassist.MainMenuActivity;
import hu.bme.hit.smartparkingassist.MainMenuItem;
import hu.bme.hit.smartparkingassist.MapActivity;
import hu.bme.hit.smartparkingassist.R;
import hu.bme.hit.smartparkingassist.communication.FindFreeLotFromAddressTask;
import hu.bme.hit.smartparkingassist.items.FreeLotItem;

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

    private TextView itemDescription;

    private static MainMenuItem selectedItem;

    private String findFreeLotResponse = null;

    private View rootView;

    private Button viewAllOnMapButton;

    public static ArrayList<FreeLotItem> freeLotItems = new ArrayList<FreeLotItem>();

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

        itemDescription = (TextView) rootView.findViewById(R.id.item_detail);
        Button findFreeLotButton = (Button) rootView.findViewById(R.id.find_free_lot_button);
        viewAllOnMapButton = (Button) rootView.findViewById(R.id.view_all_on_map_button);
        if (savedInstanceState == null) {
            viewAllOnMapButton.setVisibility(View.INVISIBLE);
        }

        final EditText address = (EditText) rootView.findViewById(R.id.get_address_field);

        findFreeLotButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new FindFreeLotFromAddressTask(getActivity()).execute(address.getText().toString());
            }
        });

        viewAllOnMapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY, freeLotItems);
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
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            findFreeLotResponse = intent.getStringExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_RESULT_KEY);
            Snackbar.make(rootView, findFreeLotResponse, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (findFreeLotResponse.equals("Free lot was found.")) {
                String address = "";
                freeLotItems = intent.getParcelableArrayListExtra(FindFreeLotFromAddressTask.FIND_FREE_LOT_FROM_ADDRESS_FREE_LOTS_KEY);

                for (int i = 0; i < freeLotItems.size(); i++) {
                    address += freeLotItems.get(i).getAddress() + "\n";
                }

                itemDescription.setText(address);
                viewAllOnMapButton.setVisibility(TextView.VISIBLE);
            }
        }
    };
}
