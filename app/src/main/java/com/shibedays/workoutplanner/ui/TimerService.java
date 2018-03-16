package com.shibedays.workoutplanner.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.shibedays.workoutplanner.R;

public class TimerService extends Service {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = TimerService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.TimerService.";
    // Notification ID
    private static final int NOTIF_ID = 1;
    private static final long ONE_SEC = 1000;
    // Actions
    public static final int REP_ACTION = 0;
    public static final int REST_ACTION = 1;
    public static final int BREAK_ACTION = 2;
    public static final int STOP_ACTION = 3;
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
    //endregion

    //region PUBLIC_VARS

    //endregion

    //region BINDER
    // Binder given to clients
    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }
    private final IBinder mBinder = new TimerBinder();
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

        // Send broadcast that the service is starting
        Intent brdIntent = new Intent(MyWorkoutActivity.FILTER);
        brdIntent.putExtra(MyWorkoutActivity.EXTRA_UPDATE_DESCRIPTION, "Service has Started");
        LocalBroadcastManager.getInstance(this).sendBroadcast(brdIntent);

        mCurrentAction = REP_ACTION;
        beginTimer(mTotalSetTime);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //endregion

    //region UTILITY
    private void beginTimer(int time){
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
        }
    };
    //endregion



}
