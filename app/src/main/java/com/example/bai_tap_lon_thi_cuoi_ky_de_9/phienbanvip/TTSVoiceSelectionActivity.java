package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TTSVoiceSelectionActivity extends AppCompatActivity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                showVoiceSelectionDialog(tts, voice -> {
                    tts.setVoice(voice);
                    Toast.makeText(this, "\uD83C\uDFA4 Đã chọn giọng: " + voice.getName(), Toast.LENGTH_SHORT).show();

                    // Phát thử 1 câu
                    tts.speak("This is a male or female voice test.", TextToSpeech.QUEUE_FLUSH, null, "testVoice");
                });
            } else {
                Toast.makeText(this, "❌ Không khởi tạo được TTS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVoiceSelectionDialog(TextToSpeech tts, OnVoiceSelected callback) {
        List<Voice> voices = new ArrayList<>(tts.getVoices());
        List<Voice> voiceEn = new ArrayList<>();

        for (Voice voice : voices) {
            if (voice.getLocale().equals(Locale.US)) {
                voiceEn.add(voice);
            }
        }

        if (voiceEn.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy giọng tiếng Anh nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] voiceNames = new String[voiceEn.size()];
        for (int i = 0; i < voiceEn.size(); i++) {
            voiceNames[i] = voiceEn.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn giọng nói tiếng Anh");
        builder.setItems(voiceNames, (dialog, which) -> callback.onSelected(voiceEn.get(which)));
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    public interface OnVoiceSelected {
        void onSelected(Voice voice);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
