package com.sentienhq.launcher.result;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.sentienhq.launcher.BuildConfig;
import com.sentienhq.launcher.MainActivity;
import com.sentienhq.launcher.R;
import com.sentienhq.launcher.UIColors;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.adapter.RecordAdapter;
import com.sentienhq.launcher.db.DBHelper;
import com.sentienhq.launcher.normalizer.StringNormalizer;
import com.sentienhq.launcher.pojo.AppPojo;
import com.sentienhq.launcher.pojo.ContactsPojo;
import com.sentienhq.launcher.pojo.PhonePojo;
import com.sentienhq.launcher.pojo.Pojo;
import com.sentienhq.launcher.pojo.SearchPojo;
import com.sentienhq.launcher.pojo.SettingsPojo;
import com.sentienhq.launcher.pojo.ShortcutsPojo;
import com.sentienhq.launcher.pojo.TagDummyPojo;
import com.sentienhq.launcher.searcher.QueryInterface;
import com.sentienhq.launcher.ui.ListPopup;
import com.sentienhq.launcher.utils.FuzzyScore;

import java.lang.ref.WeakReference;
import java.util.List;


public abstract class Result {
    /**
     * Current information pojo
     */
    @NonNull
    final Pojo pojo;

    Result(@NonNull Pojo pojo) {
        this.pojo = pojo;
    }

    public static Result fromPojo(QueryInterface parent, Pojo pojo) {
        if (pojo instanceof AppPojo)
            return new AppResult((AppPojo) pojo);
        else if (pojo instanceof ContactsPojo)
            return new ContactsResult(parent, (ContactsPojo) pojo);
        else if (pojo instanceof SearchPojo)
            return new SearchResult((SearchPojo) pojo);
        else if (pojo instanceof SettingsPojo)
            return new SettingsResult((SettingsPojo) pojo);
        else if (pojo instanceof PhonePojo)
            return new PhoneResult((PhonePojo) pojo);
        else if (pojo instanceof ShortcutsPojo)
            return new ShortcutsResult((ShortcutsPojo) pojo);
        else if (pojo instanceof TagDummyPojo)
            return new TagDummyResult((TagDummyPojo) pojo);


        throw new RuntimeException("Unable to create a result from POJO");
    }

    public String getPojoId() {
        return pojo.id;
    }

    @NonNull
    public Pojo getPojo() {
        return pojo;
    }

    @Override
    public String toString() {
        return pojo.getName();
    }

    /**
     * How to display this record ?
     *
     * @param context     android context
     * @param convertView a view to be recycled
     * @param parent      view that provides a set of LayoutParams values
     * @param fuzzyScore  information for highlighting search result
     * @return a view to display as item
     */
    @NonNull
    public abstract View display(Context context, int position, View convertView, @NonNull ViewGroup parent, FuzzyScore fuzzyScore);

    @NonNull
    public View inflateFavorite(@NonNull Context context, @Nullable View favoriteView, @NonNull ViewGroup parent) {
        if (favoriteView == null)
            favoriteView = LayoutInflater.from(context).inflate(R.layout.favorite_item, parent, false);
        Drawable drawable = getDrawable(context);
        ImageView favoriteImage = favoriteView.findViewById(R.id.favorite);
        if (drawable == null)
            favoriteImage.setImageResource(R.drawable.ic_launcher_white);
        else
            favoriteImage.setImageDrawable(drawable);
        favoriteView.setContentDescription(pojo.getName());
        return favoriteView;
    }

    public void displayHighlighted(String text, List<Pair<Integer, Integer>> positions, TextView view, Context context) {
        SpannableString enriched = new SpannableString(text);
        int primaryColor = UIColors.getPrimaryColor(context);

        for (Pair<Integer, Integer> position : positions) {
            enriched.setSpan(
                    new ForegroundColorSpan(primaryColor),
                    position.first,
                    position.second,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            );
        }
        view.setText(enriched);
    }

    public boolean displayHighlighted(StringNormalizer.Result normalized, String text, FuzzyScore fuzzyScore,
                                      TextView view, Context context) {
        FuzzyScore.MatchInfo matchInfo = fuzzyScore.match(normalized.codePoints);

        if (!matchInfo.match) {
            view.setText(text);
            return false;
        }

        SpannableString enriched = new SpannableString(text);
        int primaryColor = UIColors.getPrimaryColor(context);

        for (Pair<Integer, Integer> position : matchInfo.getMatchedSequences()) {
            enriched.setSpan(
                    new ForegroundColorSpan(primaryColor),
                    normalized.mapPosition(position.first),
                    normalized.mapPosition(position.second),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            );
        }
        view.setText(enriched);
        return true;
    }

