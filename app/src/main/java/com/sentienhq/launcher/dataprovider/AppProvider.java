package com.sentienhq.launcher.dataprovider;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Process;
import android.os.UserManager;

import androidx.annotation.RequiresApi;

import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.broadcast.PackageAddedRemovedHandler;
import com.sentienhq.launcher.loader.LoadAppPojos;
import com.sentienhq.launcher.normalizer.StringNormalizer;
import com.sentienhq.launcher.pojo.AppPojo;
import com.sentienhq.launcher.pojo.Pojo;
import com.sentienhq.launcher.searcher.Searcher;
import com.sentienhq.launcher.utils.FuzzyScore;
import com.sentienhq.launcher.utils.UserHandle;

import java.util.ArrayList;
import java.util.Objects;

public class AppProvider extends Provider<AppPojo> {

    @Override
    @SuppressLint("NewApi")
    public void onCreate() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Package installation/uninstallation events for the main
            // profile are still handled using PackageAddedRemovedHandler itself
            final UserManager manager = (UserManager) this.getSystemService(Context.USER_SERVICE);
            assert manager != null;

            final LauncherApps launcher = (LauncherApps) this.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            assert launcher != null;

            launcher.registerCallback(new LauncherApps.Callback() {
                @Override
                public void onPackageAdded(String packageName, android.os.UserHandle user) {
                    if (!Process.myUserHandle().equals(user)) {
                        PackageAddedRemovedHandler.handleEvent(AppProvider.this,
                                "android.intent.action.PACKAGE_ADDED",
                                packageName, new UserHandle(manager.getSerialNumberForUser(user), user), false
                        );
                    }
                }

                @Override
                public void onPackageChanged(String packageName, android.os.UserHandle user) {
                    if (!Process.myUserHandle().equals(user)) {
                        PackageAddedRemovedHandler.handleEvent(AppProvider.this,
                                "android.intent.action.PACKAGE_ADDED",
                                packageName, new UserHandle(manager.getSerialNumberForUser(user), user), true
                        );
                    }
                }

                @Override
                public void onPackageRemoved(String packageName, android.os.UserHandle user) {
                    if (!Process.myUserHandle().equals(user)) {
                        PackageAddedRemovedHandler.handleEvent(AppProvider.this,
                                "android.intent.action.PACKAGE_REMOVED",
                                packageName, new UserHandle(manager.getSerialNumberForUser(user), user), false
                        );
                    }
                }

                @Override
                public void onPackagesAvailable(String[] packageNames, android.os.UserHandle user, boolean replacing) {
                    if (!Process.myUserHandle().equals(user)) {
                        PackageAddedRemovedHandler.handleEvent(AppProvider.this,
                                "android.intent.action.MEDIA_MOUNTED",
                                null, new UserHandle(manager.getSerialNumberForUser(user), user), false
                        );
                    }
                }

                @Override
                public void onPackagesUnavailable(String[] packageNames, android.os.UserHandle user, boolean replacing) {
                    if (!Process.myUserHandle().equals(user)) {
                        PackageAddedRemovedHandler.handleEvent(AppProvider.this,
                                "android.intent.action.MEDIA_UNMOUNTED",
                                null, new UserHandle(manager.getSerialNumberForUser(user), user), false
                        );
                    }
                }
            });

            // Try to clean up app-related data when profile is removed
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
            filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
            this.registerReceiver(new BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Objects.equals(intent.getAction(), Intent.ACTION_MANAGED_PROFILE_ADDED)) {
                        AppProvider.this.reload();
                    } else if (Objects.equals(intent.getAction(), Intent.ACTION_MANAGED_PROFILE_REMOVED)) {
                        android.os.UserHandle profile = intent.getParcelableExtra(Intent.EXTRA_USER);

                        UserHandle user = new UserHandle(manager.getSerialNumberForUser(profile), profile);

                        LauncherApplication.getApplication(context).getDataHandler().removeFromExcluded(user);
                        LauncherApplication.getApplication(context).getDataHandler().removeFromFavorites(user);
                        AppProvider.this.reload();
                    }
                }
            }, filter);
        }

        // Get notified when app changes on standard user profile
        IntentFilter appChangedFilter = new IntentFilter();
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appChangedFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangedFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        appChangedFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        appChangedFilter.addDataScheme("package");
        appChangedFilter.addDataScheme("file");
        this.registerReceiver(new PackageAddedRemovedHandler(), appChangedFilter);

        super.onCreate();
    }

    @Override
    public void reload() {
        super.reload();
        this.initialize(new LoadAppPojos(this));
    }

    /**
     * @param query    The string to search for
     * @param searcher The receiver of results
     */

    @Override
    public void requestResults(String query, Searcher searcher) {
        StringNormalizer.Result queryNormalized = StringNormalizer.normalizeWithResult(query, false);

        if (queryNormalized.codePoints.length == 0) {
            return;
        }

        FuzzyScore fuzzyScore = new FuzzyScore(queryNormalized.codePoints);
        FuzzyScore.MatchInfo matchInfo;
        boolean match;

        for (AppPojo pojo : pojos) {
            if (pojo.isExcluded()) {
                continue;
            }

            matchInfo = fuzzyScore.match(pojo.normalizedName.codePoints);
            match = matchInfo.match;
            pojo.relevance = matchInfo.score;

            // check relevance for tags
            if (pojo.getNormalizedTags() != null) {
                matchInfo = fuzzyScore.match(pojo.getNormalizedTags().codePoints);
                if (matchInfo.match && (!match || matchInfo.score > pojo.relevance)) {
                    match = true;
                    pojo.relevance = matchInfo.score;
                }
            }

            if (match && !searcher.addResult(pojo)) {
                return;
            }
        }
    }

    /**
     * Return a Pojo
     *
     * @param id we're looking for
     * @return an AppPojo, or null
     */
    @Override
    public Pojo findById(String id) {
        for (Pojo pojo : pojos) {
            if (pojo.id.equals(id)) {
                return pojo;
            }
        }

        return null;
    }

    public ArrayList<AppPojo> getAllApps() {
        ArrayList<AppPojo> records = new ArrayList<>(pojos.size());

        for (AppPojo pojo : pojos) {
            pojo.relevance = 0;
            records.add(pojo);
        }
        return records;
    }

    public ArrayList<AppPojo> getAllAppsWithoutExcluded() {
        ArrayList<AppPojo> records = new ArrayList<>(pojos.size());

        for (AppPojo pojo : pojos) {
            if (pojo.isExcluded()) continue;

            pojo.relevance = 0;
            records.add(pojo);
        }
        return records;
    }
}
