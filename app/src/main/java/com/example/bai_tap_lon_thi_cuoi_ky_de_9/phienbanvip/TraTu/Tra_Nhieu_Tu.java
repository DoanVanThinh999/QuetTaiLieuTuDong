package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.Tra_Nhieu_Tu_Moi.DichTuDoanDai;
import com.Tra_Nhieu_Tu_Moi.TraNhieuTuAdapter;
import com.Tra_Nhieu_Tu_Moi.TraNhieuTuMoiSQLite;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Tra_Nhieu_Tu extends AppCompatActivity {
    // ngrok http 5000
    private static final String API_URL = "https://157d-2001-ee0-496e-db10-5b6-c418-3d39-b6c0.ngrok-free.app/predict";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ScrollView scrollView1, scrollView2;
    private EditText edtNoiDung;
    private TextView edtDich, tvTitle;
    private Button btnAnhViet, btnVietAnh;
    private ImageButton btnPlayResult1, btnPlayResult;
    private TextToSpeech textToSpeech;
    private TraNhieuTuMoiSQLite dbHelper;
    private ListView lv_history;
    private TraNhieuTuAdapter adapter;
    private int userId; // ID của tài khoản hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tra_nhieu_tu);

        edtNoiDung = findViewById(R.id.edtNoiDung);
        edtDich = findViewById(R.id.edtDich);
        btnAnhViet = findViewById(R.id.btn_translate);
        btnVietAnh = findViewById(R.id.btn_translate1);
        tvTitle = findViewById(R.id.tvTitle);
        scrollView2 = findViewById(R.id.scrollView2);
        scrollView1 = findViewById(R.id.scrollView1);
        lv_history = findViewById(R.id.lv_history);

        dbHelper = new TraNhieuTuMoiSQLite(this);

        // Lấy thông tin tài khoản từ Intent
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        if (taiKhoan != null) {
            userId = taiKhoan.getId(); // Lưu ID người dùng hiện tại
            tvTitle.setText("Xin chào, " + taiKhoan.getHoTen());
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Lấy danh sách lịch sử từ SQLite
        List<DichTuDoanDai> dataList = dbHelper.getLichSuTraTuByUserId(userId);

        // Khởi tạo adapter và gán cho ListView
        adapter = new TraNhieuTuAdapter(this, dataList);
        lv_history.setAdapter(adapter);

        lv_history.setOnItemLongClickListener((parent, view, position, id) -> {
            // Lấy mục đang được nhấn
            DichTuDoanDai item = (DichTuDoanDai) adapter.getItem(position);
            if (item != null) {
                // Hiển thị AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Tra_Nhieu_Tu.this);
                builder.setTitle("Xác nhận");
                builder.setMessage("Bạn có chắc chắn muốn xóa mục này không?");
                builder.setPositiveButton("Có", (dialogInterface, i) -> {
                    // Xóa mục khỏi SQLite
                    boolean result = dbHelper.deleteLichSuTraTu(item.getTuTra(), userId);
                    if (result) {
                        Toast.makeText(Tra_Nhieu_Tu.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                        updateListView();
                    } else {
                        Toast.makeText(Tra_Nhieu_Tu.this, "Lỗi khi xóa mục", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Không", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.create().show();
            } else {
                Log.e("DEBUG", "Item tại vị trí " + position + " là null");
            }
            return true;
        });

        scrollView2.post(() -> scrollView2.fullScroll(ScrollView.FOCUS_DOWN));
        scrollView1.post(() -> scrollView1.fullScroll(ScrollView.FOCUS_DOWN));

        btnPlayResult = findViewById(R.id.btnPlayResult);
        btnPlayResult1 = findViewById(R.id.btnPlayResult1);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ không được hỗ trợ!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Khởi tạo TextToSpeech thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlayResult1.setOnClickListener(v -> handlePlayButton(edtNoiDung.getText().toString().trim(), btnPlayResult1, "TEXT_1"));
        btnPlayResult.setOnClickListener(v -> handlePlayButton(edtDich.getText().toString().trim(), btnPlayResult, "TEXT_2"));

        btnAnhViet.setOnClickListener(v -> translateText("en", "vi"));
        btnVietAnh.setOnClickListener(v -> translateText("vi", "en"));
    }

    private void handlePlayButton(String text, ImageButton button, String utteranceId) {
        if (!text.isEmpty()) {
            if (isEnglish(text)) {
                textToSpeech.setLanguage(Locale.US);
            } else {
                textToSpeech.setLanguage(new Locale("vi"));
            }

            if (!textToSpeech.isSpeaking()) {
                button.setImageResource(android.R.drawable.ic_media_pause);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                }
            } else {
                textToSpeech.stop();
                button.setImageResource(android.R.drawable.ic_media_play);
            }
        } else {
            Toast.makeText(this, "Không có nội dung để đọc!", Toast.LENGTH_SHORT).show();
        }
    }

    private void translateText(String sourceLang, String targetLang) {
        String question = edtNoiDung.getText().toString().trim();
        if (!question.isEmpty()) {
            sendPredictionRequest(question, sourceLang, targetLang);
        } else {
            Toast.makeText(this, "Vui lòng nhập nội dung cần dịch!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPredictionRequest(String question, String sourceLang, String targetLang) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String json = "{\"question\":\"" + question + "\",\"source\":\"" + sourceLang + "\",\"target\":\"" + targetLang + "\"}";
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String translatedText = jsonObject.getString("translated_text");

                    boolean result = dbHelper.insertLichSuTraTu(question, translatedText, userId);

                    runOnUiThread(() -> {
                        edtDich.setText(translatedText);
                        if (result) {
                            Toast.makeText(this, "Lưu lịch sử tra từ thành công", Toast.LENGTH_SHORT).show();
                            updateListView();
                        } else {
                            Toast.makeText(this, "Lỗi khi lưu vào SQLite", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateListView() {
        List<DichTuDoanDai> updatedList = dbHelper.getLichSuTraTuByUserId(userId);
        adapter = new TraNhieuTuAdapter(this, updatedList);
        lv_history.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private boolean isEnglish(String text) {
        return text.matches("^[a-zA-Z0-9\\s,.!?]+$");
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
