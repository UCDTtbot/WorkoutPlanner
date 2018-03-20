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
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_SET_TIME = PACKAGE + "SET_TIME";
    public static final String EXTRA_REST_TIME = PACKAGE + "REST_TIME";
    public static final String EXTRA_BREAK_TIME = PACKAGE + "BREAK_TIME";
    public static final String EXTRA_REBUILD_BUNDLE = PACKAGE + "REBUILD";
    //endregion

    //region PRIVATE_VARS
    // Time Data
    private int mTotalSetTime;
    private int mTimeLeft;
    private int mRestTime;
    private int mBreakTime;
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
        Log.d(DEBUG_TAG, "TimerService onStartCommand called");


        // TODO: Receive the bundle that contains the notification information and use it to build the notif
        if(intent != null){
            mNotifBundle = intent.getBundleExtra(MyWorkoutActivity.EXTRA_NOTIF_BUNDLE);
            mTotalSetTime = intent.getIntExtra(EXTRA_SET_TIME, -1);
            mRestTime = intent.getIntExtra(EXTRA_REST_TIME, -1);
            mBreakTime = intent.getIntExtra(EXTRA_BREAK_TIME, -1);
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

        Message msg = Message.obtain(null, MyWorkoutActivity.MSG_PASS_TTS_MSG, R.string.tts_starting, 0);
        sendMessage(msg);

        mTimeLeft = mTotalSetTime;
        mCurrentAction = REP_ACTION;
        mHandler.removeCallbacks(timer);
        mHandler.postDelayed(timer, ONE_SEC * 6);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_DESTROY CALLED");
        //stopForeground(true);
        //stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(DEBUG_TAG, "TIMER SERVICE ON_UNBIND CALLED");
        mHandler.removeCallbacks(timer);
        stopForeground(true);
        stopSelf();
        return super.onUnbind(intent);
    }

    //endregion

    //region UTILITY
    private void continueTimer(int time){
        mTimeLeft = time;
        mHandler.removeCallbacks(timer);
        mHandler.postDelayed(timer, ONE_SEC);
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
                    // REST ENDING SOON
                }
                if(mTimeLeft == 5000) {
                    // 5 SECS LEFT
                }
                if(mTimeLeft == 4000) {
                    // 4 SECS LEFT
                }
                if(mTimeLeft == 3000) {
                    // 3 SECS LEFT
                }
                if(mTimeLeft == 2000) {
                    // 2 SECS LEFT
                }
                if(mTimeLeft == 1000) {
                    // 1 SECS LEFT
                }

                mHandler.postDelayed(this, ONE_SEC);
            } else if(mTimeLeft <= 0) { // Timer has finished
                Log.d(DEBUG_TAG, "Timer has finished.");

            }

            Message msg = Message.obtain(null, MyWorkoutActivity.MSG_UPDATE_TIME_DISPLAY, mTimeLeft, 0);
            sendMessage(msg);
        }
    };
    //endregion

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
