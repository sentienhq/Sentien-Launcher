package com.sentienhq.zeno.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.dataprovider.AppProvider;
import com.sentienhq.zeno.utils.UserHandle;

/**
 * This class gets called when an application is created or removed on the
 * system
 * <p/>
 * We then recreate our data set.
 *
 * @author dorvaryn
 */
public class PackageAddedRemovedHandler extends BroadcastReceiver {

    public static void handleEvent(Context ctx, String action, String packageName, UserHandle user, boolean replacing) {
        if (PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("enable-app-history", true)) {
            // Insert into history new packages (not updated ones)
            if ("android.intent.action.PACKAGE_ADDED".equals(action) && !replacing) {
                // Add new package to history
                Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent == null) {//for some plugin app
                    return;
                }

                String className = launchIntent.getComponent().getClassName();
                String pojoID = user.addUserSuffixToString("app://" + packageName + "/" + className, '/');
                ZenoApplication.getApplication(ctx).getDataHandler().addToHistory(pojoID);
                // Add shortcut
                ZenoApplication.getApplication(ctx).getDataHandler().addShortcut(packageName);
            }
        }

        if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !replacing) {
            // Remove all installed shortcuts
            ZenoApplication.getApplication(ctx).getDataHandler().removeShortcuts(packageName);
            ZenoApplication.getApplication(ctx).getDataHandler().removeFromExcluded(packageName);
        }

        ZenoApplication.getApplication(ctx).resetIconsHandler();

        // Reload application list
        final AppProvider provider = ZenoApplication.getApplication(ctx).getDataHandler().getAppProvider();
        if (provider != null) {
            provider.reload();
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {

        String packageName = intent.getData().getSchemeSpecificPart();

        if (packageName.equalsIgnoreCase(ctx.getPackageName())) {
            // When running KISS locally, sending a new version of the APK immediately triggers a "package removed" for fr.sentienhq.zeno,
            // There is no need to handle this event.
            // Discarding it makes startup time much faster locally as apps don't have to be loaded twice.
            return;
        }

        handleEvent(ctx,
                intent.getAction(),
                packageName, new UserHandle(),
                intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        );

    }

}
