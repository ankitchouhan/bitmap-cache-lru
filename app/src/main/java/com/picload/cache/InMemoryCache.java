package com.picload.cache;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE;

public class InMemoryCache implements ImageCache, ImageCache.UpdateInMemoryCache {

    private LruCache<String, Bitmap> cache;

    public InMemoryCache() {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = (int) (maxMemory / 8);
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void put(String url, Bitmap bitmap) {
        cache.put(url, bitmap);
    }

    @Override
    public Bitmap get(String url) {
        return cache.get(url);
    }

    @Override
    public void clear() {
        cache.evictAll();
    }

    @Override
    public void trimMemory(int level) {
        if (level >= TRIM_MEMORY_MODERATE) {
            cache.evictAll();
        } else if (level >= TRIM_MEMORY_BACKGROUND) {
            cache.trimToSize(cache.size() / 2);
        }
    }
}
