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

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.adapters.WorkoutAdapter;
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

    // Data
    private List<Workout> mWorkoutData;
    private List<List<Set>> mTypedSets;

    // Flags
    private boolean HIDE_ITEMS;

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
    //endregion


    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        HIDE_ITEMS = false;

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
                if(BaseApp.getNextWorkoutID() == DATA_DOESNT_EXIST){
                    Log.e(DEBUG_TAG, "NEXT WORKOUT NUM DATA DOESN'T EXIST");
                    BaseApp.setWorkoutID(mWorkoutData.size() + 1);
                }

            }else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                BaseApp.setWorkoutID(1);
                BaseApp.setSetID(50);
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.putInt(KEY_NEXT_WORKOUT_NUM, BaseApp.getNextWorkoutID());
                editor.putInt(KEY_NEXT_SET_NUM, BaseApp.getNextSetID());
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
        mWorkoutAdapter = new WorkoutAdapter(this, findViewById(R.id.main_coord_layout), new WorkoutAdapter.WorkoutAdapterListener() {
            @Override
            public void onWorkoutClicked(int workoutIndex) {
                openWorkout(workoutIndex);
            }

            @Override
            public void onWorkoutLongClick(int workoutIndex, int workoutID) {
                openBottomSheet(workoutIndex);
            }

            @Override
            public void deleteFromDB(Workout workout) {
                deleteWorkoutFromDB(workout);
            }
        });
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
        mTypedSets = new ArrayList<List<Set>>() {{
            for (String TYPE : Set.TYPES) {
                add(new ArrayList<Set>());
            }
        }};
        for(int i = 0; i < Set.TYPES.length; i++){
            final int x = i;

            mSetViewModel.getTypedSet(i).observe(this, new Observer<List<Set>>() {
                @Override
                public void onChanged(@Nullable List<Set> sets) {
                    if(sets != null){
                        if(!sets.isEmpty()) {
                            mTypedSets.set(sets.get(0).getSetType(), sets);
                        }
                    }
                }
            });
        }

        mSetViewModel.getAllSets().observe(this, new Observer<List<Set>>() {
            @Override
            public void onChanged(@Nullable List<Set> sets) {
                if(sets != null) {
                    if(!sets.isEmpty()) {
                        if(BaseApp.getNextSetID() == DATA_DOESNT_EXIST) {
                            BaseApp.setSetID(sets.size() + 1);
                        }
                    }
                }
            }
        });

        Log.d(DEBUG_TAG, "Added data");

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
        if(HIDE_ITEMS){
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
    public void renameTitle(int stringId){
        setTitle(stringId);
    }

    public void hideActionItems(){
        HIDE_ITEMS = true;
        invalidateOptionsMenu();
    }

    public void showActionItems(){
        HIDE_ITEMS = false;
        invalidateOptionsMenu();
    }
    //endregion


    //region NEW_WORKOUT
    private void openNewWorkoutFragment(){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mNewWorkoutFragment = NewWorkoutFragment.newInstance(mTypedSets);
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mNewWorkoutFragment);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.new_workout_fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.fab).setVisibility(View.GONE);
        fragmentTransaction.commit();
        renameTitle(R.string.new_workout);
        hideActionItems();
        toggleUpArrow(true);
        Log.d(DEBUG_TAG, "New Workout Fragment Created");
    }

    @Override
    public void addNewWorkout(Workout workout) {
        BaseApp.incrementWorkoutID(getApplicationContext());
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

    @Override
    public void applyUserSetToDB(Set set) {
        mSetViewModel.insert(set);
    }

    @Override
    public void removeUserSetFromDB(Set set) {
        mSetViewModel.remove(set);
    }

    //endregion


    //region OPEN_WORKOUT
    public void openWorkout(int workoutID){
        Intent intent = new Intent(this, MyWorkoutActivity.class);
        intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
        intent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NORMAL_INTENT_TYPE);
        startActivity(intent);
    }
    //endregion

    //region BOTTOM_SHEET_WORKOUTS
    public void openBottomSheet(final int workoutIndex){
        Workout workout = mWorkoutData.get(workoutIndex);
        Bundle bundle = BottomSheetDialog.getBottomSheetBundle(workout.getName(), BaseApp.getWrkBtmSheetRows(), BaseApp.getWrkBtmSheetNames(this), BaseApp.getWrkBtmSheetICs(), BaseApp.getWrkBtmSheetResults());
        BottomSheetDialog dialog = BottomSheetDialog.newInstance(bundle, new BottomSheetDialog.BottomSheetDialogListener() {
            @Override
            public void bottomSheetResult(int resultCode) {
                switch (resultCode){
                    case BaseApp.EDIT:
                        throw new RuntimeException(DEBUG_TAG + " workout bottom sheet shouldn't be sending back Edit right now");
                    case BaseApp.DELETE:
                        mWorkoutAdapter.pendingRemoval(workoutIndex);
                        break;
                    case BaseApp.DUPLCIATE:
                        Workout newWorkout = new Workout(BaseApp.getNextWorkoutID(), mWorkoutData.get(workoutIndex));
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

    public void addSetToDB(Set set){
        mSetViewModel.insert(set);
    }
    public void deleteSetFromDB(Set set){
        mSetViewModel.remove(set);
    }
    //endregion



}
