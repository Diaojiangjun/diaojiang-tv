package com.fongmi.android.tv.ads.adsterra;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.fongmi.android.tv.ads.AdCallback;
import com.fongmi.android.tv.ads.AdProvider;

public class AdsterraSmartlinkAd implements AdProvider {

    private static final String ADSTERRA_SMARTLINK = "https://www.effectivecpmnetwork.com/un51jcfx1?key=efd2039a52d24c1bac4e63d059b605c0";

    private final Activity activity;
    private AdCallback callback;
    private boolean isReady = true;

    public AdsterraSmartlinkAd(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void loadAd(AdCallback callback) {
        this.callback = callback;
        
        if (callback != null) {
            callback.onAdLoaded();
        }
    }

    @Override
    public void showAd() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ADSTERRA_SMARTLINK));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            
            if (callback != null) {
                callback.onAdClicked();
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onAdFailed("Failed to open ad: " + e.getMessage());
            }
        }
    }

    @Override
    public void destroy() {
        isReady = false;
        callback = null;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }
}