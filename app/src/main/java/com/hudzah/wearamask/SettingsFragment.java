package com.hudzah.wearamask;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    Preference username;
    Preference email;
    Preference locations;
    Preference appVersion;
    Preference logout;

    String version;
    PackageInfo pInfo;

    SwitchPreference darkModeSwitch;

    private static final String TAG = "SettingsFragment";

    Location location;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        username = (Preference) findPreference("username");
        email = (Preference) findPreference("email");
        locations = (Preference) findPreference("locations");
        appVersion = (Preference) findPreference("version");
        logout = (Preference) findPreference("logout");

        locations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                navController.navigate(R.id.locationsFragment);

                return false;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (ConnectivityReceiver.isConnected()) {
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                            }
                        }
                    });
                }
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

        username.setSummary(ParseUser.getCurrentUser().getUsername());
        email.setSummary(ParseUser.getCurrentUser().getEmail());
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
