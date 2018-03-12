package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;


public class MyWorkoutActivity extends AppCompatActivity implements SetAdapter.SetAdapaterListener, AdapterView.OnItemSelectedListener, AddSetDialog.AddSetDialogListener, TimerFragment.OnFragmentInteractionListener {

    // Static constants
    private static final String DEBUG_TAG = MyWorkoutActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MyWorkoutActivity.";
    public static final String FILTER = PACKAGE + "FILTER";
    public static final int NORMAL_INTENT_TYPE = 0;
    public static final int NOTIF_INTENT_TYPE = 1;

    //region INTENT_KEYS
    public static final String EXTRA_INTENT_TYPE = PACKAGE + "INTENT_TYPE";
    public static final String EXTRA_WORKOUT_ID = PACKAGE + "WORKOUT_ID";

    public static final String EXTRA_WORKOUT_JSON = PACKAGE + "WORKOUT_JSON";
    public static final String EXTRA_INTENT_BUNDLE = PACKAGE + "INTENT_BUNDLE";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;
    private Spinner mRoundSpinner;
    private ArrayAdapter<CharSequence> mArrayAdapter;
    // Adapters
    private SetAdapter mSetAdapter;
    // Data
    private Workout mWorkoutData;
    private LiveData<Workout> mWorkoutLiveData;
    // Instances
    private FragmentManager mFragmentManager;
    // View Model
    private WorkoutViewModel mViewModel;
    // Data Constants
    private int DATA_DOESNT_EXIST = -1;
    //endregion

    //region PUBLIC_VARS

    //endregion

