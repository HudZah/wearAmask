package com.hudzah.wearamask;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Location.class, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    private static LocationDatabase instance;

    public abstract LocationDao locationDao();

    public static synchronized LocationDatabase getInstance(Context context){
        // Singleton pattern by using synchronized
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                                            LocationDatabase.class, "location_database")
                                            .fallbackToDestructiveMigration()
                                            .allowMainThreadQueries()
                                            .build();
        }

        return instance;
    }
}
