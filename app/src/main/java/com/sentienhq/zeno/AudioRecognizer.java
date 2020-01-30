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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class AudioRecognizer {

    private Context context;
    Intent audioIntent;
    SpeechRecognizer mSpeechRecognizer;
    SpeechRecognitionListener listener;

    AudioRecognizer(Context context) {
        this.context = context;
        audioIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        audioIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        audioIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        listener = new SpeechRecognitionListener(context);
    }

    void startListening() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            throw e; // New in Java 8
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mSpeechRecognizer.setRecognitionListener(listener);
                mSpeechRecognizer.startListening(audioIntent);
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
        MainActivity activity = ((MainActivity) context);
        activity.displayLoader(false);
        String issue;
        switch (error) { // todo move strings from code to resource files and translations.
            case 6: issue = "No speech input"; break;
            case 4 : issue = "Server sends error status"; break;
            case 8 : issue = "RecognitionService busy."; break;
            case 7 : issue = "No recognition result matched."; break;
            case 1 : issue = "Network operation timed out."; break;
            case 2 : issue = "Other network related errors."; break;
            case 9 : issue = "Insufficient permissions"; break;
            case 5 : issue = " Other client side errors."; break;
            case 3 : issue = "Audio recording error."; break;
            default: issue = "Unknown error!"; break;
        }
        Toast.makeText(this.context, issue, Toast.LENGTH_LONG).show();
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
        String result = null;
        if (matches != null && matches.get(0) != null) {
            result = matches.get(0);
            TextView searchEditText = ((Activity) context).findViewById(R.id.searchEditText);
            searchEditText.setText(result);
        }

    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }
}

//Poors man audio recognition
class AudioCommand {

    private static String[][] commands={{"open", "start", "begin", "launch", "initiate", "begin with"},
            {"call", "dial", "i want to speak with", "call out a", "call a", "call me", "call to", "dial me"},
            {"message", "text", "message to", "text to", "write to", "write", "text message", "send message to", "write message to"},
            {"navigate", "navigate me to", "navigate to", "guide to", "find way to", "find way", "find on map", "route", "route to", "way to", "show the way", "show the way to", "show me the way to", "drive me to", "drive to", "go to"},
            {"search", "find", "google", "search for", "find me", "i am lookig for", "find me online", "search on the internet" , "find me the"}};

    public static ArrayList<String> parse(String query) {

        ArrayList<String> result = new ArrayList<String>();

        int phraseSize = 0;
        int tempResult = 0;
        String tempPhrase = "";

        for (int i = 0; i < commands.length; i ++) {
            if (tempResult != 0) {
                result.add(String.valueOf(tempResult));
                result.add(tempPhrase);
                return result;
            } else {
                for (String phrase : commands[i]) {
                    if (query.contains(phrase + " ") && phrase.split(" ").length > phraseSize) {
                        String[] splitted = query.split(phrase + " ", 2);
                        if (splitted.length > 1) {
                            phraseSize = phrase.split(" ").length;
                            tempResult = i + 1;
                            tempPhrase = query.split(phrase + " ", 2)[1];
                        }
                    }
                }
            }
        }

        result.add(String.valueOf(tempResult));
        result.add(tempPhrase);
        return result;
    }
}


