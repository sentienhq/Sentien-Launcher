package com.sentienhq.launcher.searcher;

import android.content.Context;

import com.sentienhq.launcher.MainActivity;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.pojo.AppPojo;
import com.sentienhq.launcher.pojo.Pojo;
import com.sentienhq.launcher.pojo.PojoComparator;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Returns the list of all applications on the system
 */
public class ApplicationsSearcher extends Searcher {
    public ApplicationsSearcher(MainActivity activity) {
        super(activity, "<application>");
    }

    @Override
    PriorityQueue<Pojo> getPojoProcessor(Context context) {
        // Sort from A to Z, so reverse (last item needs to be A, listview starts at the bottom)
        return new PriorityQueue<>(DEFAULT_MAX_RESULTS, Collections.reverseOrder(new PojoComparator()));
    }

    @Override
    protected int getMaxResultCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        List<AppPojo> pojos = LauncherApplication.getApplication(activity).getDataHandler().getApplicationsWithoutExcluded();

        if (pojos != null)
            this.addResult(pojos.toArray(new Pojo[0]));
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        super.onPostExecute(param);
        // Build sections for fast scrolling
        activityWeakReference.get().adapter.buildSections();
    }
}
