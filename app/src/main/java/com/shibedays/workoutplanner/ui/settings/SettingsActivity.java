package com.shibedays.workoutplanner.ui.settings;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = SettingsActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.settings.SettingsActivity.";

    public static final int MAIN_ACTVITIY = 0;
    public static final int MY_WORKOUT_ACTIVITY = 1;

    public static final String EXTRA_PARENT = PACKAGE + "PARENT";

    private int mParentClass;


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
            int type = in.getIntExtra(EXTRA_PARENT, -1);
            if(type == 0){
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if( type == 1){
                intent = new Intent(this, MyWorkoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                // Fatal error
                Log.e(DEBUG_TAG, "Fatal error in determining parent activity in Settings Activity");
            }
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        getParentActivityIntent();
    }
}
