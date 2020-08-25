package com.hudzah.wearamask;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.parse.Parse;
import com.parse.facebook.ParseFacebookUtils;
import com.parse.twitter.ParseTwitterUtils;

public class App extends Application implements LifecycleEventObserver {

    private static App instance;
    public static boolean inBackground = false;

    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();


        instance = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .enableLocalDataStore()
                .build()
        );

        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

    }

    public static synchronized App getInstance(){
        return instance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener){
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setLocationProviderListener(GpsLocationReceiver.GpsLocationReceiverListener listener){
        GpsLocationReceiver.gpsLocationReceiverListener = listener;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {


        Log.d("Lifecycle", "onStateChanged: lifecycle event is " + event);
        if(event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY ){
            inBackground = true;
            editor.putBoolean("inBackground", inBackground);
            editor.apply();

        }
        else if(event == Lifecycle.Event.ON_START){
            inBackground = false;
            editor.putBoolean("inBackground", inBackground);
            editor.apply();

        }
    }
}
