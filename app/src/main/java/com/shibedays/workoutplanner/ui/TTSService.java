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

    private static final String DEBUG_TAG = TTSService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.TTSService.";

    private static final String SPEECH_ID = "WorkoutPlanner.TTS";

    // Binder given to clients
    private final IBinder mBinder = new TTSBinder();

    private TextToSpeech mTTS;

    private boolean mTTSReady = false;

    public class TTSBinder extends Binder {
        TTSService getService() {
            return TTSService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {

        mTTS = new TextToSpeech(this, this);
        adjustSpeechRate(0.7f);

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Keep the service sticky
        Log.d(DEBUG_TAG, "TTS onStartCommand called");
        return START_STICKY;
    }

    private void adjustSpeechRate(float rate){
        mTTS.setSpeechRate(rate);
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
    public void onDestroy() {
        super.onDestroy();
        if(mTTS != null) {
            mTTS.shutdown();
        }
    }

    public void speak(String speech) {
        if(mTTSReady){
            mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null, SPEECH_ID);
        }
    }
}
