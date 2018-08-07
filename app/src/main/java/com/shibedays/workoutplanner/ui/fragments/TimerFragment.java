package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.services.TimerService;
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
    private TextView mSetNameView;
    private TextView mNextSetNameView;
    private ImageView mSetImageView;
    private TextView mSetDescripView;

    private TabLayout mTabs;

    private AdView mAdView;
    private Handler mAdHandler;

    private View.OnClickListener mOnPause;
    private View.OnClickListener mOnCont;

    private ProgressBar mProgressBar;
    private ImageView mPauseButtonView;
    private ImageView mContinueButtonView;
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
            mParentActivity = act;
            ((MyWorkoutActivity) mParentActivity).hideActionItems();
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
            mTimerViewModel.setCurTime(mTimerViewModel.getCurSetTime());
        } else {
            throw new RuntimeException(TimerFragment.class.getSimpleName() + " getArguments returned null. Fragment started incorrectly");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        mTabs = view.findViewById(R.id.tab_header);
        for(Set s : mTimerViewModel.getSets()) {
            mTabs.addTab(mTabs.newTab());
        }

        mSetNameView = view.findViewById(R.id.cur_set);
        mNextSetNameView = view.findViewById(R.id.next_set);
        mSetImageView = view.findViewById(R.id.set_image);
        mSetDescripView = view.findViewById(R.id.set_descrip);
        mSetDescripView.setMovementMethod(new ScrollingMovementMethod());

        mProgressBar = view.findViewById(R.id.timer_progress);
        mProgressBar.setMax(1000);

        mOnPause = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
            }
        };
        mOnCont = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueTimer();
            }
        };

        mPauseButtonView = view.findViewById(R.id.pause_button);
        mPauseButtonView.setOnClickListener(mOnPause);
        mContinueButtonView = view.findViewById(R.id.continue_button);
        mContinueButtonView.setOnClickListener(null);

        mTimeView = view.findViewById(R.id.main_time);
        mRepsView = view.findViewById(R.id.reps);
        mRoundsView = view.findViewById(R.id.rounds);

        mAdView = view.findViewById(R.id.timer_ad_view);
        setupAd();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateSetInfo(mTimerViewModel.getCurSet(), mTimerViewModel.getNextSet(), mTimerViewModel.isRest(), mTimerViewModel.isBreak());
        updateRep(mTimerViewModel.getCurRep());
        updateRound(mTimerViewModel.getCurRound());
        updateTime(mTimerViewModel.getCurTime(), mTimerViewModel.getCurSetTime());
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_START");
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mAdView != null) {
            mAdView.resume();
            AdRequest adr = new AdRequest.Builder()
                    .addTestDevice("777CB5CEE1249294D3D44B76236723E4")
                    .build();
            mAdView.loadAd(adr);
        }
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_RESUME");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAdView != null) {
            mAdView.pause();
            if(mAdHandler != null)
                mAdHandler.removeCallbacks(null);
        }
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
        mInstance = null;
        if(mAdView != null){
            mAdView.destroy();
            if(mAdHandler != null)
                mAdHandler.removeCallbacks(null);
        }

        Log.d(DEBUG_TAG, "TIMER_FRAGMENT ON_DESTROY");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        ((MyWorkoutActivity) mParentActivity).showActionItems();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "TIMER_FRAGMENT SAVING INSTANCE STATE");

    }

    //endregion

    //region TOOLBAR_MENU
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_set).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.rename_workout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region UI_UPDATE_FUNCTIONS
    public void updateSetInfo(Set cur, Set next, boolean isRest, boolean isBreak) {

        mTimerViewModel.toggleRest(isRest);
        mTimerViewModel.toggleBreak(isBreak);

        if (mTimerViewModel.getCurRep() == mTimerViewModel.getTotalReps() - 1 &&
                mTimerViewModel.getCurRound() == mTimerViewModel.getTotalRounds() - 1) {
            mNextSetNameView.setText(R.string.finished_name);
        } else if (isRest) {
            mSetNameView.setText(R.string.rest_name);
            mSetDescripView.setText(R.string.rest_descrip_display);
            mNextSetNameView.setText(cur.getName());
        } else if (isBreak) {
            mSetNameView.setText(R.string.break_name);
            mSetDescripView.setText(R.string.break_descrip_display);
            mNextSetNameView.setText(cur.getName());
        } else {
            mSetNameView.setText(cur.getName());
            mSetDescripView.setText(cur.getDescrip());
            mNextSetNameView.setText(next.getName());
        }


        mTabs.getTabAt(mTimerViewModel.getCurSetIndex()).select();
    }

    public void updateTime(int time, int totalTime){
        int[] splitTime = BaseApp.convertFromMillis(time);
        mTimerViewModel.setCurTime(time);
        int min = splitTime[0], sec = splitTime[1];

        mTimeView.setText(BaseApp.formatTime(min, sec));

        float floatTime = ((float)time / (float)totalTime) * 1000;
        int progress = 1000 - (int)floatTime;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mProgressBar.setProgress(progress, true);
        } else {
            mProgressBar.setProgress(progress);
        }
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

    public void pauseTimer(){
        if(mParentActivity instanceof MyWorkoutActivity){
            Message msg = Message.obtain(null, TimerService.MSG_PAUSE_TIMER, 0);
            ((MyWorkoutActivity) mParentActivity).sendTimerMessage(msg);
            mPauseButtonView.setVisibility(View.INVISIBLE);
            mPauseButtonView.setOnClickListener(null);
            mContinueButtonView.setVisibility(View.VISIBLE);
            mContinueButtonView.setOnClickListener(mOnCont);
        }
    }

    public void continueTimer(){
        if(mParentActivity instanceof MyWorkoutActivity){
            Message msg = Message.obtain(null, TimerService.MSG_CONTINUE_TIMER, 0);
            ((MyWorkoutActivity) mParentActivity).sendTimerMessage(msg);
            mContinueButtonView.setVisibility(View.INVISIBLE);
            mContinueButtonView.setOnClickListener(null);
            mPauseButtonView.setVisibility(View.VISIBLE);
            mPauseButtonView.setOnClickListener(mOnPause);
        }
    }

    public Set loadNextSet(){
        return mTimerViewModel.loadInNextSet();
    }
    //endregion

    public static Bundle getBundle(int id){
        Bundle args = new Bundle();
        args.putInt(EXTRA_WORKOUT_ID, id);
        return args;
    }

    private void setupAd(){
        boolean adsDisabled = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(prefs != null){
            adsDisabled = prefs.getBoolean("disable_ads", false);
        }
        if(!adsDisabled){
            MobileAds.initialize(getActivity(), "ca-app-pub-1633767409472368~4737915463");

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if(mAdHandler == null){
                        mAdHandler = new Handler();
                    }
                    mAdHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AdRequest adr = new AdRequest.Builder()
                                    .addTestDevice("777CB5CEE1249294D3D44B76236723E4")
                                    .build();
                            mAdView.loadAd(adr);
                        }
                    }, 30000);
                    super.onAdLoaded();
                }
            });
        } else {
            mAdView.setEnabled(false);
            mAdView.setVisibility(View.GONE);
        }
    }
}
