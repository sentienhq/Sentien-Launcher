package com.sentienhq.launcher.searcher;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sentienhq.launcher.MainActivity;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.pojo.Pojo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Retrieve pojos from history
 */
public class HistorySearcher extends Searcher {
    private final SharedPreferences prefs;
    private final Set<String> excludedFromHistory;

    public HistorySearcher(MainActivity activity) {
        super(activity, "<history>");
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        excludedFromHistory = LauncherApplication.getApplication(activity).getDataHandler().getExcludedFromHistory();
    }

    @Override
    int getMaxResultCount() {
        // Convert `"number-of-display-elements"` to double first before truncating to int to avoid
        // `java.lang.NumberFormatException` crashes for values larger than `Integer.MAX_VALUE`
        try {
            return Double.valueOf(prefs.getString("number-of-display-elements", String.valueOf(DEFAULT_MAX_RESULTS))).intValue();
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_RESULTS;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Ask for records
        String historyMode = prefs.getString("history-mode", "recency");
        boolean sortHistory = prefs.getBoolean("sort-history", false);
        boolean excludeFavorites = prefs.getBoolean("exclude-favorites", false);

        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        //Gather excluded
        HashSet<String> excludedPojoById = new HashSet<>(excludedFromHistory);

        if (excludeFavorites) {
            // Gather favorites
            for (Pojo favoritePojo : LauncherApplication.getApplication(activity).getDataHandler().getFavorites()) {
                excludedPojoById.add(favoritePojo.id);
            }
        }

        List<Pojo> pojos = LauncherApplication.getApplication(activity).getDataHandler()
                .getHistory(activity, getMaxResultCount(), historyMode, sortHistory, excludedPojoById);

        int size = pojos.size();
        for (int i = 0; i < size; i += 1) {
            pojos.get(i).relevance = size - i;
        }

        this.addResult(pojos.toArray(new Pojo[0]));
        return null;
    }
}
