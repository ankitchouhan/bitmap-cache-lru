package com.picload.utils;

import android.graphics.Bitmap;

import com.picload.cache.ImageCache;
import com.picload.interfaces.BitmapCallback;
import com.picload.models.CacheParams;


public class ImageLoader {

    private ImageCache cache;
    private BitmapCallback bitmapCallback;
    private static volatile ImageLoader INSTANCE;

    public static ImageLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (ImageLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageLoader();
                }
            }
        }
        return INSTANCE;
    }

    public void setCache(ImageCache imageCache) {
        cache = imageCache;
    }

    public void setBitmapCallBack(BitmapCallback bitmapCallBack) {
        this.bitmapCallback = bitmapCallBack;
    }

    /**
     * Fetching image from cache or from url if not found in cache.
     *
     * @param cacheParams Params with url and tag
     */
    public void displayImage(final CacheParams cacheParams) {
        Bitmap cachedBitmap = cache.get(cacheParams.getUrl());
        if (cachedBitmap != null) {
            updateImageView(cachedBitmap, cacheParams);
            return;
        }
        AppExecutor.submitTask(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Utility.getBitmapFromURL(cacheParams.getUrl());
                if (bitmap != null) {
                    updateImageView(bitmap, cacheParams);
                    cache.put(cacheParams.getUrl(), bitmap);
                }
            }
        });
    }

    /**
     * Updating views for received bitmap.
     */
    private void updateImageView(Bitmap cachedBitmap, CacheParams cacheParams) {
        bitmapCallback.getBitmap(cachedBitmap, cacheParams);
    }

    public void clearCache() {
        cache.clear();
    }
}
