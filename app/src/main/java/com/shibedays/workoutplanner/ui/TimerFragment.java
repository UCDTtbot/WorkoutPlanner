package com.shibedays.workoutplanner.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;
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
    private static final String ARG_WORKOUT = "WORKOUT";

    private Workout mWorkout;
    private List<Set> mSetList;
    private Set mCurSet;

    private int mCurSetTime;
    private int mRestTime;
    private int mBreakTime;
    private int mNumRounds;
    private int mNumReps;

    private TextView mDescipTextView;
    private TextView mTimeTextView;
    private TextView mCurRepTextView;
    private TextView mCurRoundTextView;

    public int mTimeLeft;
    public int mCurRep;
    public int mCurRound;

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
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORKOUT, workoutJSON);
        fragment.setArguments(args);
        return fragment;
    }

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
            mSetList = mWorkout.getSetList();
            mCurSet = mSetList.get(0);

            mCurSetTime = mCurSet.getTime();
            mRestTime = mWorkout.getTimeBetweenSets();
            mBreakTime = mWorkout.getTimeBetweenRounds();
            mNumReps = mWorkout.getNumOfSets();
            mNumRounds = mWorkout.getNumOfRounds();

            mCurRep = 0;
            mCurRound = 0;
            mTimeLeft = mCurSet.getTime();
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
        int[] time = MainActivity.convertFromMillis(mCurSetTime);
        int minutes = time[0], seconds = time[1];
        if((seconds % 10) == 0){
            mTimeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
        } else if ( seconds < 10 ){
            mTimeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
        } else {
            mTimeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
        }
        mCurRepTextView.setText(Integer.toString(mCurRep));
        mCurRoundTextView.setText(Integer.toString(mCurRound));
        mDescipTextView.setText(mCurSet.getDescrip());
        return view;
    }

    // TODO: Timer implementation, lifecycle, ui

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


    //region MENU
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

}