    private TimerFragment mTimerFragment;
    private TimerService mTimerService;
    private TTSService mTTSService;
    private boolean mTimerIsBound;
    private boolean mTTSIsBound;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String filter = intent.getAction();
            if(filter != null){
                if(filter.equals(FILTER)){
                    if(intent.getExtras() != null){
                        if(mTimerFragment != null){
                            mTimerFragment.updateDescription(Integer.toString(intent.getIntExtra(EXTRA_WORKOUT_ID, -1)));
                        }
                    }
                    Log.d(DEBUG_TAG, "Broadcast is working");
                }
            }
        }
    };


    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workout);

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        mFragmentManager = getSupportFragmentManager();

        if(savedInstanceState != null){
            TimerFragment tg = (TimerFragment) mFragmentManager.findFragmentById(R.id.fragment_container);
        }

        //region VIEW_MODEL
        mViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        //endregion

        //TODO: in onCreate, we need to check if: TTS Service already Exists, Fragment Already Exists, TimerService already exists
        // if any of the above already exist, most likely means we are returning from the notification and/or need to restore the activity
        // from some previous state
        //region INTENT
        Intent intent = getIntent();
        if(intent != null){
            int mIntentType = intent.getIntExtra(EXTRA_INTENT_TYPE, -1);
            if(mIntentType == NORMAL_INTENT_TYPE) {
                // Normal running circumstances
                int id = intent.getIntExtra(EXTRA_WORKOUT_ID, -1);
                mWorkoutLiveData = mViewModel.getWorkout(id);
                mWorkoutLiveData.observe(this, new Observer<Workout>() {
                    @Override
                    public void onChanged(@Nullable Workout workout) {
                        if (workout != null) {
                            mWorkoutData = workout;
                            mSetAdapter.setData(mWorkoutData.getSetList());
                            dataUpdate();

                        } else {
                            Log.e(DEBUG_TAG, "Workout not found");
                        }

                        }
                });
            } else if (mIntentType == NOTIF_INTENT_TYPE){
                // This is what happens if we're opening this activity from the notification
            } else {
                Log.e(DEBUG_TAG, "EXTRA_INTENT_TYPE was not set");
            }
        } else {
            Log.e(DEBUG_TAG, "Intent was empty. onCreate MyWorkoutActivity");
        }
        //endregion

        //region UI

        //endregion

        //region RECYCLER_VIEW
        mRecyclerView = findViewById(R.id.set_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSetAdapter = new SetAdapter(this, findViewById(R.id.set_coord_layout));
        mRecyclerView.setAdapter(mSetAdapter);
        mSetAdapter.notifyDataSetChanged();

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

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
                SetAdapter adapter = (SetAdapter) recyclerView.getAdapter();
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
                SetAdapter adapter = (SetAdapter) mRecyclerView.getAdapter();
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
                //if dX > 0, swiping right
                //if dX < 0 swiping left
                if(dX < 0) {
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

        //region SPINNER
        mRoundSpinner = findViewById(R.id.round_spinner);
        mRoundSpinner.setOnItemSelectedListener(this);

        mArrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRoundSpinner.setAdapter(mArrayAdapter);
        mRoundSpinner.setSelection(0);
        //endregion

        Intent TTSIntent = new Intent(this, TTSService.class);
        bindService(TTSIntent, mTTSConnection, Context.BIND_AUTO_CREATE);
        startService(TTSIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimerIsBound){
            unbindService(mTimerConnection);
            stopService(new Intent(this, TTSService.class));
            mTimerIsBound = false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_set).setVisible(true);
        menu.findItem(R.id.action_settings).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings){
            return true;
        } else if(id == R.id.add_set) {
            addNewSet();
        } else if (id == android.R.id.home){
            if(mFragmentManager.getBackStackEntryCount() > 0){
                mFragmentManager.popBackStack();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    //endregion

    //region UTILITY
    private void dataUpdate(){
        if(!getTitle().equals(mWorkoutData.getName())){
            setTitle(mWorkoutData.getName());
        }
        int numRounds = mWorkoutData.getNumOfRounds();
        int pos = mArrayAdapter.getPosition("\u0020" + Integer.toString(numRounds) + "\u0020");
        mRoundSpinner.setSelection(pos);
    }

    private void addNewSet(){
        Log.d(DEBUG_TAG, "Adding a new set");
        AddSetDialog addSetDialog = new AddSetDialog();
        addSetDialog.show(mFragmentManager, DEBUG_TAG);
    }


    //endregion

    //region INTERFACE_IMPLEMENTATIONS
    @Override
    public void deleteSet(Set set) {
        //mWorkoutData.removeSet(set);
        mViewModel.update(mWorkoutData);
    }

    //region SPINNER
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int numRounds = Integer.parseInt(parent.getItemAtPosition(position).toString().trim());
        mWorkoutData.setNumOfRounds(numRounds);
        mViewModel.update(mWorkoutData);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //endregion

    @Override
    public void onAddSetDialogPositiveClick(String name, String descrip, int min, int sec) {
        Log.d(DEBUG_TAG, "Name: " + name + " Descrip: " + descrip + " Time: " + min + ":" + sec);
        // TODO: add description to the add new set
        Set newSet = new Set(name, descrip, MainActivity.convertToMillis(min, sec));
        mWorkoutData.addSet(newSet);
        mViewModel.update(mWorkoutData);
    }

    @Override
    public void onAddSetDialogNegativeClick() {

    }
    //endregion

    //region TIMER_INTERACTIONS
    public void startTimer(View view){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mTimerFragment = TimerFragment.newInstance(mWorkoutData.toJSON());
        fragmentTransaction.replace(R.id.fragment_container, mTimerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        Log.d(DEBUG_TAG, "Timer Fragment Created");
        beginTimerService();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void beginTimerService(){
        // Bind to TimerService
        Intent timerIntent = new Intent(this, TimerService.class);
        Bundle notifBundle = new Bundle();
        // TODO: Put items that are needed to rebuild the activity and fragment into this bundle
        notifBundle.putString(EXTRA_WORKOUT_JSON, mWorkoutData.toJSON());
        notifBundle.putInt(EXTRA_WORKOUT_ID, mWorkoutData.getWorkoutID());
        timerIntent.putExtra(EXTRA_INTENT_BUNDLE, notifBundle);
        bindService(timerIntent, mTimerConnection, Context.BIND_AUTO_CREATE);
        startService(timerIntent);
    }

    private ServiceConnection mTimerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) iBinder;
            mTimerService = binder.getService();
            mTimerIsBound = true;
            Log.d(DEBUG_TAG, "Timer running and bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTimerIsBound = false;
        }
    };

    private ServiceConnection mTTSConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TTSService.TTSBinder binder = (TTSService.TTSBinder) iBinder;
            mTTSService = binder.getService();
            mTTSIsBound = true;
            Log.d(DEBUG_TAG, "TTS Running and bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    //endregion
}



        /*int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int adjustedHeight = (int)(screenHeight * 0.60);
        mRecyclerView.setMinimumHeight( screenHeight );
        */
