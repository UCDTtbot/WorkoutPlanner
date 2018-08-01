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


@Database(entities = {Workout.class, Set.class}, version = 12)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DATABASE_NAME = "workoutDB";

    private static final int ONE_MIN = 60000;
    private static final int ONE_HALF = 90000;
    private static final int FORTY_FIVE = 45000;
    private static final int THIRTY = 30000;
    private static final int FIFTEEN = 15000;
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
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp),
                    "https://www.bodybuilding.com/exercises/slow-jog");
            final Set run = new Set(
                    "Run",
                    r.getString(R.string.descrip_run),
                    Set.CARDIO, TWO_MIN,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp),
                    "https://www.bodybuilding.com/exercises/trail-runningwalking");
            final Set walk = new Set(
                    "Brisk Walk",
                    r.getString(R.string.descrip_walk),
                    Set.CARDIO, ONE_HALF,
                    r.getResourceEntryName(R.drawable.ic_walk_black_24dp));
            final Set high_steps = new Set(
                    "High Knee Jog",
                    r.getString(R.string.descrip_highstep),
                    Set.CARDIO, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/high-knee-jog");
            final Set lunges = new Set(
                    "Lunges",
                    r.getString(R.string.descrip_lunges),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-lunges");
            final Set reverse_lunge = new Set(
                    "Reverse Lunge",
                    r.getString(R.string.descrip_reverselunge),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-lunges");
            final Set side_lunge = new Set(
                    "Side Lunge",
                    r.getString(R.string.descrip_sidelunge),
                    Set.CARDIO, ONE_HALF,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/side-lunge");
            final Set jumping_jacks = new Set(
                    "Jumping Jacks",
                    r.getString(R.string.descrip_jumpingjack),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/jumping-jacks");
            final Set butt_kickers = new Set(
                    "Butt Kicks",
                    r.getString(R.string.descrip_buttkickers),
                    Set.CARDIO, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_run_black_24dp),
                    "https://www.bodybuilding.com/exercises/butt-kicks");
            final Set squat_jumps = new Set(
                    "Squat Jumps",
                    r.getString(R.string.descrip_squat_jumps),
                    Set.CARDIO, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set plank_jumps = new Set(
                    "Plank Jump",
                    r.getString(R.string.descrip_plank_jumps),
                    Set.CARDIO, THIRTY,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));
            final Set burpee = new Set(
                    "Burpees",
                    r.getString(R.string.descrip_burpees),
                    Set.CARDIO, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/burpee");
            final Set plank_jacks = new Set(
                    "Plank Jacks",
                    r.getString(R.string.descrip_plankjacks),
                    Set.CARDIO, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));

            // Upper Body
            final Set pushups = new Set(
                    "Pushups",
                    r.getString(R.string.descrip_pushups),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/pushups");
            final Set dips = new Set(
                    "Bench Dips",
                    r.getString(R.string.descrip_dips),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/bench-dips");
            final Set bicep_curls = new Set(
                    "Bicep Curls",
                    r.getString(R.string.descrip_curls),
                    Set.UPPER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-bicep-curl");
            final Set shoulder_press = new Set(
                    "Shoulder Press",
                    r.getString(R.string.descrip_shoulderpress),
                    Set.UPPER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-shoulder-press");
            final Set chest_press = new Set(
                    "Dumbbell Bench Press",
                    r.getString(R.string.descrip_chestpress),
                    Set.UPPER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-bench-press");
            final Set bent_row = new Set(
                    "Bent Over Row",
                    r.getString(R.string.descrip_upperrow),
                    Set.UPPER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/bent-over-two-dumbbell-row");
            final Set sp_squats = new Set(
                    "Shoulder Press Squats",
                    r.getString(R.string.descrip_shoulder_press_squats),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-squat-to-shoulder-press");
            final Set plank_arm_raise = new Set(
                    "Arm Raise Plank",
                    r.getString(R.string.descrip_arm_raise_plank),
                    Set.UPPER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/plank");


            // Lower Body
            final Set squats = new Set(
                    "Squats",
                    r.getString(R.string.descrip_squats),
                    Set.LOWER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/bodyweight-squat");
            final Set wall_squats = new Set(
                    "Wall Squats",
                    r.getString(R.string.descrip_wallsquats),
                    Set.LOWER_BODY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/wall-squat");
            final Set step_up = new Set(
                    "Step Ups",
                    r.getString(R.string.descrip_stepup),
                    Set.LOWER_BODY, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/dumbbell-step-ups");
            final Set pile_squat = new Set(
                    "Pile Squat",
                    r.getString(R.string.descrip_pile_squat),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.jefit.com/exercises/213/dumbbell-pile-squat");

            // Core
            final Set situps = new Set(
                    "Crunches",
                    r.getString(R.string.descrip_situps),
                    Set.CORE, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/crunches");
            final Set plank = new Set(
                    "Basic Plank",
                    r.getString(R.string.descrip_plank),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/plank");
            final Set airbike = new Set(
                    "Air Bike",
                    r.getString(R.string.descrip_airbike),
                    Set.CORE, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/air-bike");
            final Set t_plank = new Set(
                    "Plank with Twist",
                    r.getString(R.string.descrip_tplank),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/plank-with-twist");
            final Set mountain_climber = new Set(
                    "Mountain Climbers",
                    r.getString(R.string.descrip_mountain_climbers),
                    Set.CORE, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/mountain-climbers");
            final Set leg_raises = new Set(
                    "Leg Raises",
                    r.getString(R.string.descrip_leg_raises),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/flat-bench-lying-leg-raise");
            final Set reverse_crunch = new Set(
                    "Reverse Crunch",
                    r.getString(R.string.descrip_reverse_crunch),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/reverse-crunch");
            final Set superman = new Set(
                    "Superman",
                    r.getString(R.string.descrip_superman),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/superman");
            final Set leg_pullin = new Set(
                    "Seated Leg Pull In",
                    r.getString(R.string.descrip_legpulls),
                    Set.CORE, ONE_MIN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/leg-pull-in");

            // Flexibility
            final Set arm_spins = new Set(
                    "Arm Circles",
                    r.getString(R.string.descrip_arm_spins),
                    Set.FLEXIBILITY, THIRTY,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/arm-circles");
            final Set arm_cross = new Set(
                    "Shoulder Stretch",
                    r.getString(R.string.descrip_arm_cross),
                    Set.FLEXIBILITY, THIRTY,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/shoulder-stretch");
            final Set toe_touch = new Set(
                    "Toe Touch",
                    r.getString(R.string.descrip_toe_touch),
                    Set.FLEXIBILITY, FIFTEEN,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp),
                    "https://www.bodybuilding.com/exercises/standing-toe-touches");
            final Set butterfly = new Set(
                    "Butterfly Stretch",
                    r.getString(R.string.descrip_butterfly),
                    Set.FLEXIBILITY, FORTY_FIVE,
                    r.getResourceEntryName(R.drawable.ic_fitness_black_24dp));

            // Other
            final Set study = new Set(
                    "Study Session",
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
                add(plank);             // 11
                add(lunges);            // 12
                add(arm_spins);         // 13
                add(study);             // 14
                add(study_break);       // 15
                add(chest_press);       // 16
                add(bent_row);          // 17
                add(step_up);           // 18
                add(reverse_lunge);     // 19
                add(side_lunge);        // 20
                add(jumping_jacks);     // 21
                add(butt_kickers);      // 22
                add(airbike);           // 23
                add(t_plank);           // 24
                add(pile_squat);        // 25
                add(leg_raises);        // 26
                add(mountain_climber);  // 27
                add(squat_jumps);       // 28
                add(reverse_crunch);    // 29
                add(sp_squats);         // 30
                add(plank_arm_raise);   // 31
                add(superman);          // 32
                add(plank_jumps);       // 33
                add(burpee);            // 34
                add(plank_jacks);       // 35
                add(leg_pullin);        // 36
                add(arm_cross);         // 37
                add(toe_touch);         // 38
                add(butterfly);         // 39

            }};
            long[] ids = mSetDao.insertAll(allSets);
            int i = 0;
            for(Set s : allSets){
                s.setSetId((int)ids[i++]);
            }

            // Workouts //
            Workout card_1 = new Workout(Workout.CARDIO, "Jog-Walk");
            card_1.addSet(walk);
            card_1.addSet(jog);
            Workout card_4 = new Workout(Workout.CARDIO, "Run-Walk");
            card_4.addSet(walk);
            card_4.addSet(run);
            Workout card_2 = new Workout(Workout.CARDIO, "Intense Cardio");
            card_2.addSet(burpee);
            card_2.addSet(jumping_jacks);
            card_2.addSet(plank_jumps);
            Workout card_3 = new Workout(Workout.CARDIO, "Light Cardio");
            card_3.addSet(high_steps);
            card_3.addSet(butt_kickers);
            card_3.addSet(lunges);
            card_3.addSet(jumping_jacks);

            Workout str_1 = new Workout(Workout.STRENGTH, "Body Weight");
            str_1.addSet(pushups);
            str_1.addSet(squats);
            str_1.addSet(plank);
            str_1.addSet(jumping_jacks);
            str_1.addSet(leg_pullin);
            Workout str_2 = new Workout(Workout.STRENGTH, "Body Weight Light");
            str_2.addSet(wall_squats);
            str_2.addSet(situps);
            str_2.addSet(lunges);
            str_2.addSet(plank);
            Workout str_3 = new Workout(Workout.STRENGTH, "Body Weight Intense");
            str_3.addSet(mountain_climber);
            str_3.addSet(squat_jumps);
            str_3.addSet(jumping_jacks);
            str_3.addSet(airbike);
            str_3.addSet(leg_pullin);
            Workout str_4 = new Workout(Workout.STRENGTH, "Upper Body");
            str_4.addSet(pushups);
            str_4.addSet(shoulder_press);
            str_4.addSet(bicep_curls);
            str_4.addSet(plank_arm_raise);
            str_4.addSet(bent_row);
            str_4.setEquipmentRequired(true);
            Workout str_5 = new Workout(Workout.STRENGTH, "Lower Body");
            str_5.addSet(pile_squat);
            str_5.addSet(step_up);
            str_5.addSet(jumping_jacks);
            str_5.addSet(wall_squats);
            str_5.addSet(leg_raises);
            str_5.setEquipmentRequired(true);
            Workout str_6 = new Workout(Workout.STRENGTH, "Core");
            str_6.addSet(situps);
            str_6.addSet(jumping_jacks);
            str_6.addSet(plank);
            str_6.addSet(mountain_climber);
            str_6.addSet(t_plank);
            str_6.addSet(leg_pullin);

            Workout flex_1 = new Workout(Workout.FLEXIBILITY, "Arm Stretches");
            flex_1.addSet(arm_spins);
            flex_1.addSet(arm_cross);
            Workout flex_2 = new Workout(Workout.FLEXIBILITY, "Leg Stretches");
            flex_2.addSet(toe_touch);
            flex_2.addSet(butterfly);

            Workout other_1 = new Workout(Workout.OTHER, "Study Session");
            other_1.addSet(study);
            other_1.addSet(study_break);

            //Workout dummy = new Workout(-1, Workout.USER_CREATED, "Add Workout", R.drawable.ic_add_black_24dp);
            mWorkoutDao.insert(card_1);
            mWorkoutDao.insert(card_2);
            mWorkoutDao.insert(card_3);
            mWorkoutDao.insert(card_4);

            mWorkoutDao.insert(str_1);
            mWorkoutDao.insert(str_2);
            mWorkoutDao.insert(str_3);
            mWorkoutDao.insert(str_4);
            mWorkoutDao.insert(str_5);
            mWorkoutDao.insert(str_6);

            mWorkoutDao.insert(flex_1);
            mWorkoutDao.insert(flex_2);

            mWorkoutDao.insert(other_1);

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
