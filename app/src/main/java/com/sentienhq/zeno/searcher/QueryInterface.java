package com.sentienhq.zeno.searcher;

import com.sentienhq.zeno.ui.ListPopup;

public interface QueryInterface {
    void launchOccurred();

    void registerPopup(ListPopup popup);
}
