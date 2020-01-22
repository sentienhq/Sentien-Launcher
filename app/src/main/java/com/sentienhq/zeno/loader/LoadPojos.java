package com.sentienhq.zeno.loader;

import android.content.Context;
import android.os.AsyncTask;

import com.sentienhq.zeno.dataprovider.Provider;
import com.sentienhq.zeno.pojo.Pojo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class LoadPojos<T extends Pojo> extends AsyncTask<Void, Void, ArrayList<T>> {

    final WeakReference<Context> context;
    String pojoScheme = "(none)://";
    private WeakReference<Provider<T>> provider;

    LoadPojos(Context context, String pojoScheme) {
        super();
        this.context = new WeakReference<>(context);
        this.pojoScheme = pojoScheme;
    }

    public void setProvider(Provider<T> provider) {
        this.provider = new WeakReference<>(provider);
    }

    public String getPojoScheme() {
        return pojoScheme;
    }

    @Override
    protected void onPostExecute(ArrayList<T> result) {
        super.onPostExecute(result);
        if (provider != null) {
            provider.get().loadOver(result);
        }
    }

}
