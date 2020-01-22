package com.sentienhq.zeno.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.dataprovider.SearchProvider;

import com.sentienhq.zeno.R;

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
            SearchProvider searchProvider = ZenoApplication.getApplication(getContext()).getDataHandler().getSearchProvider();
            if (searchProvider != null) {
                searchProvider.reload();
            }
        }
    }
}
