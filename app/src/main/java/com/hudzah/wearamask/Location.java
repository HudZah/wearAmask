package com.hudzah.wearamask;

import android.content.Context;
import android.content.SharedPreferences;
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
    String ARRAY_LIST_TAG = "locationsArrayList";
    private String locationName;

    public ArrayList<com.hudzah.wearamask.Location> locationsArrayList = new ArrayList<>();

    public Location(int selectedRadius, int selectedColor, LatLng latLng, String address, String locationName) {
        this.selectedRadius = selectedRadius;
        this.selectedColor = selectedColor;
        this.latLng = latLng;
        this.address = address;
        this.locationName = locationName;
    }

    public void saveLocationToParse(Place place){
        ParseObject object = new ParseObject("Locations");
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
        object.put("location", parseGeoPoint);
        object.put("username", ParseUser.getCurrentUser().getUsername());
        object.put("address", place.getAddress());
        object.put("radius", selectedRadius);
        object.put("color", String.valueOf(selectedColor));
        object.put("name", locationName);

        DialogAdapter.ADAPTER.loadingDialog();
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    saved = true;
                    Log.d(TAG, "done: saved fine");
                    MapFragment.getInstance().discardLocation();
                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show();
                    saveLocationsToSharedPreferences();
                    DialogAdapter.ADAPTER.dismissLoadingDialog();
                }
                else{
                    saved = false;
                    Log.d(TAG, "done: failed to save " + e.getMessage());
                }
                CircleManager.Manager.clearAllCircles();
                getAllLocations(true);

            }

        });
    }

    public void getAllLocations(final boolean drawLocations){

        ParseQuery query = ParseQuery.getQuery("Locations");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        DialogAdapter.ADAPTER.locationFindingDialog();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    for(ParseObject object : objects){
                        ParseGeoPoint geoPoint = object.getParseGeoPoint("location");
                        Log.d(TAG, "done: stuck in here?");
                        Location loc = new Location(Integer.parseInt(String.valueOf(object.getNumber("radius"))), Integer.parseInt(String.valueOf(object.getString("color"))), new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), object.getString("address"), object.getString("name"));
                        locationsArrayList.add(loc);
                    }

                    MapFragment.getInstance().locations = locationsArrayList;
                    saveLocationsToSharedPreferences();
                    if(drawLocations) {
                        Log.d(TAG, "done: or here?");
                        drawAllLocations();
                    }
                }
                else{
                    DialogAdapter.ADAPTER.displayErrorDialog(e.getMessage(), "");
                }

                DialogAdapter.ADAPTER.dismissLocationDialog();
            }
        });

    }

    public void  saveLocationsToSharedPreferences(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(locationsArrayList);
        Log.d(TAG, "saveLocationsToSharedPreferences: json is " + json);
        editor.putString(ARRAY_LIST_TAG, json);
        editor.apply();
    }

    public void drawAllLocations(){
        if(locationsArrayList.size() > 0 && locationsArrayList != null) {
            CircleManager.Manager.drawManyCirclesOnMap(locationsArrayList);
        }
    }


    public ArrayList<Location> getLocationsFromSharedPreferences(boolean drawCircles){
        DialogAdapter.ADAPTER.locationFindingDialog();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ARRAY_LIST_TAG, null);
        Type type = new TypeToken<List<Location>>() {}.getType();
        ArrayList<Location> arrayList = gson.fromJson(json, type);

        locationsArrayList = arrayList;

        if(locationsArrayList != null) {

            if (drawCircles) {
                drawAllLocations();
            }
        }

        DialogAdapter.ADAPTER.dismissLocationDialog();

        return arrayList;

    }

    public void setContext(Context context) {
        this.context = context;
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
