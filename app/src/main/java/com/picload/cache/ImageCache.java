package com.picload.cache;

import android.graphics.Bitmap;

public interface ImageCache {
    void put(String url, Bitmap bitmap);
    Bitmap get(String url);
    void clear();

    interface UpdateInMemoryCache {
        void trimMemory(int level);
    }
}
