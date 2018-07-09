package com.shibedays.workoutplanner.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.dao.SetDao;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;

import java.util.ArrayList;
import java.util.List;


@Database(entities = {Workout.class, Set.class}, version = 11)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DATABASE_NAME = "workoutDB";

    private static final int ONE_MIN = 60000;
    private static final int ONE_HALF = 90000;
    private static final int FORTY_FIVE = 45000;
    private static final int THIRTY = 30000;
    private static final int TWO_MIN = 120000;

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
            Resources r = BaseApp.getAppContext().getResources();
            // Cardio
            final Set jog = new Set(
                    "Light Jog",
                    r.getString(R.string.descrip_jog),
                    Set.CARDIO, TWO_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set run = new Set(
                    "Run",
                    r.getString(R.string.descrip_run),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set walk = new Set(
                    "Brisk Walk",
                    r.getString(R.string.descrip_walk),
                    Set.CARDIO, TWO_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set high_steps = new Set(
                    "High Steps",
                    r.getString(R.string.descrip_highstep),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set lunges = new Set(
                    "Lunges",
                    r.getString(R.string.descrip_lunges),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set reverse_lunge = new Set(
                    "Reverse Lunge w/ Kick",
                    r.getString(R.string.descrip_reverselunge),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set side_lunge = new Set(
                    "Side Lunge",
                    r.getString(R.string.descrip_sidelunge),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp));
            final Set jumping_jacks = new Set(
                    "Jumping Jacks",
                    r.getString(R.string.descrip_jumpingjack),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set butt_kickers = new Set(
                    "Butt Kickers",
                    r.getString(R.string.descrip_buttkickers),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));

            // Upper Body
            final Set pushups = new Set(
                    "Pushups",
                    r.getString(R.string.descrip_pushups),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set dips = new Set(
                    "Dips",
                    r.getString(R.string.descrip_dips),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set bicep_curls = new Set(
                    "Bicep Curls",
                    r.getString(R.string.descrip_curls),
                    Set.UPPER_BODY, THIRTY,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set shoulder_press = new Set(
                    "Shoulder Press",
                    r.getString(R.string.descrip_shoulderpress),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set chest_press = new Set(
                    "Chest Press",
                    r.getString(R.string.descrip_chestpress),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set bent_row = new Set(
                    "Bent Over Row",
                    r.getString(R.string.descrip_upperrow),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));


            // Lower Body
            final Set squats = new Set(
                    "Squats",
                    r.getString(R.string.descrip_squats),
                    Set.LOWER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set chair_squat = new Set(
                    "Chair Squats",
                    r.getString(R.string.descrip_chairsquats),
                    Set.LOWER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set wall_squats = new Set(
                    "Wall Squats",
                    r.getString(R.string.descrip_wallsquats),
                    Set.LOWER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set step_up = new Set(
                    "Step Ups",
                    r.getString(R.string.descrip_stepup),
                    Set.LOWER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));

            // Core
            final Set situps = new Set(
                    "Sit Ups",
                    r.getString(R.string.descrip_situps),
                    Set.CORE, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set roll_ups = new Set(
                    "Roll Ups",
                            r.getString(R.string.descrip_rollups),
                    Set.CORE, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set plank = new Set(
                    "Basic Plank",
                    r.getString(R.string.descrip_plank),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set hip_airplane = new Set(
                    "Hip Airplane",
                    r.getString(R.string.descrip_airplane),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set good_morning = new Set(
                    "Good Morning",
                    r.getString(R.string.descrip_goodmorning),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set windshield = new Set(
                    "Windshield Wipers",
                    r.getString(R.string.descrip_windshield),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set airbike = new Set(
                    "Air Bike",
                    r.getString(R.string.descrip_airbike),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set t_plank = new Set(
                    "T Plank",
                    r.getString(R.string.descrip_tplank),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));

            // Flexibility
            final Set yoga = new Set(
                    "Yoga",
                    r.getString(R.string.descrip_yoga),
                    Set.FLEXIBILITY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));


            // Other
            final Set study = new Set(
                    "Study",
                    r.getString(R.string.descrip_study),
                    Set.OTHER, 900000,
                    r.getResourceEntryName(R.drawable.ic_access_alarm_black_24dp));
            final Set study_break = new Set(
                    "Study Break",
                    r.getString(R.string.descrip_studybreak),
                    Set.OTHER, 300000,
                    r.getResourceEntryName(R.drawable.ic_access_alarm_black_24dp));

            List<Set> allSets = new ArrayList<Set>(){{
                add(jog);               // 0
                add(run);               // 1
                add(walk);              // 2
                add(high_steps);        // 3
                add(pushups);           // 4
                add(dips);              // 5
                add(bicep_curls);       // 6
                add(shoulder_press);    // 7
                add(squats);            // 8
                add(wall_squats);       // 9
                add(situps);            // 10
                add(roll_ups);          // 11
                add(plank);             // 12
                add(lunges);            // 13
                add(yoga);              // 14
                add(study);             // 15
                add(study_break);       // 16
                add(chair_squat);       // 17
                add(chest_press);       // 18
                add(hip_airplane);      // 19
                add(good_morning);      // 20
                add(bent_row);          // 21
                add(step_up);           // 22
                add(reverse_lunge);     // 23
            }};
            long[] ids = mSetDao.insertAll(allSets);
            int i = 0;
            for(Set s : allSets){
                s.setSetId((int)ids[i++]);
            }

            // Workouts //
            Workout card_1 = new Workout(Workout.CARDIO, "Cardio_1");
            card_1.addSet(jog);
            card_1.addSet(run);
            Workout card_2 = new Workout(Workout.CARDIO, "Cardio_2");
            card_2.addSet(jog);
            card_2.addSet(run);
            Workout card_3 = new Workout(Workout.CARDIO, "Cardio_3");
            card_3.addSet(jog);
            card_3.addSet(run);
            Workout str_1 = new Workout(Workout.STRENGTH, "Strength_1");
            str_1.addSet(pushups);
            str_1.addSet(situps);
            str_1.addSet(plank);
            str_1.addSet(dips);
            Workout str_2 = new Workout(Workout.STRENGTH, "Strength_2");
            str_2.addSet(lunges);
            str_2.addSet(bicep_curls);
            str_2.addSet(shoulder_press);
            Workout str_3 = new Workout(Workout.STRENGTH, "Strength_3");
            str_3.addSet(lunges);
            str_3.addSet(bicep_curls);
            str_3.addSet(shoulder_press);
            Workout str_4 = new Workout(Workout.STRENGTH, "Strength_4");
            str_4.addSet(lunges);
            str_4.addSet(bicep_curls);
            str_4.addSet(shoulder_press);
            Workout str_5 = new Workout(Workout.STRENGTH, "Strength_5");
            str_5.addSet(lunges);
            str_5.addSet(bicep_curls);
            str_5.addSet(shoulder_press);
            Workout flex_1 = new Workout(Workout.FLEXIBILITY, "Flexibility_1");
            flex_1.addSet(yoga);
            Workout other_1 = new Workout(Workout.OTHER, "Other_1");
            other_1.addSet(study);
            other_1.addSet(study_break);
            Workout other_2 = new Workout(Workout.OTHER, "Other_2");
            other_2.addSet(study);
            other_2.addSet(study_break);
            Workout other_3 = new Workout(Workout.OTHER, "Other_3");
            other_3.addSet(study);
            other_3.addSet(study_break);
            Workout other_4 = new Workout(Workout.OTHER, "Other_4");
            other_4.addSet(study);
            other_4.addSet(study_break);
            //Workout dummy = new Workout(-1, Workout.USER_CREATED, "Add Workout", R.drawable.ic_add_black_24dp);
            mWorkoutDao.insert(card_1);
            mWorkoutDao.insert(card_2);
            mWorkoutDao.insert(card_3);
            mWorkoutDao.insert(str_1);
            mWorkoutDao.insert(str_2);
            mWorkoutDao.insert(str_3);
            mWorkoutDao.insert(str_4);
            mWorkoutDao.insert(str_5);
            mWorkoutDao.insert(flex_1);
            mWorkoutDao.insert(other_1);

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
