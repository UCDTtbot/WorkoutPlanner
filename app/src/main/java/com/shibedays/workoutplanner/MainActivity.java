package com.shibedays.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

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

    //BRD_FILTER_FOO

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Adapters
    private WorkoutAdapter mAdapter;

    // Data
    private List<Workout> mWorkoutList;

    // Instances
    private SharedPreferences mSharedPrefs;

    // Data Constants
    private int DOESNT_EXIST = -1;
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region OVERRIDE_FUNCTIONS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int currentVersionCode = BuildConfig.VERSION_CODE;
        // Get SharedPrefs. If this is the initial run, setup Prefs
        //region PREFS
        mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        int savedVersionCode = mSharedPrefs.getInt(KEY_VERSION_CODE, DOESNT_EXIST);
        // Check version code
        Log.d(DEBUG_TAG, "Current: " + currentVersionCode + " Saved: " + savedVersionCode);
        if(savedVersionCode == currentVersionCode){
            // Normal Run
            Log.d(DEBUG_TAG, "Normal run, retrieving prefs");
            // get data from shared prefs
            mWorkoutList = getWorkoutsFromPref();
        } else if (savedVersionCode == DOESNT_EXIST){
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

        // Get Workout Data
        //TODO: Placeholder workout data



        //region RECYCLER_VIEW
        // Initialize the RecyclerView
        // Set the Layout Manager to Linear Layout
        // Setup the adapter with correct data
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new WorkoutAdapter(this, mWorkoutList);
        mRecyclerView.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
        // Add the horizontal bar lines as an item decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        //TODO: Add recycler animation?
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //endregion
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveWorkoutsToPref();
    }


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

    public void addWorkout(){

    }
    //endregion
}
