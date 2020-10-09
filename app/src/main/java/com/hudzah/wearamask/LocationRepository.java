package com.hudzah.wearamask;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public class LocationRepository {

    private LocationDao locationDao;
    private ArrayList<Location> allLocations;

    public LocationRepository(Context context){
        LocationDatabase database = LocationDatabase.getInstance(context);
        locationDao = database.locationDao();
        allLocations = new ArrayList<Location>(locationDao.getAllLocations());
    }

    public long insert(Location location){
        new InsertLocationAsyncTask(locationDao).execute(location);
        return 1;
    }

    public int update(Location location){
        new UpdateLocationAsyncTask(locationDao).execute(location);
        return 1;
    }

    public int delete(Location location){
        new DeleteLocationAsyncTask(locationDao).execute(location);
        return 1;
    }

    public ArrayList<Location> getAllLocations(){
        return allLocations;
    }

    private static class InsertLocationAsyncTask extends AsyncTask<Location, Void, Void>{
        private LocationDao locationDao;

        private InsertLocationAsyncTask(LocationDao locationDao){
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.insert(locations[0]);
            return null;
        }


    }

    private static class UpdateLocationAsyncTask extends AsyncTask<Location, Void, Void>{
        private LocationDao locationDao;

        private UpdateLocationAsyncTask(LocationDao locationDao){
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.update(locations[0]);
            return null;
        }
    }

    private static class DeleteLocationAsyncTask extends AsyncTask<Location, Void, Void>{
        private LocationDao locationDao;

        private DeleteLocationAsyncTask(LocationDao locationDao){
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.delete(locations[0]);
            return null;
        }
    }

}
