package com.sentienhq.launcher.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sentienhq.launcher.TagsHandler;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.db.DBHelper;
import com.sentienhq.launcher.db.ShortcutRecord;
import com.sentienhq.launcher.pojo.ShortcutsPojo;
import com.sentienhq.launcher.utils.ShortcutUtil;

import java.util.ArrayList;
import java.util.List;

public class LoadShortcutsPojos extends LoadPojos<ShortcutsPojo> {

    private final TagsHandler tagsHandler;

    public LoadShortcutsPojos(Context context) {
        super(context, ShortcutsPojo.SCHEME);
        tagsHandler = LauncherApplication.getApplication(context).getDataHandler().getTagsHandler();
    }

    @Override
    protected ArrayList<ShortcutsPojo> doInBackground(Void... arg0) {
        ArrayList<ShortcutsPojo> pojos = new ArrayList<>();

        if (context.get() == null) {
            return pojos;
        }
        List<ShortcutRecord> records = DBHelper.getShortcuts(context.get());
        for (ShortcutRecord shortcutRecord : records) {
            Bitmap icon = null;

            if (shortcutRecord.icon_blob != null) {
                icon = BitmapFactory.decodeByteArray(shortcutRecord.icon_blob, 0, shortcutRecord.icon_blob.length);
            }

            String id = ShortcutUtil.generateShortcutId(shortcutRecord.name);

            ShortcutsPojo pojo = new ShortcutsPojo(id, shortcutRecord.packageName,
                    shortcutRecord.iconResource, shortcutRecord.intentUri, icon);

            pojo.setName(shortcutRecord.name);
            pojo.setTags(tagsHandler.getTags(pojo.id));

            pojos.add(pojo);
        }

        return pojos;
    }

}
