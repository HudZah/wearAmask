package com.hudzah.wearamask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
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

public class Location implements Parcelable {

    private Context context;
    private int selectedRadius;
    private int selectedColor;
    private LatLng latLng;
    private String address;
    public boolean saved = false;
    private static final String TAG = "Location";
    String ARRAY_LIST_TAG = "locationsArrayList";
    private String locationName;
    private String locationID;

    public ArrayList<com.hudzah.wearamask.Location> locationsArrayList = new ArrayList<>();

    public Location(String locationID, int selectedRadius, int selectedColor, LatLng latLng, String address, String locationName) {
        this.selectedRadius = selectedRadius;
        this.selectedColor = selectedColor;
        this.latLng = latLng;
        this.address = address;
        this.locationName = locationName;
        this.locationID = locationID;
    }

    protected Location(Parcel in) {
        selectedRadius = in.readInt();
        selectedColor = in.readInt();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        address = in.readString();
        saved = in.readByte() != 0;
        ARRAY_LIST_TAG = in.readString();
        locationName = in.readString();
        locationsArrayList = in.createTypedArrayList(Location.CREATOR);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

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
                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show();
                    saveLocationsToSharedPreferences();
                }
                else{
                    saved = false;
                    Log.d(TAG, "done: failed to save " + e.getMessage());
                }

                MapFragment.getInstance().discardLocation();
                DialogAdapter.ADAPTER.dismissLoadingDialog();
                getAllLocations(true);
                MapFragment.getInstance().getLastDeviceLocation();


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
                        Location loc = new Location(object.getObjectId() ,Integer.parseInt(String.valueOf(object.getNumber("radius"))), Integer.parseInt(String.valueOf(object.getString("color"))), new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), object.getString("address"), object.getString("name"));
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

    public String getLocationName() {
        return locationName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Location> getLocationsArrayList() {
        return locationsArrayList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLocationID() {
        return locationID;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(selectedRadius);
        dest.writeInt(selectedColor);
        dest.writeParcelable(latLng, flags);
        dest.writeString(address);
        dest.writeByte((byte) (saved ? 1 : 0));
        dest.writeString(ARRAY_LIST_TAG);
        dest.writeString(locationName);
        dest.writeTypedList(locationsArrayList);
    }
}
