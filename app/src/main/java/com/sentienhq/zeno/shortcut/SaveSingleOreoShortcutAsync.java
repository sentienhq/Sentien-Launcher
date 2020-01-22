package com.sentienhq.zeno.shortcut;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sentienhq.zeno.DataHandler;
import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.pojo.ShortcutsPojo;
import com.sentienhq.zeno.utils.ShortcutUtil;

import java.lang.ref.WeakReference;

import com.sentienhq.zeno.R;

@TargetApi(Build.VERSION_CODES.O)
public class SaveSingleOreoShortcutAsync extends AsyncTask<Void, Integer, Boolean> {

    private static String TAG = "SaveAllOreoShortcutsAsync";
    private final WeakReference<Context> context;
    private final WeakReference<DataHandler> dataHandler;
    private Intent intent;

    public SaveSingleOreoShortcutAsync(@NonNull Context context, @NonNull Intent intent) {
        this.context = new WeakReference<>(context);
        this.dataHandler = new WeakReference<>(ZenoApplication.getApplication(context).getDataHandler());
        this.intent = intent;
    }


    @Override
    protected Boolean doInBackground(Void... voids) {

        final LauncherApps.PinItemRequest pinItemRequest = intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST);
        final ShortcutInfo shortcutInfo = pinItemRequest.getShortcutInfo();

        if (shortcutInfo == null) {
            cancel(true);
            return null;
        }

        Context context = this.context.get();
        if (context == null) {
            cancel(true);
            return null;
        }

        // Create Pojo
        ShortcutsPojo pojo = ShortcutUtil.createShortcutPojo(context, shortcutInfo, false);
        if (pojo == null) {
            return false;
        }

        final DataHandler dataHandler = this.dataHandler.get();
        if (dataHandler == null) {
            cancel(true);
            return null;
        }

        // Add shortcut to the DataHandler
        if (dataHandler.addShortcut(pojo)) {
            pinItemRequest.accept();
            return true;
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (progress[0] == -1) {
            Toast.makeText(context.get(), R.string.cant_pin_shortcut, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostExecute(@NonNull Boolean success) {
        if (success) {
            Log.i(TAG, "Shortcut added to KISS");

            if (this.dataHandler.get().getShortcutsProvider() != null) {
                this.dataHandler.get().getShortcutsProvider().reload();
            }
        }
    }

}
