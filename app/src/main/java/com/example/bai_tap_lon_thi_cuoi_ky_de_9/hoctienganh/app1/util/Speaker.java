package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import org.jetbrains.annotations.NotNull;

/**
 * Biểu tượng dùng để phiên âm một từ (có hình cái loa, bấm vô cái loa sẽ phát ra âm thanh)
 */
public class Speaker {
    public static final int SPEAK_READY = 0, SPEAK_LOADING = 1, SPEAK_PLAYING = 2, SPEAK_ERROR = 3;
    private final Context mCtx;
    private int mSpeakState;
    private final String mSound;
    @NotNull
    private final ImageView mImg;
    private static ImageView sOldImg;
    private static MediaPlayer sPlayer;
    private OnSpeakStateChangeListener onSpeakStateChangeListener;

    private Speaker(Context context, @NonNull ImageView img, String sound) {
        mCtx = context;
        mImg = img;
        mSound = sound;
        if (sPlayer == null) sPlayer = new MediaPlayer();
        img.setOnClickListener(this::speak);
    }

    public static Speaker from(Context context, @NonNull ImageView img, String sound) {
        return new Speaker(context, img, sound);
    }

    private void setSpeakState(ImageView img, int speakState) {
        this.mSpeakState = speakState;
        img.setImageResource(speakState == SPEAK_LOADING ? R.drawable.loading
                : speakState == SPEAK_PLAYING ? R.drawable.pause
                : R.drawable.speak);
        if (onSpeakStateChangeListener == null) {
            if (speakState == SPEAK_ERROR) Toast.makeText(mCtx, R.string.sound_no_internet, Toast.LENGTH_SHORT).show();
            return;
        }
        onSpeakStateChangeListener.onSpeakStateChanged(img, speakState);
    }

    public void setOnSpeakStateListener(OnSpeakStateChangeListener onSpeakStateChangeListener) {
        this.onSpeakStateChangeListener = onSpeakStateChangeListener;
    }

    private boolean error(MediaPlayer player, int i, int i1) {
        setSpeakState(mImg, SPEAK_ERROR);
        return true;
    }

    private void play(MediaPlayer player) {
        setSpeakState(mImg, SPEAK_PLAYING);
        player.start();
    }

    private void reset(MediaPlayer player) {
        setSpeakState(mImg, SPEAK_READY);
        if (sOldImg != mImg) setSpeakState(sOldImg, SPEAK_READY);
        player.reset();
    }

    public void speak(View view) {
        if (mSpeakState == SPEAK_LOADING) return;
        try {
            if (sPlayer.isPlaying()) {
                reset(sPlayer);
                if (mImg == sOldImg) {
                    sOldImg = null;
                    return; // Nhấn nút dừng
                }
            }
            sOldImg = mImg;
            setSpeakState(mImg, SPEAK_LOADING);
            Log.d("Speaker", mSound);
            sPlayer.setDataSource(mSound);
            sPlayer.setOnErrorListener(this::error);
            sPlayer.setOnPreparedListener(this::play);
            sPlayer.setOnCompletionListener(this::reset);
            sPlayer.prepareAsync();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        sPlayer.release();
        sOldImg = null;
    }

    public interface OnSpeakStateChangeListener {
        void onSpeakStateChanged(ImageView view, int speakState);
    }
}
