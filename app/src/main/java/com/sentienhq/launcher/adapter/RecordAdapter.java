package com.sentienhq.launcher.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;

import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.normalizer.StringNormalizer;
import com.sentienhq.launcher.result.AppResult;
import com.sentienhq.launcher.result.ContactsResult;
import com.sentienhq.launcher.result.PhoneResult;
import com.sentienhq.launcher.result.Result;
import com.sentienhq.launcher.result.SearchResult;
import com.sentienhq.launcher.result.SettingsResult;
import com.sentienhq.launcher.result.ShortcutsResult;
import com.sentienhq.launcher.searcher.QueryInterface;
import com.sentienhq.launcher.ui.ListPopup;
import com.sentienhq.launcher.utils.FuzzyScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordAdapter extends BaseAdapter implements SectionIndexer {
    private final QueryInterface parent;
    private FuzzyScore fuzzyScore;

    /**
     * Array list containing all the results currently displayed
     */
    private List<Result> results;

    // Mapping from letter to a position (only used for fast scroll, when viewing app list)
    private HashMap<String, Integer> alphaIndexer = new HashMap<>();
    // List of available sections (only used for fast scroll)
    private String[] sections = new String[0];

    public RecordAdapter(QueryInterface parent, ArrayList<Result> results) {
        this.parent = parent;
        this.results = results;
        this.fuzzyScore = null;
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        if (results.get(position) instanceof AppResult)
            return 0;
        else if (results.get(position) instanceof SearchResult)
            return 1;
        else if (results.get(position) instanceof ContactsResult)
            return 2;
        else if (results.get(position) instanceof SettingsResult)
            return 3;
        else if (results.get(position) instanceof PhoneResult)
            return 4;
        else if (results.get(position) instanceof ShortcutsResult)
            return 5;
        else
            return -1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        // In some situation, Android tries to display an item that does not exist (e.g. item 24 in a list containing 22 items)
        // See https://github.com/Neamar/KISS/issues/890
        return position < results.size() ? results.get(position).getUniqueId() : -1;
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView != null) {
            if (!(convertView.getTag() instanceof Integer))
                convertView = null;
            else if ((Integer) convertView.getTag() != getItemViewType(position)) {
                // This is happening on HTC Desire X (Android 4.1.1, API 16)
                //throw new IllegalStateException( "can't convert view from different type" );
                convertView = null;
            }
        }
        View view = results.get(position).display(parent.getContext(), results.size() - position, convertView, parent, fuzzyScore);
        //Log.d( "TBog", "getView pos " + position + " convertView " + ((convertView == null) ? "null" : convertView.toString()) + " will return " + view.toString() );
        view.setTag(getItemViewType(position));
        return view;
    }

    public void onLongClick(final int pos, View v) {
        ListPopup menu = results.get(pos).getPopupMenu(v.getContext(), this, v);

        //check if menu contains elements and if yes show it
        if (menu.getAdapter().getCount() > 0) {
            parent.registerPopup(menu);
            menu.show(v);
        }
    }

    public void onClick(final int position, View v) {
        final Result result;

        try {
            result = results.get(position);
            result.launch(v.getContext(), v);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return;
        }

        // Record the launch after some period,
        // * to ensure the animation runs smoothly
        // * to avoid a flickering -- launchOccurred will refresh the list
        // Thus TOUCH_DELAY * 3
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parent.launchOccurred();
            }
        }, LauncherApplication.TOUCH_DELAY * 3);

    }

    public void removeResult(Context context, Result result) {
        results.remove(result);
        result.deleteRecord(context);
        notifyDataSetChanged();
    }

    public void updateResults(List<Result> results, String query) {
        this.results.clear();
        this.results.addAll(results);
        StringNormalizer.Result queryNormalized = StringNormalizer.normalizeWithResult(query, false);

        fuzzyScore = new FuzzyScore(queryNormalized.codePoints, true);
        notifyDataSetChanged();
    }

    public void clear() {
        this.results.clear();
        notifyDataSetChanged();
    }

    /**
     * When using fast scroll, generate a mapping to know where a given letter starts in the list
     * (can only be used with a sorted result set!)
     */
    public void buildSections() {
        alphaIndexer.clear();
        int size = results.size();

        // Generate the mapping letter => number
        for (int x = 0; x < size; x++) {
            String s = results.get(x).getSection();

            // Put the first one
            if (!alphaIndexer.containsKey(s)) {
                alphaIndexer.put(s, x);
            }
        }

        // Generate section list
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(alphaIndexer.entrySet());
        Collections.sort(entries, (o1, o2) -> {
            if (o2.getValue().equals(o1.getValue())) {
                return 0;
            }
            // We're displaying from A to Z, everything needs to be reversed
            return o2.getValue() > o1.getValue() ? -1 : 1;
        });
        sections = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            sections[i] = entries.get(i).getKey();
        }
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sections.length == 0) {
            return 0;
        }

        // In some rare situations, the system will ask for a section
        // that does not exist anymore.
        // It's likely there is a threading issue in our code somewhere,
        // But I was unable to find where, so the following line is a quick and dirty fix.
        sectionIndex = Math.max(0, Math.min(sections.length - 1, sectionIndex));
        return alphaIndexer.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < sections.length; i++) {
            if (alphaIndexer.get(sections[i]) > position) {
                return i - 1;
            }
        }

        // If apps starting with the letter "A" cover more than a full screen,
        // we will never get > position
        // so we just return the before-last section
        // See #1005
        return sections.length - 2;
    }
}
