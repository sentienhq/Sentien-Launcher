package com.sentienhq.launcher.result;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sentienhq.launcher.pojo.SettingsPojo;
import com.sentienhq.launcher.utils.FuzzyScore;

import com.sentienhq.launcher.R;

public class SettingsResult extends Result {
    private final SettingsPojo settingPojo;

    SettingsResult(SettingsPojo settingPojo) {
        super(settingPojo);
        this.settingPojo = settingPojo;
    }

    @NonNull
    @Override
    public View display(Context context, int position, View v, @NonNull ViewGroup parent, FuzzyScore fuzzyScore) {
        if (v == null)
            v = inflateFromId(context, R.layout.item_setting, parent);

        TextView settingName = v.findViewById(R.id.item_setting_name);
        displayHighlighted(settingPojo.normalizedName, settingPojo.getName(), fuzzyScore, settingName, context);

        ImageView settingIcon = v.findViewById(R.id.item_setting_icon);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("icons-hide", false)) {
            settingIcon.setImageDrawable(getDrawable(context));
            settingIcon.setColorFilter(getThemeFillColor(context), Mode.SRC_IN);
        } else {
            settingIcon.setImageDrawable(null);
        }

        return v;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Drawable getDrawable(Context context) {
        if (settingPojo.icon != -1) {
            return context.getResources().getDrawable(settingPojo.icon);
        }

        return null;
    }

    @Override
    public void doLaunch(Context context, View v) {
        Intent intent = new Intent(settingPojo.settingName);
        if (!settingPojo.packageName.isEmpty()) {
            intent.setClassName(settingPojo.packageName, settingPojo.settingName);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.setSourceBounds(v.getClipBounds());
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.application_not_found, Toast.LENGTH_LONG).show();
        }
    }
}
