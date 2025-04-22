package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.DefaultTimeBar;

@UnstableApi
public class PersistentMarkerTimeBar extends DefaultTimeBar {
    public PersistentMarkerTimeBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdGroupTimesMs(long[] adGroupTimesMs, boolean[] playedAdGroups, int adGroupCount) {
        boolean[] alwaysUnplayed = new boolean[adGroupCount];
        for (int i = 0; i < adGroupCount; i++) {
            alwaysUnplayed[i] = false;
        }
        super.setAdGroupTimesMs(adGroupTimesMs, alwaysUnplayed, adGroupCount);
    }
}

