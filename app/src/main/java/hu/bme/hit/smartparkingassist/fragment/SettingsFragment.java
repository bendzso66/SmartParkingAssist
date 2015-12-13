package hu.bme.hit.smartparkingassist.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.bme.hit.smartparkingassist.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
