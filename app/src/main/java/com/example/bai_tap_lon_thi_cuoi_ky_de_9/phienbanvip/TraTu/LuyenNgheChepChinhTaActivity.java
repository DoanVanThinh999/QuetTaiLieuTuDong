package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import static androidx.databinding.library.baseAdapters.BR.speaker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.Database1.LuuDangBaiNgheSQLite;
import com.Model.DangBaiNghe;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LuyenNgheChiTietDoanHoiThoaiActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuyenNgheChepChinhTaActivity extends AppCompatActivity {
    private TTSManager ttsManager;
    private RecordingManager recordingManager;
    private EditText editTextInput;
    private Button btnSpeak, btnRecord,btnKhoiPhucDB;
    private SeekBar speedControl;
    private Spinner btnPlay;
    private TextView tvTaiKhoanKH;
    private LichSuGhiAmAdapter historyAdapter;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    TaiKhoan taiKhoan;
    String lastGeneratedFile = "";
    private boolean isListVisible = true;
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_IS_HIDDEN = "isListHidden";
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_nghe_chep_chinh_ta);

        ttsManager = new TTSManager(this);
        recordingManager = new RecordingManager(this);

        editTextInput = findViewById(R.id.editTextInput);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnRecord = findViewById(R.id.btnRecord);
        btnPlay = findViewById(R.id.btnPlay);
        speedControl = findViewById(R.id.speedControl);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        btnKhoiPhucDB = findViewById(R.id.btnKhoiPhucDB);
        Intent intent = getIntent();
        taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan"); // 👈 Gán vào biến toàn cục

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isHidden = sharedPreferences.getBoolean(KEY_IS_HIDDEN, true); // mặc định ẩn


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{" ", "Hội thoại", "Đoạn văn"});
        btnPlay.setAdapter(adapter);


        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));

        requestPermissions();

        btnSpeak.setOnClickListener(v -> {
            String textToSpeak = editTextInput.getText().toString();
            if (!textToSpeak.isEmpty()) {
                ttsManager.speak(textToSpeak, getSpeed());
            } else {
                Toast.makeText(this, "Vui lòng nhập đoạn văn!", Toast.LENGTH_SHORT).show();
            }
        });

        ListView lvHistory = findViewById(R.id.lv_history_luu_doan);

        new Thread(() -> {
            LuuDangBaiNgheSQLite db = new LuuDangBaiNgheSQLite(this);
            List<DangBaiNghe> listFromDb = db.getLichSuTraTuByUserId(taiKhoan.getId());


            ArrayList<String> filePaths = new ArrayList<>();
            ArrayList<String> loaiCauList = new ArrayList<>();
            ArrayList<String> textContentList = new ArrayList<>();

            for (DangBaiNghe item : listFromDb) {
                String fullPath = getFilesDir().getAbsolutePath() + "/" + item.getFileName();
                filePaths.add(fullPath);
                loaiCauList.add(item.getLoaiCau());
                textContentList.add(item.getTextContent());
            }

            runOnUiThread(() -> {
                historyAdapter = new LichSuGhiAmAdapter(this, filePaths, loaiCauList, "", taiKhoan, textContentList);
                lvHistory.setAdapter(historyAdapter);
            });
        }).start();

        btnRecord.setOnClickListener(v -> {
            String text = editTextInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Nhập đoạn văn bản trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nhập tên file");
            final EditText input = new EditText(this);
            input.setHint("Tên file (không cần .wav)");
            builder.setView(input);

            builder.setPositiveButton("Lưu", (dialog, which) -> {
                String fileNameInput = input.getText().toString().trim();
                String fileName = fileNameInput.isEmpty() ? "tts_" + System.currentTimeMillis() : fileNameInput;
                fileName += ".wav";

                LuuDangBaiNgheSQLite db1 = new LuuDangBaiNgheSQLite(this);

                if (db1.isFileExist(fileName, taiKhoan.getId())) {
                    Toast.makeText(this, "Tên file đã tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }


                List<DangBaiNghe> currentList = db1.getLichSuTraTuByUserId(taiKhoan.getId());
                for (DangBaiNghe item : currentList) {
                    if (item.getTextContent().equals(text)) {
                        Toast.makeText(this, "Đoạn văn này đã được lưu trước đó!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String loaiCau = btnPlay.getSelectedItem().toString().trim();
                if (btnPlay.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Vui lòng chọn loại đề trước khi lưu!", Toast.LENGTH_SHORT).show();
                    return;
                }




                final String fileNameFinal = fileName;
                final String fullPath = getFilesDir().getAbsolutePath() + "/" + fileNameFinal;
                final String tenBaiFinal = "Đề nghe " + System.currentTimeMillis();

                // 👉 Nếu là HỘI THOẠI → chọn giọng, lưu voice map
                if (loaiCau.equals("Hội thoại")) {
                    showVoiceSelectionDialog(text, speakerGenders -> {
                        lastGeneratedFile = recordingManager.convertTextToSpeech(text, fileNameFinal);
                        String genderJson = new Gson().toJson(speakerGenders);
                        // ✅ THÊM LOG Ở ĐÂY ĐỂ KIỂM TRA!
                        Log.d("GENDER_JSON_DEBUG", "JSON lưu vào DB: " + genderJson);
                        String accent = "us"; // hoặc lấy từ người dùng
                        db1.insertLichSuTraTu(tenBaiFinal, loaiCau, fileNameFinal, taiKhoan.getId(), text, genderJson, accent);


                        // ✅ Cập nhật ListView
                        historyAdapter.getFilePaths().add(0, fullPath);
                        historyAdapter.getLoaiCauList().add(0, loaiCau);
                        historyAdapter.getTextContentList().add(0, text);
                        historyAdapter.notifyDataSetChanged();

                        Toast.makeText(this, "Đã lưu file: " + fileNameFinal, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // 👉 Nếu là ĐOẠN VĂN thì mặc định giọng nam (speakerGenders để trống)
                    lastGeneratedFile = recordingManager.convertTextToSpeech(text, fileNameFinal);
                    db1.insertLichSuTraTu(tenBaiFinal, loaiCau, fileNameFinal, taiKhoan.getId(), text, "", "us");

                    // ✅ Cập nhật ListView
                    historyAdapter.getFilePaths().add(0, fullPath);
                    historyAdapter.getLoaiCauList().add(0, loaiCau);
                    historyAdapter.getTextContentList().add(0, text);
                    historyAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Đã lưu file: " + fileNameFinal, Toast.LENGTH_SHORT).show();
                    db1.exportDatabase(this);
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
        // Ẩn hoặc hiện dựa vào trạng thái đã lưu
        lvHistory.setVisibility(isHidden ? View.GONE : View.VISIBLE);
        btnKhoiPhucDB.setText(isHidden ? "📁 Mở tài liệu" : "Ẩn câu hỏi");
        btnKhoiPhucDB.setOnClickListener(v -> {
            boolean isHiddenNow = sharedPreferences.getBoolean(KEY_IS_HIDDEN, true);

            if (isHiddenNow) {
                // Nếu đang ẩn → hiển thị Dialog nhập mật khẩu
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("🔐 Nhập mật khẩu khôi phục:");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                input.setHint("Nhập đúng 6 chữ số");
                builder.setView(input);

                builder.setPositiveButton("Mở", (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.equals("310400")) {
                        lvHistory.setVisibility(View.VISIBLE);
                        btnKhoiPhucDB.setText("Ẩn câu hỏi");

                        sharedPreferences.edit().putBoolean(KEY_IS_HIDDEN, false).apply();
                        reloadListView(taiKhoan);
                    } else {
                        Toast.makeText(this, "❌ Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
                builder.setCancelable(false);
                builder.show();

            } else {
                // Nếu đang hiện → ẩn luôn
                lvHistory.setVisibility(View.GONE);
                btnKhoiPhucDB.setText("📁 Mở tài liệu");

                sharedPreferences.edit().putBoolean(KEY_IS_HIDDEN, true).apply();
            }
        });

        speedControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private float getSpeed() {
        return 0.5f + (speedControl.getProgress() / 100.0f);
    }

    private boolean checkRecordPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    private void showVoiceSelectionDialog(String dialogueText, OnSpeakersConfiguredListener callback) {
        Set<String> uniqueSpeakers = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("^(\\w+):", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(dialogueText);

        while (matcher.find()) {
            uniqueSpeakers.add(matcher.group(1)); // Chỉ lấy phần tên nhân vật
        }

        Map<String, String> speakerGenders = new HashMap<>();
        Iterator<String> iterator = uniqueSpeakers.iterator();

        showNextSpeakerDialog(iterator, speakerGenders, callback);


    }
    private void showNextSpeakerDialog(Iterator<String> iterator, Map<String, String> speakerGenders, OnSpeakersConfiguredListener callback) {
        if (!iterator.hasNext()) {
            callback.onConfigured(speakerGenders);
            return;
        }

        String speaker = iterator.next();
        String[] genders = {"Nam", "Nữ"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn giọng cho nhân vật: " + speaker);

        builder.setSingleChoiceItems(genders, -1, (dialog, which) -> {
            speakerGenders.put(speaker.toLowerCase(), genders[which]);
            dialog.dismiss();

            // Gọi tiếp cho nhân vật tiếp theo
            showNextSpeakerDialog(iterator, speakerGenders, callback);
        });

        builder.setCancelable(false);
        builder.show();
    }
    public interface OnSpeakersConfiguredListener {
        void onConfigured(Map<String, String> speakerGenders);
    }



    private void requestPermissions() {
        if (!checkRecordPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }
    private void reloadListView(TaiKhoan taiKhoan) {
        new Thread(() -> {
            LuuDangBaiNgheSQLite db = new LuuDangBaiNgheSQLite(this);
            List<DangBaiNghe> listFromDb = db.getLichSuTraTuByUserId(taiKhoan.getId());

            ArrayList<String> filePaths = new ArrayList<>();
            ArrayList<String> loaiCauList = new ArrayList<>();
            ArrayList<String> textContentList = new ArrayList<>();

            for (DangBaiNghe item : listFromDb) {
                String fullPath = getFilesDir().getAbsolutePath() + "/" + item.getFileName();
                filePaths.add(fullPath);
                loaiCauList.add(item.getLoaiCau());
                textContentList.add(item.getTextContent());
            }
            runOnUiThread(() -> Toast.makeText(this, "🔄 Đang load lại DB...", Toast.LENGTH_SHORT).show());
            runOnUiThread(() -> {
                historyAdapter = new LichSuGhiAmAdapter(this, filePaths, loaiCauList, "", taiKhoan, textContentList);
                ListView lvHistory = findViewById(R.id.lv_history_luu_doan);
                lvHistory.setAdapter(historyAdapter);
            });
            Log.d("RELOAD_LIST", "Đang load dữ liệu sau khi khôi phục...");


        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền ghi âm đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền ghi âm để sử dụng tính năng này!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
