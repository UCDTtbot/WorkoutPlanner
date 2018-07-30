package com.shibedays.workoutplanner.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TTSService extends Service implements TextToSpeech.OnInitListener {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = TTSService.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.services.TTSService.";
    // TTS Constants
    private static final String SPEECH_ID = "WorkoutPlanner.TTS";
    // Message Constants
    public static final int MSG_TTS_BIND = 0;
    public static final int MSG_SPEAK = 1;
    public static final int MSG_STOP_SPEECH = 2;
    public static final int MSG_MUTE_SPEECH = 3;
    public static final int MSG_UNMUTE_SPEECH = 4;
    //endregion

    //region PRIVATE_VARS
    // TTS Object(s)
    private TextToSpeech mTTS;

    private int mVol;

    // Booleans
    private boolean mTTSReady = false;
    private boolean mIsMuted = false;
    private String mVoiceType = "en-us-x-sfg#female_1-local";
    // Instances
    private Messenger mMyWorkoutActivityMessenger;
    //endregion

    //region MESSAGE_HANDLER
    // The Messenger (originally binder) given to clients
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TTS_BIND:
                    mMyWorkoutActivityMessenger = msg.replyTo;
                    break;
                case MSG_SPEAK:
                    int strID = msg.arg1;
                    if (strID >= 0) {
                        speak(getResources().getString(strID));
                    } else {
                        Log.e(DEBUG_TAG, "MSG_SPEAK arg1 empty");
                    }
                    break;
                case MSG_STOP_SPEECH:
                    mTTS.stop();
                    break;
                case MSG_MUTE_SPEECH:
                    mIsMuted = true;
                    break;
                case MSG_UNMUTE_SPEECH:
                    mIsMuted = false;
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
                Log.d(DEBUG_TAG, Integer.toString(mVol));
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

        mTTS = new TextToSpeech(getApplicationContext(), this);
        adjustSpeechRate(0.7f);

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(DEBUG_TAG, "TTS ON_UNBIND CALLED");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "TTS ON_DESTROY CALLED");
        if(mTTS != null) {
            mTTS.shutdown();
        }
    }

    //endregion

    //region UTILITY
    public void speak(String speech) {
        if(mTTSReady && !mIsMuted){
            mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null, SPEECH_ID);
        }
    }
    private void adjustSpeechRate(float rate){
        mTTS.setSpeechRate(rate);
    }

    private void setupVoiceType(String type) {
        // en-us-x-sfg#male_1-local
        // en-us-x-sfg#male_2-local
        // en-us-x-sfg#female_1-local
        // en-us-x-sfg#female_2-local
        switch (type) {
            case "M1":
                mVoiceType = "en-us-x-sfg#male_1-local";
                break;
            case "M2":
                mVoiceType = "en-us-x-sfg#male_2-local";
                break;
            case "F1":
                mVoiceType = "en-us-x-sfg#female_1-local";
                break;
            case "F2":
                mVoiceType = "en-us-x-sfg#female_2-local";
                break;
            default:
                mVoiceType = "en-us-x-sfg#female_1-local";
                break;
        }
        Set<String> a = new HashSet<>();
        a.add("male");//here you can give male if you want to select mail voice.
        Voice v = new Voice(mVoiceType, new Locale("en", "US"), 400, 200, true, a);
        int result = mTTS.setVoice(v);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Intent installIntent = new Intent();
            installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);        }
    }
}
