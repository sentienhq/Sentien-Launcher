package com.sentienhq.launcher.shortcut;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sentienhq.launcher.DataHandler;
import com.sentienhq.launcher.R;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.pojo.AppPojo;
import com.sentienhq.launcher.pojo.ShortcutsPojo;
import com.sentienhq.launcher.utils.ShortcutUtil;
import com.sentienhq.launcher.utils.UserHandle;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.O)
public class SaveAllOreoShortcutsAsync extends AsyncTask<Void, Integer, Boolean> {

    private static String TAG = "SaveAllOreoShortcutsAsync";
    private final WeakReference<Context> context;
    private final WeakReference<DataHandler> dataHandler;

    public SaveAllOreoShortcutsAsync(@NonNull Context context) {
        this.context = new WeakReference<>(context);
        this.dataHandler = new WeakReference<>(LauncherApplication.getApplication(context).getDataHandler());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Context context = this.context.get();
        if (context == null) {
            cancel(true);
            return null;
        }

        List<ShortcutInfo> shortcuts;
        try {
            // Fetch list of all shortcuts
            shortcuts = ShortcutUtil.getAllShortcuts(context);
        } catch (SecurityException e) {
            e.printStackTrace();

            // Publish progress (display toast)
            publishProgress(-1);

            // Set flag to true, so we can rerun this class
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("first-run-shortcuts", true).apply();

            cancel(true);
            return null;
        }

        final DataHandler dataHandler = this.dataHandler.get();
        if (dataHandler == null) {
            cancel(true);
            return null;
        }

        Set<String> excludedAppList = dataHandler.getExcluded();
        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);

        for (ShortcutInfo shortcutInfo : shortcuts) {

            UserHandle user = new UserHandle(manager.getSerialNumberForUser(shortcutInfo.getUserHandle()), shortcutInfo.getUserHandle());
            boolean isExcluded = excludedAppList.contains(AppPojo.getComponentName(shortcutInfo.getPackage(),
                    shortcutInfo.getActivity().getClassName(), user));

            // Skip shortcut if app is excluded
            if (!excludedAppList.isEmpty() &&
                    isExcluded) {
                continue;
            }

            // Create Pojo
            ShortcutsPojo pojo = ShortcutUtil.createShortcutPojo(context, shortcutInfo, !shortcutInfo.isPinned());
            if (pojo == null) {
                continue;
            }

            // Add shortcut to the DataHandler
            dataHandler.addShortcut(pojo);
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (progress[0] == -1) {
            Toast.makeText(context.get(), R.string.cant_save_shortcuts, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostExecute(@NonNull Boolean success) {
        if (success) {
            Log.i(TAG, "Shortcuts added to KISS");

            if (this.dataHandler.get().getShortcutsProvider() != null) {
                this.dataHandler.get().getShortcutsProvider().reload();
            }
        }
    }

}
