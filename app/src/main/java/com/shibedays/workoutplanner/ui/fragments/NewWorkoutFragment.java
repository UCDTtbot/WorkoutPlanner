package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.adapters.sectioned.SectionedSetAdapter;
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
    public static int USER_CREATED_SECTION = 0;
    public static int DEFAULT_SECTION = 1;
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mDefaultSets;
    private List<Set> mUserCreatedSets;
    private List<Set> mUsersSets;

    private int mRounds;
    private int mRestTime;
    private int mBreakTime;
    private boolean mRestFlag;
    private boolean mBreakFlag;
    // Adapters
    private SectionedSetAdapter mLeftAdapter;
    private SectionedSetAdapter mRightAdapter;
    // UI Components
    private RecyclerView mLeftRecyclerView;
    private RecyclerView mRightRecyclerView;

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
    }
    private OnFragmentInteractionListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance(List<Set> userCreated, List<Set> defaultSets) {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        newFragment.setUserCreatedSets(userCreated);
        newFragment.setDefaultSets(defaultSets);

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
        mUsersSets = new ArrayList<>();

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

            //region RECYCLER_VIEWS

                //region LEFT_RV
        mLeftRecyclerView = view.findViewById(R.id.left_recyclerview);
        mLeftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> leftHeaderList = new ArrayList<>();
        List<List<Set>> leftDataList = new ArrayList<>();
        leftHeaderList.add(getString(R.string.header_title_user_created));
        leftDataList.add(mUserCreatedSets);
        leftHeaderList.add(getString(R.string.header_title_default));
        leftDataList.add(mDefaultSets);
        mLeftAdapter = new SectionedSetAdapter(getContext(), leftHeaderList, 0, new SectionedSetAdapter.SectionedAddSetListener() {
            @Override
            public void onClick(Set set, int section, int relativePos) {
                mRightAdapter.addSet(0, set); // Section is the recipient section, not sending section
                Log.d(DEBUG_TAG, "Left Adapter Add Set to 'My Sets'");
            }

            @Override
            public void onLongClick(Set set, int section, final int relativePos) {
                if(section == DEFAULT_SECTION){
                    Log.d(DEBUG_TAG, "Left Adapter Default Long Click: Display Info");
                    openDialog(AddEditSetDialog.DISPLAY_SET, set, relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                        @Override
                        public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                            // Nothing
                        }
                    });
                } else {
                    Log.d(DEBUG_TAG, "Left Adapter User Created Long Click: Bottom Sheet");
                    openBottomSheet(set, relativePos, section, new BottomSheetDialog.BottomSheetDialogListener() {
                        @Override
                        public void bottomSheetResult(int resultCode, int index, int section) {
                            Log.d(DEBUG_TAG, "Result Code: " + Integer.toString(resultCode) + " Section: " + section);
                            switch (resultCode){
                                case BaseApp.EDIT:
                                    openDialog(AddEditSetDialog.EDIT_SET, mUserCreatedSets.get(index), relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                                        @Override
                                        public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                                            updateUserCreatedSet(index, name, descrip, min, sec);
                                        }
                                    });
                                    break;
                                case BaseApp.DELETE:
                                    deleteUserCreatedSet(section, index);
                                    break;
                                case BaseApp.DUPLCIATE:
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void createUserSet(int section) {
                openDialog(AddEditSetDialog.NEW_SET, null, -1, section, new AddEditSetDialog.AddEditSetDialogListener() {
                    @Override
                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                        Set set = new Set(name, descrip, BaseApp.convertToMillis(min, sec));
                        addUserCreatedSet(section, set);
                        mParentActivity.addSetToDB(set);
                    }
                });
                Log.d(DEBUG_TAG, "Left Adapter Add New User Created Set");
            }
        });

        mLeftRecyclerView.setAdapter(mLeftAdapter);
        mLeftAdapter.setDataList(leftDataList);
        mLeftAdapter.shouldShowHeadersForEmptySections(true);
        mLeftAdapter.shouldShowFooters(true);

                //endregion

                //region RIGHT_RV
        mRightRecyclerView = view.findViewById(R.id.right_recyclerview);
        mRightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> rightHeaderList = new ArrayList<>();
        rightHeaderList.add(getString(R.string.header_title_my_sets));
        mRightAdapter = new SectionedSetAdapter(getContext(), rightHeaderList, -1, new SectionedSetAdapter.SectionedAddSetListener() {
            @Override
            public void onClick(Set set, int section, final int relativePos) {
                openBottomSheet(set, relativePos, section, new BottomSheetDialog.BottomSheetDialogListener() {
                    @Override
                    public void bottomSheetResult(int resultCode, int index, int section) {
                        Log.d(DEBUG_TAG, "Result Code: " + Integer.toString(resultCode) + " Section: " + section);
                        switch (resultCode){
                            case BaseApp.EDIT:
                                openDialog(AddEditSetDialog.EDIT_SET, mUsersSets.get(index), relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                                    @Override
                                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                                        updateUserSet(index, name, descrip, min, sec);
                                    }
                                });
                                break;
                            case BaseApp.DELETE:
                                deleteUserSet(section, index);
                                break;
                            case BaseApp.DUPLCIATE:
                                break;
                            default:
                                break;
                        }
                    }
                });
                Log.d(DEBUG_TAG, "Right Adapter Clicked: Bottom Sheet");
            }

            @Override
            public void onLongClick(Set set, int section, final int relativePos) {
                openBottomSheet(set, relativePos, section, new BottomSheetDialog.BottomSheetDialogListener() {
                    @Override
                    public void bottomSheetResult(int resultCode, int index, int section) {
                        Log.d(DEBUG_TAG, "Result Code: " + Integer.toString(resultCode) + " Section: " + section);
                        switch (resultCode){
                            case BaseApp.EDIT:
                                openDialog(AddEditSetDialog.EDIT_SET, mUsersSets.get(index), relativePos, section, new AddEditSetDialog.AddEditSetDialogListener() {
                                    @Override
                                    public void dialogResult(int dialogType, String name, String descrip, int min, int sec, int section, int index) {
                                        updateUserSet(index, name, descrip, min, sec);
                                    }
                                });
                                break;
                            case BaseApp.DELETE:
                                deleteUserSet(section, index);
                                break;
                            case BaseApp.DUPLCIATE:
                                break;
                            default:
                                break;
                        }
                    }
                });
                Log.d(DEBUG_TAG, "Right Adapter Long Clicked: Bottom Sheet");
            }

            @Override
            public void createUserSet(int section) {
                throw new RuntimeException(DEBUG_TAG + " createUserSet called within mRightAdapter. Footer shouldn't exist");
            }
        });

        mRightRecyclerView.setAdapter(mRightAdapter);
        mRightAdapter.addToDataList(mUsersSets);
        mRightAdapter.shouldShowHeadersForEmptySections(true);
        mRightAdapter.shouldShowFooters(false);


                //endregion

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
        mRestEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.REST_TYPE, mRestTime, mRestFlag);
            }
        });

        mBreakEntry = view.findViewById(R.id.break_entry_time);
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
    public void setRestTime(int min, int sec, boolean flag) {
        if(flag){
            mRestEntry.setText(R.string.none_text);
        }else if( sec == 0 ){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if ( (sec % 10) == 0 ) {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ) {
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }

        mRestTime = BaseApp.convertToMillis(min, sec);
    }

    public void setBreakTime(int min, int sec, boolean flag) {
        if(flag){
            mBreakEntry.setText(R.string.none_text);
        }else if( sec == 0 ){
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if ( (sec % 10) == 0 ) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }

        mBreakTime = BaseApp.convertToMillis(min, sec);
    }


    //endregion

    //region UTILITY
    private void openBottomSheet(Set set, int relativePos, int section, BottomSheetDialog.BottomSheetDialogListener listener){
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(set.getName(), relativePos, section,
                BaseApp.getSetBtmSheetRows(), BaseApp.getSetBtmSheetNames(mParentActivity), BaseApp.getSetBtmSheetICs(), BaseApp.getSetBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, listener);
        dialog.setTargetFragment(mThis, 0);
        if(getFragmentManager() != null){
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openDialog(int type, Set set, int relativePos, int section, AddEditSetDialog.AddEditSetDialogListener listener){
        Bundle bundle = null;
        if(set != null){
            bundle = AddEditSetDialog.getDialogBundle(type, set.getName(), set.getDescrip(), set.getTime(), relativePos, section);
        } else {
            bundle = AddEditSetDialog.getDialogBundle(type, "", "", -1, relativePos, section);
        }
        AddEditSetDialog dialog = AddEditSetDialog.newInstance(listener, bundle);
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

    private void updateRestTimeUI(int min, int sec, boolean flag){
        if(flag){
            mRestEntry.setText(R.string.none_text);
        }else if((sec % 10) == 0){
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if (min == 0 && sec == 0){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if ( sec < 10 ){
            mRestEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }  else {
            mRestEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateBreakTimeUI(int min, int sec, boolean flag){
        if(flag){
            mBreakEntry.setText(R.string.none_text);
        } else if((sec % 10) == 0) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }else if (min == 0 && sec == 0) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else if ( sec < 10 ) {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mBreakEntry.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    public void saveWorkout(){
        boolean isOk = true;

        String name = mNameEntry.getText().toString();
        int restTime[] = {0, 0};
        int breakTime[] = {0, 0};
        int rounds = 0;
        List<Set> setList = mUsersSets;
        if(TextUtils.isEmpty(name)){
            mNameEntry.setError("Name can not be empty.");
            isOk = false;
        }

        if(restTime[0] == 0 && restTime[1] == 0){
            restTime = BaseApp.convertFromMillis(0);
            mRestFlag = true;
        } else {
            restTime = BaseApp.convertFromMillis(mRestTime);
        }

        if(breakTime[0] == 0 && breakTime[1] == 0){
            breakTime = BaseApp.convertFromMillis(0);
            mBreakFlag = true;
        } else {
            breakTime = BaseApp.convertFromMillis(mBreakTime);
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



        if(setList.isEmpty()){
            Toast.makeText(mParentActivity, "Choose at least 1 set.", Toast.LENGTH_SHORT).show();
            isOk = false;
        }

        if(isOk) {
            Workout workout = new Workout(mParentActivity.getNextWorkoutId(), name);
            workout.setNumOfRounds(rounds);
            workout.setNoRestFlag(mRestFlag);
            workout.setNoBreakFlag(mBreakFlag);
            workout.setTimeBetweenSets(BaseApp.convertToMillis(restTime));
            workout.setTimeBetweenRounds(BaseApp.convertToMillis(breakTime));
            workout.addSets(setList);
            mListener.addNewWorkout(workout);
        }
    }
    //endregion

    //region SETTERS
    public void setDefaultSets(List<Set> sets){
        mDefaultSets = sets;
    }


    public void setUserCreatedSets(List<Set> sets){
        mUserCreatedSets = sets;
    }
    public void addUserCreatedSet(int section, Set set){
        mLeftAdapter.addSet(section, set);
    }
    public void updateUserCreatedSet(int index, String name, String descrip, int min, int sec){
        Set set = mUserCreatedSets.get(index);
        set.setName(name);
        set.setDescrip(descrip);
        set.setTime(BaseApp.convertToMillis(min, sec));
        mLeftAdapter.updateSetList(USER_CREATED_SECTION, mUserCreatedSets);
    }
    public void deleteUserCreatedSet(int section, int pos){
        mParentActivity.deleteSetFromDB(mUserCreatedSets.get(pos));
        mLeftAdapter.removeSet(section, pos);
    }


    public void updateUserSet(int index, String name, String descrip, int min, int sec){
        Set set = mUsersSets.get(index);
        set.setName(name);
        set.setDescrip(descrip);
        set.setTime(BaseApp.convertToMillis(min, sec));
        mRightAdapter.updateSetList(0, mUsersSets);
    }
    public void deleteUserSet(int section, int pos){
        mRightAdapter.removeSet(section, pos);
    }
    //endregion



}
