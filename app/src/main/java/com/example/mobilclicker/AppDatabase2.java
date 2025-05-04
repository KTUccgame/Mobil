package com.example.mobilclicker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ProfileSettings.class}, version = 1, exportSchema = false)
public abstract class AppDatabase2 extends RoomDatabase {
    public abstract ProfileSettingsDAO profileDAO();
}