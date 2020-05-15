package com.picload.cache;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.fragment.app.FragmentActivity;

import com.picload.ui.RetainFragment;

public class AppDoubleCache implements ImageCache, ImageCache.UpdateInMemoryCache {

    private InMemoryCache memoryCache;
    private DiskLruCache diskLruCache;

    private AppDoubleCache(Context context) {
        diskLruCache = DiskLruCache.openCache(context.getCacheDir(), 10 * 1024 * 1024);
        memoryCache = new InMemoryCache();
    }

    /**
     * Finding the retained instance or creating new if not found any.
     *
     * @param activity instance of activity.
     */
    public static AppDoubleCache findOrCreateCache(FragmentActivity activity) {
        // Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(
                activity.getSupportFragmentManager());

        // See if we already have an ImageCache stored in RetainFragment
        AppDoubleCache imageCache = (AppDoubleCache) mRetainFragment.getObject();

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
            imageCache = new AppDoubleCache(activity.getApplicationContext());
            mRetainFragment.setObject(imageCache);
        }
        return imageCache;
    }

    @Override
    public void put(String url, Bitmap bitmap) {
        if (memoryCache != null)
            memoryCache.put(url, bitmap);
        if (diskLruCache != null)
            diskLruCache.put(url, bitmap);
    }

    @Override
    public Bitmap get(String url) {
        if (memoryCache != null && memoryCache.get(url) != null)
            return memoryCache.get(url);
        else if (diskLruCache != null && diskLruCache.get(url) != null)
            return diskLruCache.get(url);
        return null;
    }

    @Override
    public void clear() {
        memoryCache.clear();
        diskLruCache.clearCache();
    }

    @Override
    public void trimMemory(int level) {
        memoryCache.trimMemory(level);
    }
}
