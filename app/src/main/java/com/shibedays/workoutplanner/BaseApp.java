package com.shibedays.workoutplanner;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.AppDatabase;
import com.shibedays.workoutplanner.db.entities.Workout;


public class BaseApp extends Application {

    public AppDatabase getDatabase(){
        return AppDatabase.getDatabaseInstance(this);
    }

    public DataRepo getRepo(){
        return DataRepo.getInstance(getDatabase());
    }


}
