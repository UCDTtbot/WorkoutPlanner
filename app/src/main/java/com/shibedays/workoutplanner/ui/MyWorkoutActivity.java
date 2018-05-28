package com.shibedays.workoutplanner.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.ui.adapters.ViewPagerAdapter;
import com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog;
import com.shibedays.workoutplanner.ui.dialogs.BottomSheetDialog;
import com.shibedays.workoutplanner.ui.dialogs.RenameWorkoutDialog;
import com.shibedays.workoutplanner.ui.fragments.AddNewSetFragment;
import com.shibedays.workoutplanner.ui.fragments.CreateEditSetFragment;
import com.shibedays.workoutplanner.ui.fragments.SetInfoFragment;
import com.shibedays.workoutplanner.ui.fragments.TimerFragment;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.services.TTSService;
import com.shibedays.workoutplanner.services.TimerService;
import com.shibedays.workoutplanner.ui.adapters.SetAdapter;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog;
import com.shibedays.workoutplanner.ui.settings.SettingsActivity;
import com.shibedays.workoutplanner.viewmodel.SetViewModel;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;


public class MyWorkoutActivity extends AppCompatActivity implements TimerFragment.OnFragmentInteractionListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = MyWorkoutActivity.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.MyWorkoutActivity.";
    // Intent Types
    public static final int NORMAL_INTENT_TYPE = 0;
    public static final int NOTIF_INTENT_TYPE = 1;
    // Data Constants
    private int DATA_DOESNT_EXIST = -1;

    private static final int NEW_SET = 0;
    private static final int EDIT_SET = 1;
    private static final int DISPLAY_SET = 3;
    // Message Constants

    // Bottom Sheet Constants
    public static int WORKOUT_SCREEN = 0;
    public static int NEW_SET_SCREEN = 1;
    //endregion

    //region MESSAGES
    public static final int MSG_SAY_HELLO = 0;
    public static final int MSG_UPDATE_SET_NAME = 1;
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
    public static final String EXTRA_TTS_VOLUME = PACKAGE + "Volume";
    public static final String EXTRA_WORKOUT_JSON = PACKAGE + "WORKOUT_JSON";

    public static final String EXTRA_NOTIF_BUNDLE = PACKAGE + "INTENT_BUNDLE";

    public static final String EXTRA_UPDATE_DESCRIPTION = PACKAGE + "UPDATE_DESCRIP";

    //endregion

    //region PRIVATE_VARS
    // UI Components
    private RecyclerView mRecyclerView;
    private TextView mRestTime;
    private TextView mBreakTime;
    private EditText mNumRounds;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    // Adapters
    //private SetAdapter mSetAdapter;
    // Data
    private Workout mWorkoutData;
    private List<Set> mSetList;
    private LiveData<Workout> mWorkoutLiveData;
    private List<List<Set>> mTypedSets;

    private int mVol;

    // Instances
    private FragmentManager mFragmentManager;
    private TimerFragment mTimerFragment;
    private List<SetInfoFragment> mSetInfoFrags;
    private AddNewSetFragment mAddNewSetFragment;
    private CreateEditSetFragment mCreateEditFragment;

    private Messenger mTimerService;
    private Messenger mTTSService;
    // View Model
    private WorkoutViewModel mWorkoutViewModel;
    private SetViewModel mSetViewModel;
    // Booleans
    private boolean mTimerIsBound;
    private boolean mTTSIsBound;
    private boolean HIDE_ITEMS;

    //endregion

    //region PUBLIC_VARS

    //endregion

    //region MESSAGE_HANDLING

    class IncomingTTSMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    //Toast.makeText(getApplicationContext(), "TTS Ready", Toast.LENGTH_SHORT).show();
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
                case MSG_PASS_TTS_MSG:
                    if(msg.arg1 > 0) {
                        sendTTSMessage(msg.arg1);
                    }
                    break;
                case MSG_UPDATE_SET_NAME:
                    if(mTimerFragment != null){
                        mTimerFragment.updateSetTitle();
                    } else {
                        throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_UPDATE_TIME_DISPLAY");
                    }
                case MSG_UPDATE_TIME_DISPLAY:
                    if(mTimerFragment != null){
                        mTimerFragment.updateTime(msg.arg1, msg.arg2);
                    } else {
                        throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_UPDATE_TIME_DISPLAY");
                    }
                    break;
                case MSG_NEXT_REP_UI:
                    if(msg.arg1 >= 0){
                        if(mTimerFragment != null) {
                            mTimerFragment.updateRep(msg.arg1);
                        } else {
                            throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_NEXT_REP_UI");
                        }
                    }
                    break;
                case MSG_NEXT_ROUND_UI:
                    if(msg.arg1 >= 0){
                        if(mTimerFragment != null) {
                            mTimerFragment.updateRound(msg.arg1);
                        } else {
                            throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "mTimerFragment is NULL in MSG_NEXT_ROUND_UI");
                        }
                    }
                    break;
                case MSG_NEXT_SET_TIME:
                    Set nextSet = mTimerFragment.nextSet();
                    if(nextSet != null) {
                        Message message = Message.obtain(null, TimerService.MSG_NEXT_SET_TIME, nextSet.getTime(), 0);
                        sendTimerMessage(message);
                    } else {
                        throw new RuntimeException(MyWorkoutActivity.class.getSimpleName() + "No next set exists. Something went wrong with curRep counter");
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
        HIDE_ITEMS = false;
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
        mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //endregion

        //region VIEW_MODEL
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mSetViewModel = ViewModelProviders.of(this).get(SetViewModel.class);
        //endregion

        //region INTENT
        Intent intent = getIntent();
        if(intent != null){
            int mIntentType = intent.getIntExtra(EXTRA_INTENT_TYPE, -1);
            if(mIntentType == NORMAL_INTENT_TYPE) {
                // Normal running circumstances
                int id = intent.getIntExtra(EXTRA_WORKOUT_ID, -1);
                mVol = intent.getIntExtra(EXTRA_TTS_VOLUME, 100);
                mWorkoutLiveData = mWorkoutViewModel.getWorkout(id);
                mWorkoutLiveData.observe(this, new Observer<Workout>() {
                    @Override
                    public void onChanged(@Nullable Workout workout) {
                        if (workout != null) {
                            mWorkoutData = workout;
                            mSetList = mWorkoutData.getSetList();
                            setupViewPager(mSetList);
                            //mSetAdapter.setData(mSetList);
                            dataUpdate();
                        } else {
                            throw new RuntimeException(DEBUG_TAG + " workout not found for LiveData");
                        }

                    }
                });

                mTypedSets = new ArrayList<>();
                for(int i = 0; i < Set.TYPES.length; i++) {
                    final int x = i;

                    mSetViewModel.getTypedSet(i).observe(this, new Observer<List<Set>>() {
                        @Override
                        public void onChanged(@Nullable List<Set> sets) {
                            if (sets != null) {
                                if (!sets.isEmpty()) {
                                    if (!mTypedSets.contains(sets)) mTypedSets.add(sets);
                                    else mTypedSets.set(x, sets);
                                }
                            }
                        }
                    });

                }

            } else if (mIntentType == NOTIF_INTENT_TYPE){
                // This is what happens if we're opening this activity from the notification
            } else {
                throw new RuntimeException(DEBUG_TAG + " EXTRA_INTENT_TYPE was never set");
            }
        } else {
            throw new RuntimeException(DEBUG_TAG + " Intent was empty in onCreate");
        }
        //endregion


        //region TTS_BINDING
        Intent TTSIntent = new Intent(this, TTSService.class);
        TTSIntent.putExtra("TestVol", 0);
        bindService(TTSIntent, mTTSConnection, Context.BIND_AUTO_CREATE);
        startService(TTSIntent);
        //endregion


        //region UI
        mRestTime = findViewById(R.id.rest_time);
        mRestTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPickerDialog(NumberPickerDialog.REST_TYPE);
            }
        });

        mBreakTime = findViewById(R.id.break_time);
        mBreakTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPickerDialog(NumberPickerDialog.BREAK_TYPE);
            }
        });

        mNumRounds = findViewById(R.id.number_rounds);
        mNumRounds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int i = Integer.parseInt(s.toString());
                    if(i != mWorkoutData.getNumOfRounds()) {
                        Log.d(DEBUG_TAG, Integer.toString(i));
                        if (i > 0) {
                            mWorkoutData.setNumOfRounds(i);
                            mWorkoutViewModel.update(mWorkoutData);
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

        //endregion

        //region PAGER
        mViewPager = findViewById(R.id.pager);
        mTabLayout = findViewById(R.id.pager_header);
        //endregion

        //region RECYCLER_VIEW
        /*
        mRecyclerView = findViewById(R.id.set_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setItemAnimator(new FadeInLeftAnimator());

        mSetAdapter = new SetAdapter(this, findViewById(R.id.set_coord_layout), new SetAdapter.SetAdapterListener() {
            @Override
            public void onSetClick(int setIndex) {
                openBottomSheet(setIndex);
            }

            @Override
            public void deleteSet(Set set) {
                deleteSetFromWorkout(set);
            }
        });
        mRecyclerView.setAdapter(mSetAdapter);
        mSetAdapter.notifyDataSetChanged();

        int dragDirs = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeDirs = 0;
        */
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
        if(HIDE_ITEMS){
            for(int i = 0; i < menu.size(); i++){
                menu.getItem(i).setVisible(false);
            }
        } else {
            if(mWorkoutData.getWorkoutType() == Workout.USER_CREATED) {
                menu.findItem(R.id.add_set).setVisible(true);
            } else {
                menu.findItem(R.id.add_set).setVisible(false);
            }
            menu.findItem(R.id.action_settings).setVisible(true);
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
            startActivity(intent);
            return true;
        } else if(id == R.id.add_set) {
            openAddNewSetFragment();
        } else if( id == R.id.rename_workout){
            renameWorkout();
        } else if (id == android.R.id.home){
            if(mFragmentManager.getBackStackEntryCount() > 0){
                mFragmentManager.popBackStack();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region UI_UPDATES
    private void updateRestTimeUI(int min, int sec, boolean flag){
        /*if(flag){
            mRestTime.setText(R.string.none_text);
        }else if((sec % 10) == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        } else if (min == 0 && sec == 0){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else if ( sec < 10 ){
            mRestTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }  else {
            mRestTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }
        */

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
        /*
        if(flag){
            mBreakTime.setText(R.string.none_text);
        } else if((sec % 10) == 0) {
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }else if (min == 0 && sec == 0) {
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        }else if ( sec < 10 ) {
            mBreakTime.setText(String.format(Locale.US, "%d:%d%d", min, 0, sec));
        } else {
            mBreakTime.setText(String.format(Locale.US, "%d:%d", min, sec));
        }*/

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

    //endregion

    //region UTILITY
    private void dataUpdate(){
        if(!getTitle().equals(mWorkoutData.getName())){
            setTitle(mWorkoutData.getName());
        }
        int numRounds = mWorkoutData.getNumOfRounds();
        updateRoundNumUI(numRounds);

        int[] restTime = BaseApp.convertFromMillis( mWorkoutData.getTimeBetweenSets() );
        int restMin = restTime[0], restSec = restTime[1];
        updateRestTimeUI(restMin, restSec, mWorkoutData.getNoRestFlag());

        int[] breakTime = BaseApp.convertFromMillis( mWorkoutData.getTimeBetweenRounds() );
        int breakMin = breakTime[0], breakSec = breakTime[1];
        updateBreakTimeUI(breakMin, breakSec, mWorkoutData.getNoBreakFlag());
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
            setTitle(mWorkoutData.getName());
        } else {
            setTitle(stringId);
        }
    }

    private void setupViewPager(List<Set> s){
        mViewPager.setOffscreenPageLimit(s.size() == 0 ? 1 : s.size());
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFragmentManager);
        mSetInfoFrags = new ArrayList<>();
        for(int i = 0; i < s.size(); i++){
            SetInfoFragment frag = SetInfoFragment.newInstance(s.get(i), null);
            adapter.addFragment(frag, "");
            mSetInfoFrags.add(frag);
        }

        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager, true);
    }
    //endregion


    //region RENAME_WORKOUT
    private void renameWorkout(){
        Bundle args = RenameWorkoutDialog.getBundle(mWorkoutData.getName());
        RenameWorkoutDialog dialog = RenameWorkoutDialog.newInstance(args, new RenameWorkoutDialog.RenameListener() {
            @Override
            public void RenameWorkout(String name) {
                if(!TextUtils.isEmpty(name)){
                    mWorkoutData.setName(name);
                    mWorkoutViewModel.update(mWorkoutData);
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
        mAddNewSetFragment = AddNewSetFragment.newInstance(mTypedSets, new AddNewSetFragment.NewSetListener() {
            @Override
            public void addSetsToWorkout(List<Set> sets) {
                if(sets != null) {
                    for (Set set : sets) {
                        mWorkoutData.addSet(set);
                    }
                    mWorkoutViewModel.update(mWorkoutData);
                }
                mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            @Override
            public void applyUserSetToDB(Set set) {
                mSetViewModel.insert(set);
            }

            @Override
            public void removeUserSetFromDB(Set set) {
                mSetViewModel.remove(set);
            }
        });
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slight_out_left);
        fragmentTransaction.replace(R.id.fragment_container, mAddNewSetFragment);
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
    private void openNumberPickerDialog(int type){
        Bundle args = null;
        if(type == NumberPickerDialog.REST_TYPE) {
            args = NumberPickerDialog.getDialogBundle(type, mWorkoutData.getTimeBetweenSets(), mWorkoutData.getNoRestFlag());
        } else if (type == NumberPickerDialog.BREAK_TYPE) {
            args = NumberPickerDialog.getDialogBundle(type, mWorkoutData.getTimeBetweenRounds(), mWorkoutData.getNoBreakFlag());
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
        mWorkoutData.setTimeBetweenSets(time);
        mWorkoutData.setNoRestFlag(noFlag);
        mWorkoutViewModel.update(mWorkoutData);
    }

    public void setBreakTime(int min, int sec, boolean noFlag) {
        int time = BaseApp.convertToMillis(min, sec);
        updateBreakTimeUI(min, sec, noFlag);
        mWorkoutData.setTimeBetweenRounds(time);
        mWorkoutData.setNoBreakFlag(noFlag);
        mWorkoutViewModel.update(mWorkoutData);
    }

    //endregion

    //region BOTTOM_SHEET
    public void openBottomSheet(final int setIndex){
        Bundle args = BottomSheetDialog.getBottomSheetBundle(mSetList.get(setIndex).getName(),
                BaseApp.getSetBtmSheetRows(), BaseApp.getSetBtmSheetNames(this),
                BaseApp.getSetBtmSheetICs(), BaseApp.getSetBtmSheetResults());
        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.newInstance(args, new BottomSheetDialog.BottomSheetDialogListener() {
            @Override
            public void bottomSheetResult(int resultCode) {
                switch (resultCode){
                    case BaseApp.EDIT:
                        openEditSet(mSetList.get(setIndex));
                        break;
                    case BaseApp.DELETE:
                        //mSetAdapter.pendingRemoval(setIndex);
                        break;
                    case BaseApp.DUPLCIATE:
                        break;
                    default:
                        break;
                }
            }
        });
        bottomSheetDialog.show(mFragmentManager, DEBUG_TAG);
    }

    public void deleteSetFromWorkout(Set set) {
        mWorkoutViewModel.update(mWorkoutData);
    }

    private void displayDialog(@NonNull Set set){
        Bundle bundle = AddEditSetDialog.getDialogBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        AddEditSetDialog dialog = AddEditSetDialog.newInstance(bundle);
        if (getFragmentManager() != null) {
            dialog.show(getSupportFragmentManager(), DEBUG_TAG);
        }
    }

    private void openEditSet(@NonNull final Set set){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle args = CreateEditSetFragment.getBundle(set.getSetId(), set.getName(), set.getDescrip(), set.getTime());
        mCreateEditFragment = CreateEditSetFragment.newInstance(R.string.title_activity_my_workout,args, new CreateEditSetFragment.CreateEditSetListener() {
            @Override
            public void returnData(String name, String descrip, int min, int sec, int imageId) {
                updateUserSet(set, set.getSetId(), name, descrip, min, sec);
            }
        });
        fragmentTransaction.replace(R.id.new_workout_fragment_container, mCreateEditFragment);
        fragmentTransaction.addToBackStack(null);
        renameTitle(R.string.edit_set);
        fragmentTransaction.commit();
    }
    //endregion

    private void updateUserSet(Set set, int id, String name, String descrip, int min, int sec){
        set.setName(name);
        set.setTime(BaseApp.convertToMillis(min, sec));
        set.setDescrip(descrip);
        mWorkoutData.updateSet(set, id);
        mWorkoutViewModel.update(mWorkoutData);
    }

    //region SWAP_SETS

    /*
    public void swap(int from, int to) {
        swapSets(from, to);
    }

    private void swapSets(int from, int to){
        mWorkoutData.swapSets(from, to);
    }
    */
    //endregion


    //region TIMER_FUNCTIONS
    // UI Interaction and fragment creation
    public void startTimer(View view){
        openTimerFragment(mWorkoutData);
    }

    public void openTimerFragment(Workout wrk){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mTimerFragment = TimerFragment.newInstance(wrk.toJSON());
        fragmentTransaction.replace(R.id.fragment_container, mTimerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        Log.d(DEBUG_TAG, "Timer Fragment Created");
        bindService(new Intent(this, TimerService.class), mTimerConnection, Context.BIND_AUTO_CREATE);
    }



    private void beginTimerService(){
        // TODO: Create a function that unifies notification vs start bundle
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
        return mWorkoutData;
    }
    //endregion
}