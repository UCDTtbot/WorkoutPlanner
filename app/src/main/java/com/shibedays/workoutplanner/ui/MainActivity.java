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
import android.preference.PreferenceManager;
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
import com.shibedays.workoutplanner.ui.dialogs.AddEditWorkoutDialog;
import com.shibedays.workoutplanner.ui.dialogs.WorkoutBottomSheetDialog;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.lang.reflect.Method;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AddEditWorkoutDialog.WorkoutDialogListener, WorkoutAdapter.WorkoutAdapterListener, WorkoutBottomSheetDialog.WorkoutBottomSheetDialogListener {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MainActivity.";
    private static final String PREF_IDENTIFIER = PACKAGE + "SHARED_PREFS";
    //endregion

    //region INTENT_KEYS
    //EXTRA_FOO
    //endregion

    //region PREF_KEYS
    //KEY_FOO
    private static final String KEY_VERSION_CODE = PACKAGE + "VersionCode";
    private static final String KEY_NEXT_WORKOUT_NUM = PACKAGE + "NextWorkoutNum";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;

    // Data
    private List<Workout> mWorkoutData;

    // Adapters
    private WorkoutAdapter mWorkoutAdapter;

    // Instances
    private SharedPreferences mPrivateSharedPrefs;
    private SharedPreferences mDefaultSharedPrefs;
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
        mPrivateSharedPrefs = getSharedPreferences(PREF_IDENTIFIER, MODE_PRIVATE);
        mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if(mPrivateSharedPrefs != null){
            int savedVersionCode = mPrivateSharedPrefs.getInt(KEY_VERSION_CODE, DATA_DOESNT_EXIST);
            if(savedVersionCode == currentVersionCode){
                // Normal Run
                NEXT_WORKOUT_ID = mPrivateSharedPrefs.getInt(KEY_NEXT_WORKOUT_NUM, -DATA_DOESNT_EXIST);
                if(NEXT_WORKOUT_ID == DATA_DOESNT_EXIST){
                    Log.e(DEBUG_TAG, "NEXT WORKOUT NUM DATA DOESN'T EXIST");
                }
            }else if (savedVersionCode == DATA_DOESNT_EXIST){
                // First run
                NEXT_WORKOUT_ID = 2;
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
                editor.putInt(KEY_VERSION_CODE, currentVersionCode);
                editor.putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID);
                editor.apply();
            }else if (savedVersionCode < currentVersionCode){
                // Updated run
                SharedPreferences.Editor editor = mPrivateSharedPrefs.edit();
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
                deleteICMargin = (int) getResources().getDimension(R.dimen.standard_icon_touchable_padding);
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
                mWorkoutAdapter.setData(workouts);
                mWorkoutData = workouts;
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
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_PARENT, SettingsActivity.MAIN_ACTVITIY);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    //region INTERFACE_IMPLEMENTATIONS

        //region NEW_WORKOUT
    public void addWorkout(){
        AddEditWorkoutDialog addWorkoutDialog = new AddEditWorkoutDialog();
        Bundle args = new Bundle();
        args.putInt(AddEditWorkoutDialog.EXTRA_DIALOG_TYPE, AddEditWorkoutDialog.NEW_WORKOUT);
        addWorkoutDialog.setArguments(args);
        addWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onNewWorkoutDialogPositiveClick(String name){
        if(!TextUtils.isEmpty(name)){
            Workout newWorkout = new Workout(NEXT_WORKOUT_ID++, name);
            newWorkout.addSet(new Set("My Workout Set", "Description of my Workout Set", 60000));
            mPrivateSharedPrefs.edit().putInt(KEY_NEXT_WORKOUT_NUM, NEXT_WORKOUT_ID).apply();
            mWorkoutViewModel.insert(newWorkout);
        } else {
            // TODO: Display an error message saying that name must not be null
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_LONG).show();
        }

    }
        //endregion

        //region OPEN_WORKOUT
    @Override
    public void onWorkoutClicked(int workoutID) {
        openWorkout(workoutID);
    }

    public void openWorkout(int workoutID){
        Intent intent = new Intent(this, MyWorkoutActivity.class);
        intent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, workoutID);
        intent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NORMAL_INTENT_TYPE);
        startActivity(intent);
    }
        //endregion

        //region BOTTOM_SHEET
    @Override
    public void onWorkoutLongClick(int workoutIndex, int workoutID) {
        openBottomDialog(workoutIndex, workoutID);
    }

    public void openBottomDialog(int workoutIndex, int workoutID){
        Bundle bundle = new Bundle();
        bundle.putInt(WorkoutBottomSheetDialog.EXTRA_WORKOUT_ID, workoutID);
        bundle.putInt(WorkoutBottomSheetDialog.EXTRA_WORKOUT_INDEX, workoutIndex);
        WorkoutBottomSheetDialog workoutBottomSheetDialog = new WorkoutBottomSheetDialog();
        workoutBottomSheetDialog.setArguments(bundle);
        workoutBottomSheetDialog.show(mFragmentManager, workoutBottomSheetDialog.getTag());
    }

            //region EDIT_WORKOUT
    @Override
    public void editItem(int index) {
        editWorkout(index);
    }

    public void editWorkout(int workoutIndex){
        AddEditWorkoutDialog editWorkoutDialog = new AddEditWorkoutDialog();
        Bundle args = new Bundle();
        args.putInt(AddEditWorkoutDialog.EXTRA_DIALOG_TYPE, AddEditWorkoutDialog.EDIT_WORKOUT);
        args.putString(AddEditWorkoutDialog.EXTRA_WORKOUT_NAME, mWorkoutData.get(workoutIndex).getName());
        args.putInt(AddEditWorkoutDialog.EXTRA_WORKOUT_INDEX, workoutIndex);
        editWorkoutDialog.setArguments(args);
        editWorkoutDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onEditWorkoutDialogPositiveClick(String name, int index) {
        if(!TextUtils.isEmpty(name)){
            mWorkoutData.get(index).setName(name);
            mWorkoutViewModel.update(mWorkoutData.get(index));
        } else {
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_SHORT).show();
        }
    }
            //endregion

            //region DELETE_WORKOUT
    @Override
    public void deleteItem(int index) {
        WorkoutAdapter adapter = (WorkoutAdapter) mRecyclerView.getAdapter();
        adapter.pendingRemoval(index);
        // Snackbar is creating in pendingRemoval
    }


            // Deleting workout
    @Override
    public void deleteWorkout(Workout workout) {
        mWorkoutViewModel.remove(workout);
    }
            //endregion

        //endregion

    //endregion




}
