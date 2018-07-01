package com.shibedays.workoutplanner.ui.settings;

import android.content.Intent;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

public class SettingsActivity extends AppCompatActivity {

    //region CONSTANTS
    private static final String DEBUG_TAG = SettingsActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.settings.SettingsActivity.";

    public static final int MAIN_ACTVITIY = 0;
    public static final int MY_WORKOUT_ACTIVITY = 1;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_PARENT = PACKAGE + "PARENT";
    //endregion

    //region PRIVATE_VARS
    // Parent Activity Type
    private int mParentClassType;
    //endregion

    //region GET_PARENT_ACTIVITY
    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl(){
        Intent intent = null;
        Intent in = getIntent();
        if(in != null){
            mParentClassType = in.getIntExtra(EXTRA_PARENT, -1);
            if(mParentClassType == 0){
                intent = new Intent(this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else if( mParentClassType == 1){
                intent = new Intent(this, MyWorkoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                // Fatal error
                throw new RuntimeException(SettingsActivity.class.toString() + " could not determine parent activity: " + Integer.toString(mParentClassType));
            }
        }

        return intent;
    }
    //endregion

    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_Dark);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        getParentActivityIntent();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(getParentActivityIntentImpl());
        finish();
    }

    //endregion
}
