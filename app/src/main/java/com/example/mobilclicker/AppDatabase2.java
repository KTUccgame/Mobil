package com.example.mobilclicker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ProfileSettings.class}, version = 1, exportSchema = false)
public abstract class AppDatabase2 extends RoomDatabase {
    public abstract ProfileSettingsDAO profileDAO();

    private static volatile AppDatabase2 INSTANCE;

    public static AppDatabase2 getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase2.class) { // Thread-safe singleton
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase2.class, "my_app_db2")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}