package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.List;

public class MarkerView extends View {
    private List<Long> markerTimesMs = new ArrayList<>();
    private long duration = 1;

    public MarkerView(Context context) {
        super(context);
        init();
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {}

    public void setMarkers(List<Long> markers, long videoDuration) {
        this.markerTimesMs = markers;
        this.duration = videoDuration > 0 ? videoDuration : 1;
        invalidate(); // redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);

        for (long markerTime : markerTimesMs) {
            float pos = (float) markerTime / duration;
            float x = pos * getWidth();
            canvas.drawCircle(x, height / 2f, 8, paint);
        }
    }
}
