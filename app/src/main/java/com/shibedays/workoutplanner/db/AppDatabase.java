package com.shibedays.workoutplanner.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


@Database(entities = {Workout.class}, version = 2)
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
                            //TODO: create migration schema
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
            Workout defaultWorkout = new Workout(0, "Example Workout");
            Set set_1 = new Set("Example Set", "Description of your workout.", 30000);
            Set set_2 = new Set("Example Set", "Swipe to delete", 60000);
            Set set_3 = new Set("Pushups", "Normal Pushups", 45000);
            defaultWorkout.addSet(set_1);
            defaultWorkout.addSet(set_2);
            defaultWorkout.addSet(set_3);
            mDao.insert(defaultWorkout);
            return null;
        }
    }

}
