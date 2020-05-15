package com.picload.utils;

public class Constants {
    public static final String BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=3e7cc266ae2b0e0d78e279ce8e361736&format=json&nojsoncallback=1&safe_search=1&tags=kitten&per_page=10&page=1";
    public static final String IMAGE_URL = "https://farm${this.farm}.staticflickr.com/${this.server}/${this.id}_${this.secret}_q.jpg";
    public static final String KEY_PHOTOS = "photos";
    public static final String KEY_PHOTO_ARRAY = "photo";
    public static final String KEY_ID = "id";
    public static final String KEY_SECRET = "secret";
    public static final String KEY_SERVER = "server";
    public static final String KEY_FARM = "farm";
}
