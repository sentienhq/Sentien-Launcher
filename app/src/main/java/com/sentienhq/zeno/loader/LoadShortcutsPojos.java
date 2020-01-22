package com.sentienhq.zeno.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sentienhq.zeno.TagsHandler;
import com.sentienhq.zeno.ZenoApplication;
import com.sentienhq.zeno.db.DBHelper;
import com.sentienhq.zeno.db.ShortcutRecord;
import com.sentienhq.zeno.pojo.ShortcutsPojo;
import com.sentienhq.zeno.utils.ShortcutUtil;

import java.util.ArrayList;
import java.util.List;

public class LoadShortcutsPojos extends LoadPojos<ShortcutsPojo> {

    private final TagsHandler tagsHandler;

    public LoadShortcutsPojos(Context context) {
        super(context, ShortcutsPojo.SCHEME);
        tagsHandler = ZenoApplication.getApplication(context).getDataHandler().getTagsHandler();
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