    public String getSection() {
        try {
            // get the normalized first letter of the pojo
            // Ensure accented characters are never displayed. (É => E)
            String ch = Character.toString((char) pojo.normalizedName.codePoints[0]);
            // convert to uppercase otherwise lowercase a -z will be sorted
            // after upper A-Z
            return ch.toUpperCase();
        } catch (ArrayIndexOutOfBoundsException e) {
            // Normalized name is empty.
            return "-";
        }
    }

    /**
     * How to display the popup menu
     *
     * @return a PopupMenu object
     */
    public ListPopup getPopupMenu(final Context context, final RecordAdapter parent, final View parentView) {
        ArrayAdapter<ListPopup.Item> adapter = new ArrayAdapter<>(context, R.layout.popup_list_item);
        ListPopup menu = buildPopupMenu(context, adapter, parent, parentView);

        menu.setOnItemClickListener(new ListPopup.OnItemClickListener() {
            @Override
            public void onItemClick(ListAdapter adapter, View view, int position) {
                @StringRes int stringId = ((ListPopup.Item) adapter.getItem(position)).stringId;
                popupMenuClickHandler(view.getContext(), parent, stringId, parentView);
            }
        });

        return menu;
    }

    /**
     * Default popup menu implementation, can be overridden by children class to display a more specific menu
     *
     * @return an inflated, listener-free PopupMenu
     */
    ListPopup buildPopupMenu(Context context, ArrayAdapter<ListPopup.Item> adapter, final RecordAdapter parent, View parentView) {
        adapter.add(new ListPopup.Item(context, R.string.menu_remove));
        adapter.add(new ListPopup.Item(context, R.string.menu_favorites_add));
        adapter.add(new ListPopup.Item(context, R.string.menu_favorites_remove));
        return inflatePopupMenu(adapter, context);
    }

    ListPopup inflatePopupMenu(ArrayAdapter<ListPopup.Item> adapter, Context context) {
        ListPopup menu = new ListPopup(context);
        menu.setAdapter(adapter);

        // If app already pinned, do not display the "add to favorite" option
        // otherwise don't show the "remove favorite button"
        String favApps = PreferenceManager.getDefaultSharedPreferences(context).
                getString("favorite-apps-list", "");
        if (favApps.contains(this.pojo.id + ";")) {
            for (int i = 0; i < adapter.getCount(); i += 1) {
                ListPopup.Item item = adapter.getItem(i);
                assert item != null;
                if (item.stringId == R.string.menu_favorites_add)
                    adapter.remove(item);
            }
        } else {
            for (int i = 0; i < adapter.getCount(); i += 1) {
                ListPopup.Item item = adapter.getItem(i);
                assert item != null;
                if (item.stringId == R.string.menu_favorites_remove)
                    adapter.remove(item);
            }
        }

        if (BuildConfig.DEBUG) {
            adapter.add(new ListPopup.Item("Relevance: " + pojo.relevance));
        }

        return menu;
    }

    /**
     * Handler for popup menu action.
     * Default implementation only handle remove from history action.
     *
     * @return Works in the same way as onOptionsItemSelected, return true if the action has been handled, false otherwise
     */
    boolean popupMenuClickHandler(Context context, RecordAdapter parent, @StringRes int stringId, View parentView) {
        switch (stringId) {
            case R.string.menu_remove:
                removeItem(context, parent);
                return true;
            case R.string.menu_favorites_add:
                launchAddToFavorites(context, pojo);
                break;
            case R.string.menu_favorites_remove:
                launchRemoveFromFavorites(context, pojo);
                break;
        }

        MainActivity mainActivity = (MainActivity) context;
        //Update favorite bar
        mainActivity.onFavoriteChange();
        //Update Search to reflect favorite add, if the "exclude favorites" option is active
        if (mainActivity.prefs.getBoolean("exclude-favorites", false) && mainActivity.isViewingSearchResults()) {
            mainActivity.updateSearchRecords();
        }

        return false;
    }

    private void launchAddToFavorites(Context context, Pojo app) {
        String msg = context.getResources().getString(R.string.toast_favorites_added);
        LauncherApplication.getApplication(context).getDataHandler().addToFavorites(app.id);
        Toast.makeText(context, String.format(msg, app.getName()), Toast.LENGTH_SHORT).show();
    }

