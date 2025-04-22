package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuyenNgheChiTietDoanHoiThoaiActivity extends AppCompatActivity {
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
    ArrayList<String> cauNhomList;
    private Button btnPhanDoanNgauNhien, btnNgheLai,btnKiemTra, btnTiepTheo, btnXemDapAn;
    int dapAnIndex = 0; // ➕ Thêm dòng này vào đầu class
    private ArrayList<String> dapAnList = new ArrayList<>();
    private String cauDangPhat = "";
    private List<List<String>> danhSachDapAnNguoiDung = new ArrayList<>();
    private ArrayList<EditText> editTextList = new ArrayList<>();
    private List<Set<Integer>> danhSachViTriAn = new ArrayList<>();
    TextToSpeech textToSpeech;
    // THÊM BIẾN mới ở class
    private boolean isPhatLai = false;
    private String lastSpeakerKey = "";  // Lưu nhân vật đang nói
    private String lastGender = "Nam";   // Lưu giới tính của nhân vật


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_nghe_chi_tiet_doan_hoi_thoai);
        btnNgheLai  = findViewById(R.id.btnNgheLai);
        btnPlay = findViewById(R.id.btnPlay);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvAudioTime = findViewById(R.id.tvAudioTime);
        spinnerSpeed = findViewById(R.id.spinnerSpeed);
        cauHoiContainer = findViewById(R.id.cauHoiContainer);
        btnKiemTra = findViewById(R.id.btnKiemTra);
        btnTiepTheo = findViewById(R.id.btnTiepTheo);
        btnXemDapAn = findViewById(R.id.btnXemDapAn);
        btnPhanDoanNgauNhien = findViewById(R.id.btnPhanDoanNgauNhien);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US); // hoặc Locale.UK
            }
        });

        btnPhanDoanNgauNhien.setOnClickListener(v -> {
            Intent intent = new Intent(LuyenNgheChiTietDoanHoiThoaiActivity.this, TTSVoiceSelectionActivity.class);
            startActivity(intent);
        });
        btnKiemTra.setOnClickListener(v -> {
            boolean allCorrect = true;
            for (int i = 0; i < editTextList.size(); i++) {
                String nhap = editTextList.get(i).getText().toString().trim().replaceAll("[^a-zA-Z]", "");
                String dung = dapAnList.get(i).replaceAll("[^a-zA-Z]", "");
                if (nhap.equalsIgnoreCase(dung)) editTextList.get(i).setTextColor(Color.GREEN);
                else {
                    editTextList.get(i).setTextColor(Color.RED);
                    allCorrect = false;
                }
            }
            if (allCorrect) {
                cauHoiContainer.setBackgroundColor(Color.parseColor("#A5D6A7"));
                new Handler().postDelayed(() -> btnTiepTheo.performClick(), 1000);
            }
        });
        btnXemDapAn.setOnClickListener(v -> {
            if (dapAnIndex < editTextList.size()) {
                EditText edt = editTextList.get(dapAnIndex);
                String dapAn = dapAnList.get(dapAnIndex);
                edt.setText(dapAn);
                edt.setTextColor(Color.BLUE);
                edt.setEnabled(false); // tránh sửa lại đáp án

                dapAnIndex++;
            } else {
                Toast.makeText(this, "✅ Đã hiện hết đáp án rồi!", Toast.LENGTH_SHORT).show();
            }
        });
        btnTiepTheo.setOnClickListener(v -> {
            luuDapAnNguoiDung(chiSoCau); // lưu lại câu hiện tại

            chiSoCau++;
            if (chiSoCau < cauNhomList.size()) {
                hienThiCau(chiSoCau);
            } else {
                Toast.makeText(this, "\u2714\uFE0F Hết bài nghe rồi!", Toast.LENGTH_SHORT).show();
            }
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
        cauNhomList = new ArrayList<>(cauHoiList);
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
                isPhatLai = true; // ✅ Đánh dấu là chỉ muốn phát lại
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.baseline_pause_24);
                phatCauHoi(); // Gọi phatCauHoi như bình thường
            } else {
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                dungPhat();
            }
        });



        btnNgheLai.setOnClickListener(v -> {
            if (chiSoCau > 0) {
                luuDapAnNguoiDung(chiSoCau); // Lưu trước khi quay lại
                chiSoCau--;
                hienThiCau(chiSoCau);
            } else {
                Toast.makeText(this, "⚠️ Đây là câu đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void hienThiCau(int index) {
        dapAnIndex = 0; // Reset lại khi chuyển câu mới
        cauDangPhat = cauNhomList.get(index);
        List<String> dapAnCu = (index < danhSachDapAnNguoiDung.size()) ? danhSachDapAnNguoiDung.get(index) : null;
        //taoViewChoCauHoi(cauDangPhat, dapAnCu);
        if (!isPhatLai) {
            taoViewChoCauHoi(cauDangPhat, dapAnCu);  // 🔥 Chỉ tạo mới View khi KHÔNG phải là play lại
        }

        phatLaiCau();
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
    private void phatLaiCau() {
        textToSpeech.setSpeechRate(tocDoPhat);
        long doDaiUocTinh = uocLuongThoiGian(cauDangPhat, tocDoPhat);
        seekBarAudio.setMax((int) doDaiUocTinh);
        seekBarAudio.setProgress(0);
        tvAudioTime.setText("00:00");

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
                    if (time >= doDaiUocTinh) {
                        timer.cancel();
                        isPlaying = false; // 👈 thêm dòng này
                        btnPlay.setImageResource(R.drawable.baseline_play_arrow_24); // 👈 và dòng này
                    }
                });
            }
        }, 0, 100);

        textToSpeech.speak(cauDangPhat, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void phatCauHoi() {
        dapAnIndex = 0; // Reset đáp án mỗi câu

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

            // Chuẩn hóa speakerKey
            String speakerKey = rawSpeaker.trim().replace(":", "").toLowerCase().replaceAll("\\s+", "");
            String gender = speakerGenders.getOrDefault(speakerKey, "Nam");
            lastSpeakerKey = speakerKey;
            lastGender = gender;
            TextToSpeech currentTTS = gender.equalsIgnoreCase("Nữ") ? ttsNu : ttsNam;
            Log.d("TTS_DEBUG", "Phát giọng: " + gender + ", Nhân vật: " + speakerKey);

            if (currentTTS != null) {
                currentTTS.setSpeechRate(tocDoPhat);
                currentTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, "tts" + chiSoCau);
                isPhatLai = false;
            }

            // Tạo view chép chính tả cho dòng thoại
            cauDangPhat = content;
            List<String> dapAnCu = (chiSoCau < danhSachDapAnNguoiDung.size()) ? danhSachDapAnNguoiDung.get(chiSoCau) : null;
            taoViewChoCauHoi(cauDangPhat, dapAnCu);

            // Điều khiển seekbar
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
                            isPlaying = false;
                            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                        }
                    });
                }
            }, 0, 100);

        } else {
            chiSoCau++;
            phatCauHoi();
        }
    }





