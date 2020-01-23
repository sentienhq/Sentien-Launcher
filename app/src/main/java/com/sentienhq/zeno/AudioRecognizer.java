package com.sentienhq.zeno;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;


public class AudioRecognizer {

    private Context context;

    AudioRecognizer(Context context) {
        this.context = context;
    }

    void startListening() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("RESULT", "You can speak now");

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i("RESULT", "Runnable ran");

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        context.getPackageName());

                SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
                SpeechRecognitionListener listener = new SpeechRecognitionListener(context);
                mSpeechRecognizer.setRecognitionListener(listener);
                mSpeechRecognizer.startListening(intent);
            }
        };
        mainHandler.post(myRunnable);
    }
}

class SpeechRecognitionListener implements RecognitionListener {

    private Context context;

    SpeechRecognitionListener(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null)");
        }
        this.context = context;
    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.i("RESULT", "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("RESULT", "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        Log.i("RESULT", "onError: " + error);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onReadyForSpeech(Bundle params)
    {
        Log.d("RESULT SpeechRecogniser", "onReadyForSpeech"); //$NON-NLS-1$
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.i("RESULT", String.valueOf(results));
        String note = matches.get(0);
        Log.i("RESULT_FIRST", note);
        TextView searchEditText = ((Activity) context).findViewById(R.id.searchEditText);
        searchEditText.setText(note);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }
}


