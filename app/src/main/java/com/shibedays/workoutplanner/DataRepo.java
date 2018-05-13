package com.shibedays.workoutplanner;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.shibedays.workoutplanner.db.AppDatabase;
import com.shibedays.workoutplanner.db.dao.SetDao;
import com.shibedays.workoutplanner.db.dao.WorkoutDao;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;


public class DataRepo {

    private static final int OK = 1;
    private static final int FAILED = -1;

    private static DataRepo INSTANCE;

    private final AppDatabase mDatabase;
    private LiveData<List<Workout>> mWorkouts;
    private LiveData<List<Set>> mSets;

    private DataRepo(final AppDatabase db){
        mDatabase = db;
        mWorkouts = mDatabase.workoutDao().getAll();
        mSets = mDatabase.setDao().getAll();
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

    //region WORKOUT_FUNCTIONS
    public LiveData<List<Workout>> getAllWorkouts(){
        return mWorkouts;
    }
    public LiveData<List<Workout>> getTypedWorkouts(int type){
        return mDatabase.workoutDao().getTypedWorkouts(type);
    }
    public LiveData<Workout> getWorkout(final int id){
        return mDatabase.workoutDao().getWorkout(id);
    }
    public void insertWorkout(Workout workout){
        new insertAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }
    public void removeWorkout(final Workout workout){
        new removeAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }
    public void updateWorkout(Workout workout){
        new updateAsyncWorkout(mDatabase.workoutDao()).execute(workout);
    }
    //endregion

    //region SET_FUNCTIONS
    public LiveData<List<Set>> getAllSets(){
        return mSets;
    }
    public LiveData<List<Set>> getTypedSets(int type) {
        return mDatabase.setDao().getTypedSets(type);
    }
    public void insertSet(Set set){
        new insertAsyncSet(mDatabase.setDao()).execute(set);
    }
    public void removeSet(final Set set){
        new removeAsyncSet(mDatabase.setDao()).execute(set);
    }
    public void updateSet(Set set){
        new updateAsyncSet(mDatabase.setDao()).execute(set);
    }
    //endregion

    //region ASYNC_WORKOUT
    private static class insertAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao aSyncDao;
        insertAsyncWorkout(WorkoutDao dao){
            aSyncDao = dao;
        }
        @Override
        protected Void doInBackground(final Workout... params){
            aSyncDao.insert(params[0]);
            return null;
        }
    }
    private static class removeAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao aSyncDao;
        removeAsyncWorkout(WorkoutDao dao){
            aSyncDao = dao;
        }
        @Override
        protected Void doInBackground(Workout... workouts) {
            aSyncDao.delete(workouts[0]);
            return null;
        }
    }

    private static class updateAsyncWorkout extends AsyncTask<Workout, Void, Void>{
        private WorkoutDao aSyncDao;
        updateAsyncWorkout(WorkoutDao dao){ aSyncDao = dao; }
        @Override
        protected Void doInBackground(Workout... workouts) {
            aSyncDao.update(workouts[0]);
            return null;
        }
    }
    //endregion

    //region ASYNC_SETS
    private static class insertAsyncSet extends AsyncTask<Set, Void, Void>{
        private SetDao aSyncDao;

        insertAsyncSet(SetDao dao) { aSyncDao = dao; }

        @Override
        protected Void doInBackground(Set... sets) {
            aSyncDao.insert(sets[0]);
            return null;
        }
    }
    private static class removeAsyncSet extends AsyncTask<Set, Void, Void>{
        private SetDao aSyncDao;

        removeAsyncSet(SetDao dao) { aSyncDao = dao; };

        @Override
        protected Void doInBackground(Set... sets) {
            aSyncDao.delete(sets[0]);
            return null;
        }
    }
    private static class updateAsyncSet extends AsyncTask<Set, Void, Void> {
        private SetDao aSyncDao;
        updateAsyncSet(SetDao dao) { aSyncDao = dao; }
        @Override
        protected Void doInBackground(Set... sets) {
            aSyncDao.update(sets[0]);
            return null;
        }
    }
    //endregion






















}
