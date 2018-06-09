package com.shibedays.workoutplanner.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.dao.SetDao;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


@Database(entities = {Workout.class, Set.class}, version = 10)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DATABASE_NAME = "workoutDB";

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

            // Cardio
            final Set jog = new Set(
                    "Light Jog",
                    "Jog at a comfortable pace",
                    Set.CARDIO, 120000,
                    R.drawable.ic_run_black_24dp);
            final Set run = new Set(
                    "Run",
                    "High-speeding running",
                    Set.CARDIO, 60000,
                    R.drawable.ic_run_black_24dp);
            final Set walk = new Set(
                    "Brisk Walk",
                    "Walk at a brisk pace to cool down",
                    Set.CARDIO, 120000,
                    R.drawable.ic_run_black_24dp);
            final Set high_steps = new Set(
                    "High Steps",
                    "7-steps (high knee march)",
                    Set.CARDIO, 60000,
                    R.drawable.ic_run_black_24dp);

            // Upper Body
            final Set pushups = new Set(
                    "Pushups",
                    "Lie prone on the ground with hands placed as wide or slightly wider than shoulder width. " +
                            "Keeping the body straight, lower body to the ground by bending arms at the elbows. " +
                            "Raise body up off the ground by extending the arms.",
                    Set.UPPER_BODY, 60000,
                    R.drawable.ic_fitness_black_24dp);
            final Set dips = new Set(
                    "Dips",
                    "Stand with your back to a chair or bench. Be sure that the object is sturdy and can comfortably support your body weight. " +
                            "Bend your legs and place your palms on the front edge of the bench, with your fingers pointing forward. " +
                            "Slowly walk your feet out in front of you, until the majority of your body weight is resting on your arms. " +
                            "Inhale, and keeping your elbows tucked in at your sides, slowly bend your arms and lower your body until your upper arms are parallel with the floor. " +
                            "Hold for a second, then exhale and straighten your arms back up to the starting position.",
                    Set.UPPER_BODY, 60000,
                    R.drawable.ic_fitness_black_24dp);
            final Set bicep_curls = new Set(
                    "Bicep Curls",
                    "Extend your arm down by your side and hold the weight, palms facing forward. " +
                            "Make sure your elbows are tucked and your shoulders are straight. " +
                            "When you’re in position, slowly bring the weight up to your shoulders, not outside of your shoulders and not too far into your chest.",
                    Set.UPPER_BODY, 30000,
                    R.drawable.ic_fitness_black_24dp);
            final Set shoulder_press = new Set(
                    "Shoulder Press",
                    "7-steps",
                    Set.UPPER_BODY, 60000,
                    R.drawable.ic_fitness_black_24dp);

            // Lower Body
            final Set squats = new Set(
                    "Squats",
                    "Stand up straight with your feet firmly planted on the ground approximately shoulder width apart. " +
                            "Contract your abdominal muscles as you bend your legs at the knees. " +
                            "Either stretch your arms out ahead of you, lightly position your hands behind your ears or hold your arms at your side as you slowly lower yourself into a squatting position. " +
                            "Lower your body to a position where your thighs are almost parallel to the floor. Return to the starting position and repeat.",
                    Set.LOWER_BODY, 60000,
                    R.drawable.ic_fitness_black_24dp);
            final Set wall_squats = new Set(
                    "Wall Squats",
                    "",
                    Set.LOWER_BODY, 45000,
                    R.drawable.ic_fitness_black_24dp);

            // Core
            final Set situps = new Set(
                    "Sit Ups",
                    "Lay down with feet flat on the ground creating a 45 degree angle with your legs." +
                            "With hands by your side or across your chest, inhale and slowly raise your body up to your knees." +
                            "Exhale as you reach the top. Slowly let yourself back down to the starting position.",
                    Set.CORE, 45000,
                    R.drawable.ic_fitness_black_24dp);
            final Set roll_ups = new Set(
                    "Roll Ups",
                            "Start with your arms all the way back behind you and slowly bring them all the way forward into a sitting position. " +
                            "Inhale as you begin to move upward and exhale as you complete.",
                    Set.CORE, 45000,
                    R.drawable.ic_fitness_black_24dp);
            final Set plank = new Set(
                    "Basic Plank",
                    "Lie on your stomach, elbows close to your sides and directly under your shoulders, palms down. " +
                            "Engage the abs and slowly lift your torso off the floor, maintaining a stiff torso and legs. " +
                            "Avoid sagging at the low back or hiking up your hips. Continue to breathe while holding this position for 15 seconds or more.",
                    Set.CORE, 60000,
                    R.drawable.ic_fitness_black_24dp);
            final Set lunges = new Set(
                    "Lunges",
                    "7-steps",
                    Set.CORE, 60000,
                    R.drawable.ic_fitness_black_24dp);
            // Flexibility
            final Set yoga = new Set(
                    "Yoga",
                    "Yoga",
                    Set.FLEXIBILITY, 45000,
                    R.drawable.ic_fitness_black_24dp);

            // Other
            final Set study = new Set(
                    "Study",
                    "Focus on studying with no distractions",
                    Set.OTHER, 900000,
                    R.drawable.ic_access_alarm_black_24dp);
            final Set study_break = new Set(
                    "Study Break",
                    "Take a break from studying. Take a walk, get a drink, use the restroom",
                    Set.OTHER, 300000,
                    R.drawable.ic_access_alarm_black_24dp);

            List<Set> allSets = new ArrayList<Set>(){{
                add(jog);
                add(run);
                add(walk);
                add(high_steps);
                add(pushups);
                add(dips);
                add(bicep_curls);
                add(shoulder_press);
                add(squats);
                add(wall_squats);
                add(situps);
                add(roll_ups);
                add(plank);
                add(lunges);
                add(yoga);
                add(study);
                add(study_break);            }};
            mSetDao.insertAll(allSets);

            // Workouts //
            Workout workout_1 = new Workout(Workout.CARDIO, "Cardio_1");
            workout_1.addSet(jog);
            workout_1.addSet(run);
            Workout workout_2 = new Workout(Workout.STRENGTH, "Strength_1");
            workout_2.addSet(pushups);
            workout_2.addSet(situps);
            workout_2.addSet(plank);
            workout_2.addSet(dips);
            Workout workout_3 = new Workout(Workout.STRENGTH, "Strength_2");
            workout_3.addSet(lunges);
            workout_3.addSet(bicep_curls);
            workout_3.addSet(shoulder_press);
            Workout workout_4 = new Workout(Workout.FLEXIBILITY, "Flexibility_1");
            workout_4.addSet(yoga);
            Workout workout_5 = new Workout(Workout.OTHER, "Other_1");
            workout_5.addSet(study);
            workout_5.addSet(study_break);
            //Workout dummy = new Workout(-1, Workout.USER_CREATED, "Add Workout", R.drawable.ic_add_black_24dp);
            mWorkoutDao.insert(workout_1);
            mWorkoutDao.insert(workout_2);
            mWorkoutDao.insert(workout_3);
            mWorkoutDao.insert(workout_4);
            mWorkoutDao.insert(workout_5);
            //mWorkoutDao.insert(dummy);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
