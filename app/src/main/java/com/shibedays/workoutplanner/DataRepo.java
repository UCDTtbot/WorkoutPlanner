package com.shibedays.workoutplanner;

import java.util.List;

/**
 * Created by ttbot on 2/21/18.
 */

public class DataRepo {

    private static final int OK = 1;
    private static final int FAILED = -1;

    private static DataRepo Instance;

    private final AppDatabase db;
    private List<Workout> workouts;

    private DataRepo(final AppDatabase db){
        this.db = db;
    }

    public static DataRepo getInstance(final AppDatabase db){
        if(Instance == null){
            Instance = new DataRepo(db);
        }
        return Instance;
    }


    //Functions for getting data from Workouts
    public List<Workout> getAllWorkouts(){
        return workouts;
    }

    public Workout getWorkoutById(int pos){
        return db.workoutDao().findWorkoutByID(pos);
    }

    public Workout getWorkoutByName(String name){
        return db.workoutDao().findWorkoutByName(name);
    }

    public List<Workout> addWorkout(Workout workout){
        db.workoutDao().insert(workout);
        if(db.workoutDao().findWorkoutByName(workout.getName()) != null){
            workouts = db.workoutDao().getAll();
            return workouts;
        }else{
            return workouts;
        }
    }

    public List<Workout> removeWorkout(Workout workout){
        db.workoutDao().delete(workout);
        if(db.workoutDao().findWorkoutByName(workout.getName()) == null){
            workouts = db.workoutDao().getAll();
            return workouts;
        } else{
            return workouts;
        }
    }
}
