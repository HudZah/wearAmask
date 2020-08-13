package com.hudzah.wearamask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Location {

    private Context context;
    private int selectedRadius;
    private int selectedColor;
    private LatLng latLng;
    private String address;
    public boolean saved = false;
    private static final String TAG = "Location";
    Activity activity;
    DialogAdapter dialog;
    public ArrayList<Location> locationsArrayList = new ArrayList<>();
    CircleManager manager;

    public Location(Context context, int selectedRadius, int selectedColor, LatLng latLng, String address) {
        this.context = context;
        this.selectedRadius = selectedRadius;
        this.selectedColor = selectedColor;
        this.latLng = latLng;
        this.address = address;
        activity = (Activity) context;
    }

    public void saveLocationToParse(Place place){
        dialog = new DialogAdapter(activity);
        ParseObject object = new ParseObject("Locations");
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
        object.put("location", parseGeoPoint);
        object.put("username", ParseUser.getCurrentUser().getUsername());
        object.put("address", place.getAddress());
        object.put("radius", selectedRadius);
        object.put("color", String.valueOf(selectedColor));

        dialog.loadingDialog();
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    saved = true;
                    Log.d(TAG, "done: saved fine");
                    MapFragment.getInstance().discardLocation();
                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show();
                    saveLocationsToSharedPreferences();
                }
                else{
                    saved = false;
                    Log.d(TAG, "done: failed to save " + e.getMessage());
                }
                // TODO: 8/11/2020 Check this
                manager.clearAllCircles();
                getAllLocations(true);
                dialog.dismissLoadingDialog();

            }

        });
    }

    public void getAllLocations(final boolean drawLocations){

        dialog = new DialogAdapter(activity);
        ParseQuery query = ParseQuery.getQuery("Locations");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        dialog.locationFindingDialog();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    for(ParseObject object : objects){
                        ParseGeoPoint geoPoint = object.getParseGeoPoint("location");
                        Location loc = new Location(context, Integer.parseInt(String.valueOf(object.getNumber("radius"))), Integer.parseInt(String.valueOf(object.getString("color"))), new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), object.getString("address"));
                        locationsArrayList.add(loc);
                    }

                    MapFragment.getInstance().locations = locationsArrayList;
                    if(drawLocations) {
                        drawAllLocations();
                    }
                }
                else{
                    dialog.displayErrorDialog(e.getMessage());
                }

                dialog.dismissLocationDialog();
            }
        });
        
    }

    // TODO: 8/10/2020 Create draw all locations method
    private void drawAllLocations(){
        if(locationsArrayList.size() > 0) {
            manager = MapFragment.getInstance().circleManager;
            manager.drawManyCirclesOnMap(locationsArrayList);
        }
    }

    private void saveLocationsToSharedPreferences(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String ARRAY_LIST_TAG = "locationsArrayList";

        Gson gson = new Gson();

        String json = gson.toJson(locationsArrayList);

        editor.putString(ARRAY_LIST_TAG, json);
        editor.commit();
    }

    public ArrayList<Location> getLocationsFromSharedPreferences(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(TAG, "");
        Type type = new TypeToken<List<Location>>() {}.getType();
        ArrayList<Location> arrayList = gson.fromJson(json, type);

        return arrayList;

    }

    public int getSelectedRadius() {
        return selectedRadius;
    }

    public void setSelectedRadius(int selectedRadius) {
        this.selectedRadius = selectedRadius;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
