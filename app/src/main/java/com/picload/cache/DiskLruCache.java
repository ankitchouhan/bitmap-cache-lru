package com.picload.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiskLruCache {

    private static final String TAG = "DiskLruCache";

    private static final String CACHE_FILENAME_PREFIX = "cache_";
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final int MAX_REMOVALS = 4;
    private final int maxCacheItemSize = 64; // 64 item default
    private final File mCacheDir;
    private int cacheSize;
    private int cacheByteSize = 0;
    private long maxCacheSizeInBytes = 5 * 1024 * 1025; // 5MB default value
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 90;


    private final Map<String, String> map =
            new LinkedHashMap<>(16, 0.75f, true);

    /**
     * A filename filter to use to identify the cache filenames which have CACHE_FILENAME_PREFIX
     * prepended.
     */
    private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return filename.startsWith(CACHE_FILENAME_PREFIX);
        }
    };

    /**
     * Using private constructor to runs some extra checks before
     * creating a DiskLruCache instance.
     *
     * @param cacheDirectory Directory to store and access cache data.
     * @param maxSize        max size of the cache.
     */
    private DiskLruCache(File cacheDirectory, long maxSize) {
        this.mCacheDir = cacheDirectory;
        this.maxCacheSizeInBytes = maxSize;
    }

    /**
     * Used to fetch an instance of DiskLruCache.
     *
     * @param cacheDir Directory to store and access cache data.
     * @param maxSize  max size of the cache.
     */
    public static DiskLruCache openCache(File cacheDir, long maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        if (cacheDir.isDirectory() && cacheDir.canWrite()) {
            return new DiskLruCache(cacheDir, maxSize);
        }
        return null;
    }

    /**
     * Add a bitmap to the disk cache.
     *
     * @param key    A unique identifier for the bitmap.
     * @param bitmap The bitmap to store.
     */
    public void put(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            throw new NullPointerException("key == null || bitmap == null");
        }
        synchronized (map) {
            if (map.get(key) == null) {
                try {
                    final String file = createFilePath(mCacheDir, key);
                    if (writeBitmapToFile(bitmap, file)) {
                        put(key, file);
                        flushCache();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error in put: " + e.getMessage());
                    System.out.println(e.getMessage() + " error while adding " + key);
                } catch (IOException e) {
                    Log.e(TAG, "Error in put: " + e.getMessage());
                    System.out.println(e.getMessage() + " error while adding " + key);
                }
            }
        }
    }

    /**
     * Adding key and file to map.
     */
    private void put(String key, String fileName) {
        map.put(key, fileName);
        cacheSize = map.size();
        cacheByteSize += new File(fileName).length();
    }

    /**
     * Flush the cache, removing oldest entries if the total size is over the specified cache size.
     */
    private void flushCache() {
        Map.Entry<String, String> eldestEntry;
        File eldestFile;
        long eldestFileSize;
        int count = 0;
        while (count < MAX_REMOVALS &&
                (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheSizeInBytes)) {
            eldestEntry = map.entrySet().iterator().next();
            eldestFile = new File(eldestEntry.getValue());
            eldestFileSize = eldestFile.length();
            map.remove(eldestEntry.getKey());
            eldestFile.delete();
            cacheSize = map.size();
            cacheByteSize -= eldestFileSize;
            count++;
            Log.d(TAG, "flushCache - Removed cache file, " + eldestFile + ", "
                    + eldestFileSize);
        }
    }

    /**
     * Get an image from the disk cache.
     *
     * @param key The unique key for the bitmap
     */
    public Bitmap get(String key) {
        synchronized (map) {
            final String file = map.get(key);
            if (file != null) {
                Log.d(TAG, "Disk cache hit");
                return BitmapFactory.decodeFile(file);
            } else {
                final String existingFile = createFilePath(mCacheDir, key);
                if (existingFile == null) return null;
                if (new File(existingFile).exists()) {
                    put(key, existingFile);
                    Log.d(TAG, "Disk cache hit (existing file)");
                    return BitmapFactory.decodeFile(existingFile);
                }
            }
            return null;
        }
    }

    /**
     * Removes all disk cache entries from this instance cache dir.
     */
    public synchronized void clearCache() {
        DiskLruCache.clearCache(mCacheDir);
        map.clear();
        cacheSize = 0;
        cacheByteSize = 0;
    }

    private static void clearCache(File cacheDir) {
        final File[] files = cacheDir.listFiles(cacheFileFilter);
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    /**
     * Creates a constant cache file path given a target cache directory and an image key.
     *
     * @param cacheDir
     * @param key
     * @return
     */
    public static String createFilePath(File cacheDir, String key) {
        try {
            return cacheDir.getAbsolutePath() + File.separator + CACHE_FILENAME_PREFIX +
                    URLEncoder.encode(key.replace("*", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Writes a bitmap to a file.
     *
     * @param bitmap bitmap to cache
     * @param file   file name in which bitmap to be written
     * @return
     */
    private boolean writeBitmapToFile(Bitmap bitmap, String file) throws IOException, FileNotFoundException {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, outputStream);
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
    }

    public void setCompressParams(Bitmap.CompressFormat compressFormat, int quality) {
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }
}
