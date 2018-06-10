package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.adapters.ViewPagerAdapter;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.dialogs.DisplaySetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.viewmodel.fragments.NewWorkoutViewModel;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;

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
    private NewWorkoutViewModel mViewModel;
    private SetViewModel mSetViewModel;

    private List<SetListFragment> mSetListFrags;
    // Adapters
    // UI Components
    private CoordinatorLayout mCoordLayout; // Displaying Toasts / Undo bar

    private EditText mNameEntry;
    private EditText mRoundEntry;
    private TextView mRestEntry;
    private TextView mBreakEntry;
    private FrameLayout mFragContainer;

    private Button mSaveButton;

    // Parent
    private MainActivity mParentActivity;
    private NewWorkoutFragment mThis;

    private CreateEditSetFragment mCreateEditFragment;

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
    public interface NewWorkoutListener {
        void addNewWorkout(Workout workout);
    }
    private NewWorkoutListener mListener;
    //endregion

    //region FACTORY_CONSTRUCTORS
    // Empty default constructor
    public NewWorkoutFragment() {
        // Required empty public constructor
    }


    public static NewWorkoutFragment newInstance(NewWorkoutListener listener) {
        NewWorkoutFragment newFragment = new NewWorkoutFragment();
        newFragment.setListener(listener);

        return newFragment;
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
        mThis = this;
    }

    // onCreate for data
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //region UI

        View view = inflater.inflate(R.layout.fragment_add_workout, container, false);
        mCoordLayout = mParentActivity.findViewById(R.id.main_coord_layout);

        mNameEntry = view.findViewById(R.id.name_entry);

        mFragContainer = view.findViewById(R.id.frag_containter);

        //region PAGER

        ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(Set.TYPES.length);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        mSetListFrags = new ArrayList<>();
        for(int type = 0; type < Set.TYPES.length; type++){
            SetListFragment frag = SetListFragment.newInstance(type, new SetListFragment.SetListListener() {

                @Override
                public void openBottomSheet(int setType, int setID) {

                    final Set set = mSetViewModel.getSetById(setID);

                    if(set == null) throw new RuntimeException(DEBUG_TAG + " set came up null");

                    NewWorkoutFragment.this.openBottomSheet(set, new BottomSheetDialog.BottomSheetDialogListener() {

                        @Override
                        public void bottomSheetResult(int resultCode) {
                            if(resultCode == BaseApp.EDIT){
                                openEditSet(set);
                            } else if (resultCode == BaseApp.DELETE) {
                                deleteSetConfirmation(set);
                            } else {
                                Log.e(DEBUG_TAG, "Invalid Result Code " + resultCode);
                            }
                        }
                    });
                }

                @Override
                public void openSetDialog(int type, int setType, int setID) {

                    final Set set = mSetViewModel.getSetById(setID);

                    if(type == SetListFragment.NEW_SET){
                        openNewSet();
                    } else if (type == SetListFragment.EDIT_SET) {
                        if(set != null)
                            openEditSet(set);
                        else
                            throw new RuntimeException(DEBUG_TAG + " set was null");
                    } else if (type == SetListFragment.DISPLAY_SET) {
                        displayDialog(set);
                    } else {
                        throw new RuntimeException(DEBUG_TAG + " invalid DisplaySetDialog type");
                    }
                }
            });
            adapter.addFragment(frag, Set.TYPES[type]);
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
        mRestEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.REST_TYPE, mViewModel.getRestTime(), mViewModel.getRestFlag());
            }
        });

        mBreakEntry = view.findViewById(R.id.break_entry_time);
        mBreakEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.BREAK_TYPE, mViewModel.getBreakTime(), mViewModel.getBreakFlag());
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
        setupViewModels();
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

    private void displayDialog(@NonNull Set set){
        Bundle bundle = DisplaySetDialog.getDialogBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId());
        DisplaySetDialog dialog = DisplaySetDialog.newInstance(bundle);
        dialog.setTargetFragment(mThis, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openNewSet(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getBundle(-1, "", "", 0, R.drawable.ic_fitness_black_24dp);
        mCreateEditFragment = CreateEditSetFragment.newInstance(R.string.new_workout, args, new CreateEditSetFragment.CreateEditSetListener() {
            @Override
            public void returnData(String name, String descrip, int min, int sec, int imageId) {
                Set set = new Set(name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec), imageId);
                mSetViewModel.insert(set);
            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mParentActivity.renameTitle(R.string.new_set);
    }

    private void openEditSet(@NonNull final Set set){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId());
        mCreateEditFragment = CreateEditSetFragment.newInstance(R.string.new_workout, args, new CreateEditSetFragment.CreateEditSetListener() {
            @Override
            public void returnData(String name, String descrip, int min, int sec, int imageId) {
                updateUserSet(set, name, descrip, min, sec, imageId);
            }
        });
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mParentActivity.renameTitle(R.string.edit_set);
    }

    private void openNumberPicker(int type, int time, boolean flag){
        Bundle args = NumberPickerDialog.getDialogBundle(type, time, flag);
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(args, new NumberPickerDialog.NumberPickerDialogListener() {
            @Override
            public void setTime(int type, int min, int sec, boolean noFlag) {
                if(type == NumberPickerDialog.REST_TYPE){
                    mViewModel.setRestTime(BaseApp.convertToMillis(min, sec));
                    mViewModel.setRestFlag(noFlag);
                    updateRestTimeUI(min, sec, noFlag);
                } else if( type == NumberPickerDialog.BREAK_TYPE){
                    mViewModel.setBreakTime(BaseApp.convertToMillis(min, sec));
                    mViewModel.setBreakFlag(noFlag);
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
    private void updateUserSet(Set set, String name, String descrip, int min, int sec, int imageId){
        if(set != null) {
            set.setName(name);
            set.setDescrip(descrip);
            set.setTime(BaseApp.convertToMillis(min, sec));
            set.setSetImageId(imageId);
            mSetViewModel.insert(set);
        } else {
            throw new RuntimeException(DEBUG_TAG + " trying to update set, was null");
        }
    }

    private void deleteUserSet(Set set){
        mSetViewModel.remove(set);
    }
    //endregion


    private void setupViewModels(){
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        mViewModel = ViewModelProviders.of(this).get(NewWorkoutViewModel.class);


        mViewModel.setRounds(1);
        mViewModel.setRestTime(60000);
        mViewModel.setBreakTime(60000);
        mViewModel.setRestFlag(false);
        mViewModel.setBreakFlag(false);
        int[] breakTime = BaseApp.convertFromMillis(60000);
        int[] restTime = BaseApp.convertFromMillis(60000);
        updateBreakTimeUI(breakTime[0], breakTime[1], false);
        updateRestTimeUI(restTime[0], restTime[1], false);
    }

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

    /*
    private Set getSetByID(int setID, int type){
        for(Set s : mTypedSetList.get(type)){
            if (s.getSetId() == setID) return s;
        }

        return null;
    }
    */

    private void setListener(NewWorkoutListener listener){
        mListener = listener;
    }

    //endregion

    public void saveWorkout(){
        boolean isOk = true;

        String name = mNameEntry.getText().toString();
        int restTime[] = BaseApp.convertFromMillis(mViewModel.getRestTime());
        int breakTime[] = BaseApp.convertFromMillis(mViewModel.getBreakTime());
        int rounds = 0;
        List<Set> selectedSets = new ArrayList<>();
        if(TextUtils.isEmpty(name)){
            mNameEntry.setError("Name can not be empty.");
            isOk = false;
        }

        if(restTime[0] == 0 && restTime[1] == 0){
            mViewModel.setRestFlag(true);
        }

        if(breakTime[0] == 0 && breakTime[1] == 0){
            mViewModel.setBreakFlag(true);
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
            Workout workout = new Workout(Workout.USER_CREATED, name);
            workout.setNumOfRounds(rounds);
            workout.setNoRestFlag(mViewModel.getRestFlag());
            workout.setNoBreakFlag(mViewModel.getBreakFlag());
            workout.setTimeBetweenSets(BaseApp.convertToMillis(restTime));
            workout.setTimeBetweenRounds(BaseApp.convertToMillis(breakTime));
            workout.addSets(selectedSets);
            mListener.addNewWorkout(workout);
        }
    }


}
