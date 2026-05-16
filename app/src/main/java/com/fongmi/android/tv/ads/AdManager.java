package com.fongmi.android.tv.ads;

import android.app.Activity;

import com.fongmi.android.tv.ads.adsterra.AdsterraSmartlinkAd;

public class AdManager {

    private static AdManager instance;
    private AdsterraSmartlinkAd currentAd;
    private AdConfig config;

    private AdManager() {
        config = new AdConfig();
    }

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void init(AdConfig config) {
        this.config = config;
    }

    public void loadSplashAd(Activity activity, AdCallback callback) {
        if (!config.isSplashAdEnabled()) {
            if (callback != null) callback.onAdFailed("Splash ad disabled");
            return;
        }

        if (!config.shouldShowAd()) {
            if (callback != null) callback.onAdFailed("Ad already clicked today");
            return;
        }

        currentAd = new AdsterraSmartlinkAd(activity);
        currentAd.loadAd(new AdCallback() {
            @Override
            public void onAdLoaded() {
                config.recordAdShow();
                if (callback != null) callback.onAdLoaded();
            }

            @Override
            public void onAdFailed(String error) {
                if (callback != null) callback.onAdFailed(error);
            }

            @Override
            public void onAdClosed() {
                if (callback != null) callback.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                config.recordAdClick();
                if (callback != null) callback.onAdClicked();
            }
        });
    }

    public void showSplashAd() {
        if (currentAd != null && currentAd.isReady()) {
            currentAd.showAd();
        }
    }

    public void destroy() {
        if (currentAd != null) {
            currentAd.destroy();
            currentAd = null;
        }
    }

    public AdsterraSmartlinkAd getCurrentAd() {
        return currentAd;
    }
}