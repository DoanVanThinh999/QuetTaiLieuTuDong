package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TTSVoiceSelectionActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TichDoanVanHoiThoai extends AppCompatActivity {
    private ImageButton btnPlay;
    private SeekBar seekBarAudio;
    private TextView tvAudioTime;
    private Spinner spinnerSpeed;
    private LinearLayout cauHoiContainer;
    private float tocDoPhat = 1.0f;
    private boolean isPlaying = false;
    private Timer timer;
    private List<String> cauHoiList;
    private int chiSoCau = 0;
    private TextToSpeech ttsNam, ttsNu;
    private Map<String, String> speakerGenders = new HashMap<>();
    private Button btnPhanDoanNgauNhien;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_nghe_chi_tiet_doan_hoi_thoai);

        btnPlay = findViewById(R.id.btnPlay);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvAudioTime = findViewById(R.id.tvAudioTime);
        spinnerSpeed = findViewById(R.id.spinnerSpeed);
        cauHoiContainer = findViewById(R.id.cauHoiContainer);
        btnPhanDoanNgauNhien = findViewById(R.id.btnPhanDoanNgauNhien);

        btnPhanDoanNgauNhien.setOnClickListener(v -> {
            Intent intent = new Intent(TichDoanVanHoiThoai.this, TTSVoiceSelectionActivity.class);
            startActivity(intent);
        });

        Intent intent = getIntent();
        String textContent = intent.getStringExtra("textContent");
        String genderJson = intent.getStringExtra("speakerGenders");
        String accent = intent.getStringExtra("accent"); // "us" hoặc "uk"
        if (accent == null) accent = "us"; // mặc định Mỹ nếu không có
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        if (genderJson != null) {
            speakerGenders = new Gson().fromJson(genderJson, type);
            Log.d("GENDER_JSON_DEBUG", "JSON đọc từ DB: " + genderJson);
            Log.d("GENDER_JSON_DEBUG", "speakerGenders Map: " + speakerGenders.toString());

        }

        cauHoiList = tachCauHoi(textContent);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"0.5x", "0.75x", "Normal", "1.25x", "1.5x", "2x"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(adapter);
        spinnerSpeed.setSelection(2);
        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tocDoPhat = chuyenTocDo(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        initTextToSpeech(accent);

        btnPlay.setOnClickListener(v -> {
            if (!isPlaying) {
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.baseline_pause_24);
                phatCauHoi();
            } else {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                dungPhat();
            }
        });
    }




    private void initTextToSpeech(String accent) {
        Locale locale = "uk".equalsIgnoreCase(accent) ? Locale.UK : Locale.US;

        ttsNu = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsNu.setLanguage(locale); // ⬅ ảnh hưởng accent
                for (Voice voice : ttsNu.getVoices()) {
                    if ("en-US-language".equals(voice.getName())) {
                        ttsNu.setVoice(voice); // ⬅ cố định giọng nữ
                        Log.d("TTS_SELECT", "✅ Giọng NỮ được chọn: " + voice.getName());
                        break;
                    }
                }
            }
        });

        ttsNam = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsNam.setLanguage(locale); // ⬅ ảnh hưởng accent
                for (Voice voice : ttsNam.getVoices()) {
                    if ("en-us-x-iom-network".equals(voice.getName())) {
                        ttsNam.setVoice(voice); // ⬅ cố định giọng nam
                        Log.d("TTS_SELECT", "✅ Giọng NAM được chọn: " + voice.getName());
                        break;
                    }
                }
            }
        });


    }






        private void phatCauHoi() {
        if (chiSoCau >= cauHoiList.size()) {
            Toast.makeText(this, "✅ Hết đoạn hội thoại!", Toast.LENGTH_SHORT).show();
            isPlaying = false;
            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
            return;
        }

        String dong = cauHoiList.get(chiSoCau);
        Matcher matcher = Pattern.compile("^(\\w+):\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(dong);


        if (matcher.find()) {
            String rawSpeaker = matcher.group(1).trim();
            String content = matcher.group(2).trim();

            TextView tv = new TextView(this);
            tv.setText(dong);
            tv.setTextSize(16f);
            tv.setTextColor(Color.BLACK);
            cauHoiContainer.addView(tv);

            // Chuẩn hóa speakerKey tuyệt đối:
            String speakerKey = rawSpeaker.trim()
                    .replace(":", "")
                    .toLowerCase()
                    .replaceAll("\\s+", "");
            String gender = speakerGenders.getOrDefault(speakerKey, "Nam");
            // ✅ Sửa luôn kiểm tra như thế này để đảm bảo:
            TextToSpeech currentTTS;
            if (gender.equalsIgnoreCase("Nữ") || gender.equalsIgnoreCase("nu")) {
                currentTTS = ttsNu;
            } else {
                currentTTS = ttsNam;
            }
            Log.d("TTS_DEBUG", "Phát giọng: " + gender + ", Nhân vật: " + speakerKey);
            // Bắt đầu phát giọng đã chọn
            if (currentTTS != null) {
                currentTTS.setSpeechRate(tocDoPhat);
                currentTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, "tts" + chiSoCau);
            }
            //TextToSpeech currentTTS = "Nữ".equalsIgnoreCase(gender) ? ttsNu : ttsNam;

            if (currentTTS != null) {
                currentTTS.setSpeechRate(tocDoPhat);
                currentTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, "tts" + chiSoCau);
            }

            long duration = (long) ((content.split("\\s+").length * 0.5f / tocDoPhat) * 1000);
            seekBarAudio.setMax((int) duration);
            seekBarAudio.setProgress(0);

            if (timer != null) timer.cancel();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int time = 0;

                @Override
                public void run() {
                    runOnUiThread(() -> {
                        time += 100;
                        seekBarAudio.setProgress(time);
                        tvAudioTime.setText(formatTime(time));
                        if (time >= duration) {
                            timer.cancel();
                            chiSoCau++;
                            if (isPlaying) phatCauHoi();
                        }
                    });
                }
            }, 0, 100);
        } else {
            chiSoCau++;
            phatCauHoi();
        }
    }



    private void dungPhat() {
        if (ttsNam != null) ttsNam.stop();
        if (ttsNu != null) ttsNu.stop();
        if (timer != null) timer.cancel();
    }

    private float chuyenTocDo(String val) {
        switch (val) {
            case "0.5x": return 0.5f;
            case "0.75x": return 0.75f;
            case "1.25x": return 1.25f;
            case "1.5x": return 1.5f;
            case "2x": return 2.0f;
            default: return 1.0f;
        }
    }

    private List<String> tachCauHoi(String fullText) {
        List<String> ketQua = new ArrayList<>();
        String[] lines = fullText.split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) ketQua.add(line.trim());
        }
        return ketQua;
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
    }

    @Override
    protected void onDestroy() {
        if (ttsNam != null) {
            ttsNam.stop();
            ttsNam.shutdown();
        }
        if (ttsNu != null) {
            ttsNu.stop();
            ttsNu.shutdown();
        }
        super.onDestroy();
    }
}