    private void launchRemoveFromFavorites(Context context, Pojo app) {
        String msg = context.getResources().getString(R.string.toast_favorites_removed);
        LauncherApplication.getApplication(context).getDataHandler().removeFromFavorites(app.id);
        Toast.makeText(context, String.format(msg, app.getName()), Toast.LENGTH_SHORT).show();
    }

    /**
     * Remove the current result from the list
     *
     * @param context android context
     * @param parent  adapter on which to remove the item
     */
    private void removeItem(Context context, RecordAdapter parent) {
        Toast.makeText(context, R.string.removed_item, Toast.LENGTH_SHORT).show();
        parent.removeResult(context, this);
    }

    public final void launch(Context context, View v) {
        Log.i("log", "Launching " + pojo.id);

        recordLaunch(context);

        // Launch
        doLaunch(context, v);
    }

    /**
     * How to launch this record ? Most probably, will fire an intent. This
     * function must call recordLaunch()
     *
     * @param context android context
     */
    protected abstract void doLaunch(Context context, View v);

    /**
     * How to launch this record "quickly" ? Most probably, same as doLaunch().
     * Override to define another behavior.
     *
     * @param context android context
     */
    public void fastLaunch(Context context, View v) {
        this.launch(context, v);
    }

    /**
     * Return the icon for this Result, or null if non existing.
     *
     * @param context android context
     */
    public Drawable getDrawable(Context context) {
        return null;
    }

    boolean isDrawableCached() {
        return false;
    }

    void setDrawableCache(Drawable drawable) {
    }

    void setAsyncDrawable(ImageView view) {
        // the ImageView tag will store the async task if it's running
        if (view.getTag() instanceof AsyncSetImage) {
            AsyncSetImage asyncSetImage = (AsyncSetImage) view.getTag();
            if (this.equals(asyncSetImage.appResultWeakReference.get())) {
                // we are already loading the icon for this
                return;
            } else {
                asyncSetImage.cancel(true);
                view.setTag(null);
            }
        }
        // the ImageView will store the Result after the AsyncTask finished
        else if (this.equals(view.getTag())) {
            ((Result) view.getTag()).setDrawableCache(view.getDrawable());
            return;
        }
        if (isDrawableCached()) {
            view.setImageDrawable(getDrawable(view.getContext()));
            view.setTag(this);
        } else {
            view.setTag(createAsyncSetImage(view).execute());
        }
    }

    private AsyncSetImage createAsyncSetImage(ImageView imageView) {
        return new AsyncSetImage(imageView, this);
    }

    /**
     * Helper function to get a view
     *
     * @param context android context
     * @param id      id to inflate
     * @param parent  view that provides a set of LayoutParams values
     * @return the view specified by the id
     */
    View inflateFromId(Context context, @LayoutRes int id, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(id, parent, false);
    }

    /**
     * Put this item in application history
     *
     * @param context android context
     */
    void recordLaunch(Context context) {
        // Save in history
        LauncherApplication.getApplication(context).getDataHandler().addToHistory(pojo.id);
    }

    public void deleteRecord(Context context) {
        DBHelper.removeFromHistory(context, pojo.id);
    }

    /*
     * Get fill color from theme
     *
     */
    int getThemeFillColor(Context context) {
        int[] attrs = new int[]{R.attr.resultColor /* index 0 */};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.WHITE);
        ta.recycle();
        return color;
    }

    public long getUniqueId() {
        // we can consider hashCode unique enough in this context
        return this.pojo.id.hashCode();
    }

    static class AsyncSetImage extends AsyncTask<Void, Void, Drawable> {
        final WeakReference<ImageView> imageViewWeakReference;
        final WeakReference<Result> appResultWeakReference;

        AsyncSetImage(ImageView image, Result result) {
            super();
            image.setTag(this);
            image.setImageResource(android.R.color.transparent);
            this.imageViewWeakReference = new WeakReference<>(image);
            this.appResultWeakReference = new WeakReference<>(result);
        }

        @Override
        protected Drawable doInBackground(Void... voids) {
            ImageView image = imageViewWeakReference.get();
            if (isCancelled() || image == null || image.getTag() != this) {
                imageViewWeakReference.clear();
                return null;
            }
            Result result = appResultWeakReference.get();
            if (result == null)
                return null;
            return result.getDrawable(image.getContext());
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            ImageView image = imageViewWeakReference.get();
            if (isCancelled() || image == null || drawable == null) {
                imageViewWeakReference.clear();
                return;
            }
            image.setImageDrawable(drawable);
            image.setTag(appResultWeakReference.get());
        }
    }
}