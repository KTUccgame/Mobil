package com.example.mobilclicker;

import android.app.Application;
import androidx.room.Room;

public class AppActivity extends Application
{
    static AppDatabase db;
    static AppDatabase2 db2;
    @Override
    public void onCreate() {
        super.onCreate();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my_app_db")
                .allowMainThreadQueries().build();
        db2 = Room.databaseBuilder(getApplicationContext(), AppDatabase2.class, "my_app_db2")
                .allowMainThreadQueries().build();
    }
    public static AppDatabase getDatabase() {return db;}
    public static AppDatabase2 getDatabase2() { return db2;}
}
