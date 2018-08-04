package com.shibedays.workoutplanner.ui.helpers;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.View;

import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.adapters.WorkoutItemHelper;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PendingRemovalHelper {

    // Timeout Constant
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case

    private static PendingRemovalHelper mInstance;

    private List<Workout> mMainWorkoutData;
    private List<Workout> mWorkoutsPendingRemoval;


    private MainActivity mParent;
    // Threading Components

    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks


    public interface PendingListener {
        void deleteFromDB(Workout w);
        void undo(Workout w);
    }
    private PendingListener mListener;
    public static PendingRemovalHelper getInstance(Activity act, PendingListener listener) {
        if(mInstance == null){
            mInstance = new PendingRemovalHelper();
            mInstance.setParent(act);
            mInstance.setListener(listener);
            return mInstance;
        } else {
            return mInstance;
        }
    }

    private PendingRemovalHelper(){
        mWorkoutsPendingRemoval = new ArrayList<>();
    }

    //region PENDING_DELETE
    public void pendingRemoval(final int id){
        final Workout workout = getWorkoutByID(id);
        final int index = mMainWorkoutData.indexOf(workout);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mMainWorkoutData.remove(index);

            Runnable pendingRemovalRunnable = new Runnable(){
                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), index);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            View root = mParent.findViewById(android.R.id.content);
            Snackbar undoBar = Snackbar.make(root, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    undo(index, pendingPos);
                }
            });
            undoBar.show();

        }
    }
    public boolean isPendingRemoval(Workout w){
        return mWorkoutsPendingRemoval.contains(w);
    }

    private void deletePending(int pendingIndex, int origWorkoutPos){
        mListener.deleteFromDB(mWorkoutsPendingRemoval.get(pendingIndex));
        mWorkoutsPendingRemoval.remove(pendingIndex);
        // Update the file that we have removed a curWorkout
    }



    // Function for re-adding a pending item using it's orig position number
    private void undo(int itemPos, int pendingPos) {
        // Stop the relevant runnable
        // Remove item from mWorkoutsPendingRemoval
        // Re-add item to mWorkoutData at correct pos
        if(mWorkoutsPendingRemoval.get(pendingPos) != null) {
            Workout workout = mWorkoutsPendingRemoval.get(pendingPos);
            Runnable pendingRunnable = pendingRunnables.get(workout);
            pendingRunnables.remove(workout);
            if (pendingRunnable != null) {
                handler.removeCallbacks(pendingRunnable);
            }
            mWorkoutsPendingRemoval.remove(pendingPos);
            mMainWorkoutData.add(itemPos, workout);
            mListener.undo(workout);
        }
    }

    private void setParent(Activity act){
        if(act instanceof MainActivity){
            mParent = (MainActivity) act;
        }else{
            throw new RuntimeException(PendingRemovalHelper.class.getSimpleName() + " wrong parent");
        }
    }

    private void setListener(PendingListener listener){
        mListener = listener;
    }

    public void updateFullData(List<Workout> data){
        mMainWorkoutData = data;
    }

    private Workout getWorkoutByID(int id){
        for(Workout w : mMainWorkoutData){
            if (w.getWorkoutID() == id) return w;
        }
        return null;
    }

    public List<Workout> getPendingWorkouts(int type){
        List<Workout> wrks = new ArrayList<>();
        for(Workout w : mWorkoutsPendingRemoval){
            if (w.getWorkoutType() == type)
                wrks.add(w);
        }
        return wrks;
    }
}
