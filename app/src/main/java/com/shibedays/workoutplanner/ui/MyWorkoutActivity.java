package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.services.TTSService;
import com.shibedays.workoutplanner.services.TimerService;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.dialogs.AddSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.Locale;
import java.util.Timer;


public class MyWorkoutActivity extends AppCompatActivity implements SetAdapter.SetAdapaterListener, AdapterView.OnItemSelectedListener, AddSetDialog.AddSetDialogListener, TimerFragment.OnFragmentInteractionListener, NumberPickerDialog.NumberPickerDialogListener {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MyWorkoutActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MyWorkoutActivity.";
    // Intent Types
    public static final int NORMAL_INTENT_TYPE = 0;
    public static final int NOTIF_INTENT_TYPE = 1;
    // Data Constants
    private int DATA_DOESNT_EXIST = -1;
    // Message Constants
    //endregion

    //region MESSAGES
    public static final int MSG_SAY_HELLO = 0;

    public static final int MSG_UPDATE_FRAGMENT_UI_SERVICE_RUNNING = 1;
    public static final int MSG_PASS_TTS_MSG = 2;
    public static final int MSG_UPDATE_TIME_DISPLAY = 3;
    public static final int MSG_NEXT_REP_UI = 4;
    public static final int MSG_NEXT_ROUND_UI = 5;
    public static final int MSG_NEXT_SET_TIME = 6;
    public static final int MSG_GET_FIRST_SET =  7;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_INTENT_TYPE = PACKAGE + "INTENT_TYPE";

    public static final String EXTRA_WORKOUT_ID = PACKAGE + "WORKOUT_ID";
    public static final String EXTRA_WORKOUT_JSON = PACKAGE + "WORKOUT_JSON";

    public static final String EXTRA_NOTIF_BUNDLE = PACKAGE + "INTENT_BUNDLE";

    public static final String EXTRA_UPDATE_DESCRIPTION = PACKAGE + "UPDATE_DESCRIP";

    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;
    private Spinner mRoundSpinner;
    private ArrayAdapter<CharSequence> mArrayAdapter;
    private TextView mRestTime;
    private TextView mBreakTime;
    // Adapters
    private SetAdapter mSetAdapter;
    // Data
    private Workout mWorkoutData;
    private LiveData<Workout> mWorkoutLiveData;
    // Instances
    private FragmentManager mFragmentManager;
    private TimerFragment mTimerFragment;

    private Messenger mTimerService;
    private Messenger mTTSService;
    // View Model
    private WorkoutViewModel mViewModel;
    // Booleans
    private boolean mTimerIsBound;
    private boolean mTTSIsBound;

    //endregion

    //region PUBLIC_VARS

    //endregion

    //region MESSAGE_HANDLING

    class IncomingTTSMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "TTS Ready", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    final Messenger mIncomingTTSMessenger = new Messenger(new IncomingTTSMessageHandler());

