package com.example.mobilclicker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Upgrade.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Abstract method for DAO access
    public abstract UpgradeDAO upgradeDAO();

    // Singleton instance of the database
    private static AppDatabase instance;

    // Get the database instance
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Build the database instance
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database") // Database name
                    .fallbackToDestructiveMigration() // Handle migrations if schema changes
                    .build();
        }
        return instance;
    }
}
