package hu.bme.hit.smartparkingassist;

import android.app.Activity;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

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

    private static MainMenuItems selectedItem;

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
                selectedItem = new MainMenuItems(getArguments().getString(KEY_TITLE_DESCRIPTION_QUERY));

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
        View rootView = inflater.inflate(R.layout.fragment_find_free_lot, container, false);

        itemDescription = (TextView) rootView.findViewById(R.id.item_detail);
        final AccessServlet servlet = new AccessServlet(this.getActivity(), itemDescription);

        final Geocoder geocoder = new Geocoder(this.getActivity(), Locale.ENGLISH);
        Button btn = (Button) rootView.findViewById(R.id.find_free_lot_button);
        final EditText address = (EditText) rootView.findViewById(R.id.get_address_field);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                servlet.findFreeLotFromAddress(address.getText().toString());
            }
        });

        return rootView;
    }
}
