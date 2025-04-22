package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.InputFilter;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import java.util.*;

public class LuyenNgheChiTietActivity extends AppCompatActivity {

    private TextView tvAudioTime;
    private Button btnNgheLai, btnKiemTra, btnTiepTheo, btnXemDapAn, btnPhanDoanNgauNhien;
    private ImageButton btnPlay;
    private Spinner spinnerSpeed;
    private EditText tvPhanTungDoan;
    private SeekBar seekBarAudio;
    private LinearLayout cauHoiContainer;
    private ArrayList<EditText> editTextList = new ArrayList<>();
    private ArrayList<String> dapAnList = new ArrayList<>();
    private ArrayList<String> cauGocList;
    private ArrayList<String> cauNhomList;
    private String cauDangPhat = "";
    private float tocDoPhat = 1.0f;
    private int chiSoCau = 0;
    private Timer timer;
    private List<Set<Integer>> danhSachViTriAn = new ArrayList<>();
    private TextToSpeech textToSpeech;
    private List<List<String>> danhSachDapAnNguoiDung = new ArrayList<>();
    private boolean isPlaying = false;
    private int dapAnIndex = 0; // ➕ Thêm dòng này vào đầu class
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_nghe_chi_tiet);

        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvAudioTime = findViewById(R.id.tvAudioTime);
        cauHoiContainer = findViewById(R.id.cauHoiContainer);
        btnNgheLai = findViewById(R.id.btnNgheLai);
        btnKiemTra = findViewById(R.id.btnKiemTra);
        btnTiepTheo = findViewById(R.id.btnTiepTheo);
        btnXemDapAn = findViewById(R.id.btnXemDapAn);
        spinnerSpeed = findViewById(R.id.spinnerSpeed);
        btnPhanDoanNgauNhien = findViewById(R.id.btnPhanDoanNgauNhien);
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(v -> {
            if (!isPlaying) {
                // Đang ở trạng thái dừng, giờ phát
                isPlaying = true;
                btnPlay.setImageResource(R.drawable.baseline_pause_24); // ảnh pause bạn vừa gửi
                batDauPhatAmThanh();
            } else {
                // Đang phát, giờ dừng
                isPlaying = false;
                btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                dungPhatAmThanh();
            }
        });


        tvPhanTungDoan = findViewById(R.id.tvPhanTungDoan);

        tvPhanTungDoan.setMovementMethod(new ScrollingMovementMethod());
        tvPhanTungDoan.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        Intent intent = getIntent();
        String noiDung = intent.getStringExtra("textContent");
        cauGocList = new ArrayList<>(Arrays.asList(noiDung.split("(?<=[.!?])\\s+")));
        cauNhomList = taoNhomCau(cauGocList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"0.5x", "0.75x", "Normal", "1.25x", "1.5x", "2x"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(adapter);
        spinnerSpeed.setSelection(2);
        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tocDoPhat = chuyenTocDo(parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        String accent = getIntent().getStringExtra("accent");
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if ("uk".equals(accent)) {
                    textToSpeech.setLanguage(Locale.UK);
                } else {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });


        hienThiCau(chiSoCau);

        btnNgheLai.setOnClickListener(v -> {
            if (chiSoCau > 0) {
                luuDapAnNguoiDung(chiSoCau); // Lưu trước khi quay lại
                chiSoCau--;
                hienThiCau(chiSoCau);
            } else {
                Toast.makeText(this, "⚠️ Đây là câu đầu tiên!", Toast.LENGTH_SHORT).show();
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

        btnPhanDoanNgauNhien.setOnClickListener(v -> {
            cauNhomList = taoNhomCau(cauGocList);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cauNhomList.size(); i++) {
                sb.append((i + 1)).append(". ").append(cauNhomList.get(i)).append("\n\n");
            }
            tvPhanTungDoan.setText(sb.toString().trim());
            chiSoCau = 0;
            hienThiCau(chiSoCau);
        });
    }
    private void batDauPhatAmThanh() {
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
                        isPlaying = false;
                        btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
                    }
                });
            }
        }, 0, 100);

        textToSpeech.speak(cauDangPhat, TextToSpeech.QUEUE_FLUSH, null, "tts1");
    }
    private void dungPhatAmThanh() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);
        }
        if (timer != null) {timer.cancel();
            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24);}
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
    private LinearLayout taoDongMoi() {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return line;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
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





    private void hienThiCau(int index) {
        dapAnIndex = 0; // Reset lại khi chuyển câu mới
        cauDangPhat = cauNhomList.get(index);
        List<String> dapAnCu = (index < danhSachDapAnNguoiDung.size()) ? danhSachDapAnNguoiDung.get(index) : null;
        taoViewChoCauHoi(cauDangPhat, dapAnCu);
        phatLaiCau();
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
                    if (time >= doDaiUocTinh) timer.cancel();
                });
            }
        }, 0, 100);
        textToSpeech.speak(cauDangPhat, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private long uocLuongThoiGian(String text, float speed) {
        int wordCount = text.split("\\s+").length;
        return (long) ((wordCount * 0.4f / speed) * 1000);
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds % 60);
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

    private ArrayList<String> taoNhomCau(ArrayList<String> ds) {
        ArrayList<String> kq = new ArrayList<>();
        for (String cau : ds) {
            String[] tu = cau.trim().split("\\s+");
            int tongTu = tu.length;
            int batDau = 0;

            while (batDau < tongTu) {
                int soTu = new Random().nextInt(4) + 6; // 6–9 từ
                if (batDau + soTu > tongTu) {
                    soTu = tongTu - batDau;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < soTu && (batDau + i) < tongTu; i++) {
                    sb.append(tu[batDau + i]).append(" ");
                }

                String group = sb.toString().trim();

                // ➕ Nếu chưa kết thúc bằng dấu chấm, thêm vào
                if (!group.endsWith(".") && batDau + soTu >= tongTu) {
                    group += ".";
                }

                kq.add(group.trim());
                batDau += soTu;
            }
        }
        return kq;
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}