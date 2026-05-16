package com.fongmi.android.tv.ads;

/**
 * 激励广告
 */
public interface AdProvider {
    void loadAd(AdCallback callback);
    void showAd();
    void destroy();
    boolean isReady();
}
