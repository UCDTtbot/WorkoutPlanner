package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.ui.adapters.ViewPagerAdapter;
import com.shibedays.workoutplanner.ui.dialogs.RenameWorkoutDialog;
import com.shibedays.workoutplanner.ui.dialogs.ReorderSetsDialog;
import com.shibedays.workoutplanner.ui.fragments.AddSetsFragment;
import com.shibedays.workoutplanner.ui.fragments.SetInfoFragment;
import com.shibedays.workoutplanner.ui.fragments.TimerFragment;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.services.TTSService;
import com.shibedays.workoutplanner.services.TimerService;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.activities.MyWorkoutViewModel;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MyWorkoutActivity extends AppCompatActivity implements TimerFragment.OnFragmentInteractionListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MyWorkoutActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MyWorkoutActivity.";
    // Intent Types
    public static final int NORMAL_INTENT_TYPE = 0;
    public static final int NOTIF_INTENT_TYPE = 3;

    public static final int PAUSE_INTENT_TYPE = 5;
    public static final int PLAY_INTENT_TYPE = 10;
    //endregion

    //region MESSAGES
    public static final int MSG_UPDATE_SET_INFO = 1;
    public static final int MSG_PASS_TTS_MSG = 2;
    public static final int MSG_UPDATE_TIME_DISPLAY = 3;
    public static final int MSG_NO_REST_NEXT_SET = 4;
    public static final int MSG_NO_BREAK_NEXT_ROUND = 5;
    public static final int MSG_NEXT_ROUND = 6;
    public static final int MSG_NEXT_REP = 7;
    public static final int MSG_LOAD_NEXT_SET = 8;
    public static final int MSG_LOAD_FIRST_SET = 9;
    public static final int MSG_STOP_TIMER = 10;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_INTENT_TYPE = PACKAGE + "INTENT_TYPE";

    public static final String EXTRA_WORKOUT_ID = PACKAGE + "WORKOUT_ID";
    public static final String EXTRA_WORKOUT_TYPE = PACKAGE + "Type";

    public static final String EXTRA_UPDATE_DESCRIPTION = PACKAGE + "UPDATE_DESCRIP";

    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;
    private TextView mRestTime;
    private TextView mBreakTime;
    private EditText mNumRounds;
    private TextView mTotalTime;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Button mStartButton;
    // Adapters
    //private SetAdapter mSetAdapter;
    // Data
    private int mType;
    // Instances
    private FragmentManager mFragmentManager;
    private TimerFragment mTimerFragment;
    private AddSetsFragment mAddSetsFragment;
    private List<SetInfoFragment> mSetInfoFrags;
    private ViewPagerAdapter mViewPagerAdapter;

    private Messenger mTimerService;
    private Messenger mTTSService;
    // View Model

    private WorkoutViewModel mWorkoutViewModel;
    private MyWorkoutViewModel mMainVM;
    // Booleans
    private boolean mTimerIsBound;
    private boolean mTTSIsBound;
    private boolean mIsTTSMuted;
    private boolean HIDE_ITEMS;

    private boolean mIsInBackground;
    private boolean mPendingTimerClose;

    private List<Message> mMsgQueue;

    private Intent mIntentBundle;

    private static boolean mTimerFinished;

    private BroadcastReceiver mNotifReceiver;
    public static final String FILTER_TIMER = PACKAGE + "TIMER_NOTIF";
    //endregion

    //region MESSAGE_HANDLING

    class IncomingTTSMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    }
    final Messenger mIncomingTTSMessenger = new Messenger(new IncomingTTSMessageHandler());

    public class IncomingTimerMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Set s = null;
            Message m = null;
            switch (msg.what){
                case MSG_PASS_TTS_MSG:
                    if(msg.arg1 > 0) {
                        if(msg.obj instanceof String)
                        sendTTSMessage(msg.arg1, (String) msg.obj);
                    }
                    break;

                case MSG_UPDATE_SET_INFO:
                    if(mTimerFragment != null){
                        mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), mTimerFragment.getNextSet(), false, false);
                    } else {
                        throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_UPDATE_TIME_DISPLAY");
                    }
                    break;

                case MSG_LOAD_NEXT_SET:
                    s = mTimerFragment.getNextSet();

                    mTimerFragment.loadNextSet();
                    mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), s, true, false);
                    m = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, s.getTime(), s.getSetImageId(), s.getName());
                    sendTimerMessage(m);
                    break;

                case MSG_LOAD_FIRST_SET:
                    s = mTimerFragment.getNextSet();

                    mTimerFragment.loadNextSet();
                    mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), s, false, true);
                    m = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, s.getTime(), s.getSetImageId(), s.getName());
                    sendTimerMessage(m);
                    break;

                case MSG_NO_REST_NEXT_SET:
                    s = mTimerFragment.getNextSet();
                    m = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, s.getTime(), s.getSetImageId(), s.getName());
                    sendTimerMessage(m);
                    mTimerFragment.loadNextSet();
                    break;
                case MSG_NO_BREAK_NEXT_ROUND:
                    int rnd = msg.arg1;
                    mTimerFragment.updateRep(0);
                    mTimerFragment.updateRound(rnd);
                    mTimerFragment.loadNextSet();
                    mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), mTimerFragment.getNextSet(), false, false);
                    break;
                case MSG_NEXT_REP:
                    int rep = msg.arg1;
                    mTimerFragment.updateRep(rep);
                    mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), mTimerFragment.getNextSet(), false, false);
                    break;
                case MSG_NEXT_ROUND:
                    int round = msg.arg1;
                    mTimerFragment.updateRep(0);
                    mTimerFragment.updateRound(round);
                    mTimerFragment.updateSetInfo(mTimerFragment.getCurSet(), mTimerFragment.getNextSet(), false, false);

                case MSG_UPDATE_TIME_DISPLAY:
                    if(mTimerFragment != null){
                        mTimerFragment.updateTime(msg.arg1, msg.arg2);
                    } else {
                        throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_UPDATE_TIME_DISPLAY");
                    }
                    break;
                case MSG_STOP_TIMER:
                    if(mFragmentManager != null){
                        if(mFragmentManager.getBackStackEntryCount() > 0){
                            if(!mIsInBackground)
                                mFragmentManager.popBackStack();
                            else {
                                mPendingTimerClose = true;
                                mTimerFinished = true;
                            }
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public final Messenger mIncomingTimerMessenger = new Messenger(new IncomingTimerMessageHandler());
    //endregion

    //region LIFECYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        setContentView(R.layout.activity_my_workout);

        mPendingTimerClose = false;
        mTimerFinished = false;

        mFragmentManager = getSupportFragmentManager();

        //region INSTANCE_STATE
        if(savedInstanceState != null){
            mTimerFragment = (TimerFragment) mFragmentManager.findFragmentById(R.id.fragment_container);
        }
        //endregion

        //region TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region VIEW_MODEL
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mMainVM = ViewModelProviders.of(this).get(MyWorkoutViewModel.class);
        //endregion

        //region INTENT
        Intent intent = getIntent();
        if(intent != null){
            int intentType = intent.getIntExtra(EXTRA_INTENT_TYPE, -1);
            mIntentBundle = intent;
            if(intentType == NORMAL_INTENT_TYPE) {
                // Normal running circumstances
                int id = intent.getIntExtra(EXTRA_WORKOUT_ID, -1);
                mMainVM.setId(id);
                mType = intent.getIntExtra(EXTRA_WORKOUT_TYPE, 0);
            } else if (intentType == NOTIF_INTENT_TYPE){
                int id = intent.getIntExtra(EXTRA_WORKOUT_ID, -1);
                mMainVM.setId(id);
                mType = intent.getIntExtra(EXTRA_WORKOUT_TYPE, 0);
            } else {
                throw new RuntimeException(DEBUG_TAG + " EXTRA_INTENT_TYPE was never set");
            }
        } else {
            throw new RuntimeException(DEBUG_TAG + " Intent was empty in onCreate");
        }
        //endregion

        if(mNotifReceiver == null){
            mNotifReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent in) {
                    if(in != null){
                        int type = in.getIntExtra(EXTRA_INTENT_TYPE, -1);
                        if (type == PAUSE_INTENT_TYPE) {
                            Log.d(DEBUG_TAG, "pausing");
                            if(mTimerFragment != null){
                                mTimerFragment.pauseTimer();
                            }
                        } else if(type == PLAY_INTENT_TYPE) {
                            Log.d(DEBUG_TAG, "resuming");
                            if(mTimerFragment != null){
                                mTimerFragment.continueTimer();
                            }
                        }
                    }

                }
            };
        }
        IntentFilter fil = new IntentFilter(FILTER_TIMER);
        registerReceiver(mNotifReceiver, fil);

        //region TTS_BINDING
        if(!mTTSIsBound) {
            Intent TTSIntent = new Intent(this, TTSService.class);
            bindService(TTSIntent, mTTSConnection, Context.BIND_AUTO_CREATE);
            startService(TTSIntent);
        }
        //endregion

        //region UI
        int colorAccent = getColor(R.color.colorAccent);
        if(BaseApp.isDarkTheme()){
            colorAccent = getColor(R.color.colorAccentDarkTheme);
        }

        mRestTime = findViewById(R.id.rest_time);
        mRestTime.setFocusable(false);
        mRestTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPickerDialog(NumberPickerDialog.REST_TYPE);
            }
        });
        if(BaseApp.isMyWorkFirstRun()){
            ViewTooltip.on(mRestTime)
                    .clickToHide(true)
                    .autoHide(true, 3500)
                    .align(ViewTooltip.ALIGN.CENTER)
                    .position(ViewTooltip.Position.BOTTOM)
                    .text("Time between sets")
                    .color(colorAccent)
                    .show();
        }

        mBreakTime = findViewById(R.id.break_time);
        mBreakTime.setFocusable(false);
        mBreakTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPickerDialog(NumberPickerDialog.BREAK_TYPE);
            }
        });
        if(BaseApp.isMyWorkFirstRun()){
            ViewTooltip.on(mBreakTime)
                    .clickToHide(true)
                    .autoHide(true, 3500)
                    .align(ViewTooltip.ALIGN.CENTER)
                    .position(ViewTooltip.Position.BOTTOM)
                    .text("Time between rounds")
                    .color(colorAccent)
                    .show();
        }

        mTotalTime = findViewById(R.id.total_time);
        mTotalTime.setFocusable(false);
        mTotalTime.setClickable(false);
        mTotalTime.setKeyListener(null);

        mNumRounds = findViewById(R.id.number_rounds);
        mNumRounds.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    if(imm != null)
                        imm.hideSoftInputFromWindow(mNumRounds.getWindowToken(), 0);
                    mNumRounds.clearFocus();
                }
                return false;
            }
        });
        mNumRounds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    Workout w = mMainVM.getWorkoutData();
                    int i = Integer.parseInt(s.toString());
                    if(i != w.getNumOfRounds()) {
                        Log.d(DEBUG_TAG, Integer.toString(i));
                        if (i > 0) {
                            w.setNumOfRounds(i);
                            mWorkoutViewModel.update(w);
                        }
                        Log.d(DEBUG_TAG, "Changed Num of rounds to: " + Integer.toString(i));
                    }
                } catch (NumberFormatException e) {
                    Log.e(DEBUG_TAG, e.getMessage());
                } finally {
                    mNumRounds.setSelection(mNumRounds.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mStartButton = findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = getColor(R.color.colorAccent);
                if(BaseApp.isDarkTheme()){
                    color = getColor(R.color.colorAccentDarkTheme);
                }
                if(mMainVM.getWorkoutData().getNumOfSets() <= 0){
                    setButtonError();
                } else {
                    startTimer();
                }
            }
        });

        //endregion

        //region PAGER
        mViewPager = findViewById(R.id.pager);
        mTabLayout = findViewById(R.id.pager_header);
        //endregion



    }

    private void setButtonError(){
        int colorAccent = getColor(R.color.colorAccent);
        if(BaseApp.isDarkTheme()){
            colorAccent = getColor(R.color.colorAccentDarkTheme);
        }
        ViewTooltip.on(findViewById(R.id.view_pager_layout))
                .clickToHide(true)
                .autoHide(true, 3500)
                .align(ViewTooltip.ALIGN.CENTER)
                .position(ViewTooltip.Position.BOTTOM)
                .text("Please choose at least one set!")
                .color(colorAccent)
                .show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_START");
        HIDE_ITEMS = false;
        setupViewModels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_RESUME");
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(defaultPrefs != null){
            mIsTTSMuted = defaultPrefs.getBoolean("voice_mute", false);
            String op = defaultPrefs.getString("voice_type", "F1");
            Message msg;
            if(mIsTTSMuted){
                msg = Message.obtain(null, TTSService.MSG_MUTE_SPEECH, 0, 0, op);
            } else {
                msg = Message.obtain(null, TTSService.MSG_UNMUTE_SPEECH, 0, 0, op);
            }
            if(mTTSIsBound) {
                try {
                    mTTSService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                if(mMsgQueue == null){
                    mMsgQueue = new ArrayList<>();
                }
                mMsgQueue.add(msg);
            }
        }
        mIsInBackground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_PAUSE");
        mIsInBackground = true;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if(mTimerFinished) {
            if (mFragmentManager.getBackStackEntryCount() > 0) {
                mFragmentManager.popBackStack();
                mPendingTimerClose = false;
            }
        }
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
            stopService(new Intent(this, TTSService.class));
            mTTSIsBound = false;
        }
        if(mNotifReceiver != null)
            unregisterReceiver(mNotifReceiver);
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY ON_DESTROY");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "MY WORKOUT ACTIVITY SAVING INSTANCE STATE");

    }

    @Override
    public void onBackPressed() {
        stopTTSSpeech();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.SETTINGS_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                recreate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //endregion

    //region ACTION_BAR_MENU
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(HIDE_ITEMS){
            for(int i = 0; i < menu.size(); i++){
                menu.getItem(i).setVisible(false);
            }
        } else {
            if(mType == Workout.USER_CREATED) {
                menu.findItem(R.id.add_set).setVisible(true);
            } else {
                menu.findItem(R.id.add_set).setVisible(false);
            }
            menu.findItem(R.id.action_settings).setVisible(true);
            menu.findItem(R.id.rename_workout).setVisible(true);
        }
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
            intent.putExtra(EXTRA_WORKOUT_ID, mMainVM.getId());
            intent.putExtra(EXTRA_WORKOUT_TYPE, mType);
            startActivityForResult(intent, MainActivity.SETTINGS_REQUEST_CODE);
            return true;
        } else if(id == R.id.add_set) {
            openAddNewSetFragment();
        } else if( id == R.id.rename_workout) {
            renameWorkout();
        } else if( id == R.id.re_order) {
            openReorderDialog();
        } else if (id == android.R.id.home){
            if(mFragmentManager.getBackStackEntryCount() > 0){
                mFragmentManager.popBackStack();
                stopTTSSpeech();
                return true;
            } else {
                this.finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region UI_UPDATES
    private void updateRestTimeUI(int min, int sec, boolean flag){
        if(flag){
            mRestTime.setText(R.string.none_text);
        } else if(sec == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if(sec < 10) {
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateBreakTimeUI(int min, int sec, boolean flag){
        if(flag){
            mBreakTime.setText(R.string.none_text);
        } else if(sec == 0){
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, sec, 0));
        } else if(sec < 10) {
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
    }

    private void updateRoundNumUI(int num){
        mNumRounds.setText(String.format(Locale.US, "%d", num));
        mNumRounds.setSelection(mNumRounds.getText().length());
    }

    private void updateTotalTime(int min, int sec){
        mTotalTime.setText(BaseApp.formatTime(min, sec));
    }

    //endregion

    //region UTILITY
    private void dataUpdate(Workout w){
        if(!getTitle().equals(w.getName())){
            setTitle(w.getName());
        }
        int numRounds = w.getNumOfRounds();
        updateRoundNumUI(numRounds);

        int[] restTime = BaseApp.convertFromMillis( w.getTimeBetweenSets() );
        int restMin = restTime[0], restSec = restTime[1];
        updateRestTimeUI(restMin, restSec, w.getNoRestFlag());

        int[] breakTime = BaseApp.convertFromMillis( w.getTimeBetweenRounds() );
        int breakMin = breakTime[0], breakSec = breakTime[1];
        updateBreakTimeUI(breakMin, breakSec, w.getNoBreakFlag());

        int[] total = BaseApp.convertFromMillis(w.getTotalTime());
        updateTotalTime(total[0], total[1]);
    }

    public void hideActionItems(){
        HIDE_ITEMS = true;
        invalidateOptionsMenu();
    }

    public void showActionItems(){
        HIDE_ITEMS = false;
        invalidateOptionsMenu();
    }

    public void renameTitle(int stringId){
        if(stringId < 0){
            setTitle(mMainVM.getWorkoutData().getName());
        } else {
            setTitle(stringId);
        }
    }

    private void setupViewPager(List<Set> s){
        mViewPager.setOffscreenPageLimit(s.size() == 0 ? 1 : s.size());
        mViewPagerAdapter = new ViewPagerAdapter(mFragmentManager);
        mSetInfoFrags = new ArrayList<>();
        for(int i = 0; i < s.size(); i++){
            Bundle args = SetInfoFragment.getBundle(s.get(i), i ,mMainVM.getId());
            SetInfoFragment frag = SetInfoFragment.newInstance(args,null);
            mViewPagerAdapter.addFragment(frag, "");
            mSetInfoFrags.add(frag);
        }
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager, true);
    }

    //endregion


    private void setupViewModels(){
        mWorkoutViewModel.getWorkout(mMainVM.getId()).observe(this, new Observer<Workout>() {
            @Override
            public void onChanged(@Nullable Workout workout) {
                if(workout != null) {
                    mMainVM.setWorkout(workout);
                    if(workout.getNumOfSets() <= 0){
                        findViewById(R.id.add_one_set).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.add_one_set).setVisibility(View.GONE);
                    }
                    if(mSetInfoFrags == null){
                        setupViewPager(workout.getSetList());
                    } else {
                        List<Set> newSetList = workout.getSetList();
                        if(newSetList.size() != mSetInfoFrags.size()){
                            setupViewPager(workout.getSetList());
                            Log.e(DEBUG_TAG, "Set Info Frag List doesn't match the workout set list");
                        } else {
                            int i = 0;
                            for(SetInfoFragment f : mSetInfoFrags){
                                f.updateData(newSetList.get(i++));
                            }
                        }
                    }
                    dataUpdate(mMainVM.getWorkoutData());
                    if(BaseApp.isMyWorkFirstRun()){
                        BaseApp.toggleMyWorkFirstRun();
                    }
                }
            }
        });
    }

    public static Bundle getNotifBundle(int workoutId, int workoutType){
        Bundle args = new Bundle();
        args.putInt(EXTRA_WORKOUT_ID, workoutId);
        args.putInt(EXTRA_WORKOUT_TYPE, workoutType);
        return args;
    }

    private void openReorderDialog(){
        Bundle args = ReorderSetsDialog.getBundle(mMainVM.getWorkoutData().getSetList(), mMainVM.getWorkoutData().getWorkoutID());
        ReorderSetsDialog dialog = ReorderSetsDialog.newInstance(args);
        dialog.show(mFragmentManager, DEBUG_TAG);
    }

    //region RENAME_WORKOUT
    private void renameWorkout(){
        Bundle args = RenameWorkoutDialog.getBundle(mMainVM.getWorkoutData().getName());
        RenameWorkoutDialog dialog = RenameWorkoutDialog.newInstance(args, new RenameWorkoutDialog.RenameListener() {
            @Override
            public void RenameWorkout(String name) {
                if(!TextUtils.isEmpty(name)){
                    mMainVM.getWorkoutData().setName(name);
                    mWorkoutViewModel.update(mMainVM.getWorkoutData());
                } else {
                    Toast.makeText(getApplicationContext(), "Name can't be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show(mFragmentManager, DEBUG_TAG);
    }
    //endregion

    //region ADD_SET

    private void openAddNewSetFragment(){
        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Bundle args = AddSetsFragment.getBundle(mMainVM.getId(), mMainVM.getWorkoutData().getNumOfSets());
        mAddSetsFragment = AddSetsFragment.newInstance(args, new AddSetsFragment.NewSetListener() {
            @Override
            public void addSetsToWorkout(List<Set> sets) {
                if(sets != null) {
                    for (Set set : sets) {
                        mMainVM.getWorkoutData().addSet(set);
                    }
                    mWorkoutViewModel.update(mMainVM.getWorkoutData());
                }
                mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.fragment_container, mAddSetsFragment);
        fragmentTransaction.addToBackStack(null);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        renameTitle(R.string.add_set);
        hideActionItems();
        fragmentTransaction.commit();
    }

    //endregion

    //region FRAGMENT_CLOSING
    @Override
    public void closeFragmentAndService() {
        if(mTimerIsBound){
            unbindService(mTimerConnection);
        }
        //FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        //fragmentTransaction.remove(mTimerFragment);
        //fragmentTransaction.commit();
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
    private void openNumberPickerDialog(int type){
        Bundle args;
        Workout w = mMainVM.getWorkoutData();
        if(type == NumberPickerDialog.REST_TYPE) {
            args = NumberPickerDialog.getDialogBundle(type, w.getTimeBetweenSets(), w.getNoRestFlag());
        } else if (type == NumberPickerDialog.BREAK_TYPE) {
            args = NumberPickerDialog.getDialogBundle(type, w.getTimeBetweenRounds(), w.getNoBreakFlag());
        } else {
            throw new RuntimeException(DEBUG_TAG + " invalid type");
        }
        NumberPickerDialog numberPickerDialog = NumberPickerDialog.newInstance(args, new NumberPickerDialog.NumberPickerDialogListener() {
            @Override
            public void setTime(int type, int min, int sec, boolean noFlag) {
                if(type == NumberPickerDialog.REST_TYPE){
                    setRestTime(min, sec, noFlag);
                } else if (type == NumberPickerDialog.BREAK_TYPE){
                    setBreakTime(min, sec, noFlag);
                } else {
                    throw new RuntimeException(DEBUG_TAG + " invalid type in number dialog listener");
                }
            }
        });
        numberPickerDialog.show(mFragmentManager, DEBUG_TAG);
    }

    public void setRestTime(int min, int sec, boolean noFlag) {
        int time = BaseApp.convertToMillis(min, sec);
        updateRestTimeUI(min, sec, noFlag);
        mMainVM.getWorkoutData().setTimeBetweenSets(time);
        mMainVM.getWorkoutData().setNoRestFlag(noFlag);
        mWorkoutViewModel.update(mMainVM.getWorkoutData());
    }

    public void setBreakTime(int min, int sec, boolean noFlag) {
        int time = BaseApp.convertToMillis(min, sec);
        updateBreakTimeUI(min, sec, noFlag);
        mMainVM.getWorkoutData().setTimeBetweenRounds(time);
        mMainVM.getWorkoutData().setNoBreakFlag(noFlag);
        mWorkoutViewModel.update(mMainVM.getWorkoutData());
    }
    //endregion


    //region TIMER_FUNCTIONS
    // UI Interaction and fragment creation
    public void startTimer(){
        openTimerFragment(mMainVM.getWorkoutData());
    }

    public void openTimerFragment(Workout wrk){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Bundle args = TimerFragment.getBundle(wrk.getWorkoutID());
        mTimerFragment = TimerFragment.newInstance(args);
        fragmentTransaction.replace(R.id.fragment_container, mTimerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        Log.d(DEBUG_TAG, "Timer Fragment Created");
        bindService(new Intent(this, TimerService.class), mTimerConnection, Context.BIND_AUTO_CREATE);
    }

    private void beginTimerService(){
        // TODO: Create a function that unifies notification vs start bundle
        Workout w = mMainVM.getWorkoutData();

        final Bundle notifBundle = getNotifBundle(w.getWorkoutID(), w.getWorkoutType());
        //Context context, Bundle notif, int setTime, int restTime, int breakTime, int reps, int rounds, boolean no_rest, boolean no_break
        int curTime = mTimerFragment != null ? mTimerFragment.getCurSetTime() : w.getSetList().get(0).getTime();
        final Intent timerIntent = TimerService.getServiceIntent(
                this,
                notifBundle,
                w.getSetList().get(0).getName(),
                w.getSetList().get(0).getSetImageId(),
                curTime,
                w.getTimeBetweenSets(),
                w.getTimeBetweenRounds(),
                w.getNumOfSets(),
                w.getNumOfRounds(),
                w.getNoRestFlag(),
                w.getNoBreakFlag(),
                mIsTTSMuted);

        startService(timerIntent);
    }

    private void stopTimerService(){
        stopService(new Intent(this, TTSService.class));
    }
    //endregion

    //region TTS_Outgoing_Messaging
    public void sendTTSMessage(int string_id, String extra){
        if(mTTSIsBound) {
            if (string_id > 0) {
                Message msg = Message.obtain(null, TTSService.MSG_SPEAK, string_id, 0, extra);
                try {
                    mTTSService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(DEBUG_TAG, "Invalid String ID in sendTTSMessage");
            }
        } else {
            throw new RuntimeException(DEBUG_TAG + " TTS was not bound. Unable to send message");
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
            throw new RuntimeException(DEBUG_TAG + " Timer is unbound. Unable to send message");
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

            if(!mMsgQueue.isEmpty()){
                for(Message m : mMsgQueue){
                    try {
                        mTTSService.send(m);
                    } catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTTSService = null;
            mTTSIsBound = false;
        }
    };
    //endregion

    //region TESTING
    // FOR TESTING PURPOSES
    public Workout getWrkoutData(){
        return mMainVM.getWorkoutData();
    }
    //endregion
}