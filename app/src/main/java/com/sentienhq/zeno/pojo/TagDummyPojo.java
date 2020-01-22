package com.sentienhq.zeno.pojo;

import com.sentienhq.zeno.dataprovider.simpleprovider.TagsProvider;

public class TagDummyPojo extends Pojo {

    public TagDummyPojo(String id) {
        super(id);
        setName(id.substring(TagsProvider.SCHEME.length()), false);
    }
}
