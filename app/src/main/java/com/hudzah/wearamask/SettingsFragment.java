package com.hudzah.wearamask;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

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
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    Preference username;
    Preference email;
    Preference locations;
    Preference appVersion;
    Preference logout;

    String version;
    PackageInfo pInfo;

    SwitchPreference darkModeSwitch;

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
                NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
                navController.navigate(R.id.locationsFragment);

                return false;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(ConnectivityReceiver.isConnected()){
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else{
                                DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                            }
                        }
                    });
                }
                return false;
            }
        });

        darkModeSwitch = (SwitchPreference) findPreference("enable_dark_mode");

        location = MapFragment.getInstance().location;

        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("enable_dark_mode")) {
            boolean darkMode = sharedPreferences.getBoolean("enable_dark_mode", false);
            //Do whatever you want here. This is an example.
            if (darkMode) {
                // TODO: 8/31/2020 show dark mode
                Toast.makeText(getContext(), "dark mode enabled", Toast.LENGTH_SHORT).show();

            } else {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean darkMode = preferences.getBoolean("enable_dark_mode", false);

        if (darkMode) {
            Toast.makeText(getContext(), "dark mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "dark mode disabled", Toast.LENGTH_SHORT).show();
        }
    }
}
