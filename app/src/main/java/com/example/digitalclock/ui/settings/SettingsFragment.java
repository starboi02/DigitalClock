package com.example.digitalclock.ui.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.digitalclock.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        ListPreference hourFormat = (ListPreference)findPreference("hour_format");
        ListPreference clockFont = (ListPreference)findPreference("font");
        ListPreference clockType = findPreference("clock_type");
        if(clockType.getValue().equals("Digital")){
            hourFormat.setEnabled(true);
            clockFont.setEnabled(true);
        }
        else{
            hourFormat.setEnabled(false);
            clockFont.setEnabled(false);
        }

        clockType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String val = newValue.toString();
                int index = clockType.findIndexOfValue(val);
                if(index==0){
                    hourFormat.setEnabled(true);
                    clockFont.setEnabled(true);
                }
                else{
                    hourFormat.setEnabled(false);
                    clockFont.setEnabled(false);
                }
                return true;
            }
        });
    }
}