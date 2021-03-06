package com.hudzah.wearamask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class GpsLocationReceiver extends BroadcastReceiver {

    public static GpsLocationReceiverListener gpsLocationReceiverListener;

    private static final String TAG = "GpsLocationReceiver";

    private boolean shown = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            Log.d(TAG, "onReceive: is location, " + checkLocationServicesEnabled(context));
            boolean isLocationOn = checkLocationServicesEnabled(context);
            NotificationHelper notificationHelper = new NotificationHelper(context);


            if(!isLocationOn && !shown){
                notificationHelper.sendHighPriorityNotification(context.getResources().getString(R.string.notification_location_off_title),
                        context.getResources().getString(R.string.notification_location_off_text),
                        context.getResources().getString(R.string.notification_location_off_big_text),
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_location_off_image),
                        MainActivity.class);
                        shown = true;
            }

            if(gpsLocationReceiverListener != null){
                gpsLocationReceiverListener.onLocationProviderChanged(isLocationOn);
            }
        }
    }

    public static boolean checkLocationServicesEnabled(Context mContext){
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "checkLocationServicesEnabled: in here");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return locationManager.isLocationEnabled();

        else{
            int mode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    public interface GpsLocationReceiverListener{
        void onLocationProviderChanged(boolean isLocationOn);
    }


}
