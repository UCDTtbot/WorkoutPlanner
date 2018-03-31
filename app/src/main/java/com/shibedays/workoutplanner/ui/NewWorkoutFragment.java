package com.shibedays.workoutplanner.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shibedays.workoutplanner.ListItemTouchHelper;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;

public class NewWorkoutFragment extends Fragment {
    //region CONSTANTS
    // Factory Constant
    private static final String ARG_WORKOUT = "WORKOUT";
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Workout
    private Workout mNewWorkout;
    // Data
    private List<Set> mDefaultSets;
    private List<Set> mUserSets;
    // Adapters
    private SetAdapter mLeftAdapter;
    private SetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;
    private CoordinatorLayout mCoordLayout;
    private Button mSaveButton;
    // Parent
    private MainActivity mParentActivity;
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region INTERFACES
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void addNewWorkout();
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance() {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        Bundle args = new Bundle();
        newFragment.setArguments(args);

        return newFragment;
    }
    //endregion

    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        mDefaultSets = new ArrayList<>();
        mUserSets = new ArrayList<>();
        mDefaultSets.add(new Set("Set 1", "Descrip 1", 10000));
        mDefaultSets.add(new Set("Set 2", "Descrip 1", 10000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));
        mUserSets.add(new Set("My Set", "Descrip", 50000));

        mNewWorkout = new Workout(mParentActivity.getNextWorkoutId(), "No Name");

        mNewWorkout.addSets(mUserSets);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //region UI
        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);
        mSaveButton = view.findViewById(R.id.button_save);
        mCoordLayout = mParentActivity.findViewById(R.id.main_coord_layout);
        //endregion

        //region RECYCLER_VIEWS
            //region LEFT_RV
        mLeftRecyclerView = view.findViewById(R.id.left_recyclerview);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLeftRecyclerView.setItemAnimator(new FadeInLeftAnimator());

        mLeftAdapter = new SetAdapter(getContext(), mCoordLayout, false);
        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.setData(mDefaultSets);
        mLeftAdapter.notifyDataSetChanged();

        int leftDragDirs = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
        int swipeDirs = 0;
        ListItemTouchHelper leftHelper = new ListItemTouchHelper(getContext(), true, leftDragDirs, false, swipeDirs, mLeftAdapter);
        leftHelper.getHelper().attachToRecyclerView(mLeftRecyclerView);
            //endregion

            //region RIGHT_RV
        mRightRecyclerView = view.findViewById(R.id.right_recyclerview);
        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRightRecyclerView.setItemAnimator(new FadeInLeftAnimator());

        mRightAdapter = new SetAdapter(getContext(), mCoordLayout, true);
        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.setData(mUserSets);
        mRightAdapter.notifyDataSetChanged();

        int rightDragDirs = 0;
        ListItemTouchHelper rightHelper = new ListItemTouchHelper(getContext(), false, rightDragDirs, false, swipeDirs, mRightAdapter);
        rightHelper.getHelper().attachToRecyclerView(mRightRecyclerView);
            //endregion
        //endregion
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_START");
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
        mParentActivity.findViewById(R.id.new_workout_fragment_container).setVisibility(View.GONE);
        mParentActivity.findViewById(R.id.fab).setVisibility(View.VISIBLE);
        mParentActivity.toggleUpArrow(false);
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_DESTROY");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT SAVING INSTANCE STATE");
    }

    //endregion

    //region TOOLBAR_MENU

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //endregion

    public void deleteSet(Set set){
        mNewWorkout.removeSet(set);
    }

    public void swapSets(int from, int to){
        mNewWorkout.swapSets(from, to);

    }

}
