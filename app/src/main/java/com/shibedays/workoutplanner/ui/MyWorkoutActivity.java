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

import com.shibedays.workoutplanner.ListItemTouchHelper;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.services.TTSService;
import com.shibedays.workoutplanner.services.TimerService;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.ui.dialogs.NumberRoundsDialog;
import com.shibedays.workoutplanner.ui.dialogs.SetBottomSheetDialog;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.List;
import java.util.Locale;


public class MyWorkoutActivity extends AppCompatActivity implements SetAdapter.SetAdapaterListener, AddEditSetDialog.AddSetDialogListener, TimerFragment.OnFragmentInteractionListener,
                                                                    NumberPickerDialog.NumberPickerDialogListener, SetBottomSheetDialog.SetBottomSheetDialogListener,
                                                                    NumberRoundsDialog.NumberRoundsListener{

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
    private TextView mRestTime;
    private TextView mBreakTime;
    private TextView mNumRounds;
    // Adapters
    private SetAdapter mSetAdapter;
    // Data
    private Workout mWorkoutData;
    private List<Set> mSetList;
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
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_START");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_workout);
        mFragmentManager = getSupportFragmentManager();

        //TODO: in onCreate, we need to check if: TTS Service already Exists, Fragment Already Exists, TimerService already exists
        // if any of the above already exist, most likely means we are returning from the notification and/or need to restore the activity
        // from some previous state

        //region INSTANCE_STATE
        if(savedInstanceState != null){
            TimerFragment tg = (TimerFragment) mFragmentManager.findFragmentById(R.id.fragment_container);
        }
        //endregion

        //region TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region TTS_BINDING
        Intent TTSIntent = new Intent(this, TTSService.class);
        bindService(TTSIntent, mTTSConnection, Context.BIND_AUTO_CREATE);
        startService(TTSIntent);
        //endregion

        //region VIEW_MODEL
        mViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        //endregion

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
                            mSetList = mWorkoutData.getSetList();
                            mSetAdapter.setData(mSetList);
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

        mNumRounds = findViewById(R.id.number_rounds);
        mNumRounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberRoundsDialog();
            }
        });
        //endregion

        //region RECYCLER_VIEW
        mRecyclerView = findViewById(R.id.set_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSetAdapter = new SetAdapter(this, findViewById(R.id.set_coord_layout));
        mRecyclerView.setAdapter(mSetAdapter);
        mSetAdapter.notifyDataSetChanged();

        int dragDirs = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        ListItemTouchHelper listHelper =
                new ListItemTouchHelper(this, true, dragDirs, true, swipeDirs, mSetAdapter);

        listHelper.getHelper().attachToRecyclerView(mRecyclerView);

        //endregion

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_PAUSE");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_STOP");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTSIsBound){
            unbindService(mTTSConnection);
            Log.d(DEBUG_TAG, "TTS Shutdown ON DESTROY");
            Toast.makeText(getApplicationContext(), "TTS Shutoff ON DESTROY", Toast.LENGTH_SHORT).show();
            stopService(new Intent(this, TTSService.class));
            mTTSIsBound = false;
        }
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_DESTROY");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO: What do we gotta save?
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY SAVING INSTANCE STATE");

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
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_PARENT, SettingsActivity.MY_WORKOUT_ACTIVITY);
            startActivity(intent);
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
        updateRoundNumUI(numRounds);

        int[] restTime = MainActivity.convertFromMillis( mWorkoutData.getTimeBetweenSets() );
        int restMin = restTime[0], restSec = restTime[1];
        updateRestTimeUI(restMin, restSec, mWorkoutData.getNoRestFlag());

        int[] breakTime = MainActivity.convertFromMillis( mWorkoutData.getTimeBetweenRounds() );
        int breakMin = breakTime[0], breakSec = breakTime[1];
        updateBreakTimeUI(breakMin, breakSec, mWorkoutData.getNoBreakFlag());
    }

    private void swapSets(int from, int to){
        mWorkoutData.swapSets(from, to);
    }

    //endregion

    //region UI_UPDATES
    private void updateRestTimeUI(int min, int sec, boolean flag){
        if(flag){
            mRestTime.setText("None");
        }else if((sec % 10) == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if (min == 0 && sec == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateBreakTimeUI(int min, int sec, boolean flag){
        if(flag){
            mBreakTime.setText("None");
        } else if((sec % 10) == 0){
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if ( sec < 10 ){
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if (min == 0 && sec == 0){
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateRoundNumUI(int num){
        mNumRounds.setText(String.format(Locale.US, " %d ", num));
    }

    //endregion

    //region INTERFACE_IMPLEMENTATIONS

        //region ADD_SET

    private void openAddNewSetDialog(){
        AddEditSetDialog addEditSetDialog = new AddEditSetDialog();
        Bundle args = new Bundle();
        args.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.NEW_SET);
        addEditSetDialog.setArguments(args);
        addEditSetDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onAddSetDialogPositiveClick(String name, String descrip, int min, int sec) {
        Log.d(DEBUG_TAG, "Name: " + name + " Descrip: " + descrip + " Time: " + min + ":" + sec);
        // TODO: add description to the add new set
        Set newSet = new Set(name, descrip, MainActivity.convertToMillis(min, sec));
        mWorkoutData.addSet(newSet);
        mViewModel.update(mWorkoutData);
    }

        //endregion

        //region FRAGMENT_CLOSING
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
        //endregion

        //region NUMBER_PICKERS
    private void openRestNumberPickerDialog(){
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt(NumberPickerDialog.EXTRA_DIALOG_TYPE, NumberPickerDialog.REST_TYPE);
        args.putInt(NumberPickerDialog.EXTRA_GIVEN_TIME, mWorkoutData.getTimeBetweenSets());
        args.putBoolean(NumberPickerDialog.EXTRA_NO_FLAG, mWorkoutData.getNoRestFlag());
        numberPickerDialog.setArguments(args);
        numberPickerDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void setRestTime(int min, int sec, boolean noFlag) {
        int time = MainActivity.convertToMillis(min, sec);
        updateRestTimeUI(min, sec, noFlag);
        mWorkoutData.setTimeBetweenSets(time);
        mWorkoutData.setNoRestFlag(noFlag);
        mViewModel.update(mWorkoutData);
    }

    private void openBreakNumberPickerDialog(){
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt(NumberPickerDialog.EXTRA_DIALOG_TYPE, NumberPickerDialog.BREAK_TYPE);
        args.putInt(NumberPickerDialog.EXTRA_GIVEN_TIME, mWorkoutData.getTimeBetweenRounds());
        args.putBoolean(NumberPickerDialog.EXTRA_NO_FLAG, mWorkoutData.getNoBreakFlag());
        numberPickerDialog.setArguments(args);
        numberPickerDialog.show(mFragmentManager, DEBUG_TAG);
    }
    @Override
    public void setBreakTime(int min, int sec, boolean noFlag) {
        int time = MainActivity.convertToMillis(min, sec);
        updateBreakTimeUI(min, sec, noFlag);
        mWorkoutData.setTimeBetweenRounds(time);
        mWorkoutData.setNoBreakFlag(noFlag);
        mViewModel.update(mWorkoutData);
    }

        //endregion

        //region NUMBER_ROUND_DIALOG
    private void openNumberRoundsDialog(){
        NumberRoundsDialog numberRoundsDialog = new NumberRoundsDialog();
        Bundle args = new Bundle();
        args.putInt(NumberRoundsDialog.EXTRA_NUM_ROUNDS, mWorkoutData.getNumOfRounds());
        numberRoundsDialog.setArguments(args);
        numberRoundsDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void setNumberRounds(int num) {
        mWorkoutData.setNumOfRounds(num);
        mViewModel.update(mWorkoutData);
    }
        //endregion

        //region BOTTOM_SHEET
    @Override
    public void onSetClick(int setIndex) {
        openBottomDialog(setIndex);
    }
    public void openBottomDialog(int setIndex){
        Bundle bundle = new Bundle();
        bundle.putInt(SetBottomSheetDialog.EXTRA_SET_INDEX, setIndex);
        bundle.putString(SetBottomSheetDialog.EXTRA_SET_NAME, mSetList.get(setIndex).getName());
        SetBottomSheetDialog setBottomSheetDialog = new SetBottomSheetDialog();
        setBottomSheetDialog.setArguments(bundle);
        setBottomSheetDialog.show(mFragmentManager, setBottomSheetDialog.getTag());
    }

            //region EDIT
    @Override
    public void editItem(int index) {
        editSet(index);
    }

    private void editSet(int setIndex){
        AddEditSetDialog editSetDialog = new AddEditSetDialog();
        Bundle args = new Bundle();
        Set curSet = mSetList.get(setIndex);
        args.putInt(AddEditSetDialog.EXTRA_DIALOG_TYPE, AddEditSetDialog.EDIT_SET);
        args.putInt(AddEditSetDialog.EXTRA_SET_INDEX, setIndex);
        args.putString(AddEditSetDialog.EXTRA_SET_NAME, curSet.getName());
        args.putString(AddEditSetDialog.EXTRA_SET_DESCIP, curSet.getDescrip());
        int[] time = MainActivity.convertFromMillis(curSet.getTime());
        args.putInt(AddEditSetDialog.EXTRA_SET_MIN, time[0]);
        args.putInt(AddEditSetDialog.EXTRA_SET_SEC, time[1]);
        editSetDialog.setArguments(args);
        editSetDialog.show(mFragmentManager, DEBUG_TAG);
    }

    @Override
    public void onEditSetDialogPositiveClick(int index, String name, String descrip, int min, int sec) {
        Set editSet = mSetList.get(index);
        editSet.setName(name);
        editSet.setDescrip(descrip);
        editSet.setTime(MainActivity.convertToMillis(min, sec));
        mWorkoutData.updateSet(editSet, index);
        mViewModel.update(mWorkoutData);
    }
            //endregion

            //region DELETE
    @Override
    public void deleteItem(int index) {
        SetAdapter adapter = (SetAdapter) mRecyclerView.getAdapter();
        adapter.pendingRemoval(index);
    }

    @Override
    public void deleteSet(Set set) {
        mViewModel.update(mWorkoutData);
    }

            //endregion

        //endregion

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
        timerIntent.putExtra(TimerService.EXTRA_NO_REST_FLAG, mWorkoutData.getNoRestFlag());
        timerIntent.putExtra(TimerService.EXTRA_NO_BREAK_FLAG, mWorkoutData.getNoBreakFlag());

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
            Log.e(DEBUG_TAG, "TTS is not bound but trying to send a message");
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
            Log.e(DEBUG_TAG, "Timer is not bound");
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTTSService = null;
            mTTSIsBound = false;
        }
    };
    //endregion

}