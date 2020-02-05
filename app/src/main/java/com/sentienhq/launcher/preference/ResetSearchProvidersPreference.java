package com.sentienhq.launcher.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.dataprovider.SearchProvider;

import com.sentienhq.launcher.R;

public class ResetSearchProvidersPreference extends DialogPreference {

    public ResetSearchProvidersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .remove("available-search-providers").apply();
            Toast.makeText(getContext(), R.string.search_provider_reset_done_desc, Toast.LENGTH_LONG).show();
            SearchProvider searchProvider = LauncherApplication.getApplication(getContext()).getDataHandler().getSearchProvider();
            if (searchProvider != null) {
                searchProvider.reload();
            }
        }
    }
}
