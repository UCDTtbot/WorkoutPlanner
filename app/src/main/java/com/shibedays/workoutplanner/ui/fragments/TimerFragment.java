package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;
import com.shibedays.workoutplanner.viewmodel.fragments.TimerViewModel;

import java.lang.ref.WeakReference;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerFragment extends Fragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.TimerFragment.";
    private static final String DEBUG_TAG = TimerFragment.class.getSimpleName();

    private static final String EXTRA_WORKOUT_ID = PACKAGE + "ID";

    private static WeakReference<TimerFragment> mInstance;

    //region PRIVATE_VARS
    // Data
    private TimerViewModel mTimerViewModel;
    private WorkoutViewModel mWorkoutViewModel;
    // UI Components
    private TextView mSetTitleView;
    private ImageView mSetImageView;
    private TextView mSetPreTitleView;
    private TextView mSetDescripView;

    private ProgressBar mProgressBar;
    private ImageView mPlayButtonView;
    private TextView mTimeView;
    private TextView mRepsView;
    private TextView mRoundsView;
    // Activity
    private Activity mParentActivity;
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
        void closeFragmentAndService();
        void stopTTSSpeech();
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public TimerFragment() {
        // Required empty public constructor
    }

    public static TimerFragment newInstance(Bundle args) {
        if(mInstance == null) {
            mInstance = new WeakReference<>(new TimerFragment());
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement NewWorkoutListener");
        }
        Activity act = getActivity();
        if(act instanceof MyWorkoutActivity){
            mParentActivity = (MyWorkoutActivity) act;
        } else {
            throw new RuntimeException(DEBUG_TAG + " wasn't created from MyWorkoutActivity or SingleFragmentTester");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mTimerViewModel = ViewModelProviders.of(this).get(TimerViewModel.class);
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);

        if (args != null) {
            Workout w = mWorkoutViewModel.getWorkoutByID(args.getInt(EXTRA_WORKOUT_ID));
            mTimerViewModel.setWorkout(w);
            mTimerViewModel.setSets(w.getSetList());
            mTimerViewModel.setCurSet(w.getSetList().get(0));
            mTimerViewModel.setCurRep(0);
            mTimerViewModel.setCurRound(0);
        } else {
            throw new RuntimeException(TimerFragment.class.getSimpleName() + " getArguments returned null. Fragment started incorrectly");
        }
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        mSetTitleView = view.findViewById(R.id.set_title);
        mSetImageView = view.findViewById(R.id.set_image);
        mSetDescripView = view.findViewById(R.id.set_descrip);
        mSetPreTitleView = view.findViewById(R.id.set_pre);

        mProgressBar = view.findViewById(R.id.timer_progress);
        mProgressBar.setMax(1000);
        mPlayButtonView = view.findViewById(R.id.play_button);
        mTimeView = view.findViewById(R.id.main_time);
        mRepsView = view.findViewById(R.id.reps);
        mRoundsView = view.findViewById(R.id.rounds);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateSetInfo(mTimerViewModel.getCurSet());
        updateTime(mTimerViewModel.getCurSetTime(), mTimerViewModel.getCurSetTime());
        updateRep(0);
        updateRound(0);
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_START");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_RESUME");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_PAUSE");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_STOP");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mParentActivity.findViewById(R.id.fragment_container).setVisibility(View.GONE);
        mListener.closeFragmentAndService();
        mListener.stopTTSSpeech();
        mInstance = null;
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_DESTROY");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT SAVING INSTANCE STATE");

    }

    //endregion

    //region TOOLBAR_MENU
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_set).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region UI_UPDATE_FUNCTIONS
    public void updateSetInfo(Set set){
        mSetPreTitleView.setText("Current:");
        mSetTitleView.setText(set.getName());
        mSetImageView.setImageResource(set.getSetImageId());
        mSetDescripView.setText(set.getDescrip());
    }

    public void showNextSetInfo(Set set){
        mSetPreTitleView.setText("Next:");
        mSetTitleView.setText(set.getName());
        mSetImageView.setImageResource(set.getSetImageId());
        mSetDescripView.setText(set.getDescrip());
    }

    public void updateTime(int time, int totalTime){
        int[] splitTime = BaseApp.convertFromMillis(time);
        int min = splitTime[0], sec = splitTime[1];

        mTimeView.setText(BaseApp.formatTime(min, sec));

        float floatTime = ((float)time / (float)totalTime) * 1000;
        int progress = 1000 - (int)floatTime;
        // TODO: If API < 24, do a different thing
        mProgressBar.setProgress(progress, true);
    }

    public void updateRep(int rep){
        mTimerViewModel.setCurRep(rep);
        mRepsView.setText(String.format(Locale.US, "%d / %d", (mTimerViewModel.getCurRep() + 1), mTimerViewModel.getTotalReps()));
    }

    public void updateRound(int round){
        mTimerViewModel.setCurRound(round);
        mRoundsView.setText(String.format(Locale.US, "%d / %d", (mTimerViewModel.getCurRound() + 1), mTimerViewModel.getTotalRounds()));
    }
    //endregion

    //region GET_DATA_FUNCTIONS
    public int getCurSetTime(){
        return mTimerViewModel.getCurSetTime();
    }
    //endregion

    //region TIMER_INTERACTIONS
    public Set getCurSet(){ return mTimerViewModel.getCurSet(); }
    public Set getNextSet() { return mTimerViewModel.getNextSet(); }

    public Set loadNextSet(){
        return mTimerViewModel.loadInNextSet();
    }
    //endregion

    public static Bundle getBundle(int id){
        Bundle args = new Bundle();
        args.putInt(EXTRA_WORKOUT_ID, id);
        return args;
    }
}
