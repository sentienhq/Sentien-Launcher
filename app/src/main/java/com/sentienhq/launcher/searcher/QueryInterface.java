package com.sentienhq.launcher.searcher;

import com.sentienhq.launcher.ui.ListPopup;

public interface QueryInterface {
    void launchOccurred();

    void registerPopup(ListPopup popup);
}
