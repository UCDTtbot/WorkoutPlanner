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


@Database(entities = {Workout.class, Set.class}, version = 7)
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
                            .addMigrations(MIGRATION_5_6)
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

        private final WorkoutDao mWorkoutDao;
        private final SetDao mSetDao;

        PopulateDBAsync(AppDatabase db){
            mWorkoutDao = db.workoutDao();
            mSetDao = db.setDao();
        }

        @Override
        protected Void doInBackground(final Void... params){
            // TODO: EDIT ALL SET DESCRIPS TO BE STRING RESOURCES
            // Sets //
            int setsId = 0;
            // User Created
            final Set headerDummy = new Set(setsId++, "Header Dummy", "This shouldn't be addable", Set.USER_CREATED, 0);
            // Endurance
            final Set jog = new Set(setsId++, "Light Jog", "Jog at a comfortable pace", Set.ENDURANCE, 90000);
            final Set walk = new Set(setsId++, "Brisk Walk", "Walk at a brisk pace to relax", Set.ENDURANCE, 30000);

            // Strength
            final Set pushups = new Set(setsId++, "Pushups", "Back straight and arms in line with shouldars", Set.STRENGTH, 45000);
            final Set situps = new Set(setsId++, "Situps", "Arms across chest, use your core to rise to your knees", Set.STRENGTH, 45000);
            final Set plank = new Set(setsId++, "Plank", "Get in a pushup position. Rest your elbows on the floor and hold this position", Set.STRENGTH, 60000);

            // Balance
            final Set one_foot = new Set(setsId++, "One foot stand", "Stand on one foot, with other leg tucked in, resting your foot on your inner thigh", Set.BALANCE, 60000);

            // Flexibility
            final Set yoga_dog = new Set(setsId++, "Yoda Dog Pose", "Do the yoga dog pose thing, UPDATE THIS", Set.FLEXIBILITY, 45000);

            // Other
            final Set study = new Set(setsId++, "Study", "Focus on studying with no distractions", Set.OTHER, 900000);
            final Set study_break = new Set(setsId++, "Study Break", "Take a break from studying. Take a walk, get a drink, use the restroom", Set.OTHER, 300000);
            List<Set> allSets = new ArrayList<Set>(){{
                add(headerDummy);
                add(jog);
                add(walk);
                add(pushups);
                add(situps);
                add(plank);
                add(one_foot);
                add(yoga_dog);
                add(study);
                add(study_break);
            }};
            mSetDao.insertAll(allSets);

            // Workouts //
            int workoutIds = 0;
            Workout workout_1 = new Workout(0, Workout.CARDIO, "Cardio_1");
            workout_1.addSet(jog);
            workout_1.addSet(walk);
            Workout workout_2 = new Workout(1, Workout.STRENGTH, "Strength_1");
            workout_2.addSet(pushups);
            workout_2.addSet(situps);
            workout_2.addSet(plank);
            Workout workout_3 = new Workout(2, Workout.BALANCE, "Balance_1");
            workout_3.addSet(one_foot);
            Workout workout_4 = new Workout(3, Workout.FLEXIBILITY, "Flexibility_1");
            workout_4.addSet(yoga_dog);
            Workout workout_5 = new Workout(4, Workout.OTHER, "Other_1");
            workout_5.addSet(study);
            workout_5.addSet(study_break);
            Workout dummy = new Workout(5, Workout.USER_CREATED, "Dummy_1");
            mWorkoutDao.insert(workout_1);
            mWorkoutDao.insert(workout_2);
            mWorkoutDao.insert(workout_3);
            mWorkoutDao.insert(workout_4);
            mWorkoutDao.insert(workout_5);
            mWorkoutDao.insert(dummy);

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

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("CREATE TABLE sets_new (id INTEGER NOT NULL, setType INTEGER NOT NULL, " +
                    "name TEXT, descrip TEXT, time INTEGER NOT NULL, PRIMARY KEY(id))");
            database.execSQL("INSERT INTO sets_new (id, setType, name, descrip, time) SELECT id, setType, name, descrip, " +
                    "time FROM sets");
            database.execSQL("DROP TABLE sets");
            database.execSQL("ALTER TABLE sets_new RENAME TO sets");
        }
    };

}
