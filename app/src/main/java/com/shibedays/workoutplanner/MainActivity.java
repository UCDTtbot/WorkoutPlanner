package com.shibedays.workoutplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NewWorkoutDialog.WorkoutDialogListener{

    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.MainActivity.";

    //region INTENT_KEYS
    //EXTRA_FOO
    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private static final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    private static final String KEY_WORKOUT_DATA = PACKAGE + "WorkoutData";
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    //endregion

    //TODO: BRD_FILTER_FOO

    private AppDatabase db;
    private Executor exe;

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

        exe = Executors.newSingleThreadExecutor();
        db = AppDatabase.getDatabaseInstance(this, exe);
        exe.execute(new Runnable() {
            @Override
            public void run() {
                mWorkoutList = db.workoutDao().getAll();
            }
        });
        /*
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
        */

        /*
        //region RECYCLER_VIEW
        // Initialize the RecyclerView
        // Set the Layout Manager to Linear Layout
        // Setup the adapter with correct data
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkoutAdapter = new WorkoutAdapter(this, mWorkoutList, this);
        mRecyclerView.setAdapter(mWorkoutAdapter);
        //mWorkoutAdapter.notifyDataSetChanged();
        // Add the horizontal bar lines as an item decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        //TODO: Add recycler animation?

        //region SWIPE_SETUP
        int dragDirs = 0;
        final int swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
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

            // For getting the swiped direction. If we somehow swipe an item that's already pendingRemoval, return 0
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
                int itemPos = viewHolder.getAdapterPosition();
                WorkoutAdapter adapter = (WorkoutAdapter)recyclerView.getAdapter();
                if(adapter.isPendingRemoval(itemPos)){
                    return 0;
                } else {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
            }

            // When an item is swiped, put it up for removal
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPos = viewHolder.getAdapterPosition();
                WorkoutAdapter adapter = (WorkoutAdapter)mRecyclerView.getAdapter();
                adapter.pendingRemoval(swipedPos);
                // Snackbar is creating in pendingRemoval
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){

                View itemView = viewHolder.itemView;

                // This also gets called for viewholders that are already swiped away, so handle for that
                if(viewHolder.getAdapterPosition() < 0){
                    return;
                }

                if(!initiated){
                    init();
                }

                Log.d(DEBUG_TAG, "dX: " + Float.toString(dX));

                 //if dX > 0, swiping right
                    //if dX < 0 swiping left
                if(dX < 0) {
                    Log.d(DEBUG_TAG, "Swiping Left");
                    // draw the background for the child view
                    // The background bounds will be from the edge of the view to the edge of the device
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Draw the relevent icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    //TODO: What's intristic mean
                    int intristicHeight = deleteIC.getIntrinsicHeight();
                    int intristicWidth = deleteIC.getIntrinsicWidth();

                    int deleteICLeft = itemView.getRight() - deleteICMargin - intristicWidth;
                    int deleteICRight = itemView.getRight() - deleteICMargin;
                    int deleteICTop = itemView.getTop() + (itemHeight - intristicHeight) / 2; // divide by 2 to get the center
                    int deleteICBottom = deleteICTop + intristicHeight;
                    deleteIC.setBounds(deleteICLeft, deleteICTop, deleteICRight, deleteICBottom);

                    deleteIC.draw(c);
                } else if (dX > 0) {
                    Log.d(DEBUG_TAG, "Swiping Right");
                    // draw the background for the child view
                    // The background bounds will be from the edge of the view to the edge of the device
                    //background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),itemView.getRight(), itemView.getBottom());
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + (int) dX, itemView.getBottom());
                    background.draw(c);

                    // Draw the relevent icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    //TODO: What's intristic mean
                    int intristicHeight = deleteIC.getIntrinsicHeight();
                    int intristicWidth = deleteIC.getIntrinsicWidth();

                    int deleteICLeft = itemView.getLeft() + deleteICMargin;
                    int deleteICRight = itemView.getLeft() + deleteICMargin + intristicWidth;
                    int deleteICTop = itemView.getTop() + (itemHeight - intristicHeight) / 2; // divide by 2 to get the center
                    int deleteICBottom = deleteICTop + intristicHeight;
                    deleteIC.setBounds(deleteICLeft, deleteICTop, deleteICRight, deleteICBottom);

                    deleteIC.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        //endregion
        //endregion

        //region ADDITIONAL_UI

        //endregion
        */
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
        Log.d(DEBUG_TAG, "test");
        // TODO: DB debugging
        //NewWorkoutDialog newWorkoutDialog = new NewWorkoutDialog();
        //newWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
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

    public void openWorkout(int workoutPos){
        Log.d(DEBUG_TAG, "Opening Workout: " + workoutPos);
        Log.d(DEBUG_TAG, mWorkoutList.get(workoutPos).getName());

        // TODO: Commenting out opening workouts
        //Intent intent = new Intent(this, MyWorkoutActivity.class);
        //startActivity(intent);
    }
}
