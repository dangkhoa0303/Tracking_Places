package com.example.android.bus.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.android.bus.R;
import com.example.android.bus.Util;

/**
 * Created by Dell on 4/13/2016.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindSummaryToPreferenceValue(findPreference(getString(R.string.pref_radius_key)));
        bindSummaryToPreferenceValue(findPreference(getString(R.string.pref_location_type_key)));
    }

    /*
    * For proper lifecycle management,
    * it is recommended to register and unregister
    * in the onResume() and onPause() methods
    */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            // For List preference, we need to look up the correct display value in
            // the preference's entries list (since they have separate value/key)
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation
            preference.setSummary(stringValue);
        }
    }

    private void bindSummaryToPreferenceValue (Preference preference) {
        // set listener to watch for the value changes
        preference.setOnPreferenceChangeListener(this);

        // set the preference summaries
        setPreferenceSummary(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "") // get default value
        );
    }

    // This gets called before preference is changed
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        // this method is call in order to update the summaries correctly
        setPreferenceSummary(preference, value);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_radius_key))) {
            Util.resetRadius(getActivity(), key, sharedPreferences.getString(key, ""));
        } else {
            if (key.equals(getString(R.string.pref_location_type_key))) {
                Util.resetLocationType(getActivity(), key, sharedPreferences.getString(key, ""));
            }
        }

    }
}
