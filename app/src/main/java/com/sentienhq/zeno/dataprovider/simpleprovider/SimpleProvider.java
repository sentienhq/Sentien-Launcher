package com.sentienhq.zeno.dataprovider.simpleprovider;

import com.sentienhq.zeno.dataprovider.IProvider;
import com.sentienhq.zeno.pojo.Pojo;

import java.util.List;

/**
 * Unlike normal providers, simple providers are not Android Services but classic Android class
 * Android Services are expensive to create, and use a lot of memory,
 * so whenever we can, we avoid using them.
 */
public abstract class SimpleProvider implements IProvider {
    @Override
    public void reload() {
        // Simple providers can't be reloaded
    }

    @Override
    public final boolean isLoaded() {
        return true;
    }

    @Override
    public boolean mayFindById(String id) {
        return false;
    }

    @Override
    public Pojo findById(String id) {
        return null;
    }

    @Override
    public List<? extends Pojo> getPojos() {
        return null;
    }
}
