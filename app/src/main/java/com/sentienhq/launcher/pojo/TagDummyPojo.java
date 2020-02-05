package com.sentienhq.launcher.pojo;

import com.sentienhq.launcher.dataprovider.simpleprovider.TagsProvider;

public class TagDummyPojo extends Pojo {

    public TagDummyPojo(String id) {
        super(id);
        setName(id.substring(TagsProvider.SCHEME.length()), false);
    }
}
