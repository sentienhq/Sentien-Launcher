package com.sentienhq.zeno.forwarder;

import android.content.SharedPreferences;

import com.sentienhq.zeno.MainActivity;

abstract class Forwarder {
    final MainActivity mainActivity;
    final SharedPreferences prefs;

    Forwarder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.prefs = mainActivity.prefs;
    }
}