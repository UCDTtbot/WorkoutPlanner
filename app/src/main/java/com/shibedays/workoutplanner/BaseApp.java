package com.shibedays.workoutplanner;

import android.app.Application;

import com.shibedays.workoutplanner.db.AppDatabase;

/**
 * Created by ttbot on 2/23/18.
 */

public class BaseApp extends Application {

    public AppDatabase getDatabase(){
        return AppDatabase.getDatabaseInstance(this);
    }

    public DataRepo getRepo(){
        return DataRepo.getInstance(getDatabase());
    }
}