//    private void phatCauHoi() {
//        if (chiSoCau >= cauHoiList.size()) {
//            Toast.makeText(this, "✅ Hết đoạn hội thoại!", Toast.LENGTH_SHORT).show();
//            isPlaying = false;
//            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
//            return;
//        }
//
//        String dong = cauHoiList.get(chiSoCau);
//        Matcher matcher = Pattern.compile("^(\\w+):\\s*(.*)", Pattern.CASE_INSENSITIVE).matcher(dong);
//
//
//        if (matcher.find()) {
//            String rawSpeaker = matcher.group(1).trim();
//            String content = matcher.group(2).trim();
//
//            TextView tv = new TextView(this);
//            tv.setText(dong);
//            tv.setTextSize(16f);
//            tv.setTextColor(Color.BLACK);
//            cauHoiContainer.addView(tv);
//
//            // Chuẩn hóa speakerKey tuyệt đối:
//            String speakerKey = rawSpeaker.trim()
//                    .replace(":", "")
//                    .toLowerCase()
//                    .replaceAll("\\s+", "");
//            String gender = speakerGenders.getOrDefault(speakerKey, "Nam");
//            // ✅ Sửa luôn kiểm tra như thế này để đảm bảo:
//            TextToSpeech currentTTS;
//            if (gender.equalsIgnoreCase("Nữ") || gender.equalsIgnoreCase("nu")) {
//                currentTTS = ttsNu;
//            } else {
//                currentTTS = ttsNam;
//            }
//            Log.d("TTS_DEBUG", "Phát giọng: " + gender + ", Nhân vật: " + speakerKey);
//            // Bắt đầu phát giọng đã chọn
//            if (currentTTS != null) {
//                currentTTS.setSpeechRate(tocDoPhat);
//                currentTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, "tts" + chiSoCau);
//            }
//            //TextToSpeech currentTTS = "Nữ".equalsIgnoreCase(gender) ? ttsNu : ttsNam;
//
//            if (currentTTS != null) {
//                currentTTS.setSpeechRate(tocDoPhat);
//                currentTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null, "tts" + chiSoCau);
//            }
//
//            long duration = (long) ((content.split("\\s+").length * 0.5f / tocDoPhat) * 1000);
//            seekBarAudio.setMax((int) duration);
//            seekBarAudio.setProgress(0);
//
//            if (timer != null) timer.cancel();
//            timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask() {
//                int time = 0;
//
//                @Override
//                public void run() {
//                    runOnUiThread(() -> {
//                        time += 100;
//                        seekBarAudio.setProgress(time);
//                        tvAudioTime.setText(formatTime(time));
//                        if (time >= duration) {
//                            timer.cancel();
//                            chiSoCau++;
//                            if (isPlaying) phatCauHoi();
//                        }
//                    });
//                }
//            }, 0, 100);
//        } else {
//            chiSoCau++;
//            phatCauHoi();
//        }
//    }



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
    private void taoViewChoCauHoi(String cau, List<String> dapAnCu) {
        cauHoiContainer.removeAllViews();
        editTextList.clear();
        dapAnList.clear();

        String[] tu = cau.split(" ");
        Set<Integer> viTriAn;
        if (chiSoCau < danhSachViTriAn.size()) {
            viTriAn = danhSachViTriAn.get(chiSoCau);
        } else {
            viTriAn = chonTuAn(tu); // chỉ gọi khi lần đầu tiên
            danhSachViTriAn.add(viTriAn);
        }
        int indexDapAn = 0;
        LinearLayout dong = taoDongMoi();
        int widthUsed = 0;
        int maxWidth = Resources.getSystem().getDisplayMetrics().widthPixels - dpToPx(48); // tính cả padding

        for (int i = 0; i < tu.length; i++) {
            String word = tu[i];
            View view;

            if (viTriAn.contains(i)) {
                EditText edt = new EditText(this);
                edt.setHint("____");
                edt.setTextSize(16f);
                edt.setSingleLine();
                edt.setBackgroundResource(R.drawable.bg_blank);
                edt.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(20),
                        (source, start, end, dest, dstart, dend) -> source.toString().contains(" ") ? "" : null
                });

                edt.setMinWidth(dpToPx(40));
                edt.setMaxWidth(dpToPx(100));
                edt.setEms(4);
                view = edt;

                if (dapAnCu != null && indexDapAn < dapAnCu.size()) {
                    edt.setText(dapAnCu.get(indexDapAn));
                }
                editTextList.add(edt);
                indexDapAn++;

                dapAnList.add(word);
            } else {
                TextView tv = new TextView(this);
                tv.setText(word + " ");
                tv.setTextSize(16f);
                tv.setTextColor(Color.BLACK);
                tv.setMaxWidth(dpToPx(150));
                view = tv;
            }

            // Đo view
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int viewWidth = view.getMeasuredWidth() + dpToPx(8);

            // Nếu vượt dòng thì xuống dòng
            if (widthUsed + viewWidth > maxWidth) {
                cauHoiContainer.addView(dong);
                dong = taoDongMoi();
                widthUsed = 0;
            }

            // Set margin
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            view.setLayoutParams(params);

            dong.addView(view);
            widthUsed += viewWidth;

            // 👇 Nếu từ kết thúc bằng ".", coi như kết thúc 1 câu -> ép xuống dòng
            if (word.endsWith(".")) {
                cauHoiContainer.addView(dong);
                dong = taoDongMoi();
                widthUsed = 0;
            }
        }

        // Thêm dòng cuối
        cauHoiContainer.addView(dong);
    }
    private int dpToPx(int dp) {
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
    }
    private void luuDapAnNguoiDung(int index) {
        if (index >= danhSachDapAnNguoiDung.size()) {
            // Nếu chưa có, thêm mới
            while (danhSachDapAnNguoiDung.size() <= index) {
                danhSachDapAnNguoiDung.add(new ArrayList<>());
            }
        }

        List<String> dapAn = new ArrayList<>();
        for (EditText edt : editTextList) {
            dapAn.add(edt.getText().toString());
        }
        danhSachDapAnNguoiDung.set(index, dapAn);
    }
    private List<String> tachCauHoi(String fullText) {
        List<String> ketQua = new ArrayList<>();
        String[] lines = fullText.split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) ketQua.add(line.trim());
        }
        return ketQua;
    }
    private LinearLayout taoDongMoi() {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return line;
    }
    private long uocLuongThoiGian(String text, float speed) {
        int wordCount = text.split("\\s+").length;
        return (long) ((wordCount * 0.4f / speed) * 1000);
    }
    private Set<Integer> chonTuAn(String[] tu) {
        Set<Integer> set = new HashSet<>();
        int length = tu.length;
        int soTuAn;

        if (length < 6) {
            soTuAn = new Random().nextInt(3) + 3;
        } else {
            soTuAn = new Random().nextInt(4) + 2;
        }

        while (set.size() < soTuAn && set.size() < length - 1) {
            int index = new Random().nextInt(length);
            if (tu[index].matches("[a-zA-Z]+")) { // tránh chọn dấu câu hoặc số
                set.add(index);
            }
        }

        return set;
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