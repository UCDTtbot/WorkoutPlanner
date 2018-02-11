package com.shibedays.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

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
    private static final String KEY_WORKOUT_DATA = PACKAGE + ".WorkoutData";
    //endregion

    //BRD_FILTER_FOO

    //region PRIVATE_VARS
    private RecyclerView mRecyclerView;
    private List<Workout> mWorkoutList;
    private WorkoutAdapter mAdapter;

    private SharedPreferences mSharedPrefs;
    private final String PREF_IDENTIFIER = PACKAGE + ".PREFS";

    //endregion

    //region PUBLIC_VARS

    //endregion

    //region OVERRIDE_FUNCTIONS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the RecyclerView
        // Set the Layout Manager to Linear Layout
        //Init the Array list and setup the adapter
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkoutList = new ArrayList<>();
        mAdapter = new WorkoutAdapter(this, mWorkoutList);
        mRecyclerView.setAdapter(mAdapter);

        // Get SharedPrefs. If this is the initial run, setup Prefs
        //region PREFS
        mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        if(mSharedPrefs == null){
            SharedPreferences.Editor tempEditor = mSharedPrefs.edit();


            // FILL PREF_DATA
            tempEditor.apply();
        }
        //endregion


        // Get Workout Data
        //TODO: Placeholder workout data
        Workout tempWorkout = new Workout(0, "Cardio Day");
        mWorkoutList.add(tempWorkout);
        mAdapter.notifyDataSetChanged();

        //region RECYCLER_VIEW
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
    //endregion
}
