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


@Database(entities = {Workout.class, Set.class}, version = 5)
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
                            .addMigrations(MIGRATION_4_5)
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
            Workout running = new Workout(1, "Run and Walk");
            running.setNoRestFlag(true);
            running.setNoBreakFlag(true);
            Set run = new Set("Light Jog", "Jog at a comfortable pace", Set.ENDURANCE, 90000);
            Set walk = new Set("Brisk Walk", "Walk at a brisk pace to relax", Set.ENDURANCE, 30000);
            running.addSet(run);
            running.addSet(walk);
            mWorkoutDao.insert(running);

            // User Created
            mSetDao.insert(new Set("Header Dummy", "This shouldn't be addable", Set.USER_CREATED, 0));
            mSetDao.insert(new Set("Custom Set", "Custom set created by the user.", Set.USER_CREATED, 60000));
            // Endurance
            mSetDao.insert(run);
            mSetDao.insert(walk);

            // Strength
            mSetDao.insert(new Set("Pushups", "Back straight and arms in line with shouldars", Set.STRENGTH, 45000));
            mSetDao.insert(new Set("Situps", "Arms across chest, use your core to rise to your knees", Set.STRENGTH, 45000));
            mSetDao.insert(new Set("Plank", "Get in a pushup position. Rest your elbows on the floor and hold this position", Set.STRENGTH, 60000));

            // Balance
            mSetDao.insert(new Set("One foot stand", "Stand on one foot, with other leg tucked in, resting your foot on your inner thigh", Set.BALANCE, 60000));

            // Flexibility
            mSetDao.insert(new Set("Yoda Dog Pose", "Do the yoga dog pose thing, UPDATE THIS", Set.FLEXIBILITY, 45000));

            // Other
            mSetDao.insert(new Set("Study", "Focus on studying with no distractions", Set.OTHER, 900000));
            mSetDao.insert(new Set("Study Break", "Take a break from studying. Take a walk, get a drink, use the restroom", Set.OTHER, 300000));

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

    private static final Migration MIGRATION_4_5 = new Migration(4, 5){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sets ADD COLUMN setType INTEGER NOT NULL DEFAULT 0");
        }
    };

}
