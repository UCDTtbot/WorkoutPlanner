package com.shibedays.workoutplanner;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.shibedays.workoutplanner.db.AppDatabase;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;

/**
 * Created by ttbot on 2/21/18.
 */

public class DataRepo {

    private static final int OK = 1;
    private static final int FAILED = -1;

    private static DataRepo INSTANCE;

    private final AppDatabase mDatabase;
    private LiveData<List<Workout>> mWorkouts;

    private DataRepo(final AppDatabase db){
        mDatabase = db;
        mWorkouts = mDatabase.workoutDao().getAll();
    }


    public static DataRepo getInstance(final AppDatabase db){
        if(INSTANCE == null){
            synchronized (DataRepo.class){
                if(INSTANCE == null){
                    INSTANCE = new DataRepo(db);
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<Workout>> getAllWorkouts(){
        return mWorkouts;
    }

    public LiveData<Workout> getWorkout(final int id){
        return mDatabase.workoutDao().getWorkout(id);
    }

    public void insert(Workout workout){
        new insertAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }

    public void remove(final Workout workout){
        new removeAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }

    private static class insertAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao aSyncTaskDao;
        insertAsyncWorkout(WorkoutDao dao){
            aSyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(final Workout... params){
            aSyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class removeAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao aSyncTaskDao;
        removeAsyncWorkout(WorkoutDao dao){
            aSyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(Workout... workouts) {
            aSyncTaskDao.delete(workouts[0]);
            return null;
        }
    }
}
