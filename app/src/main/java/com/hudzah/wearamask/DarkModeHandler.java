package com.hudzah.wearamask;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public enum DarkModeHandler {
    DARK_MODE_HANDLER;

    public void checkDarkMode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean darkMode = preferences.getBoolean("enable_dark_mode", false);

        if (darkMode) {
            Toast.makeText(context, "Dark mode enabled in " + context.toString(), Toast.LENGTH_SHORT).show();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            Toast.makeText(context, "Dark mode disabled", Toast.LENGTH_SHORT).show();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