    class IncomingTimerMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_UPDATE_FRAGMENT_UI_SERVICE_RUNNING:
                    mTimerFragment.updateServiceText("Service is Running");
                    break;
                case MSG_PASS_TTS_MSG:
                    if(msg.arg1 > 0) {
                        sendTTSMessage(msg.arg1);
                    }
                    break;
                case MSG_UPDATE_TIME_DISPLAY:
                    if(mTimerFragment != null){
                        mTimerFragment.updateTime(msg.arg1);
                    } else {
                        Log.e(DEBUG_TAG, "mTimerFragment is NULL in MSG_UPDATE_TIME_DISPLAY");
                    }
                    break;
                case MSG_NEXT_REP_UI:
                    if(msg.arg1 >= 0){
                        if(mTimerFragment != null) {
                            mTimerFragment.updateRep(msg.arg1);
                        } else {
                            Log.e(DEBUG_TAG, "mTimerFragment is NULL in MSG_NEXT_REP_UI");
                        }
                    }
                    break;
                case MSG_NEXT_ROUND_UI:
                    if(msg.arg1 >= 0){
                        if(mTimerFragment != null) {
                            mTimerFragment.updateRound(msg.arg1);
                        } else {
                            Log.e(DEBUG_TAG, "mTimerFragment is NULL in MSG_NEXT_ROUND_UI");
                        }
                    }
                    break;
                case MSG_NEXT_SET_TIME:
                    Set nextSet = mTimerFragment.nextSet();
                    if(nextSet != null) {
                        Message message = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, nextSet.getTime(), 0);
                        sendTimerMessage(message);
                    } else {
                        Log.e(DEBUG_TAG, "There is no next set");
                    }
                    break;
                case MSG_GET_FIRST_SET:
                    Set firstSet = mTimerFragment.firstSet();
                    if(firstSet != null){
                        Message message = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, firstSet.getTime(), 0);
                        sendTimerMessage(message);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    final Messenger mIncomingTimerMessenger = new Messenger(new IncomingTimerMessageHandler());
    //endregion

    //region LIFECYCLE
    @Override
    protected void onStart(){
        super.onStart();

        Intent TTSIntent = new Intent(this, TTSService.class);
        bindService(TTSIntent, mTTSConnection, Context.BIND_AUTO_CREATE);
        startService(TTSIntent);
    }

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
        mRestTime = findViewById(R.id.rest_time);
        mRestTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRestNumberPickerDialog();
            }
        });

        mBreakTime = findViewById(R.id.break_time);
        mBreakTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBreakNumberPickerDialog();
            }
        });

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
        int dragDirs = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
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
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                // Collections.swap(data, from, to);
                mSetAdapter.notifyItemMoved(from, to);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTSIsBound){
            unbindService(mTTSConnection);
            stopService(new Intent(this, TTSService.class));
            mTTSIsBound = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    //endregion

    //region ACTION_BAR_MENU
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
            openAddNewSetDialog();
        } else if (id == android.R.id.home){
            if(mFragmentManager.getBackStackEntryCount() > 0){
                mFragmentManager.popBackStack();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

        int[] restTime = MainActivity.convertFromMillis( mWorkoutData.getTimeBetweenSets() );
        int restMin = restTime[0], restSec = restTime[1];
        updateRestTimeUI(restMin, restSec);

        int[] breakTime = MainActivity.convertFromMillis( mWorkoutData.getTimeBetweenRounds() );
        int breakMin = breakTime[0], breakSec = breakTime[1];
        updateBreakTimeUI(breakMin, breakSec);
    }

    private void openAddNewSetDialog(){
        Log.d(DEBUG_TAG, "Adding a new set");
        AddSetDialog addSetDialog = new AddSetDialog();
        addSetDialog.show(mFragmentManager, DEBUG_TAG);
    }

    private void openRestNumberPickerDialog(){
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt(NumberPickerDialog.EXTRA_DIALOG_TYPE, NumberPickerDialog.REST_TYPE);
        numberPickerDialog.setArguments(args);
        numberPickerDialog.show(mFragmentManager, DEBUG_TAG);
    }

    private void openBreakNumberPickerDialog(){
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt(NumberPickerDialog.EXTRA_DIALOG_TYPE, NumberPickerDialog.BREAK_TYPE);
        numberPickerDialog.setArguments(args);
        numberPickerDialog.show(mFragmentManager, DEBUG_TAG);
    }

    //endregion

    private void updateRestTimeUI(int min, int sec){
        if((sec % 10) == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if (min == 0 && sec == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateBreakTimeUI(int min, int sec){
        if((sec % 10) == 0){
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ){
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if (min == 0 && sec == 0){
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    //region INTERFACE_IMPLEMENTATIONS

    // Spinner Listeners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int numRounds = Integer.parseInt(parent.getItemAtPosition(position).toString().trim());
        mWorkoutData.setNumOfRounds(numRounds);
        mViewModel.update(mWorkoutData);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Add Set Dialog Listeners
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

    // Delete Set Listener (Adapter)
    @Override
    public void deleteSet(Set set) {
        //mWorkoutData.removeSet(set);
        mViewModel.update(mWorkoutData);
    }

    // Fragment callback function
    @Override
    public void closeFragmentAndService() {
        if(mTimerIsBound){
            unbindService(mTimerConnection);
        }
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(mTimerFragment);
        fragmentTransaction.commit();
        mTimerFragment = null;
    }

    @Override
    public void stopTTSSpeech() {
        if(mTTSIsBound){
            Message msg = Message.obtain(null, TTSService.MSG_STOP_SPEECH);
            try {
                mTTSService.send(msg);
            } catch (RemoteException e){
                e.printStackTrace();
            }
        }
    }

    // Number Picker Dialog Listeners
    @Override
    public void setRestTime(int min, int sec) {
        int time = MainActivity.convertToMillis(min, sec);
        updateRestTimeUI(min, sec);
        mWorkoutData.setTimeBetweenSets(time);
        mViewModel.update(mWorkoutData);
    }

    @Override
    public void setBreakTime(int min, int sec) {
        int time = MainActivity.convertToMillis(min, sec);
        mWorkoutData.setTimeBetweenRounds(time);
        updateBreakTimeUI(min, sec);
        mViewModel.update(mWorkoutData);
    }

    //endregion

    //region TIMER_INTERACTIONS
    // UI Interaction and fragment creation
    public void startTimer(View view){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mTimerFragment = TimerFragment.newInstance(mWorkoutData.toJSON());
        fragmentTransaction.replace(R.id.fragment_container, mTimerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        Log.d(DEBUG_TAG, "Timer Fragment Created");
        bindService(new Intent(this, TimerService.class), mTimerConnection, Context.BIND_AUTO_CREATE);

    }

    private void beginTimerService(){
        Intent timerIntent = new Intent(this, TimerService.class);
        Bundle notifBundle = new Bundle();
        // TODO: Put items that are needed to rebuild the activity and fragment into this bundle

        // Build the bundle that the notification will use to restart everything
        notifBundle.putString(EXTRA_WORKOUT_JSON, mWorkoutData.toJSON());
        notifBundle.putInt(EXTRA_WORKOUT_ID, mWorkoutData.getWorkoutID());

        timerIntent.putExtra(EXTRA_NOTIF_BUNDLE, notifBundle);
        timerIntent.putExtra(TimerService.EXTRA_SET_TIME, mTimerFragment.getCurSetTime());
        timerIntent.putExtra(TimerService.EXTRA_REST_TIME, mWorkoutData.getTimeBetweenSets());
        timerIntent.putExtra(TimerService.EXTRA_BREAK_TIME, mWorkoutData.getTimeBetweenRounds());
        timerIntent.putExtra(TimerService.EXTRA_NUM_REPS, mWorkoutData.getNumOfSets());
        timerIntent.putExtra(TimerService.EXTRA_NUM_ROUNDS, mWorkoutData.getNumOfRounds());

        startService(timerIntent);
    }

    private void stopTimerService(){
        stopService(new Intent(this, TTSService.class));
    }
    //endregion

    //region TTS_Outgoing_Messaging
    public void sendTTSMessage(int string_id){
        if(mTTSIsBound) {
            if (string_id > 0) {
                Message msg = Message.obtain(null, TTSService.MSG_SPEAK, string_id, 0);
                try {
                    mTTSService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(DEBUG_TAG, "Invalid String ID in sendTTSMessage");
            }
        } else {
            Log.e(DEBUG_TAG, "TTS is unbound but trying to send a message");
        }
    }
    //endregion

    //region TIMER_Outgoing_Messaging
    public void sendTimerMessage(Message msg){
        if(mTimerIsBound) {
            if(msg != null){
                try{
                    mTimerService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(DEBUG_TAG, "Timer is unbound");
        }
    }
    //endregion

    //region SERVICE_CONNECTIONS
    private ServiceConnection mTimerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mTimerService = new Messenger(service);
            mTimerIsBound = true;

            Message msg = Message.obtain(null, TimerService.MSG_TIMER_BIND);
            msg.replyTo = mIncomingTimerMessenger;

            try {
                mTimerService.send(msg);
            }catch (RemoteException e){
                e.printStackTrace();
            }

            beginTimerService();

            Log.d(DEBUG_TAG, "Timer running and bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTimerService = null;
            mTimerIsBound = false;
            stopTimerService();
        }
    };

    private ServiceConnection mTTSConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mTTSService = new Messenger(service);
            mTTSIsBound = true;

            Message msg = Message.obtain(null, TTSService.MSG_TTS_BIND);
            msg.replyTo = mIncomingTTSMessenger;
            try {
                mTTSService.send(msg);
            } catch (RemoteException e){
                e.printStackTrace();
            }
            Log.d(DEBUG_TAG, "TTS Running and bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTTSService = null;
            mTTSIsBound = false;
        }
    };
    //endregion

}