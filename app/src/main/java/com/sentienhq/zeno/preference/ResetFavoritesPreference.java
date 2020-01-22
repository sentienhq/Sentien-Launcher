package com.sentienhq.zeno.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.sentienhq.zeno.ZenoApplication;

import com.sentienhq.zeno.R;

public class ResetFavoritesPreference extends DialogPreference {

    public ResetFavoritesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressWarnings("CatchAndPrintStackTrace")
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putString("favorite-apps-list", "").apply();

            try {
                ZenoApplication.getApplication(getContext()).getDataHandler().getAppProvider().reload();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            Toast.makeText(getContext(), R.string.favorites_erased, Toast.LENGTH_LONG).show();
        }

    }

}
