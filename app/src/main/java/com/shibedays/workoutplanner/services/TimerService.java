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
    public static final String NOTIF_CHANNEL = "N_TIMER";
    // Notification ID
    private static final int NOTIF_ID = 1;
    private static final long ONE_SEC = 1000;
    // Actions
    public static final int REP_ACTION = 0;
    public static final int REST_ACTION = 1;
    public static final int BREAK_ACTION = 2;
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
    public final int NOTIF_PLAY_ACTION = 4;

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
    private boolean mMsgSent;
    // Action Tracker
    private int mCurrentAction;
    // Notification Variables
    private Bundle mNotifBundle;
    // Threading Handler
    private Handler mHandler = new Handler();
    // Notification Variables
    private NotificationCompat.Builder mBuilder;
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
                        mMsgSent = true;
                    }else{
                        Log.e(DEBUG_TAG, "NEXT SET TIME HAS INVALID ARG");
                    }
                    break;
                case MSG_PAUSE_TIMER:
                    if(mHandler != null){
                        mHandler.removeCallbacks(timer);
                        updateNotification(NOTIF_CURRENT, NOTIF_PLAY_ACTION, false);
                        Log.d(DEBUG_TAG, "Timer (should have been) paused.");
                    }
                    break;
                case MSG_CONTINUE_TIMER:
                    if(mHandler != null){
                        mHandler.removeCallbacks(timer);
                        updateNotification(NOTIF_CURRENT, 0, false);
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

        if(intent != null){
            mNotifBundle = intent.getBundleExtra(EXTRA_NOTIF_BUNDLE);
            mTotalCurTime = intent.getIntExtra(EXTRA_SET_TIME, -1);
            mTimeLeft = mTotalCurTime;
            mRestTime = intent.getIntExtra(EXTRA_REST_TIME, -1);
            mBreakTime = intent.getIntExtra(EXTRA_BREAK_TIME, -1);
            mNumReps = intent.getIntExtra(EXTRA_NUM_REPS, -1);
            mNumRounds = intent.getIntExtra(EXTRA_NUM_ROUNDS, -1);
            mNoRestFlag = intent.getBooleanExtra(EXTRA_NO_REST_FLAG, false);
            mNoBreakFlag = intent.getBooleanExtra(EXTRA_NO_BREAK_FLAG, false);
            mSetName = intent.getStringExtra(EXTRA_SET_NAME);
            mSetImage = intent.getIntExtra(EXTRA_SET_IMAGE, R.drawable.ic_fitness_black_24dp);
            mIsTTSMuted = intent.getBooleanExtra(EXTRA_IS_TTS_MUTED, false);
        }
        //region NOTIFICATION_BUILDING
        // Create the intent for rebuilding the activity
        updateNotification(NOTIF_CURRENT, 0, true);
        //endregion

        sendTTSMessage(R.string.tts_starting, "");

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

        if(mCurrentAction == REST_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_LOAD_NEXT_SET, 0, 0);
            sendMessage(msg);
            updateNotification(NOTIF_REST, 0, false);
        } else if(mCurrentAction == BREAK_ACTION){
            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_LOAD_FIRST_SET, 0, 0);
            sendMessage(msg);
            updateNotification(NOTIF_BREAK, 0, false);
        }

        mMsgSent = false;
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

                if (!mMsgSent && mTimeLeft <= 15000 && mNoRestFlag && mCurrentAction == REP_ACTION) {
                    if (mCurRep != mNumReps - 1 && !mNoBreakFlag) {
                        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NO_REST_NEXT_SET, 0, 0);
                        sendMessage(msg);
                    } else if(mCurRep != mNumReps - 1 && mNoBreakFlag){
                        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NO_REST_NEXT_SET, 0, 0);
                        sendMessage(msg);
                    }
                    mMsgSent = true;
                }

                if(mTimeLeft == 7000 && mCurrentAction == REST_ACTION){
                    sendTTSMessage(R.string.tts_rest_ending, "");
                }
                if(mTimeLeft == 5000) {
                    sendTTSMessage(R.string.tts_five, "");
                }
                if(mTimeLeft == 4000) {
                    sendTTSMessage(R.string.tts_four, "");
                }
                if(mTimeLeft == 3000) {
                    sendTTSMessage(R.string.tts_three, "");
                }
                if(mTimeLeft == 2000) {
                    sendTTSMessage(R.string.tts_two, "");
                }
                if(mTimeLeft == 1000) {
                    sendTTSMessage(R.string.tts_one, "");
                }

                if(mTimeLeft % 5000 == 0){
                    if(mCurrentAction == REST_ACTION)
                        updateNotification(NOTIF_REST, 0, false);
                    else if (mCurrentAction == BREAK_ACTION)
                        updateNotification(NOTIF_BREAK, 0, false);
                    else
                        updateNotification(NOTIF_CURRENT, 0, false);
                }

                mHandler.postDelayed(this, ONE_SEC);
            } else { // Timer has finished
                Log.d(DEBUG_TAG, "Timer has finished.");

                if(mCurrentAction == REP_ACTION && mCurRep == (mNumReps - 1) && mCurRound != (mNumRounds - 1)) {  // Round Finished. Break
                    if(mNoBreakFlag){ // No Break
                        nextRound();

                        sendTTSMessage(R.string.tts_next_round, mSetName);
                        mCurrentAction = REP_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mNextSetTime, TTS_NO_DELAY);
                        else
                            beginTimer(mNextSetTime, 1);

                    } else {    // Yes Break
                        sendTTSMessage(R.string.tts_round_finished, "");
                        mCurrentAction = BREAK_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mBreakTime, TTS_NO_DELAY);
                        else
                            beginTimer(mBreakTime, TTS_BREAK_DELAY);
                    }
                } else if(mCurrentAction == REP_ACTION && mCurRep == (mNumReps - 1) && mCurRound == (mNumRounds - 1)){ // Workout Finished. Finished
                    sendTTSMessage(R.string.tts_finished, "");

                    //TODO: Send us back to MyWorkoutActivity, unbind, and kill-self

                } else if(mCurrentAction == REP_ACTION && mCurRep < (mNumReps - 1 )){ // Set finished. Rest
                    //Repetition finished. Rest.

                    if(mNoRestFlag){ // No Rest
                        nextRep();

                        sendTTSMessage(R.string.tts_begin, mSetName);
                        mCurrentAction = REP_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mNextSetTime, TTS_NO_DELAY);
                        else
                            beginTimer(mNextSetTime, 1);
                    } else { // Yes Rest
                        sendTTSMessage(R.string.tts_take_rest, "");
                        mCurrentAction = REST_ACTION;
                        if(mIsTTSMuted)
                            beginTimer(mRestTime, TTS_NO_DELAY);
                        else
                            beginTimer(mRestTime, TTS_REST_DELAY);
                    }

                } else if(mCurrentAction == REST_ACTION){ // Rest finished. Next Rep
                    //Rest finished. Start next rep.
                    nextRep();

                    sendTTSMessage(R.string.tts_begin, mNextSetName);
                    mCurrentAction = REP_ACTION;
                    if(mIsTTSMuted)
                        beginTimer(mNextSetTime, TTS_NO_DELAY);
                    else
                        beginTimer(mNextSetTime, 1);

                } else if(mCurrentAction == BREAK_ACTION){ // Break Finished. Next Round
                    //Break finished, start next round
                    nextRound();

                    sendTTSMessage(R.string.tts_next_round, mSetName);
                    mCurrentAction = REP_ACTION;
                    if(mIsTTSMuted)
                        beginTimer(mNextSetTime, TTS_NO_DELAY);
                    else
                        beginTimer(mNextSetTime, TTS_REST_DELAY);
                } else {
                    Log.e(DEBUG_TAG, "mTimeLeft is negative ?");
                }
            }

            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_UPDATE_TIME_DISPLAY, mTimeLeft, mTotalCurTime);
            sendMessage(msg);
        }
    };
    //endregion

    private void updateNotification(int type, int notifFlags, boolean isFirstRun){

        if(mBuilder == null)
            mBuilder = new NotificationCompat.Builder(this, NOTIF_CHANNEL);

        Intent notifIntent = new Intent(this, MyWorkoutActivity.class);
        notifIntent.putExtra(EXTRA_REBUILD_BUNDLE, mNotifBundle);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifIntent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NOTIF_INTENT_TYPE);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, NOTIF_ID, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this, "MainTimerChannel");
        Bitmap b = BitmapFactory.decodeResource(getResources(), mSetImage);

        int time = mTotalCurTime;
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
                time = mTimeLeft;
                break;
            case NOTIF_BREAK:
                name = "Break Time";
                time = mTimeLeft;
                break;
        }
        mBuilder.setContentTitle(name)
                .setContentText(BaseApp.formatTime(time) + " left.")
                .setContentIntent(mainPendingIntent)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(BaseApp.formatTime(time) + " left.")
                        .addLine(String.format(Locale.US, "Set: %d  Round: %d", mCurRep + 1, mCurRound + 1)));
        if(notifFlags == NOTIF_PLAY_ACTION){
            Intent playIntent = new Intent();
            playIntent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.PLAY_INTENT_TYPE);
            playIntent.setAction(MyWorkoutActivity.FILTER_TIMER);
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(R.drawable.ic_play_arrow_black_24dp, "Play", playPendingIntent);
        } else {
            Intent pauseIntent = new Intent();
            pauseIntent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.PAUSE_INTENT_TYPE);
            pauseIntent.setAction(MyWorkoutActivity.FILTER_TIMER);
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(R.drawable.ic_pause_black_24dp, "Pause", pausePendingIntent);

        }

        if(isFirstRun){
            startForeground(NOTIF_ID, mBuilder.build());
        } else {
            NotificationManagerCompat.from(this).notify(NOTIF_ID, mBuilder.build());
        }
    }

    private void nextRep(){
        mCurRep++;
        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_REP, mCurRep, 0);
        sendMessage(msg);
        updateNotification(NOTIF_NEXT, 0, false);
    }

    private void nextRound(){
        Message msg_round;
        mCurRep = 0;
        mCurRound++;
        if(mNoBreakFlag){
            msg_round = Message.obtain(null, MyWorkoutActivity.MSG_NO_BREAK_NEXT_ROUND, mCurRound, 0);
        } else {
            msg_round = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_ROUND, mCurRound, 0);
        }
        sendMessage(msg_round);
        updateNotification(NOTIF_NEXT, 0, false);
    }

    private void sendTTSMessage(int stringID, String extra){
        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_PASS_TTS_MSG, stringID, 0, extra);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mHandler.removeCallbacks(timer);
        stopForeground(true);
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
