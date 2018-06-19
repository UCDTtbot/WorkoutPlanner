package com.shibedays.workoutplanner.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.Locale;

public class TimerService extends Service {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = TimerService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.services.TimerService.";
    // Notification ID
    private static final int NOTIF_ID = 1;
    private static final long ONE_SEC = 1000;
    // Actions
    public static final int REP_ACTION = 0;
    public static final int REST_ACTION = 1;
    public static final int BREAK_ACTION = 2;
    public static final int STOP_ACTION = 3;
    // Message Constants
    public static final int MSG_TIMER_BIND = 0;
    public static final int MSG_NEXT_SET_TIME = 1;
    public static final int MSG_PAUSE_TIMER = 2;
    public static final int MSG_CONTINUE_TIMER = 3;

    private static final int TTS_STARTING_DELAY = 6;
    private static final int TTS_BREAK_DELAY = 2;
    private static final int TTS_REST_DELAY = 2;
    private static final int TTS_NO_DELAY = 1;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_TIME = PACKAGE + "SET_TIME";
    public static final String EXTRA_REST_TIME = PACKAGE + "REST_TIME";
    public static final String EXTRA_BREAK_TIME = PACKAGE + "BREAK_TIME";
    public static final String EXTRA_NUM_REPS = PACKAGE + "NUM_REPS";
    public static final String EXTRA_NUM_ROUNDS = PACKAGE + "NUM_ROUNDS";
    public static final String EXTRA_NO_REST_FLAG = PACKAGE + "NO_REST_FLAG";
    public static final String EXTRA_NO_BREAK_FLAG = PACKAGE + "NO_BREAK_FLAG";
    public static final String EXTRA_REBUILD_BUNDLE = PACKAGE + "REBUILD";
    public static final String EXTRA_NOTIF_BUNDLE = PACKAGE + "NOTIF";
    public static final String EXTRA_SET_NAME = PACKAGE + "NAME";
    public static final String EXTRA_SET_IMAGE = PACKAGE + "IMAGE";
    public static final String EXTRA_IS_TTS_MUTED = PACKAGE + "IS_MUTED";
    //endregion

    public final int NOTIF_CURRENT = 0;
    public final int NOTIF_REST = 1;
    public final int NOTIF_BREAK = 2;
    public final int NOTIF_NEXT = 3;

    //region PRIVATE_VARS
    // Time Data
    private int mTotalCurTime;
    private int mTimeLeft;
    private int mTimeElapsed;
    private int mNextSetTime;
    private String mSetName;
    private int mSetImage;

    private String mNextSetName;
    private int mNextSetImage;

    private int mRestTime;
    private int mBreakTime;
    private int mCurRep;
    private int mCurRound;
    private int mNumReps;
    private int mNumRounds;
    private boolean mNoRestFlag;
    private boolean mNoBreakFlag;

    private boolean mIsTTSMuted;
    // Action Tracker
    private int mCurrentAction;
    // Notification Variables
    private Bundle mNotifBundle;
    private PendingIntent mPendingIntent;
    // Threading Handler
    private Handler mHandler = new Handler();
    // Notification Variables
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifManager;
    // Instances
    private Messenger mMyWorkoutActivityMessenger;
    // Booleans
    private boolean mIsMessengerBound = false;
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region MESSAGE_HANDLER
    // The Messenger (originally binder) given to clients
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_TIMER_BIND:
                    mMyWorkoutActivityMessenger = msg.replyTo;
                    mIsMessengerBound = true;
                    break;
                case MSG_NEXT_SET_TIME:
                    if(msg.arg1 > 0) {
                        setNextSetTime(msg.arg1);
                        setNextSetImage(msg.arg2);
                        setNextSetName(msg.obj.toString());
                    }else{
                        Log.e(DEBUG_TAG, "NEXT SET TIME HAS INVALID ARG");
                    }
                    break;
                case MSG_PAUSE_TIMER:
                    if(mHandler != null){
                        mHandler.removeCallbacks(timer);
                        Log.d(DEBUG_TAG, "Timer (should have been) paused.");
                    }
                    break;
                case MSG_CONTINUE_TIMER:
                    if(mHandler != null){
                        mHandler.removeCallbacks(timer);
                        mHandler.postDelayed(timer, ONE_SEC / 2);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    //endregion

    //region LIFECYCLE
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_START_COMMAND");


        // TODO: Receive the bundle that contains the notification information and use it to build the notif
        if(intent != null){
            mNotifBundle = intent.getBundleExtra(EXTRA_NOTIF_BUNDLE);
            mTotalCurTime = intent.getIntExtra(EXTRA_SET_TIME, -1);
            mRestTime = intent.getIntExtra(EXTRA_REST_TIME, -1);
            mBreakTime = intent.getIntExtra(EXTRA_BREAK_TIME, -1);
            mNumReps = intent.getIntExtra(EXTRA_NUM_REPS, -1);
            mNumRounds = intent.getIntExtra(EXTRA_NUM_ROUNDS, -1);
            mNoRestFlag = intent.getBooleanExtra(EXTRA_NO_REST_FLAG, false);
            mNoBreakFlag = intent.getBooleanExtra(EXTRA_NO_BREAK_FLAG, false);
            mSetName = intent.getStringExtra(EXTRA_SET_NAME);
            mSetImage = intent.getIntExtra(EXTRA_SET_IMAGE, R.drawable.ic_fitness_black_24dp);
            mIsTTSMuted = intent.getBooleanExtra(EXTRA_IS_TTS_MUTED, false);
        } else {
            // abort?
        }

        //region NOTIFICATION_BUILDING
        // Create the intent for rebuilding the activity
        Intent notifIntent = new Intent(this, MyWorkoutActivity.class);
        notifIntent.putExtra(EXTRA_REBUILD_BUNDLE, mNotifBundle);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifIntent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NOTIF_INTENT_TYPE);
        // Create the pending intent that will bring us back to MyWorkoutActivity
        mPendingIntent = PendingIntent.getActivity(this, NOTIF_ID, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Setup the notification with correct data
        mBuilder = new NotificationCompat.Builder(this, "MainTimerChannel");
        Bitmap b = BitmapFactory.decodeResource(getResources(), mSetImage);
        mBuilder.setContentTitle(mSetName)
                .setContentText(BaseApp.formatTime(mTotalCurTime) + " left.")
                .setContentIntent(mPendingIntent)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(BaseApp.formatTime(mTotalCurTime) + " left.")
                        .addLine(String.format(Locale.US, "Set: %d  Round: %d", mCurRep + 1, mCurRound + 1)));
        startForeground(NOTIF_ID, mBuilder.build());
        //endregion

