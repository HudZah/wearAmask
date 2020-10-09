package com.hudzah.wearamask;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.gson.Gson;

import java.util.ArrayList;

@Entity(tableName = "location_table")
public class Location implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int locationID;

    @Ignore
    private Context context;


    private int selectedRadius;
    private int selectedColor;
    private Double latitude;
    private Double longitude;
    private String address;
    @Ignore
    public boolean saved = false;
    @Ignore
    private static final String TAG = "Location";
    @Ignore
    String ARRAY_LIST_TAG = "locationsArrayList";
    private String locationName;

    @Ignore
    Gson gson;

    @Ignore
    public ArrayList<com.hudzah.wearamask.Location> locationsArrayList = new ArrayList<>();

    public Location(int selectedRadius, int selectedColor, Double latitude, Double longitude, String address, String locationName) {
        this.selectedRadius = selectedRadius;
        this.selectedColor = selectedColor;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.locationName = locationName;
    }

    protected Location(Parcel in) {
        locationID = in.readInt();
        selectedRadius = in.readInt();
        selectedColor = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
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

    public void saveLocation(Place place){
        DialogAdapter.ADAPTER.loadingDialog();
        LocationRepository locationRepository = new LocationRepository(context);

        if(locationRepository.insert(new Location(selectedRadius,
                                                    selectedColor, place.getLatLng().latitude,
                                                    place.getLatLng().longitude, place.getAddress(),
                                                    place.getName())) >= 0){

            Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show();
            MapFragment.getInstance().discardLocation();
            CircleManager.Manager.clearGeofences();
            DialogAdapter.ADAPTER.dismissLoadingDialog();
            getAllLocations(true);
            MapFragment.getInstance().getLastDeviceLocation();

        }
    }

    public void drawAllLocations(){
        if(locationsArrayList.size() > 0 && locationsArrayList != null) {
            CircleManager.Manager.drawManyCirclesOnMap(locationsArrayList);
        }
    }


    public ArrayList<Location> getAllLocations(boolean drawCircles){

        LocationRepository locationRepository = new LocationRepository(context);

        locationsArrayList = locationRepository.getAllLocations();

        if(locationsArrayList != null) {

            if (drawCircles) {
                drawAllLocations();
            }
        }

        Log.d(TAG, "getLocationsFromSharedPreferences: locations from sharedprefs are " + locationsArrayList);

        return locationsArrayList;

    }

    public int getSelectedRadius() {
        return selectedRadius;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    public ArrayList<Location> getLocationsArrayList() {
        return locationsArrayList;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getLocationID() {
        return locationID;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(locationID);
        dest.writeInt(selectedRadius);
        dest.writeInt(selectedColor);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeByte((byte) (saved ? 1 : 0));
        dest.writeString(ARRAY_LIST_TAG);
        dest.writeString(locationName);
        dest.writeTypedList(locationsArrayList);
    }
}
