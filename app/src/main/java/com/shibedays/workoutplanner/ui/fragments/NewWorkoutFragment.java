package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DebugUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.adapters.ViewPagerAdapter;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewWorkoutFragment extends Fragment{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<List<Set>> mTypedSetList;

    private List<SetListFragment> mSetListFrags;

    private int mRounds;
    private int mRestTime;
    private int mBreakTime;
    private boolean mRestFlag;
    private boolean mBreakFlag;
    // Adapters
    // UI Components
    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    private EditText mNameEntry;
    private EditText mRoundEntry;
    private TextView mRestEntry;
    private TextView mBreakEntry;

    private Button mSaveButton;


    // Parent
    private MainActivity mParentActivity;
    private NewWorkoutFragment mThis;


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
        void addNewWorkout(Workout workout);
        void applyUserSetToDB(Set set);
        void removeUserSetFromDB(Set set);
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance(List<List<Set>> typedList) {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        newFragment.setTypedList(typedList);;

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
        mThis = this;
    }

    // onCreate for data
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRounds = 1;
        mRestTime = 60000;
        mBreakTime = 60000;
        mRestFlag = false;
        mBreakFlag = false;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //region UI

        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);
        mCoordLayout = mParentActivity.findViewById(R.id.main_coord_layout);

        mNameEntry = view.findViewById(R.id.name_entry);

        //region PAGER

        ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(Set.TYPES.length);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        mSetListFrags = new ArrayList<>();
        for(int i = 0; i < Set.TYPES.length; i++){
            boolean header = i == 0;
            SetListFragment frag = SetListFragment.newInstance(mTypedSetList.get(i), header, new SetListFragment.SetListListener() {

                @Override
                public void openBottomSheet(int setID) {

                    final Set set = getSetByID(setID);
                    if(set == null) throw new RuntimeException(DEBUG_TAG + " set came up null");

                    NewWorkoutFragment.this.openBottomSheet(set, new BottomSheetDialog.BottomSheetDialogListener() {

                        @Override
                        public void bottomSheetResult(int resultCode) {
                            if(resultCode == BaseApp.EDIT){
                                openDialog(AddEditSetDialog.EDIT_SET, set, new AddEditSetDialog.AddEditSetDialogListener() {
                                    @Override
                                    public void newSet(String name, String descrip, int min, int sec) {
                                        throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                                    }

                                    @Override
                                    public void editSet(int id, String name, String descrip, int min, int sec) {
                                        if(id == set.getSetId()){
                                            updateUserSet(set, name, descrip, min, sec);
                                        }
                                    }
                                });
                            } else if (resultCode == BaseApp.DELETE) {
                                deleteSetConfirmation(set);
                            } else {
                                Log.e(DEBUG_TAG, "Invalid Result Code " + resultCode);
                            }
                        }
                    });
                }

                @Override
                public void openSetDialog(int type, int setID) {

                    final Set set = getSetByID(setID);

                    if(type == AddEditSetDialog.NEW_SET){
                        openDialog(type, set, new AddEditSetDialog.AddEditSetDialogListener() {
                            @Override
                            public void newSet(String name, String descrip, int min, int sec) {
                                Set newSet = new Set(BaseApp.getNextSetID(), name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec));
                                BaseApp.incrementSetID(getContext());
                                mTypedSetList.get(Set.USER_CREATED).add(newSet);
                                mSetListFrags.get(Set.USER_CREATED).setData(mTypedSetList.get(Set.USER_CREATED));
                                mSetListFrags.get(Set.USER_CREATED).notifyData();
                                mListener.applyUserSetToDB(newSet);
                            }

                            @Override
                            public void editSet(int id, String name, String descrip, int min, int sec) {
                                throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                            }
                        });
                    } else if (type == AddEditSetDialog.EDIT_SET) {
                        openDialog(type, set, new AddEditSetDialog.AddEditSetDialogListener() {
                            @Override
                            public void newSet(String name, String descrip, int min, int sec) {
                                throw new RuntimeException("Should not have reached this point " + DEBUG_TAG);
                            }

                            @Override
                            public void editSet(int id, String name, String descrip, int min, int sec) {
                                updateUserSet(set, name, descrip, min, sec);
                            }
                        });
                    } else if (type == AddEditSetDialog.DISPLAY_SET) {
                        openDialog(type, set, null);
                    } else {
                        throw new RuntimeException(DEBUG_TAG + " invalid AddEditSetDialog type");
                    }
                }
            });
            adapter.addFragment(frag, Set.TYPES[i]);
            mSetListFrags.add(frag);
        }

        viewPager.setAdapter(adapter);

        TabLayout mTabLayout = view.findViewById(R.id.pager_header);
        mTabLayout.setupWithViewPager(viewPager);
        //endregion

        mRoundEntry = view.findViewById(R.id.round_entry_num);
        mRoundEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: Round input validation
                if(s.length() > 2){
                    String working = s.subSequence(0, 2).toString();
                    mRoundEntry.setText(working);
                    mRoundEntry.setSelection( (start + 1) <= working.length() ? start + 1 : working.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mRestEntry = view.findViewById(R.id.rest_entry_time);
        mRestTime = 15000;
        int[] rtime = BaseApp.convertFromMillis(mRestTime);
        updateRestTimeUI(rtime[0], rtime[1], false);
        mRestEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.REST_TYPE, mRestTime, mRestFlag);
            }
        });

        mBreakEntry = view.findViewById(R.id.break_entry_time);
        mBreakTime = 60000;
        int[] btime = BaseApp.convertFromMillis(mBreakTime);
        updateBreakTimeUI(btime[0], btime[1], false);
        mBreakEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.BREAK_TYPE, mBreakTime, mBreakFlag);
            }
        });

        mSaveButton = view.findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: New workout
                Log.d(DEBUG_TAG, "Adding working: " + mNameEntry.getText().toString());
                saveWorkout();
            }
        });

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
        mParentActivity.renameTitle(R.string.app_name);
        mParentActivity.showActionItems();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    //region UI_UPDATE_FUNCTIONS
    private void updateRestTimeUI(int min, int sec, boolean flag){
        if(flag){
            mRestEntry.setText(R.string.none_text);
        }else if(sec == 0){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if( sec < 10){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }
        else {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }


    }

    private void updateBreakTimeUI(int min, int sec, boolean flag){
        if(flag){
            mBreakEntry.setText(R.string.none_text);
        } else if(sec == 0){
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if( sec < 10){
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }
        else {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }
    //endregion

    //region OPEN_FUNCTIONS
    private void openBottomSheet(Set set, BottomSheetDialog.BottomSheetDialogListener listener){
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(set.getName(),
                BaseApp.getSetBtmSheetRows(), BaseApp.getSetBtmSheetNames(mParentActivity),
                BaseApp.getSetBtmSheetICs(), BaseApp.getSetBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if(getFragmentManager() != null){
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openDialog(int type, Set set, AddEditSetDialog.AddEditSetDialogListener listener){
        Bundle bundle = null;
        if( (type == AddEditSetDialog.EDIT_SET) && set != null){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        } else if(type == AddEditSetDialog.NEW_SET){
            bundle = AddEditSetDialog.getDialogBundle(type, -1, "", "", -1);
        } else if (type == AddEditSetDialog.DISPLAY_SET){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        } else {
            throw new RuntimeException(DEBUG_TAG + "dialog type was invalid" + type);
        }
        AddEditSetDialog dialog = AddEditSetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openNumberPicker(int type, int time, boolean flag){
        Bundle args = NumberPickerDialog.getDialogBundle(type, time, flag);
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(args, new NumberPickerDialog.NumberPickerDialogListener() {
            @Override
            public void setTime(int type, int min, int sec, boolean noFlag) {
                if(type == NumberPickerDialog.REST_TYPE){
                    mRestTime = BaseApp.convertToMillis(min, sec);
                    mRestFlag = noFlag;
                    updateRestTimeUI(min, sec, noFlag);
                } else if( type == NumberPickerDialog.BREAK_TYPE){
                    mBreakTime = BaseApp.convertToMillis(min, sec);
                    mBreakFlag = noFlag;
                    updateBreakTimeUI(min, sec, noFlag);
                } else {
                    throw new RuntimeException(DEBUG_TAG + " no time type given in set time return");
                }
            }
        });
        if(getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }
    //endregion

    //region DATA_FUNCTIONS
    private void updateUserSet(Set set, String name, String descrip, int min, int sec){
        if(set != null) {
            set.setName(name);
            set.setDescrip(descrip);
            set.setTime(BaseApp.convertToMillis(min, sec));
            mSetListFrags.get(Set.USER_CREATED).updateSet(set);
            mTypedSetList.get(Set.USER_CREATED).set(mTypedSetList.get(Set.USER_CREATED).indexOf(set), set);
            mListener.applyUserSetToDB(set);
        } else {
            throw new RuntimeException(DEBUG_TAG + " trying to update set, was null");
        }
    }

    private void deleteUserSet(Set set){
        mListener.removeUserSetFromDB(set);
        mSetListFrags.get(Set.USER_CREATED).removeSet(set);
        mTypedSetList.get(Set.USER_CREATED).remove(set);
    }
    //endregion

    //region UTILITY
    private void deleteSetConfirmation(final Set set){
        if(getContext() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setTitle("Delete Set")
                    .setMessage("Are you sure you want to delete " + set.getName() + " ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteUserSet(set);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private Set getSetByID(int setID){
        for(Set s : mTypedSetList.get(Set.USER_CREATED)){
            if (s.getSetId() == setID) return s;
        }

        return null;
    }
    //endregion

    //region SETTERS
    public void setTypedList(List<List<Set>> sets){
        mTypedSetList = sets;
    }
    //endregion

    public void saveWorkout(){
        boolean isOk = true;

        String name = mNameEntry.getText().toString();
        int restTime[] = BaseApp.convertFromMillis(mRestTime);
        int breakTime[] = BaseApp.convertFromMillis(mBreakTime);
        int rounds = 0;
        List<Set> selectedSets = new ArrayList<>();
        if(TextUtils.isEmpty(name)){
            mNameEntry.setError("Name can not be empty.");
            isOk = false;
        }

        if(restTime[0] == 0 && restTime[1] == 0){
            mRestFlag = true;
        }

        if(breakTime[0] == 0 && breakTime[1] == 0){
            mBreakFlag = true;
        }


        if(!TextUtils.isEmpty(mRoundEntry.getText())){
            rounds = Integer.parseInt(mRoundEntry.getText().toString());
            if(rounds <= 0){
                mRoundEntry.setError("Must be greater than 0!");
                isOk = false;
            } else if( rounds >= 100){
                mRoundEntry.setError("Must be less than 100!");
                isOk = false;
            }
        } else {
            mRoundEntry.setError("Must choose number of rounds.");
            isOk = false;
        }

        for(SetListFragment frag : mSetListFrags){
            List<Set> setList = frag.getSelectedSets();
            if(setList != null){
                selectedSets.addAll(setList);
            }
        }
        if(selectedSets.isEmpty()){
            Toast.makeText(mParentActivity, "Choose at least 1 set.", Toast.LENGTH_SHORT).show();
            isOk = false;
        }

        if(isOk) {
            Workout workout = new Workout(BaseApp.getNextWorkoutID(), Workout.USER_CREATED, name);
            workout.setNumOfRounds(rounds);
            workout.setNoRestFlag(mRestFlag);
            workout.setNoBreakFlag(mBreakFlag);
            workout.setTimeBetweenSets(BaseApp.convertToMillis(restTime));
            workout.setTimeBetweenRounds(BaseApp.convertToMillis(breakTime));
            workout.addSets(selectedSets);
            mListener.addNewWorkout(workout);
        }
    }


}
