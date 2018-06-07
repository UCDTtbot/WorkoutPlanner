package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.ui.adapters.WorkoutRowAdapter;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.fragments.CreateEditSetFragment;
import com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NewWorkoutFragment.OnFragmentInteractionListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MainActivity.";
    public static final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    //endregion

    //region INTENT_KEYS
    //EXTRA_FOO
    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    public static final String KEY_NEXT_WORKOUT_NUM = PACKAGE + "NextWorkoutNum";
    public static final String KEY_NEXT_SET_NUM = PACKAGE + "NextSetNum";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Flags
    private boolean HIDE_ACTION_ITEMS;

    // Adapters
    private WorkoutRowAdapter mWorkoutRowAdapter;

    // Instances
    private SharedPreferences mPrivateSharedPrefs;
    private SharedPreferences mDefaultSharedPrefs;
    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;

    // Data Constants
    private int DATA_DOESNT_EXIST = -1;

    // View Model
    private WorkoutViewModel mWorkoutViewModel;
    //private SetViewModel mSetViewModel;

    // Fragment(s)
    NewWorkoutFragment mNewWorkoutFragment;

    //endregion

    //region PUBLIC_VARS
    //endregion


    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        HIDE_ACTION_ITEMS = false;


        //region SHARED_PREFS
        // TODO: shared prefs
        mPrivateSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        int currentVersionCode = BuildConfig.VERSION_CODE;

        if(mPrivateSharedPrefs != null){
            int savedVersionCode = mPrivateSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
            if(savedVersionCode == currentVersionCode){
                // Normal Run
                BaseApp.setWorkoutID(mPrivateSharedPrefs.getInt(KEY_NEXT_WORKOUT_NUM, DATA_DOESNT_EXIST));
                BaseApp.setSetID(mPrivateSharedPrefs.getInt(KEY_NEXT_SET_NUM, DATA_DOESNT_EXIST));
            } else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.putInt(KEY_NEXT_WORKOUT_NUM, BaseApp.getNextWorkoutID());
                editor.putInt(KEY_NEXT_SET_NUM, BaseApp.getNextSetID());
                editor.apply();
                BaseApp.setWorkoutID(DATA_DOESNT_EXIST);
                BaseApp.setSetID(DATA_DOESNT_EXIST);
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

        mRecyclerView.setHasFixedSize(true);

        // Set the Layout Manager to Linear Layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Setup the adapter with correct data
        mWorkoutRowAdapter = new WorkoutRowAdapter(this, (CoordinatorLayout) findViewById(R.id.main_coord_layout), new WorkoutRowAdapter.WorkoutRowListener() {
            @Override
            public void onWorkoutClicked(int id, int type) {
                if(id >= 0) {
                    openWorkout(id);
                } else if (type == Workout.USER_CREATED) {
                    openNewWorkoutFragment();
                }
            }

            @Override
            public void onWorkoutLongClick(int id, int type) {
                if(type == Workout.USER_CREATED) {
                    openBottomSheet(id, type);
                }
            }

            @Override
            public void deleteFromDB(Workout workout) {
                deleteWorkoutFromDB(workout);
            }
        });
        mRecyclerView.setAdapter(mWorkoutRowAdapter);
        mWorkoutRowAdapter.initiateData();
        mWorkoutRowAdapter.notifyDataSetChanged();
        //endregion


        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion


        //region FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewWorkoutFragment();
            }
        });
        //endregion

    }

    @Override
    protected void onStart() {
        super.onStart();
        mActionBar = getSupportActionBar();
        setupData();

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
        if(HIDE_ACTION_ITEMS){
            for(int i = 0; i < menu.size(); i++){
                menu.getItem(i).setVisible(false);
            }
        }
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
                if(mFragmentManager.getBackStackEntryCount() <= 0)
                    toggleUpArrow(false);
            }
            return true;
        } else if(id == R.id.debug_add) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Bundle args = CreateEditSetFragment.getBundle(-1, "", "", 0, R.drawable.ic_fitness_black_24dp);
            CreateEditSetFragment createEditSetFragment = CreateEditSetFragment.newInstance(R.string.new_workout, args, new CreateEditSetFragment.CreateEditSetListener() {
                @Override
                public void returnData(String name, String descrip, int min, int sec, int imageId) {
                    Set set = new Set(BaseApp.getNextSetID(), name, descrip, Set.USER_CREATED, BaseApp.convertToMillis(min, sec), imageId);
                    BaseApp.incrementSetID(getApplicationContext());
                }
            });
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
            fragmentTransaction.replace(R.id.new_workout_fragment_container, createEditSetFragment);
            findViewById(R.id.new_workout_fragment_container).setVisibility(View.VISIBLE);
            fragmentTransaction.addToBackStack(null);
            renameTitle(R.string.new_set);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    // Action Bar Function
    public void toggleUpArrow(boolean flag){
        mActionBar.setDisplayHomeAsUpEnabled(flag);
    }
    //endregion

    //region UTILITY
    public void renameTitle(int stringId){
        setTitle(stringId);
    }

    public void hideActionItems(){
        HIDE_ACTION_ITEMS = true;
        invalidateOptionsMenu();
    }

    public void showActionItems(){
        HIDE_ACTION_ITEMS = false;
        invalidateOptionsMenu();
    }

    private void setupData(){
        //region VIEW_MODEL
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                if(workouts != null){
                    if(!workouts.isEmpty()){
                        BaseApp.setWorkoutID(workouts.size() + 1);
                    }
                }
            }
        });

        for(int i = 0; i < Set.TYPES.length; i++){
            mWorkoutViewModel.getAllTypedWorkouts(i).observe(this, new Observer<List<Workout>>() {
                @Override
                public void onChanged(@Nullable List<Workout> workouts) {
                    if(workouts != null){
                        if(!workouts.isEmpty()){
                            if(mWorkoutRowAdapter != null){
                                int type = workouts.get(0).getWorkoutType();
                                mWorkoutRowAdapter.updateData(type, workouts);
                            }
                        }
                    }
                }
            });
        }


        Log.d(DEBUG_TAG, "Added data");

        //endregion
    }
    //endregion


    //region NEW_WORKOUT
    private void openNewWorkoutFragment(){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mNewWorkoutFragment = NewWorkoutFragment.newInstance();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mNewWorkoutFragment);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.new_workout_fragment_container).setVisibility(View.VISIBLE);
        fragmentTransaction.commit();
        renameTitle(R.string.new_workout);
        hideActionItems();
        toggleUpArrow(true);
        Log.d(DEBUG_TAG, "New Workout Fragment Created");

    }

    @Override
    public void addNewWorkout(Workout workout) {
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

    //endregion


    //region OPEN_WORKOUT
    public void openWorkout(int workoutID){
        if(workoutID >= 0) {
            Intent intent = new Intent(this, MyWorkoutActivity.class);
            intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
            intent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NORMAL_INTENT_TYPE);
            startActivity(intent);
        }
    }
    //endregion

    //region BOTTOM_SHEET_WORKOUTS
    public void openBottomSheet(final int id, final int type){
        final Workout workout = mWorkoutViewModel.getWorkoutByID(id);
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(workout.getName(), BaseApp.getWrkBtmSheetRows(), BaseApp.getWrkBtmSheetNames(this), BaseApp.getWrkBtmSheetICs(), BaseApp.getWrkBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, new BottomSheetDialog.BottomSheetDialogListener() {
            @Override
            public void bottomSheetResult(int resultCode) {
                switch (resultCode){
                    case BaseApp.EDIT:
                        throw new RuntimeException(DEBUG_TAG + " workout bottom sheet shouldn't be sending back Edit right now");
                    case BaseApp.DELETE:
                        mWorkoutRowAdapter.pendingRemoval(id, type);
                        break;
                    case BaseApp.DUPLCIATE:
                        Workout newWorkout = new Workout(BaseApp.getNextWorkoutID(), workout);
                        addNewWorkout(newWorkout);
                        break;
                    default:
                        break;
                }
            }
        });
        if(mFragmentManager != null){
            dialog.show(mFragmentManager, DEBUG_TAG);
        }
    }

    public void deleteWorkoutFromDB(Workout workout) {
        mWorkoutViewModel.remove(workout);
    }

    //endregion



}
