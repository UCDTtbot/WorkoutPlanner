package com.shibedays.workoutplanner;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MyWorkoutActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = MyWorkoutActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.MainActivity.";

    //region INTENT_KEYS
    //endregion

    //region PRIVATE_KEYS
    private MainActivity mParentActivity;
    // UI Components
    private RecyclerView mRecyclerView;
    // Adapters
    private SetAdapter mSetAdapter;
    // Data
    private Workout mWorkoutData;
    // Instances
    private FragmentManager mFragmentManager;

    // Data Constants
    private int DATA_DOESNT_EXIST = -1;
    //endregion

    //region PUBLIC_KEYS

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mFragmentManager = getSupportFragmentManager();

        Intent intentIn = getIntent();
        if(intentIn != null){
            // Need mWorkoutData, mParentActivity
        } else {
            Log.e(DEBUG_TAG, "Intent was empty. onCreate MyWorkoutActivity");
        }

        mRecyclerView = findViewById(R.id.set_recycler_view);

    }

}
