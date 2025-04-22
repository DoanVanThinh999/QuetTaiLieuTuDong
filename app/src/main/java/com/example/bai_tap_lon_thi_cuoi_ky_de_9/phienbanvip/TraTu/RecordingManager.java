package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class RecordingManager {
    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    private Context context;

    public RecordingManager(Context context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    // Chuyển văn bản thành giọng nói và lưu thành file
    public String convertTextToSpeech(String text, String filename) {
        File dir = context.getExternalFilesDir(null);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, filename);
        textToSpeech.synthesizeToFile(text, null, file, filename);

        return file.getAbsolutePath();
    }

    // Phát file với tốc độ chỉnh
    public void playWithSpeed(String filePath, float speed) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("RecordingManager", "Lỗi khi phát lại: " + e.getMessage());
        }
    }
}
