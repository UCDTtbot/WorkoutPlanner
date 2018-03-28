package com.shibedays.workoutplanner;

import android.app.Application;

import com.shibedays.workoutplanner.db.AppDatabase;


public class BaseApp extends Application {

    public AppDatabase getDatabase(){
        return AppDatabase.getDatabaseInstance(this);
    }

    public DataRepo getRepo(){
        return DataRepo.getInstance(getDatabase());
    }
}
