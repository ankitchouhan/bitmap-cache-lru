package com.picload.ui;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.picload.R;
import com.picload.cache.AppDoubleCache;
import com.picload.interfaces.BitmapCallback;
import com.picload.models.CacheParams;
import com.picload.models.PhotoData;
import com.picload.utils.AppExecutor;
import com.picload.utils.Constants;
import com.picload.utils.ImageLoader;
import com.picload.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements BitmapCallback, ComponentCallbacks2 {

    private Button previous, next;
    private ImageView imageView;
    private List<PhotoData> photoDataList;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private AppDoubleCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        // Not caching api response due to which api will be called every time activity created.
        // Caching response is preferred in real use case scenarios.
        getApiResponse();
    }

    private void initView() {
        next = (Button) findViewById(R.id.next);
        previous = (Button) findViewById(R.id.previous);
        imageView = (ImageView) findViewById(R.id.imageview);
        cache = AppDoubleCache.findOrCreateCache(this);
        ImageLoader.getInstance().setCache(cache);
        ImageLoader.getInstance().setBitmapCallBack(this);
        //onclick of previous button should navigate the user to previous image
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoDataList != null && photoDataList.size() > 0) {
                    fetchImage(photoDataList.get(Utility.generateRandomNumberFromGivenRange(0, photoDataList.size())));
                }
            }
        });
        //onclick of next button should navigate the user to next image
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoDataList != null && photoDataList.size() > 0) {
                    fetchImage(photoDataList.get(Utility.generateRandomNumberFromGivenRange(0, photoDataList.size())));
                }
            }
        });
    }

    private void getApiResponse() {
        if (!Utility.isNetworkAvailable(getApplicationContext())) {
            Utility.showToastMessage(this, "Internet not available.");
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String response = Utility.getResponse(Constants.BASE_URL);
                parseJsonResponse(response);
                if (photoDataList != null && photoDataList.size() > 0) {
                    PhotoData data = photoDataList.get(0);
                    fetchImage(data);
                }
            }
        };
        AppExecutor.submitTask(runnable);
    }

    /**
     * Parsing received response.
     */
    private void parseJsonResponse(String response) {
        try {
            JSONObject ob1 = new JSONObject(response);
            if (ob1.has(Constants.KEY_PHOTOS)) {
                JSONObject ob2 = ob1.getJSONObject(Constants.KEY_PHOTOS);
                if (ob2.has(Constants.KEY_PHOTO_ARRAY)) {
                    JSONArray array = ob2.getJSONArray(Constants.KEY_PHOTO_ARRAY);
                    if (array.length() > 0) {
                        photoDataList = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            String id = object.getString(Constants.KEY_ID);
                            String secret = object.getString(Constants.KEY_SECRET);
                            String server = object.getString(Constants.KEY_SERVER);
                            int farm = object.getInt(Constants.KEY_FARM);
                            PhotoData photoData = new PhotoData(id, secret, server, farm);
                            photoDataList.add(photoData);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ImageLoader.getInstance().clearCache();
    }

    @Override
    public void getBitmap(final Bitmap bitmap, final CacheParams cacheParams) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && imageView != null && cacheParams.getTag().equals(imageView.getTag()))
                    imageView.setImageBitmap(bitmap);
            }
        });
    }

    /**
     * Fetch image from the network or cache.
     */
    private void fetchImage(PhotoData data) {
        if (!Utility.isNetworkAvailable(getApplicationContext())) {
            Utility.showToastMessage(this, "Internet not available.");
            return;
        }
        String url = Utility.getUrl(data);
        imageView.setTag(data.getId());
        CacheParams params = new CacheParams(url, data.getId());
        ImageLoader.getInstance().displayImage(params);
    }

    @Override
    public void onTrimMemory(int level) {
        cache.trimMemory(level);
    }
}
