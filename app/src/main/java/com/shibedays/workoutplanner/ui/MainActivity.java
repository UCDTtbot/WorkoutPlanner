package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.shibedays.workoutplanner.BuildConfig;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.WorkoutAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.lang.reflect.Method;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NewWorkoutDialog.WorkoutDialogListener, WorkoutAdapter.WorkoutAdapterListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MainActivity.";
    //endregion

    //region INTENT_KEYS
    //EXTRA_FOO
    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private static final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    private static final String KEY_NEXT_WORKOUT_NUM = PACKAGE + "NextWorkoutNum";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Adapters
    private WorkoutAdapter mWorkoutAdapter;

    // Instances
    private SharedPreferences mSharedPrefs;
    private FragmentManager mFragmentManager;

    // Data Constants
    private int DATA_DOESNT_EXIST = -1;

    // View Model
    private WorkoutViewModel mWorkoutViewModel;

    //endregion

    //region PUBLIC_VARS
    public static int NEXT_WORKOUT_ID;
    //endregion

    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mFragmentManager = getSupportFragmentManager();

        //region SHARED_PREFS
        // TODO: shared prefs
        mSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if(mSharedPrefs != null){
            int savedVersionCode = mSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
            if(savedVersionCode == currentVersionCode){
                // Normal Run
                NEXT_WORKOUT_ID = mSharedPrefs.getInt(KEY_NEXT_WORKOUT_NUM, -DATA_DOESNT_EXIST);
                if(NEXT_WORKOUT_ID == DATA_DOESNT_EXIST){
                    Log.e(DEBUG_TAG, "NEXT WORKOUT NUM DATA DOESN'T EXIST");
                }
            }else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                NEXT_WORKOUT_ID = 2;
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID);
                editor.apply();
            }else if (savedVersionCode < currentVersionCode){
                // Updated run
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.apply();
            }else{
                // Fatal error
                Log.e(DEBUG_TAG, "Unknown Error in SharedPrefs");
            }
        }

        //endregion

        //region RECYCLER_VIEW
        // Initialize the RecyclerView
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        // Set the Layout Manager to Linear Layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup the adapter with correct data
        mWorkoutAdapter = new WorkoutAdapter(this, findViewById(R.id.main_coord_layout));
        mRecyclerView.setAdapter(mWorkoutAdapter);
        mWorkoutAdapter.notifyDataSetChanged();


        // Add the horizontal bar lines as an item decoration
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        //TODO: Add recycler animation?

        //region TOUCH_SWIPE_SETUP
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
                deleteIC = getDrawable(R.drawable.ic_delete_white_24dp);
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
                addWorkout();
            }
        });
        //endregion

        //region VIEW_MODEL
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mWorkoutViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                // TODO: What happens when data changes
                mWorkoutAdapter.setData(workouts);
            }
        });
        //endregion

        // FOR DEBUGGING PURPOSES
        //showDebugDBAddressLogToast(this);
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

    public static int convertToMillis(int[] time){
        return ((time[0] * 60) + time[1]) * 1000;
    }

    public static int convertToMillis(int min, int sec){
        return((min * 60) + sec) * 1000;
    }

    public static int[] convertFromMillis(int time){
        int[] newTime = {0, 0};
        newTime[0] = (int)(Math.floor(time/1000)/60);
        newTime[1] = ((time/1000) % 60);
        return newTime;
    }

    public void openWorkout(int workoutID){
        Intent intent = new Intent(this, MyWorkoutActivity.class);
        intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
        intent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NORMAL_INTENT_TYPE);
        startActivity(intent);
    }

    public void openBottomDialog(int workoutID){
        Bundle bundle = new Bundle();
        bundle.putInt(WorkoutBottomSheetDialog.EXTRA_WORKOUT_ID, workoutID);
        WorkoutBottomSheetDialog workoutBottomSheetDialog = new WorkoutBottomSheetDialog();
        workoutBottomSheetDialog.setArguments(bundle);
        workoutBottomSheetDialog.show(mFragmentManager, workoutBottomSheetDialog.getTag());
    }

    // FOR DEBUGGING PURPOSES ONLY
    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
    }

    //endregion

    //region ADD_NEW_WORKOUT

    public void addWorkout(){
        // TODO: DB debugging
        NewWorkoutDialog newWorkoutDialog = new NewWorkoutDialog();
        newWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onNewWorkoutDialogPositiveClick(String name){
        if(!TextUtils.isEmpty(name)){
            Workout newWorkout = new Workout(NEXT_WORKOUT_ID++, name);
            newWorkout.addSet(new Set("My Workout Set", "Description of my Workout Set", 60000));
            mSharedPrefs.edit().putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID).apply();
            mWorkoutViewModel.insert(newWorkout);
            } else {
            // TODO: Display an error message saying that name must not be null
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onNewWorkoutDialogNegativeClick(){

    }


    //endregion

    //region INTERFACE_IMPLEMENTATIONS
    @Override
    public void onWorkoutClicked(int workoutID) {
        openWorkout(workoutID);
    }

    @Override
    public void deleteWorkout(Workout workout) {
        mWorkoutViewModel.remove(workout);
    }

    @Override
    public void onWorkoutLongClick(int workoutID) {
        openBottomDialog(workoutID);
    }

    //endregion




}
