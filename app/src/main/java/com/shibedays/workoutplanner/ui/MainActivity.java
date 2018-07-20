package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.ui.adapters.WorkoutRowAdapter;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.fragments.NewWorkoutFragment;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.fragments.ShowAllWorkoutsFragment;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

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
    private AdView mAdView;

    private Handler mAdHandler;

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
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

        setContentView(R.layout.activity_main);



        mFragmentManager = getSupportFragmentManager();
        HIDE_ACTION_ITEMS = false;

        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);

        //region SHARED_PREFS
        // TODO: shared prefs
        mPrivateSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        int currentVersionCode = BuildConfig.VERSION_CODE;

        if(mPrivateSharedPrefs != null){
            int savedVersionCode = mPrivateSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
            if(savedVersionCode == currentVersionCode){
                // Normal Run

            } else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                initFirstRunData();
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
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

        boolean adsDisabled = false;
        if(mDefaultSharedPrefs != null){
            adsDisabled = mDefaultSharedPrefs.getBoolean("disable_ads", false);
        }


        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mAdView = findViewById(R.id.main_ad_view);
        if(!adsDisabled){
            MobileAds.initialize(this, "ca-app-pub-1633767409472368~4737915463");

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
            setBottomMargin(mRecyclerView, 180);
        } else {
            mAdView.setEnabled(false);
            mAdView.setVisibility(View.GONE);
            //TODO : MOVE THE MARGIN OF THE RECYCLER VIEW SO THERES NOT AN EMPTY SPOT AT THE BOTTOM
            setBottomMargin(mRecyclerView, 0);
        }
        //endregion


        //region RECYCLER_VIEW
        // Initialize the RecyclerView

        mRecyclerView.setHasFixedSize(true);

        // Set the Layout Manager to Linear Layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Setup the adapter with correct data
        mWorkoutRowAdapter = new WorkoutRowAdapter(this, (CoordinatorLayout) findViewById(R.id.main_coord_layout), new WorkoutRowAdapter.WorkoutRowListener() {
            @Override
            public void onWorkoutClicked(int id, int type) {
                if(id >= 0) {
                    openWorkout(id, type);
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

            @Override
            public void openMoreFragment(String type){
                int pos = Arrays.asList(Workout.TYPES).indexOf(type);
                showAllWorkoutsForType(pos);
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
        setupViewModels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdView != null) {
            mAdView.resume();
            AdRequest adr = new AdRequest.Builder()
                    .addTestDevice("777CB5CEE1249294D3D44B76236723E4")
                    .build();
            mAdView.loadAd(adr);
        }
        Log.d(DEBUG_TAG, "MAIN ACTIVITY ON_RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAdView != null) {
            mAdView.pause();
            if(mAdHandler != null)
                mAdHandler.removeCallbacks(null);
        }
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
        if(mAdView != null) {
            mAdView.destroy();
            if(mAdHandler != null)
                mAdHandler.removeCallbacks(null);
        }
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
                if((mFragmentManager.getBackStackEntryCount() - 1) <= 0) {
                    toggleUpArrow(false);
                }
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

    public void renameTitle(String title){
        setTitle(title);
    }

    public void hideActionItems(){
        HIDE_ACTION_ITEMS = true;
        invalidateOptionsMenu();
    }

    public void showActionItems(){
        HIDE_ACTION_ITEMS = false;
        invalidateOptionsMenu();
    }

    private void setupViewModels(){
        //region VIEW_MODEL
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                
            }
        });

        for(int i = 0; i < Workout.TYPES.length; i++){
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

    private void showAllWorkoutsForType(int type){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Bundle args = ShowAllWorkoutsFragment.getBundle(type);
        ShowAllWorkoutsFragment frag = ShowAllWorkoutsFragment.newInstance(args, (CoordinatorLayout)findViewById(R.id.main_coord_layout), new ShowAllWorkoutsFragment.ShowAllListener() {
            @Override
            public void openWorkout(int id, int type) {

            }

            @Override
            public void openNewWorkout() {

            }

            @Override
            public void openBottomSheet(int id, int type) {

            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.show_all_workouts_frag_container, frag);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.show_all_workouts_frag_container).setVisibility(View.VISIBLE);
        fragmentTransaction.commit();
        renameTitle(Workout.TYPES[type]);
        hideActionItems();
        toggleUpArrow(true);
        Log.d(DEBUG_TAG, "Show All Workouts Fragment Created");
    }


    //region NEW_WORKOUT
    private void openNewWorkoutFragment(){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mNewWorkoutFragment = NewWorkoutFragment.newInstance(new NewWorkoutFragment.NewWorkoutListener() {
            @Override
            public void addNewWorkout(Workout workout) {
                mWorkoutViewModel.insert(workout);
                View view = getCurrentFocus();
                if(view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mNewWorkoutFragment);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.new_workout_fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.main_ad_view).setVisibility(View.GONE);
        fragmentTransaction.commit();
        renameTitle(R.string.new_workout);
        hideActionItems();
        toggleUpArrow(true);
        Log.d(DEBUG_TAG, "New Workout Fragment Created");

    }
    //endregion


    //region OPEN_WORKOUT
    public void openWorkout(int workoutID, int type){
        if(workoutID >= 0) {
            Intent intent = new Intent(this, MyWorkoutActivity.class);
            intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
            intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_TYPE, type);
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
                        Workout newWorkout = new Workout(workout);
                        mWorkoutViewModel.insert(newWorkout);
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

    private void initFirstRunData(){
    }

    private void setBottomMargin(View view, int b){
        if(view instanceof RecyclerView) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin,
                    params.rightMargin, b);
        }
    }

}
