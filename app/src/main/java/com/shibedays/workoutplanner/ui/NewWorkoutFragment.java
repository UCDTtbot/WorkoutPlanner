package com.shibedays.workoutplanner.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;

public class NewWorkoutFragment extends Fragment {
    //region CONSTANTS
    // Factory Constant
    private static final String ARG_WORKOUT = "WORKOUT";
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data

    // UI Components

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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
        getActivity().findViewById(R.id.new_workout_fragment_container).setVisibility(View.GONE);
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

}
