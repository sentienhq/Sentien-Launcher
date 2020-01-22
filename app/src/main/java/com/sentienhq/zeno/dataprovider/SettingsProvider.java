package com.sentienhq.zeno.dataprovider;

import com.sentienhq.zeno.loader.LoadSettingsPojos;
import com.sentienhq.zeno.normalizer.StringNormalizer;
import com.sentienhq.zeno.pojo.SettingsPojo;
import com.sentienhq.zeno.searcher.Searcher;
import com.sentienhq.zeno.utils.FuzzyScore;

import java.util.Locale;

import com.sentienhq.zeno.R;

public class SettingsProvider extends Provider<SettingsPojo> {
    private String settingName;

    @Override
    public void reload() {
        super.reload();
        this.initialize(new LoadSettingsPojos(this));

        settingName = this.getString(R.string.settings_prefix).toLowerCase(Locale.ROOT);
    }

    @Override
    public void requestResults(String query, Searcher searcher) {
        StringNormalizer.Result queryNormalized = StringNormalizer.normalizeWithResult(query, false);

        if (queryNormalized.codePoints.length == 0) {
            return;
        }

        FuzzyScore fuzzyScore = new FuzzyScore(queryNormalized.codePoints);
        FuzzyScore.MatchInfo matchInfo;
        boolean match;

        for (SettingsPojo pojo : pojos) {
            matchInfo = fuzzyScore.match(pojo.normalizedName.codePoints);
            match = matchInfo.match;
            pojo.relevance = matchInfo.score;

            if (!match) {
                // Match localized setting name
                matchInfo = fuzzyScore.match(settingName);
                match = matchInfo.match;
                pojo.relevance = matchInfo.score;
            }

            if (match && !searcher.addResult(pojo)) {
                return;
            }
        }
    }
}
