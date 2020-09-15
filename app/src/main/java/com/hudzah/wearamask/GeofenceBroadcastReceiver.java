package com.hudzah.wearamask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";

    public List<Geofence> geofenceList;
    private GeofencingEvent geofencingEvent;

    private Context mContext;

    private NotificationHelper notificationHelper;



    @Override
    public void onReceive(Context context, Intent intent) {

        notificationHelper = new NotificationHelper(context);

        this.mContext = context;

        geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()){
            Log.d(TAG, "onReceive: error receiving geofence event " + geofencingEvent.getErrorCode());
            return;
        }

        geofenceList = geofencingEvent.getTriggeringGeofences();

        for(Geofence geofence : geofenceList){
            Log.d(TAG, "onReceive: " + geofence.toString());
        }

        checkGeofenceEvent();


    }

    private void checkGeofenceEvent(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE).edit();

        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);

        int transitionType = geofencingEvent.getGeofenceTransition();
        String address = getAddress(geofencingEvent.getTriggeringLocation().getLatitude(), geofencingEvent.getTriggeringLocation().getLongitude());
        Log.d(TAG, "checkGeofenceEvent: triggered address is " + address);

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                editor.putInt("transitionState", Geofence.GEOFENCE_TRANSITION_ENTER);
                editor.apply();
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "checkGeofenceEvent: DWELL");
                editor.putInt("transitionState", Geofence.GEOFENCE_TRANSITION_DWELL);
                editor.apply();
                Log.d(TAG, "checkGeofenceEvent: app is in background is " + App.inBackground);
                if(!prefs.getBoolean("inBackground", false)) MapFragment.getInstance().switchFabSafeState(Geofence.GEOFENCE_TRANSITION_ENTER);
                notificationHelper.sendHighPriorityNotification(mContext.getResources().getString(R.string.notification_enter_title),
                        mContext.getResources().getString(R.string.notification_enter_text),
                        ParseUser.getCurrentUser().getUsername() + " "  + mContext.getResources().getString(R.string.notification_enter_big_text) + " " + address + ", " + mContext.getResources().getString(R.string.notification_enter_part),
                        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_noti_safe),
                        MainActivity.class);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                editor.putInt("transitionState", Geofence.GEOFENCE_TRANSITION_EXIT);
                editor.apply();
                Log.d(TAG, "checkGeofenceEvent: app is in background is " + App.inBackground);
                if(!prefs.getBoolean("inBackground", false)) MapFragment.getInstance().switchFabSafeState(Geofence.GEOFENCE_TRANSITION_EXIT);
                notificationHelper.sendHighPriorityNotification(mContext.getResources().getString(R.string.notification_exit_title),
                        mContext.getResources().getString(R.string.notification_exit_text),
                        mContext.getResources().getString(R.string.notification_exit_big_text),
                        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_warning_red),
                        MainActivity.class);
                break;


        }

    }

    private String getAddress(double latitude, double longitude){
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();


    }


}
