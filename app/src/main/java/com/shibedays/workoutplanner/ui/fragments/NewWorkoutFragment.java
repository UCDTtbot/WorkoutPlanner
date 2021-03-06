package com.shibedays.workoutplanner.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewWorkoutFragment extends Fragment{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment.";
    private static final String DEBUG_TAG = NewWorkoutFragment.class.getSimpleName();
    //endregion

    private static WeakReference<NewWorkoutFragment> mInstance;

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
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;
    private CheckBox mEquipRequired;

    private Button mSaveButton;

    // Parent
    private MainActivity mParentActivity;

    private CreateEditSetFragment mCreateEditFragment;

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
        if(mInstance == null){
            mInstance = new WeakReference<>(new NewWorkoutFragment());
            mInstance.get().setListener(listener);

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

        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        mSetViewModel.getAllSets().observe(this, new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {

            }
        });
        mViewModel = ViewModelProviders.of(this).get(NewWorkoutViewModel.class);

        mViewModel.setRounds(1);
        mViewModel.setRestTime(60000);
        mViewModel.setBreakTime(60000);
        mViewModel.setRestFlag(false);
        mViewModel.setBreakFlag(false);
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

        mViewPager = view.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(Set.TYPES.length);
        mTabLayout = view.findViewById(R.id.pager_header);
        //endregion

        mRoundEntry = view.findViewById(R.id.round_entry_num);
        mRoundEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    Activity act = getActivity();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null)
                        imm.hideSoftInputFromWindow(mRoundEntry.getWindowToken(), 0);
                    mRoundEntry.clearFocus();
                }
                return false;
            }
        });
        mRoundEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
        mRestEntry.setFocusable(false);
        mRestEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPicker(NumberPickerDialog.REST_TYPE, mViewModel.getRestTime(), mViewModel.getRestFlag());
            }
        });

        mBreakEntry = view.findViewById(R.id.break_entry_time);
        mBreakEntry.setFocusable(false);
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
                Log.d(DEBUG_TAG, "Adding working: " + mNameEntry.getText().toString());
                saveWorkout();
            }
        });

        mEquipRequired = view.findViewById(R.id.equip_checkbox);
        setBottomMargin(mSaveButton, 0);
        //endregion
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "NEW_WORKOUT_FRAGMENT ON_START");
        setupViewPager();
        int[] breakTime = BaseApp.convertFromMillis(60000);
        int[] restTime = BaseApp.convertFromMillis(60000);
        updateBreakTimeUI(breakTime[0], breakTime[1], false);
        updateRestTimeUI(restTime[0], restTime[1], false);
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
        mParentActivity.findViewById(R.id.new_workout_fragment_container).setVisibility(View.GONE);
        if(!BaseApp.areAdsDisabled())
            mParentActivity.findViewById(R.id.main_ad_view).setVisibility(View.VISIBLE);
        mParentActivity.showActionItems();
        mParentActivity.renameTitle(mParentActivity.getLastTitle());
        mInstance = null;
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
        dialog.setTargetFragment(mInstance.get(), 0);
        if(getFragmentManager() != null){
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void displayDialog(@NonNull Set set){
        Bundle bundle = DisplaySetDialog.getDialogBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId(), set.getURL());
        DisplaySetDialog dialog = DisplaySetDialog.newInstance(bundle);
        dialog.setTargetFragment(mInstance.get(), 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), DEBUG_TAG);
        }
    }

    private void openNewSet(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getIdBundle(-1, -1,  "", "", 0, R.drawable.ic_fitness_black_24dp, -1, "");
        mCreateEditFragment = CreateEditSetFragment.newInstance(getActivity().getTitle().toString(), CreateEditSetFragment.TYPE_NEW_SET, args);
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        mParentActivity.renameTitle(R.string.new_set);
    }

    private void openEditSet(@NonNull final Set set){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getIdBundle(set.getSetId(), -1, set.getName(), set.getDescrip(), set.getTime(), set.getSetImageId(), set.getSetType(), set.getURL());
        mCreateEditFragment = CreateEditSetFragment.newInstance(getActivity().getTitle().toString(), CreateEditSetFragment.TYPE_EDIT, args);
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
            set.setImageById(imageId);
            mSetViewModel.insert(set);
        } else {
            throw new RuntimeException(DEBUG_TAG + " trying to update set, was null");
        }
    }

    private void deleteUserSet(Set set){
        mSetViewModel.remove(set);
    }
    //endregion

    //region UTILITY
    private void deleteSetConfirmation(final Set set){
        if(getContext() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setTitle("Delete Set")
                    .setMessage("Are you sure you want to delete \n" + set.getName() + " ?")
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

    private void setupViewPager(){
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        if(mSetListFrags == null) {
            mSetListFrags = new ArrayList<>();
            for(int type = 0; type < Set.TYPES.length; type++) {
                SetListFragment frag = SetListFragment.newInstance(type, new SetListFragment.SetListListener() {
                    @Override
                    public void openBottomSheet(int setType, int setID) {

                        final Set set = mSetViewModel.getSetById(setID);

                        if (set == null)
                            throw new RuntimeException(DEBUG_TAG + " set came up null");

                        NewWorkoutFragment.this.openBottomSheet(set, new BottomSheetDialog.BottomSheetDialogListener() {

                            @Override
                            public void bottomSheetResult(int resultCode) {
                                if (resultCode == BaseApp.EDIT) {
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

                        if (type == SetListFragment.NEW_SET) {
                            openNewSet();
                        } else if (type == SetListFragment.EDIT_SET) {
                            if (set != null)
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
                mSetListFrags.add(frag);
            }
        }
        int type = 0;
        for(SetListFragment frag : mSetListFrags){
            mViewPagerAdapter.addFragment(frag, Set.TYPES[type++]);
        }


        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
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
        if(selectedSets.size() > Workout.MAX_SETS){
            Toast.makeText(mParentActivity, String.format(Locale.US, "Max sets reached. Please only choose %d sets or less.", Workout.MAX_SETS), Toast.LENGTH_SHORT).show();
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
            workout.setEquipmentRequired(mEquipRequired.isChecked());
            mListener.addNewWorkout(workout);
        }
    }

    private void setBottomMargin(View view, int b){
        if(view instanceof Button) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin,
                    params.rightMargin, b);
        }
    }


}
