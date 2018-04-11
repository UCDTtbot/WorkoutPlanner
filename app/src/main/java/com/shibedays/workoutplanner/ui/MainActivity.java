package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.ui.dialogs.SetBottomSheetDialog;
import com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment;
import com.shibedays.workoutplanner.ui.helpers.ListItemTouchHelper;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.WorkoutAdapter;
import com.shibedays.workoutplanner.ui.dialogs.WorkoutBottomSheetDialog;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutAdapterListener, WorkoutBottomSheetDialog.WorkoutBottomSheetDialogListener,
                                                                NewWorkoutFragment.OnFragmentInteractionListener, SetAdapter.SetAdapaterListener, ListItemTouchHelper.SwapItemsListener,
                                                                AddEditSetDialog.AddSetDialogListener, SetBottomSheetDialog.SetBottomSheetDialogListener, NumberPickerDialog.NumberPickerDialogListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MainActivity.";
    private static final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    //endregion

    //region INTENT_KEYS
    //EXTRA_FOO
    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    private static final String KEY_NEXT_WORKOUT_NUM = PACKAGE + "NextWorkoutNum";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Data
    private List<Workout> mWorkoutData;
    private List<Set> mUserCreatedSets;
    private List<Set> mDefaultSets;

    // Adapters
    private WorkoutAdapter mWorkoutAdapter;

    // Instances
    private SharedPreferences mPrivateSharedPrefs;
    private SharedPreferences mDefaultSharedPrefs;
    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;

    // Data Constants
    private int DATA_DOESNT_EXIST = -1;

    // View Model
    private WorkoutViewModel mWorkoutViewModel;
    private SetViewModel mSetViewModel;

    // Fragment(s)
    NewWorkoutFragment mNewWorkoutFragment;

    //endregion

    //region PUBLIC_VARS
    public static int NEXT_WORKOUT_ID;
    //endregion

    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        //region SHARED_PREFS
        // TODO: shared prefs
        mPrivateSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if(mPrivateSharedPrefs != null){
            int savedVersionCode = mPrivateSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
            if(savedVersionCode == currentVersionCode){
                // Normal Run
                NEXT_WORKOUT_ID = mPrivateSharedPrefs.getInt(KEY_NEXT_WORKOUT_NUM, -DATA_DOESNT_EXIST);
                if(NEXT_WORKOUT_ID == DATA_DOESNT_EXIST){
                    Log.e(DEBUG_TAG, "NEXT WORKOUT NUM DATA DOESN'T EXIST");
                    NEXT_WORKOUT_ID = mWorkoutData.size() + 1;
                }
            }else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                NEXT_WORKOUT_ID = 2;
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID);
                editor.apply();
            }else if (savedVersionCode < currentVersionCode){
                // Updated run
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.apply();
            }else{
                // Fatal error
                throw new RuntimeException(MainActivity.class.getSimpleName() + " Unknown error in Shared Prefs. savedVersionCode may not exist or is incorrect");
            }
        }

        //endregion

        //region RECYCLER_VIEW
        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        // Set the Layout Manager to Linear Layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the adapter with correct data
        mWorkoutAdapter = new WorkoutAdapter(this, findViewById(R.id.main_coord_layout));
        mRecyclerView.setAdapter(mWorkoutAdapter);
        mWorkoutAdapter.notifyDataSetChanged();
        //endregion

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region ADDITIONAL_UI

        //endregion

        //region FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewWorkoutFragment();
            }
        });
        //endregion

        //region VIEW_MODEL
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                mWorkoutData = workouts;
                mWorkoutAdapter.setData(workouts);
            }
        });

        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        mSetViewModel.getAllSets().observe(this, new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                mUserCreatedSets = sets;
            }
        });

        mDefaultSets = new ArrayList<>();
        mDefaultSets.add(new Set("Jogging", "Light Jog", 90000));
        mDefaultSets.add(new Set("Walk", "Brisk walk", 30000));
        mDefaultSets.add(new Set("Pushups", "As many pushups as possible in the time limit", 45000));
        mDefaultSets.add(new Set("Situps", "Arms across chest", 45000));
        //endregion

    }

    @Override
    protected void onStart() {
        super.onStart();
        mActionBar = getSupportActionBar();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "MAIN ACTIVITY ON_RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "MAIN ACTIVITY ON_PAUSE");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "MAIN ACTIVITY ON_STOP");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "MAIN ACTIVITY ON_DESTROY");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "MAIN ACTIVITY SAVING INSTANCE STATE");
    }

    //endregion

    //region TOOLBAR

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_PARENT, SettingsActivity.MAIN_ACTVITIY);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home){
            if(mFragmentManager.getBackStackEntryCount() > 0){
                mFragmentManager.popBackStack();
                toggleUpArrow(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Action Bar Function
    public void toggleUpArrow(boolean flag){
        mActionBar.setDisplayHomeAsUpEnabled(flag);
    }
    //endregion

    //region UTILITY
    public static int convertToMillis(int[] time){
        return ((time[0] * 60) + time[1]) * 1000;
    }

    public static int convertToMillis(int min, int sec){
        return((min * 60) + sec) * 1000;
    }

    public static int[] convertFromMillis(int time){
        int[] newTime = {0, 0};
        newTime[0] = (int)(Math.floor(time/1000)/60);
        newTime[1] = ((time/1000) % 60);
        return newTime;
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
    }
    //endregion

    //region NEW_WORKOUT

    private void openNewWorkoutFragment(){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mNewWorkoutFragment = NewWorkoutFragment.newInstance(mUserCreatedSets, mDefaultSets);
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mNewWorkoutFragment);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.new_workout_fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.GONE);
        fragmentTransaction.commit();
        toggleUpArrow(true);
        Log.d(DEBUG_TAG, "New Workout Fragment Created");
        //if(mUserCreatedSets != null)
        //    mNewWorkoutFragment.setUserCreatedSets(mUserCreatedSets);
    }

    // For New Workouts
    public int getNextWorkoutId(){
        return NEXT_WORKOUT_ID;
    }

    @Override
    public void addNewWorkout(Workout workout) {
        NEXT_WORKOUT_ID++;
        mWorkoutViewModel.insert(workout);
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    //region OLD_ADD_WORKOUT_CODE
    /*
    public void openNewWorkoutFragment(){
        AddEditWorkoutDialog addWorkoutDialog = new AddEditWorkoutDialog();
        Bundle args = new Bundle();
        args.putInt(AddEditWorkoutDialog.EXTRA_DIALOG_TYPE, AddEditWorkoutDialog.NEW_WORKOUT);
        addWorkoutDialog.setArguments(args);
        addWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onNewWorkoutDialogPositiveClick(String name){
        if(!TextUtils.isEmpty(name)){
            Workout newWorkout = new Workout(NEXT_WORKOUT_ID++, name);
            newWorkout.addSet(new Set("My Workout Set", "Description of my Workout Set", 60000));
            mPrivateSharedPrefs.edit().putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID).apply();
            mWorkoutViewModel.insertWorkout(newWorkout);
        } else {
            // TODO: Display an error message saying that name must not be null
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onEditWorkoutDialogPositiveClick(String name, int index) {

    }
    */
    //endregion

    //endregion

    //region NUMBER_PICKER_LISTENERS
    @Override
    public void setRestTime(int min, int sec, boolean noFlag) {
        if(mNewWorkoutFragment != null){
            mNewWorkoutFragment.setRestTime(min, sec, noFlag);
        }
    }

    @Override
    public void setBreakTime(int min, int sec, boolean noFlag) {
        if(mNewWorkoutFragment != null){
            mNewWorkoutFragment.setBreakTime(min, sec, noFlag);
        }
    }
    //endregion

    //region OPEN_WORKOUT
    @Override
    public void onWorkoutClicked(int workoutID) {
        openWorkout(workoutID);
    }

    public void openWorkout(int workoutID){
        Intent intent = new Intent(this, MyWorkoutActivity.class);
        intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
        intent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NORMAL_INTENT_TYPE);
        startActivity(intent);
    }
    //endregion

    //region BOTTOM_SHEET_WORKOUTS

    @Override
    public void onWorkoutLongClick(int workoutIndex, int workoutID) {
        openBottomDialog(workoutIndex, workoutID);
    }

    public void openBottomDialog(int workoutIndex, int workoutID){
        Bundle bundle = new Bundle();
        bundle.putInt(WorkoutBottomSheetDialog.EXTRA_WORKOUT_ID, workoutID);
        bundle.putInt(WorkoutBottomSheetDialog.EXTRA_WORKOUT_INDEX, workoutIndex);
        WorkoutBottomSheetDialog workoutBottomSheetDialog = new WorkoutBottomSheetDialog();
        workoutBottomSheetDialog.setArguments(bundle);
        workoutBottomSheetDialog.show(mFragmentManager, workoutBottomSheetDialog.getTag());
    }

    @Override
    public void onClickDuplicateWorkout(int index) {
        Workout workout = new Workout(NEXT_WORKOUT_ID++, mWorkoutData.get(index));
        mWorkoutViewModel.insert(workout);
    }

    // Deleting workout
    @Override
    public void onClickDeleteWorkout(int index) {
        WorkoutAdapter adapter = (WorkoutAdapter) mRecyclerView.getAdapter();
        adapter.pendingRemoval(index);
        // Snackbar is creating in pendingRemoval
    }

    @Override
    public void deleteWorkoutFromDB(Workout workout) {
        mWorkoutViewModel.remove(workout);
    }



    //region OLD_EDIT_WORKOUT_CODE
    /*
    @Override
    public void onEditWorkoutDialogPositiveClick(String name, int index) {
        if(!TextUtils.isEmpty(name)){
            mWorkoutData.get(index).setName(name);
            mWorkoutViewModel.updateWorkout(mWorkoutData.get(index));
        } else {
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_SHORT).show();
        }
    }
    */
    //endregion


    //endregion

    //region BOTTOM_SHEET_SET_CALLBACKS
    @Override
    public void bottomSheetTopRowClicked(int index, int section) {
        if(mNewWorkoutFragment != null){
            if(section == NewWorkoutFragment.RIGHT_SIDE) {
                mNewWorkoutFragment.editUserSet(index, section);
            } else if (section == NewWorkoutFragment.LEFT_SIDE){
                mNewWorkoutFragment.editUserCreatedSet(index, section);
            } else {
                throw new RuntimeException(DEBUG_TAG + " no section info was passed to bottomSheetTopRowClicked");
            }
        }
    }

    @Override
    public void bottomSheetBottomRowClicked(int index, int section) {
        if(mNewWorkoutFragment != null){
            if(section == NewWorkoutFragment.RIGHT_SIDE) {
                mNewWorkoutFragment.deleteUserSet(index);
            } else if (section == NewWorkoutFragment.LEFT_SIDE){
                Set set = mUserCreatedSets.get(index);
                mNewWorkoutFragment.deleteUserCreatedSet(index);
                mSetViewModel.remove(set);
            } else {
                throw new RuntimeException(DEBUG_TAG + " no section info was passed to bottomSheetBottomRowClicked");
            }
        }
    }
    //endregion

    //region MY_WORKOUT_SET_FUNCTIONS
    @Override
    public void onSetClick(int setIndex) {

    }

    @Override
    public void deleteSet(Set set) {

    }

    @Override
    public void swap(int from, int to) {
        // TODO : SWAP WORKOUTS YA
        Workout wrkFrom = mWorkoutData.get(from);
        Workout wrkTo = mWorkoutData.get(to);
        mWorkoutData.set(from, wrkTo);
        mWorkoutData.set(to, wrkFrom);
    }
    //endregion

    //region USER_CREATED_SET_FUNCTIONS
    @Override
    public void addUserCreatedSet(String name, String descrip, int min, int sec) {
        if(mNewWorkoutFragment != null){
            Set set = new Set(name, descrip, MainActivity.convertToMillis(min, sec));
            mNewWorkoutFragment.addUserCreatedSet(set);
            mSetViewModel.insert(set);
        }
    }

    @Override
    public void editUserCreatedSet(int index, String name, String descrip, int min, int sec) {
        if(mNewWorkoutFragment != null){
            mNewWorkoutFragment.updateUserCreatedSet(index, name, descrip, min, sec);
            Set set = mUserCreatedSets.get(index);
            mSetViewModel.update(set);
        }
    }
    //endregion

    //region USER_SETS
    @Override
    public void editUserSet(int index, String name, String descrip, int min, int sec){
        if(mNewWorkoutFragment != null){
            mNewWorkoutFragment.updateUserSet(index, name, descrip, min, sec);
        }
    }

    //endregion

}
