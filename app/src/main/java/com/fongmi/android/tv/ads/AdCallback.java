package com.fongmi.android.tv.ads;

/**
 * 激励广告回调
 */
public interface AdCallback {
    void onAdLoaded();
    void onAdFailed(String error);
    void onAdClosed();
    void onAdClicked();
}
