package com.sentienhq.zeno.searcher;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.CallSuper;

import com.sentienhq.zeno.MainActivity;
import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.pojo.Pojo;
import com.sentienhq.zeno.pojo.PojoComparator;
import com.sentienhq.zeno.result.Result;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Searcher extends AsyncTask<Void, Result, Void> {
    // define a different thread than the default AsyncTask thread or else we will block everything else that uses AsyncTask while we search
    public static final ExecutorService SEARCH_THREAD = Executors.newSingleThreadExecutor();
    static final int DEFAULT_MAX_RESULTS = 50;
    final WeakReference<MainActivity> activityWeakReference;
    private final PriorityQueue<Pojo> processedPojos;
    private long start;
    protected final String query;

    Searcher(MainActivity activity, String query) {
        super();
        this.query = query;
        this.activityWeakReference = new WeakReference<>(activity);
        this.processedPojos = getPojoProcessor(activity);
    }

    PriorityQueue<Pojo> getPojoProcessor(Context context) {
        return new PriorityQueue<>(DEFAULT_MAX_RESULTS, new PojoComparator());
    }

    int getMaxResultCount() {
        return DEFAULT_MAX_RESULTS;
    }

    /**
     * This is called from the background thread by the providers
     */
    public boolean addResult(Pojo... pojos) {
        if (isCancelled())
            return false;

        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return false;

        Collections.addAll(this.processedPojos, pojos);
        int maxResults = getMaxResultCount();
        while (this.processedPojos.size() > maxResults)
            this.processedPojos.poll();

        return true;
    }

    @CallSuper
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        start = System.currentTimeMillis();

        displayActivityLoader();
    }

    void displayActivityLoader() {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return;

        activity.displayLoader(true);
    }

    @Override
    protected void onPostExecute(Void param) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return;

        // Loader should still be displayed until all the providers have finished loading
        activity.displayLoader(!ZenoApplication.getApplication(activity).getDataHandler().allProvidersHaveLoaded);

        if (this.processedPojos.isEmpty()) {
            activity.adapter.clear();
        } else {
            PriorityQueue<Pojo> queue = this.processedPojos;
            ArrayList<Result> results = new ArrayList<>(queue.size());
            while (queue.peek() != null) {
                results.add(Result.fromPojo(activity, queue.poll()));
            }
            activity.beforeListChange();

            activity.adapter.updateResults(results, query);

            activity.afterListChange();
        }

        activity.resetTask();

        long time = System.currentTimeMillis() - start;
        Log.v("Timing", "Time to run query `" + query + "` on " + getClass().getSimpleName() + " to completion: " + time + "ms");
    }

    public interface DataObserver {
        void beforeListChange();

        void afterListChange();
    }
}
