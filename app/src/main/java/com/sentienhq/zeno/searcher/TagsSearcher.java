package com.sentienhq.zeno.searcher;

import com.sentienhq.zeno.MainActivity;
import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.pojo.Pojo;
import com.sentienhq.zeno.pojo.PojoWithTags;

/**
 * Returns a list of all applications that match the tags toggled
 */

public class TagsSearcher extends Searcher {
//	private static final Pattern patternTagSplit = Pattern.compile("\\s+");

    public TagsSearcher(MainActivity activity, String query) {
        super(activity, query == null ? "<tags>" : query);
    }

    @Override
    public boolean addResult(Pojo... pojos) {
        for (Pojo pojo : pojos) {
            if (!(pojo instanceof PojoWithTags)) {
                continue;
            }
            PojoWithTags pojoWithTags = (PojoWithTags) pojo;
            if (pojoWithTags.getTags() == null || pojoWithTags.getTags().isEmpty()) {
                continue;
            }

            if (!pojoWithTags.getTags().contains(query)) {
                continue;
            }

            super.addResult(pojo);
        }
        return false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        ZenoApplication.getApplication(activity).getDataHandler().requestAllRecords(this);

        return null;
    }
}
