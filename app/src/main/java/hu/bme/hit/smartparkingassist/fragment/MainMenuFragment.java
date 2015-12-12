package hu.bme.hit.smartparkingassist.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import hu.bme.hit.smartparkingassist.adapters.MainMenuAdapter;
import hu.bme.hit.smartparkingassist.items.MainMenuItem;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FindFreeLotFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IMainMenuFragment}
 * interface.
 */
public class MainMenuFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private IMainMenuFragment listener;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface IMainMenuFragment {
        /**
         * Callback for when an item has been selected.
         */
        // public void onItemSelected(MainMenuItem selectedItem);
        void onItemSelected(MainMenuItem aItem);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainMenuFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<MainMenuItem> items = new ArrayList<>();
        items.add(new MainMenuItem("Find free lot"));
        items.add(new MainMenuItem("Log in"));
        items.add(new MainMenuItem("Registration"));
        items.add(new MainMenuItem("Settings"));
        MainMenuAdapter mainMenuAdapter = new MainMenuAdapter(getActivity().getApplicationContext(), items);
        setListAdapter(mainMenuAdapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        registerForContextMenu(getListView());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof IMainMenuFragment)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        listener = (IMainMenuFragment) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        listener = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        MainMenuItem selectedItem = (MainMenuItem) getListAdapter().getItem(position);
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        if(listener != null) {
            listener.onItemSelected(selectedItem);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
