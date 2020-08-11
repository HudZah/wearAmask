package com.hudzah.wearamask;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;

public class CircleManager {

    private Context context;
    private GoogleMap googleMap;

    public CircleManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
    }

    public void drawCircleOnMap(int radius, int color, Place place){
        googleMap.clear();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

        MarkerOptions options = new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getAddress());
        googleMap.addMarker(options);
        googleMap.addCircle(
                new CircleOptions()
                        .center(place.getLatLng())
                        .radius(radius)
                        .strokeWidth(3f)
                        .strokeColor(Color.argb(255, red, green, blue))
                        .fillColor(Color.argb(70, red, green, blue))
        );
    }

    public void drawManyCirclesOnMap(ArrayList<Location> locations){
        googleMap.clear();
        for(Location location : locations){
            int red = Color.red(location.getSelectedColor());
            int green = Color.green(location.getSelectedColor());
            int blue = Color.blue(location.getSelectedColor());

            Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

            MarkerOptions options = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(location.getAddress());
            googleMap.addMarker(options);
            googleMap.addCircle(
                    new CircleOptions()
                            .center(location.getLatLng())
                            .radius(location.getSelectedRadius())
                            .strokeWidth(3f)
                            .strokeColor(Color.argb(255, red, green, blue))
                            .fillColor(Color.argb(70, red, green, blue))
            );
        }
    }
    public void clearAllCircles(){
        googleMap.clear();
    }
}