        sendTTSMessage(R.string.tts_starting);

        mCurRep = 0;
        mCurRound = 0;
        mCurrentAction = REP_ACTION;
        if(mIsTTSMuted)
            beginTimer(mTotalCurTime, TTS_NO_DELAY);
        else
            beginTimer(mTotalCurTime, TTS_STARTING_DELAY);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_BIND");
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_DESTROY");
        //stopForeground(true);
        //stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_UNBIND");
        mHandler.removeCallbacks(timer);
        stopForeground(true);
        stopSelf();
        return super.onUnbind(intent);
    }

    //endregion

    //region UTILITY
    private void beginTimer(int time, int delay){
        mTotalCurTime = time;
        mTimeLeft = time;
        mTimeElapsed = 0;



        /*if(mCurRep == (mNumReps - 1) && mCurrentAction == REP_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_PRELOAD_FIRST_SET, 0, 0);
            sendMessage(msg);
        } else if(mCurrentAction == REP_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_PRELOAD_NEXT_SET, 0, 0);
            sendMessage(msg);
        } else */
        if(mCurrentAction == REST_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_LOAD_NEXT_SET, 0, 0);
            sendMessage(msg);
            updateNotification(NOTIF_REST);
        } else if(mCurrentAction == BREAK_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_LOAD_FIRST_SET, 0, 0);
            sendMessage(msg);
            updateNotification(NOTIF_BREAK);
        }

        mHandler.removeCallbacks(timer);
        mHandler.postDelayed(timer, ONE_SEC * delay);
    }

    public void setNextSetTime(int time){
        mNextSetTime = time;
    }

    public void setNextSetName(String name){
        mNextSetName = name;
    }

    public void setNextSetImage(int id){
        mNextSetImage = id;
    }
    //endregion

    //region TIMER_LOOP
    private Runnable timer = new Runnable(){
        public void run(){
            mTimeLeft -= ONE_SEC;
            mTimeElapsed += ONE_SEC;

            if(mTimeLeft > 0){ // Still running
                if(mTimeLeft == 7000 && mCurrentAction == REST_ACTION){
                    sendTTSMessage(R.string.tts_rest_ending);
                }
                if(mTimeLeft == 5000) {
                    sendTTSMessage(R.string.tts_five);
                }
                if(mTimeLeft == 4000) {
                    sendTTSMessage(R.string.tts_four);
                }
                if(mTimeLeft == 3000) {
                    sendTTSMessage(R.string.tts_three);
                }
                if(mTimeLeft == 2000) {
                    sendTTSMessage(R.string.tts_two);
                }
                if(mTimeLeft == 1000) {
                    sendTTSMessage(R.string.tts_one);
                }

                if(mTimeElapsed % 10000 == 0){
                    updateNotification(NOTIF_CURRENT);
                }

                mHandler.postDelayed(this, ONE_SEC);
            } else { // Timer has finished
                Log.d(DEBUG_TAG, "Timer has finished.");

                if(mCurrentAction == REP_ACTION && mCurRep == (mNumReps - 1) && mCurRound != (mNumRounds - 1)) {  // Round Finished. Break
                    if(mNoBreakFlag){ // No Break
                        nextRound();

                        sendTTSMessage(R.string.tts_begin);
                        mCurrentAction = REP_ACTION;
                        beginTimer(mNextSetTime, TTS_NO_DELAY);

                    } else {    // Yes Break
                        sendTTSMessage(R.string.tts_round_finished);
                        mCurrentAction = BREAK_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mBreakTime, TTS_NO_DELAY);
                        else
                            beginTimer(mBreakTime, TTS_BREAK_DELAY);
                    }
                } else if(mCurrentAction == REP_ACTION && mCurRep == (mNumReps - 1) && mCurRound == (mNumRounds - 1)){ // Workout Finished. Finished
                    sendTTSMessage(R.string.tts_finished);

                    //TODO: Send us back to MyWorkoutActivity, unbind, and kill-self

                } else if(mCurrentAction == REP_ACTION && mCurRep < (mNumReps - 1 )){ // Set finished. Rest
                    //Repetition finished. Rest.

                    if(mNoRestFlag){ // No Rest
                        nextRep();

                        sendTTSMessage(R.string.tts_begin);
                        mCurrentAction = REP_ACTION;
                        beginTimer(mNextSetTime, TTS_NO_DELAY);
                    } else { // Yes Rest
                        sendTTSMessage(R.string.tts_take_rest);
                        mCurrentAction = REST_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mRestTime, TTS_NO_DELAY);
                        else
                            beginTimer(mRestTime, TTS_REST_DELAY);
                    }

                } else if(mCurrentAction == REST_ACTION){ // Rest finished. Next Rep
                    //Rest finished. Start next rep.
                    nextRep();

                    //TODO: Custom Message
                    sendTTSMessage(R.string.tts_begin);
                    mCurrentAction = REP_ACTION;
                    if(mIsTTSMuted)
                        beginTimer(mNextSetTime, TTS_NO_DELAY);
                    else
                        beginTimer(mNextSetTime, 1);

                } else if(mCurrentAction == BREAK_ACTION){ // Break Finished. Next Round
                    //Break finished, start next round
                    nextRound();

                    sendTTSMessage(R.string.tts_next_round);
                    mCurrentAction = REP_ACTION;
                    if(mIsTTSMuted)
                        beginTimer(mNextSetTime, TTS_NO_DELAY);
                    else
                        beginTimer(mNextSetTime, TTS_REST_DELAY);
                } else {
                    //TODO: Something bad happened (?)
                    Log.e(DEBUG_TAG, "mTimeLeft is negative ?");
                }
            }

            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_UPDATE_TIME_DISPLAY, mTimeLeft, mTotalCurTime);
            sendMessage(msg);
        }
    };
    //endregion

    private void updateNotification(int type){
        //TODO: Update the notif
        int time = 0;
        String name = "";
        switch (type){
            case NOTIF_CURRENT:
                time = mTimeLeft;
                name = mSetName;
                break;
            case NOTIF_NEXT:
                mSetName = mNextSetName;
                mSetImage = mNextSetImage;
                name = mSetName;
                time = mNextSetTime;
                break;
            case NOTIF_REST:
                name = "Rest Time";
                time = mRestTime;
                break;
            case NOTIF_BREAK:
                name = "Break Time";
                time = mBreakTime;
                break;
        }
        mBuilder.setContentTitle(name)
                .setContentText(BaseApp.formatTime(time) + " left.")
                .setContentIntent(mPendingIntent)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(BaseApp.formatTime(time) + " left.")
                        .addLine(String.format(Locale.US, "Set: %d  Round: %d", mCurRep + 1, mCurRound + 1)));
        NotificationManagerCompat.from(this).notify(NOTIF_ID, mBuilder.build());

    }

    private void nextRep(){
        mCurRep++;
        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_REP, mCurRep, 0);
        sendMessage(msg);
        updateNotification(NOTIF_NEXT);
    }

    private void nextRound(){
        mCurRep = 0;
        mCurRound++;
        Message msg_round = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_ROUND, mCurRound, 0);
        sendMessage(msg_round);
        updateNotification(NOTIF_NEXT);
    }

    private void sendTTSMessage(int stringID){
        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_PASS_TTS_MSG, stringID, 0);
        sendMessage(msg);
    }

    private void sendMessage(Message msg){
        if(mIsMessengerBound) {
            try {
                mMyWorkoutActivityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static Intent getServiceIntent(Context context,
                                          Bundle notif,
                                          String name,
                                          int image,
                                          int setTime,
                                          int restTime,
                                          int breakTime,
                                          int reps,
                                          int rounds,
                                          boolean no_rest,
                                          boolean no_break,
                                          boolean muted){
        Intent intent = new Intent(context, TimerService.class);
        intent.putExtra(EXTRA_NOTIF_BUNDLE, notif);
        intent.putExtra(EXTRA_SET_TIME, setTime);
        intent.putExtra(EXTRA_SET_IMAGE, image);
        intent.putExtra(EXTRA_REST_TIME, restTime);
        intent.putExtra(EXTRA_BREAK_TIME, breakTime);
        intent.putExtra(EXTRA_NUM_REPS, reps);
        intent.putExtra(EXTRA_NUM_ROUNDS, rounds);
        intent.putExtra(EXTRA_NO_REST_FLAG, no_rest);
        intent.putExtra(EXTRA_NO_BREAK_FLAG, no_break);
        intent.putExtra(EXTRA_SET_NAME, name);
        intent.putExtra(EXTRA_IS_TTS_MUTED, muted);
        return intent;
    }

}
