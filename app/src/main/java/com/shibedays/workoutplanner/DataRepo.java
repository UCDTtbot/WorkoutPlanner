package com.shibedays.workoutplanner;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.shibedays.workoutplanner.db.AppDatabase;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.List;


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

    public Workout findWorkoutByID(final int id){
        return mDatabase.workoutDao().findWorkoutByID(id);
    }

    public void insert(Workout workout){
        new insertAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }

    public void remove(final Workout workout){
        new removeAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }

    public void update(Workout workout){
        new updateAsyncWorkout(mDatabase.workoutDao()).execute(workout);
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

    private static class updateAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao dao;
        updateAsyncWorkout(WorkoutDao dao){ this.dao = dao; }
        @Override
        protected Void doInBackground(Workout... workouts) {
            dao.update(workouts[0]);
            return null;
        }
    }

    private static class findByIdAsync extends AsyncTask<Integer, Void, Workout>{
        private WorkoutDao aSyncTaskDao;
        findByIdAsync(WorkoutDao dao){ aSyncTaskDao = dao; }
        @Override
        protected Workout doInBackground(Integer... params) {
            return aSyncTaskDao.findWorkoutByID(params[0]);
        }
    }
}
