package com.example.android.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by Alan on 05/01/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();
        int count = prefScreen.getPreferenceCount();
        for(int i = 0; i < count; i++){
            Preference pref = prefScreen.getPreference(i);
            if(!(pref instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, value);
            }
        }
    }

    private void setPreferenceSummary(Preference pref, Object value){
        String strValue = value.toString();

        if(pref instanceof ListPreference){
            ListPreference listPreference = (ListPreference) pref;
            int index = listPreference.findIndexOfValue(strValue);
            if(index >= 0){
                pref.setSummary(listPreference.getEntries()[index]);
            }
        } else {
            pref.setSummary(strValue);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if(pref != null) {
            if (!(pref instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(key, "");
                setPreferenceSummary(pref, value);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
