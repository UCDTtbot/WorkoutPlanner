package com.shibedays.workoutplanner.ui;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.w3c.dom.Text;

import java.util.Locale;

public class TTSService extends Service implements TextToSpeech.OnInitListener {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = TTSService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.TTSService.";
    // TTS Constants
    private static final String SPEECH_ID = "WorkoutPlanner.TTS";
    //endregion

    //region PRIVATE_VARS
    // TTS Object(s)
    private TextToSpeech mTTS;
    // Booleans
    private boolean mTTSReady = false;
    //endregion

    //region BINDER
    // Binder given to clients
    public class TTSBinder extends Binder {
        TTSService getService() {
            return TTSService.this;
        }
    }
    private final IBinder mBinder = new TTSBinder();
    //endregion

    //region LIFECYCLE

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Keep the service sticky
        Log.d(DEBUG_TAG, "TTS onStartCommand called");
        return START_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            int result = mTTS.setLanguage(Locale.US);

            if(result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED){
                Log.d(DEBUG_TAG, "TTS up and running");
                mTTSReady = true;
            } else {
                Log.e(DEBUG_TAG, "Missing language data or unsupported");
            }
        } else {
            Log.e(DEBUG_TAG, "TTS Failed to Start");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        mTTS = new TextToSpeech(this, this);
        adjustSpeechRate(0.7f);

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTTS != null) {
            mTTS.shutdown();
        }
    }

    //endregion

    //region UTILITY
    public void speak(String speech) {
        if(mTTSReady){
            mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null, SPEECH_ID);
        }
    }
    private void adjustSpeechRate(float rate){
        mTTS.setSpeechRate(rate);
    }
    //endregion
}
