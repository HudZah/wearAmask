package com.hudzah.wearamask;


import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    Preference locations;
    Preference appVersion;
    Preference data;

    String version;
    PackageInfo pInfo;

    SwitchPreference darkModeSwitch;

    private static final String TAG = "SettingsFragment";

    Location location;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);


        locations = (Preference) findPreference("locations");
        appVersion = (Preference) findPreference("version");
        data = (Preference) findPreference("data");

        locations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                navController.navigate(R.id.locationsFragment);

                return false;
            }
        });

        data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new AlertDialog.Builder(getContext())
                        .setTitle("wearAmask and your data")
                        .setMessage("To protect your location data, we store sensitive information directly on your phone.\n\nwearAmask does" +
                                " not collect your data nor store it on a server, but rather it is stored on your local database.")
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return false;
            }
        });

        darkModeSwitch = (SwitchPreference) findPreference("enable_dark_mode");
        if (darkModeSwitch != null) {
            darkModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(TAG, "onPreferenceChange: changed with " + newValue.toString());
                    if ((Boolean) newValue) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    return true;
                }
            });
        }

        location = MapFragment.getInstance().location;

        try {
            pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        locations.setSummary(location.getLocationsArrayList().size() + " locations");
        appVersion.setSummary(version);
    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean darkMode = preferences.getBoolean("enable_dark_mode", false);

    }
}
