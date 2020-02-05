package com.sentienhq.launcher.forwarder;

import android.content.SharedPreferences;

import com.sentienhq.launcher.MainActivity;

abstract class Forwarder {
    final MainActivity mainActivity;
    final SharedPreferences prefs;

    Forwarder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.prefs = mainActivity.prefs;
    }
}