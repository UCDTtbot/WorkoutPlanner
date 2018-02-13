package com.shibedays.workoutplanner;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NewWorkoutDialog.WorkoutDialogListener{

    private final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.MainActivity.";

    //region INTENT_KEYS
    //EXTRA_FOO

    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    private static final String KEY_WORKOUT_DATA = PACKAGE + "WorkoutData";
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    //endregion

    //TODO: BRD_FILTER_FOO

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Adapters
    private WorkoutAdapter mWorkoutAdapter;

    // Data
    private List<Workout> mWorkoutList;

    // Instances
    private SharedPreferences mSharedPrefs;
    private FragmentManager mFragmentManager;

    // Data Constants
    private int DATA_DOESNT_EXIST = -1;
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region OVERRIDE_FUNCTIONS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int currentVersionCode = BuildConfig.VERSION_CODE;

        mFragmentManager = getSupportFragmentManager();

        //region PREFS
        // Get SharedPrefs. If this is the initial run, setup Prefs
        mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        int savedVersionCode = mSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
        // Check version code
        Log.d(DEBUG_TAG, "Current: " + currentVersionCode + " Saved: " + savedVersionCode);
        if(savedVersionCode == currentVersionCode){
            // Normal Run
            Log.d(DEBUG_TAG, "Normal run, retrieving prefs");
            // get data from shared prefs
            mWorkoutList = getWorkoutsFromPref();
        } else if (savedVersionCode == DATA_DOESNT_EXIST){
            // First run
            Log.d(DEBUG_TAG, "First time run. Creating default workouts and prefs");
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            // If SharedPrefs didn't exist, create default workout data for init purposes
            Workout defaultWorkout1 = new Workout(0, "Cardio Day");
            Workout defaultWorkout2 = new Workout(1, "Leg Day");
            mWorkoutList = new ArrayList<Workout>();
            mWorkoutList.add(defaultWorkout1);
            mWorkoutList.add(defaultWorkout2);
            Gson gson = new Gson();
            String json = gson.toJson(mWorkoutList);
            editor.putString(KEY_WORKOUT_DATA, json);
            editor.putInt(KEY_VERSION_CODE, currentVersionCode);
            // FILL PREF_DATA
            editor.apply();
        } else if (savedVersionCode < currentVersionCode){
            // Upgraded run
            Log.d(DEBUG_TAG, "App has been upgraded. Updating Prefs");
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putInt(KEY_VERSION_CODE, currentVersionCode);
            editor.apply();

        } else {
            // Fatal error?
            Log.e(DEBUG_TAG, "Something wrong with version code.");
        }
        //endregion

        //region RECYCLER_VIEW
        // Initialize the RecyclerView
        // Set the Layout Manager to Linear Layout
        // Setup the adapter with correct data
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkoutAdapter = new WorkoutAdapter(this, mWorkoutList);
        mRecyclerView.setAdapter(mWorkoutAdapter);
        //mWorkoutAdapter.notifyDataSetChanged();
        // Add the horizontal bar lines as an item decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        //TODO: Add recycler animation?

        //region SWIPE_SETUP
        int dragDirs = 0;
        int swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                dragDirs, swipeDirs) {

            // Swipe to delete help from:
            // https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/blob/master/app/src/main/java/net/nemanjakovacevic/recyclerviewswipetodelete/
            // Cache the vars needed for onChildDraw
            Drawable background;
            Drawable deleteIC;
            int deleteICMargin;
            boolean initiated;

            // Initiate the above needed data
            private void init(){
                background = new ColorDrawable(Color.RED);
                deleteIC = getDrawable(R.drawable.ic_delete);
                deleteICMargin = (int) getResources().getDimension(R.dimen.ic_delete_margin);
                initiated = true;
            }

            // This is for dragging, we don't need (for now)
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        //endregion
        //endregion

        //region ADDITIONAL_UI

        //endregion

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addWorkout();
            }
        });
        //endregion
    }

    @Override
    protected void onDestroy() {
        saveWorkoutsToPref();
        super.onDestroy();
    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion




    //region UTILITY
    /**
     * Converts the time from a 2 piece array to milliseconds
     * @param time int[]
     * @return Returns the time in millis
     */
    public static int convertToMillis(int[] time){
        return ((time[0] * 60) + time[1]) * 1000;
    }

    /**
     * Converts the time from milliseconds to a 2 piece array
     * @param time int
     * @return returns the time as M/S
     */
    public static int[] convertFromMillis(int time){
        int[] newTime = {0, 0};
        newTime[0] = (int)(Math.floor(time/1000)/60);
        newTime[1] = ((time/1000) % 60);
        return newTime;
    }

    public void saveWorkoutsToPref(){
        if(mSharedPrefs == null){
            mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mWorkoutList);
        editor.putString(KEY_WORKOUT_DATA, json);
        editor.apply();
    }

    public List<Workout> getWorkoutsFromPref(){
        List<Workout> inData = null;
        Gson gson = new Gson();
        if(mSharedPrefs == null) {
            mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        }
        String json = mSharedPrefs.getString(KEY_WORKOUT_DATA, "");
        Type type = new TypeToken<List<Workout>>(){}.getType();
        inData = gson.fromJson(json, type);
        if(inData == null){
            Log.e(DEBUG_TAG, "No data for workouts was found in SharedPrefs");
        }
        return inData;
    }
    //endregion

    //region ADD_NEW_WORKOUT

    public void addWorkout(){
        NewWorkoutDialog newWorkoutDialog = new NewWorkoutDialog();
        newWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onDialogPositiveClick(String name){
        if(!TextUtils.isEmpty(name)){
            Workout newWorkout = new Workout(mWorkoutList.size(), name);

            // TODO: Add default sets into the workout

            mWorkoutList.add(newWorkout);
            mWorkoutAdapter.notifyDataSetChanged();
            saveWorkoutsToPref();
        } else {
            // TODO: Display an error message saying that name must not be null
        }

    }

    @Override
    public void onDialogNegativeClick(){

    }
    //endregion
}
