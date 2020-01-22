package com.sentienhq.zeno.pojo;

import com.sentienhq.zeno.normalizer.StringNormalizer;

public class PojoWithTags extends Pojo {
    // tags normalized, for faster search
    private StringNormalizer.Result normalizedTags = null;
    // Tags assigned to this pojo
    private String tags = "";

    public PojoWithTags(String id) {
        super(id);
    }

    public StringNormalizer.Result getNormalizedTags() {
        return normalizedTags;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        if (tags != null) {
            // Set the actual user-friendly name
            this.tags = tags;
            this.normalizedTags = StringNormalizer.normalizeWithResult(this.tags, false);
        } else {
            this.tags = null;
            this.normalizedTags = null;
        }
    }
}
