package com.shibedays.workoutplanner.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by ttbot on 2/20/2018.
 */

@Database(entities = {Workout.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DATABASE_NAME = "workout-database";

    public abstract WorkoutDao workoutDao();

    public static AppDatabase getDatabaseInstance(final Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class) {
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(sAppDatabaseCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    private static RoomDatabase.Callback sAppDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);
            new PopulateDBAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDBAsync extends AsyncTask<Void, Void, Void>{

        private final WorkoutDao mDao;

        PopulateDBAsync(AppDatabase db){
            mDao = db.workoutDao();
        }

        @Override
        protected Void doInBackground(final Void... params){
            Workout default1 = new Workout(0, "Cardio");
            Workout default2 = new Workout(1, "Leg Day");
            mDao.insert(default1);
            mDao.insert(default2);
            return null;
        }
    }

}
