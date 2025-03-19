package com.example.mobilclicker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Upgrade.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UpgradeDAO upgradeDAO();
}