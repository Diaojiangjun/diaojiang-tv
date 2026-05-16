package com.fongmi.android.tv.utils;

public class Github {

    public static final String URL = "https://cdn.jsdelivr.net/gh/HAOyangcl/diaojiangjun@main";

    private static String getUrl(String name) {
        return URL + "/apk/" + name;
    }

    public static String getJson(String name) {
        return getUrl(name + ".json");
    }

    public static String getApk(String name) {
        return getUrl(name + ".apk");
    }
}
