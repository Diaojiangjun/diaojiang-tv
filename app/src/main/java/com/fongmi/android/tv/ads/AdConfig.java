package com.fongmi.android.tv.ads;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AdConfig {

    private static final String PREF_NAME = "ad_config";
    private static final String KEY_LAST_SHOW_TIME = "last_show_time";
    private static final String KEY_SHOW_COUNT = "show_count";
    private static final String KEY_LAST_SHOW_DATE = "last_show_date";
    private static final String KEY_CLICKED_DATE = "clicked_date";

    private boolean splashAdEnabled = true;
    private int maxShowsPerDay = 10;

    private SharedPreferences prefs;

    public AdConfig() {}

    public void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSplashAdEnabled() {
        return splashAdEnabled;
    }

    public void setSplashAdEnabled(boolean enabled) {
        this.splashAdEnabled = enabled;
    }



    public boolean shouldShowAd() {
        if (prefs == null) return true;

        if (hasClickedToday()) {
            return false;
        }

        int todayCount = getTodayShowCount();
        if (todayCount >= maxShowsPerDay) return false;

        return true;
    }

    public void recordAdShow() {
        if (prefs == null) return;
        
        int currentCount = getTodayShowCount();
        prefs.edit()
                .putLong(KEY_LAST_SHOW_TIME, System.currentTimeMillis())
                .putInt(KEY_SHOW_COUNT, currentCount + 1)
                .putString(KEY_LAST_SHOW_DATE, getCurrentDateKey())
                .apply();
    }

    public void recordAdClick() {
        if (prefs == null) return;
        
        prefs.edit()
                .putString(KEY_CLICKED_DATE, getCurrentDateKey())
                .apply();
    }

    private boolean hasClickedToday() {
        String today = getCurrentDateKey();
        String clickedDate = prefs.getString(KEY_CLICKED_DATE, "");
        return today.equals(clickedDate);
    }

    private int getTodayShowCount() {
        String today = getCurrentDateKey();
        String lastDate = prefs.getString(KEY_LAST_SHOW_DATE, "");
        
        if (!today.equals(lastDate)) {
            return 0;
        }
        
        return prefs.getInt(KEY_SHOW_COUNT, 0);
    }
    
    private String getCurrentDateKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + 
               String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" + 
               String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }
}