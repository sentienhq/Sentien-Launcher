package com.sentienhq.zeno.loader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.UserManager;
import android.util.Log;

import com.sentienhq.zeno.TagsHandler;
import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.pojo.AppPojo;
import com.sentienhq.zeno.utils.UserHandle;

import java.util.ArrayList;
import java.util.Set;

public class LoadAppPojos extends LoadPojos<AppPojo> {

    private final TagsHandler tagsHandler;

    public LoadAppPojos(Context context) {
        super(context, "app://");
        tagsHandler = ZenoApplication.getApplication(context).getDataHandler().getTagsHandler();
    }

    @Override
    protected ArrayList<AppPojo> doInBackground(Void... params) {
        long start = System.nanoTime();

        ArrayList<AppPojo> apps = new ArrayList<>();

        Context ctx = context.get();
        if (ctx == null) {
            return apps;
        }

        Set<String> excludedAppList = ZenoApplication.getApplication(ctx).getDataHandler().getExcluded();
        Set<String> excludedFromHistoryAppList = ZenoApplication.getApplication(ctx).getDataHandler().getExcludedFromHistory();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UserManager manager = (UserManager) ctx.getSystemService(Context.USER_SERVICE);
            LauncherApps launcher = (LauncherApps) ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE);

            // Handle multi-profile support introduced in Android 5 (#542)
            for (android.os.UserHandle profile : manager.getUserProfiles()) {
                UserHandle user = new UserHandle(manager.getSerialNumberForUser(profile), profile);
                for (LauncherActivityInfo activityInfo : launcher.getActivityList(null, profile)) {
                    ApplicationInfo appInfo = activityInfo.getApplicationInfo();

                    String id = user.addUserSuffixToString(pojoScheme + appInfo.packageName + "/" + activityInfo.getName(), '/');

                    boolean isExcluded = excludedAppList.contains(AppPojo.getComponentName(appInfo.packageName, activityInfo.getName(), user));
                    boolean isExcludedFromHistory = excludedFromHistoryAppList.contains(id);

                    AppPojo app = new AppPojo(id, appInfo.packageName, activityInfo.getName(), user,
                            isExcluded, isExcludedFromHistory);

                    app.setName(activityInfo.getLabel().toString());

                    app.setTags(tagsHandler.getTags(app.id));

                    apps.add(app);
                }
            }
        } else {
            PackageManager manager = ctx.getPackageManager();

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            for (ResolveInfo info : manager.queryIntentActivities(mainIntent, 0)) {
                ApplicationInfo appInfo = info.activityInfo.applicationInfo;
                String id = pojoScheme + appInfo.packageName + "/" + info.activityInfo.name;
                boolean isExcluded = excludedAppList.contains(
                        AppPojo.getComponentName(appInfo.packageName, info.activityInfo.name, new UserHandle())
                );
                boolean isExcludedFromHistory = excludedFromHistoryAppList.contains(id);

                AppPojo app = new AppPojo(id, appInfo.packageName, info.activityInfo.name, new UserHandle(),
                        isExcluded, isExcludedFromHistory);

                app.setName(info.loadLabel(manager).toString());

                app.setTags(tagsHandler.getTags(app.id));

                apps.add(app);
            }
        }

        long end = System.nanoTime();
        Log.i("time", (end - start) / 1000000 + " milliseconds to list apps");

        return apps;
    }
}
