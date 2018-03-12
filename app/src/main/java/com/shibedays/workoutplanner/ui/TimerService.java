package com.shibedays.workoutplanner.ui;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TimerService extends Service {

    private static final String DEBUG_TAG = TimerService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.TimerService.";

    // Binder given to clients

    private int mTotalTime;
    private int mCurTime;

    private Bundle mNotifBundle;

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }
    private final IBinder mBinder = new TimerBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG_TAG, "TimerService onStartCommand called");

        if(intent != null){
            mNotifBundle = intent.getBundleExtra(MyWorkoutActivity.EXTRA_INTENT_BUNDLE);
        }

        Intent brdIntent = new Intent(MyWorkoutActivity.FILTER);
        brdIntent.putExtra(MyWorkoutActivity.EXTRA_WORKOUT_ID, 10);
        LocalBroadcastManager.getInstance(this).sendBroadcast(brdIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
