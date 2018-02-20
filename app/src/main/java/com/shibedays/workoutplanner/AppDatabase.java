package com.shibedays.workoutplanner;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ttbot on 2/20/2018.
 */

@Database(entities = {Workout.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase Instance;

    public static final String DATABASE_NAME = "workout-database";

    public abstract WorkoutDao workoutDao();

    public static AppDatabase getDatabaseInstance(Context context, Executor exe){
        if(Instance == null){
            Instance = buildDatabase(context, exe);
        }

        return Instance;
    }

    private static AppDatabase buildDatabase(final Context appContext, final Executor exe){
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        exe.execute(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase database = AppDatabase.getDatabaseInstance(appContext, exe);
                                List<Workout> workouts = generateData();
                                insertData(database, workouts);
                            }
                        });
                    }
                }).build();
    }

    public static void destroyInstance(){
        Instance = null;
    }

    private static List<Workout> generateData(){
        Workout defaultWorkout1 = new Workout(0, "Cardio Day");
        Workout defaultWorkout2 = new Workout(1, "Leg Day");
        List<Workout> newList = new ArrayList<Workout>();
        newList.add(defaultWorkout1);
        newList.add(defaultWorkout2);
        return newList;
    }

    private static void insertData(final AppDatabase database, final List<Workout> workouts){
        database.runInTransaction(new Runnable() {
            @Override
            public void run() {
                database.workoutDao().insertAll(workouts);
            }
        });
    }

}
