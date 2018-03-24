package com.shibedays.workoutplanner.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

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
    public static final String EXTRA_REBUILD_BUNDLE = PACKAGE + "REBUILD";
    //endregion

    //region PRIVATE_VARS
    // Time Data
    private int mTotalSetTime;
    private int mTimeLeft;
    private int mRestTime;
    private int mBreakTime;
    private int mCurRep;
    private int mCurRound;
    private int mNumReps;
    private int mNumRounds;
    // Action Tracker
    private int mCurrentAction;
    // Notification Variables
    private Bundle mNotifBundle;
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
                    Message reply = Message.obtain(null, MyWorkoutActivity.MSG_UPDATE_FRAGMENT_UI_SERVICE_RUNNING);
                    try {
                        mMyWorkoutActivityMessenger.send(reply);
                    } catch (RemoteException e){
                        e.printStackTrace();
                    }
                    break;
                case MSG_NEXT_SET_TIME:
                    if(msg.arg1 > 0) {
                        setNextSetTime(msg.arg1);
                    }else{
                        Log.e(DEBUG_TAG, "NEXT SET TIME HAS INVALID ARG");
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
            mNotifBundle = intent.getBundleExtra(MyWorkoutActivity.EXTRA_NOTIF_BUNDLE);
            mTotalSetTime = intent.getIntExtra(EXTRA_SET_TIME, -1);
            mRestTime = intent.getIntExtra(EXTRA_REST_TIME, -1);
            mBreakTime = intent.getIntExtra(EXTRA_BREAK_TIME, -1);
            mNumReps = intent.getIntExtra(EXTRA_NUM_REPS, -1);
            mNumRounds = intent.getIntExtra(EXTRA_NUM_ROUNDS, -1);
        } else {
            // abort?
        }

        //region NOTIFICATION_BUILDING
        // Create the intent for rebuilding the activity
        Intent notifIntent = new Intent(this, MyWorkoutActivity.class);
        notifIntent.putExtra(EXTRA_REBUILD_BUNDLE, mNotifBundle);
        notifIntent.putExtra(MyWorkoutActivity.EXTRA_INTENT_TYPE, MyWorkoutActivity.NOTIF_INTENT_TYPE);
        // Create the pending intent that will bring us back to MyWorkoutActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIF_ID, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this, "MainTimerChannel");
        mBuilder.setContentTitle("Workout Timer")
                .setContentText("XX Minutes Left")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        startForeground(NOTIF_ID, mBuilder.build());
        //endregion

        sendTTSMessage(R.string.tts_starting);

        mCurRep = 0;
        mCurRound = 0;
        mCurrentAction = REP_ACTION;
        beginTimer(mTotalSetTime, TTS_STARTING_DELAY);

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
        mTimeLeft = time;
        mHandler.removeCallbacks(timer);
        mHandler.postDelayed(timer, ONE_SEC * delay);
    }

    public void setNextSetTime(int time){
        mTotalSetTime = time;
    }
    //endregion

    //region TIMER_LOOP
    private Runnable timer = new Runnable(){
        public void run(){
            mTimeLeft -= ONE_SEC;

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

                mHandler.postDelayed(this, ONE_SEC);
            } else if(mTimeLeft <= 0) { // Timer has finished
                Log.d(DEBUG_TAG, "Timer has finished.");

                if(mCurrentAction == REP_ACTION && mCurRep == (mNumReps - 1) && mCurRound != (mNumRounds - 1)) {  // Round Finished. Break

                    sendTTSMessage(R.string.tts_round_finished);

                    Message msg = Message.obtain(null, MyWorkoutActivity.MSG_GET_FIRST_SET, 0, 0);
                    sendMessage(msg);

                    mCurrentAction = BREAK_ACTION;
                    beginTimer(mBreakTime, TTS_BREAK_DELAY);

                } else if(mCurrentAction == REP_ACTION && mCurRep <= (mNumReps - 1) && mCurRound == (mNumRounds - 1)){ // Workout Finished. Finished
                    sendTTSMessage(R.string.tts_finished);

                    //TODO: Send us back to MyWorkoutActivity, unbind, and kill-self

                } else if(mCurrentAction == REP_ACTION && mCurRep < (mNumReps - 1 )){ // Set finished. Rest
                    //Repetition finished. Rest.
                    sendTTSMessage(R.string.tts_take_rest);

                    Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_SET_TIME, 0, 0);
                    sendMessage(msg);

                    mCurrentAction = REST_ACTION;
                    beginTimer(mRestTime, TTS_REST_DELAY);

                } else if(mCurrentAction == REST_ACTION){ // Rest finished. Next Rep
                    //Rest finished. Start next rep.
                    mCurRep++;
                    Message msg = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_REP_UI, mCurRep, 0);
                    sendMessage(msg);

                    sendTTSMessage(R.string.tts_begin);
                    updateNotification();

                    mCurrentAction = REP_ACTION;
                    if(mTotalSetTime > 0) {
                        beginTimer(mTotalSetTime, 1);
                    } else {
                        //TODO: mTotalSetTime was not set fast enough
                        Log.e(DEBUG_TAG, "mTotalSetTime was not set fast enough");
                    }
                } else if(mCurrentAction == BREAK_ACTION){ // Break Finished. Next Round
                    //Break finished, start next round
                    sendTTSMessage(R.string.tts_next_round);

                    mCurRound++;
                    Message msg_round = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_ROUND_UI, mCurRound, 0);
                    sendMessage(msg_round);

                    mCurRep = 0;
                    Message msg_rep = Message.obtain(null, MyWorkoutActivity.MSG_NEXT_REP_UI, mCurRep, 0);
                    sendMessage(msg_rep);

                    updateNotification();
                    mCurrentAction = REP_ACTION;
                    beginTimer(mTotalSetTime, 2);
                } else {
                    //TODO: Something bad happened (?)
                    Log.e(DEBUG_TAG, "mTimeLeft is negative ?");
                }
            }

            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_UPDATE_TIME_DISPLAY, mTimeLeft, 0);
            sendMessage(msg);
        }
    };
    //endregion

    private void updateNotification(){
        //TODO: Update the notif
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


}
