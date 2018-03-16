package com.shibedays.workoutplanner.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;
import java.util.Random;


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
    // Factory Constant
    private static final String ARG_WORKOUT = "WORKOUT";
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.TimerFragment.";
    private static final String DEBUG_TAG = TimerFragment.class.getSimpleName();
    // Fragment Instance
    private static TimerFragment mTimerFragmentInstance;
    //endregion

    //region PRIVATE_VARS
    // Data
    private Workout mWorkout;
    private List<Set> mSets;
    private Set mCurSet;
    private int mCurSetIndex;
    private int mCurRep;
    private int mCurRound;
    // UI Components
    private TextView mDescipTextView;
    private TextView mTimeTextView;
    private TextView mCurRepTextView;
    private TextView mCurRoundTextView;
    private TextView mServiceTextView;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public TimerFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to instantiate a new instance of a TimerFragment
     *
     * @param workoutJSON String
     * @return TimerFragment
     */
    // TODO: Rename and change types and number of parameters
    public static TimerFragment newInstance(String workoutJSON) {
        if(mTimerFragmentInstance == null) {
            mTimerFragmentInstance = new TimerFragment();
            Bundle args = new Bundle();
            args.putString(ARG_WORKOUT, workoutJSON);
            mTimerFragmentInstance.setArguments(args);
            return mTimerFragmentInstance;
        } else {
            return mTimerFragmentInstance;
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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson json = new Gson();
            String workoutJSON = getArguments().getString(ARG_WORKOUT);
            mWorkout = json.fromJson(workoutJSON, Workout.class);
            mSets = mWorkout.getSetList();
            mCurSetIndex = 0;
            mCurSet = mSets.get(mCurSetIndex);
            mCurRep = 0;
            mCurRound = 0;
        } else {
            Log.e(DEBUG_TAG, "get args was null");
        }
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        mTimeTextView = view.findViewById(R.id.main_timer);
        mCurRepTextView = view.findViewById(R.id.current_rep);
        mCurRoundTextView = view.findViewById(R.id.current_round);
        mDescipTextView = view.findViewById(R.id.descrip);
        mServiceTextView = view.findViewById(R.id.service_running);
        int[] time = MainActivity.convertFromMillis(mCurSet.getTime());
        int minutes = time[0], seconds = time[1];
        updateTime(minutes, seconds);
        updateRep(mCurRep);
        updateRound(mCurRound);
        updateDescription(mCurSet.getDescrip());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy(){
        getActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public void updateTime(int min, int sec){
        if((sec % 10) == 0){
            mTimeTextView.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ){
            mTimeTextView.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mTimeTextView.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    public void updateRep(int rep){
        mCurRepTextView.setText(String.format(Locale.US, "%d", (rep + 1)));
    }

    public void updateRound(int round){
        mCurRoundTextView.setText(String.format(Locale.US, "%d", (round + 1)));
    }

    public void updateDescription(String descrip){
        mDescipTextView.setText(descrip);
    }

    public void updateServiceText(String msg){
        mServiceTextView.setText(msg);
    }
    //endregion

    //region GET_DATA_FUNCTIONS
    public int getCurSetTime(){
        return mCurSet.getTime();
    }

    public int getCurRep(){
        return mCurRep;
    }

    public int getCurRound(){
        return mCurRound;
    }
    //endregion

    //region TIMER_INTERACTIONS
    public Set nextSet(){
        mCurSetIndex++;
        if(mCurSetIndex < mSets.size()){
            mCurSet = mSets.get(mCurSetIndex);
            return mCurSet;
        }  else {
            return null;
        }
    }

    public int nextRep(){
        return ++mCurRep;
    }

    public int nextRound(){
        return ++mCurRound;
    }
    //endregion
}
