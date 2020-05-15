package com.picload.interfaces;

import android.graphics.Bitmap;

import com.picload.models.CacheParams;

public interface BitmapCallback {
    void getBitmap(Bitmap bitmap, CacheParams cacheParams);
}
