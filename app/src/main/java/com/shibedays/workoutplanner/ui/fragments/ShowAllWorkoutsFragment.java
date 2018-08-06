package com.shibedays.workoutplanner.ui.fragments;


import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.adapters.WorkoutItemHelper;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ShowAllWorkoutsFragment extends Fragment {


    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.ShowAllWorkoutsFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    public static final String EXTRA_WORKOUT_TYPE = PACKAGE + "WorkoutType";

    private static WeakReference<ShowAllWorkoutsFragment> mInstance;

    private RecyclerView mWorkoutsView;
    private WorkoutViewModel mWorkoutVM;
    private int mType;

    private MainActivity mParentActivity;

    private RecyclerView mRecyclerView;
    private WorkoutItemHelper mAdapter;
    private List<Workout> mWorkouts;


    public interface ShowAllListener {
        void workoutClicked(int id, int type);
        void workoutLongClicked(int id, int type);
    }
    private ShowAllListener mListener;



    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public ShowAllWorkoutsFragment() {
        // Required empty public constructor
    }


    public static ShowAllWorkoutsFragment newInstance(Bundle args,  ShowAllListener listener) {
        if(mInstance == null){
            mInstance = new WeakReference<>(new ShowAllWorkoutsFragment());
            mInstance.get().setListener(listener);
            mInstance.get().setArguments(args);
            return mInstance.get();
        } else {
            return mInstance.get();
        }
    }
    //endregion


    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity act = getActivity();
        if(act instanceof MainActivity){
            mParentActivity = (MainActivity) act;
        } else {
            throw new RuntimeException(DEBUG_TAG + " wasn't called from MainActivity");
        }
    }

    // onCreate for data
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            mType = args.getInt(EXTRA_WORKOUT_TYPE);
        } else {
            mType = 0;
        }

        mWorkoutVM = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mWorkoutVM.getAllTypedWorkouts(mType).observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                if(mAdapter != null){
                    List<Workout> pending = mParentActivity.getPendingHelper().getPendingWorkouts(mType);
                    mWorkouts = workouts;
                    if(mWorkouts != null) {
                        for (Workout w : pending) {
                            if (mWorkouts.contains(w)) {
                                mWorkouts.remove(w);
                            }
                        }
                    }
                    mAdapter.updateData(mWorkouts);
                }
            }
        });
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_all_workouts, container, false);

        mRecyclerView = view.findViewById(R.id.all_recyclerview);
        mRecyclerView.setVerticalScrollBarEnabled(false);

        // Setup the adapter with correct data
        mAdapter = new WorkoutItemHelper(getActivity(), new ArrayList<Workout>(), mType, new WorkoutItemHelper.WorkoutAdapterListener() {
            @Override
            public void onWorkoutClicked(int id, int type) {
                mListener.workoutClicked(id, type);
            }

            @Override
            public void onWorkoutLongClick(int workoutID, int type) {
                mListener.workoutLongClicked(workoutID, type);
            }
        });
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "SHOW_ALL_WORKOUTS ON_START");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_RESUME");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_PAUSE");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_STOP");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentManager mng = mParentActivity.getSupportFragmentManager();
        if(mng.getBackStackEntryCount() <= 0){
            mParentActivity.toggleUpArrow(false);
        }
        mParentActivity.renameTitle(R.string.app_name);
        mParentActivity.showActionItems();
        mParentActivity.findViewById(R.id.show_all_workouts_frag_container).setVisibility(View.GONE);
        mParentActivity.closeShowAllFragment();
        mInstance = null;
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_DESTROY");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT SAVING INSTANCE STATE");
    }

    //endregion


    private void setListener(ShowAllListener listener){
        mListener = listener;
    }

    public static Bundle getBundle(int type){
        Bundle args = new Bundle();
        args.putInt(EXTRA_WORKOUT_TYPE, type);
        return args;
    }


    public void removeWorkout(Workout w){
        if(mAdapter != null){
            mAdapter.removeWorkout(w);
        }

    }

    public void addWorkout(Workout w){
        if(mAdapter != null){
            mAdapter.addWorkout(w);
        }

    }

}
