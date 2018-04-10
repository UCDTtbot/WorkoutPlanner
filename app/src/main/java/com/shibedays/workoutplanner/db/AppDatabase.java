package com.shibedays.workoutplanner.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.dao.SetDao;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


@Database(entities = {Workout.class, Set.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DATABASE_NAME = "workout-database";

    public abstract WorkoutDao workoutDao();
    public abstract SetDao setDao();

    public static AppDatabase getDatabaseInstance(final Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class) {
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
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

        private final WorkoutDao mWorkoutDao;
        private final SetDao mSetDao;

        PopulateDBAsync(AppDatabase db){
            mWorkoutDao = db.workoutDao();
            mSetDao = db.setDao();
        }

        @Override
        protected Void doInBackground(final Void... params){
            Workout defaultWorkout = new Workout(0, "Example Workout");
            Set set_1 = new Set("Example Set", "Description of your workout.", 30000);
            Set set_2 = new Set("Example Set", "Swipe to delete", 60000);
            defaultWorkout.addSet(set_1);
            defaultWorkout.addSet(set_2);
            mWorkoutDao.insert(defaultWorkout);

            Workout running = new Workout(1, "Run and Walk");
            running.setNoRestFlag(true);
            running.setNoBreakFlag(true);
            Set run = new Set("Run", "Jog for a minute and a half", 90000);
            Set walk = new Set("Walk", "Walk at a brisk pace", 30000);
            running.addSet(run);
            running.addSet(walk);
            mWorkoutDao.insert(running);

            Set user = new Set("User Created", "Custom Set", 60000);
            mSetDao.insert(user);
            return null;
        }
    }

    private static final Migration MIGRATION_2_3 = new Migration(2,3){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE workouts ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `sets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `descrip` TEXT, `time` INTEGER NOT NULL)");
        }
    };

}
