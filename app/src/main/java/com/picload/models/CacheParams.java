package com.picload.models;

public class CacheParams {

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String tag;
    private String url;

    public CacheParams(String url, String tag) {
        this.url = url;
        this.tag = tag;
    }

    public CacheParams() {
    }
}
