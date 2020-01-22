package com.sentienhq.zeno.dataprovider.simpleprovider;

import android.content.Context;
import android.content.pm.PackageManager;

import com.sentienhq.zeno.pojo.PhonePojo;
import com.sentienhq.zeno.pojo.Pojo;
import com.sentienhq.zeno.searcher.Searcher;

import java.util.regex.Pattern;

public class PhoneProvider extends SimpleProvider {
    private static final String PHONE_SCHEME = "phone://";
    private boolean deviceIsPhone;
    private Pattern phonePattern = Pattern.compile("^[*+0-9# ]{3,}$");

    public PhoneProvider(Context context) {
        PackageManager pm = context.getPackageManager();
        deviceIsPhone = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    @Override
    public void requestResults(String query, Searcher searcher) {
        // Append an item only if query looks like a phone number and device has phone capabilities
        if (deviceIsPhone && phonePattern.matcher(query).find()) {
            searcher.addResult(getResult(query));
        }
    }

    @Override
    public boolean mayFindById(String id) {
        return id.startsWith(PHONE_SCHEME);
    }

    public Pojo findById(String id) {
        return getResult(id.replaceFirst(Pattern.quote(PHONE_SCHEME), ""));
    }

    private Pojo getResult(String phoneNumber) {
        PhonePojo pojo = new PhonePojo(PHONE_SCHEME + phoneNumber, phoneNumber);
        pojo.relevance = 20;
        pojo.setName(phoneNumber, false);
        return pojo;
    }
}
