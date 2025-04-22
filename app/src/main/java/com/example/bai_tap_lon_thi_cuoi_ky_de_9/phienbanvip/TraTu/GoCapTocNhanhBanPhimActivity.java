package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

public class GoCapTocNhanhBanPhimActivity extends AppCompatActivity {

    EditText etHours, etMinutes, etSeconds, etUserInput, tvCauHoi;
    TextView tvTimer, tvPhanTich, tvTaiKhoanKH;
    Button btnStart, btnRetry;
    ScrollView scrollView1,scrollView2;
    CountDownTimer countDownTimer;
    long totalTimeMillis = 0;

    final String cauHoi
            = "I have many friends, but my best friend is Anna. She is my classmate, and we have known each other since primary school. Anna is a kind and cheerful girl with a bright smile that makes everyone around her happy. She has long, wavy brown hair and big, sparkling eyes.\n" +
            "Anna is not only friendly but also very intelligent. She always helps me with my studies, especially in English and Math. Whenever I have difficulties, she patiently explains everything to me. She is also very funny and knows how to make me laugh even when I feel sad.";

    boolean isFinished = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_cap_toc_nhanh_ban_phim);

        // Ánh xạ View
        etHours = findViewById(R.id.etHours);
        etMinutes = findViewById(R.id.etMinutes);
        etSeconds = findViewById(R.id.etSeconds);
        etUserInput = findViewById(R.id.etUserInput);
        tvTimer = findViewById(R.id.tvTimer);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        tvCauHoi.setMovementMethod(new ScrollingMovementMethod()); // Bật cuộn trong EditText

        tvCauHoi.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true); // Chặn ScrollView ngoài
            return false;
        });


        tvPhanTich = findViewById(R.id.tvPhanTich);
        btnStart = findViewById(R.id.btnStart);
        btnRetry = findViewById(R.id.btnRetry);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        tvCauHoi.setText(cauHoi);
        btnRetry.setEnabled(false);
        scrollView1 = findViewById(R.id.scrollView1);
        tvCauHoi.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        if (scrollView1 != null) {
            scrollView1.post(() -> {
                if (tvCauHoi.getLineCount() > 5) {
                    // ✅ Cuộn xuống mượt mà nhưng vẫn có thể vuốt lên
                    scrollView1.smoothScrollTo(0, tvCauHoi.getBottom());
                }
            });
        }
        scrollView2 = findViewById(R.id.scrollView2);
        etUserInput.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        if (scrollView2 != null) {
            scrollView2.post(() -> {
                if (etUserInput.getLineCount() > 5) {
                    // ✅ Cuộn xuống mượt mà nhưng vẫn có thể vuốt lên
                    scrollView2.smoothScrollTo(0, etUserInput.getBottom());
                }
            });
        }

        etUserInput.setEnabled(false); // 🔒 Không cho nhập khi chưa bấm "Bắt đầu"




        btnStart.setOnClickListener(v -> {
            etUserInput.setText("");
            isFinished = false;
            etUserInput.setEnabled(true);
            tvPhanTich.setText("");

            int hours = parseInt(etHours.getText().toString());
            int minutes = parseInt(etMinutes.getText().toString());
            int seconds = parseInt(etSeconds.getText().toString());

            totalTimeMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;

            if (totalTimeMillis <= 0) {
                Toast.makeText(this, "Vui lòng nhập thời gian hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            startCountdown(totalTimeMillis);
        });

        btnRetry.setOnClickListener(v -> {
            etUserInput.setText("");
            etUserInput.setEnabled(false);
            tvPhanTich.setText("");
            btnRetry.setEnabled(false);
            tvTimer.setText("Thời gian còn lại: 00:00:00");
        });

        etUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence userInput, int start, int before, int count) {
                highlightText(userInput.toString());
                if (!isFinished && userInput.length() == cauHoi.length() &&
                        userInput.toString().equals(cauHoi)) {
                    countDownTimer.cancel();
                    isFinished = true;
                    etUserInput.setEnabled(false);
                    tvPhanTich.setText("🎉 Bạn đã thắng!");
                    btnRetry.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void startCountdown(long millisInFuture) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long h = seconds / 3600;
                long m = (seconds % 3600) / 60;
                long s = seconds % 60;
                tvTimer.setText(String.format("Thời gian còn lại: %02d:%02d:%02d", h, m, s));
            }

            public void onFinish() {
                if (!isFinished) {
                    tvTimer.setText("Hết giờ!");
                    etUserInput.setEnabled(false);
                    tvPhanTich.setText("❌ Bạn đã thua! Hãy thử lại.");
                    btnRetry.setEnabled(true);
                }
            }
        };
        countDownTimer.start();
    }

    private void highlightText(String userInput) {
        SpannableString spannable = new SpannableString(cauHoi);

        for (int i = 0; i < cauHoi.length(); i++) {
            if (i < userInput.length()) {
                int color = (userInput.charAt(i) == cauHoi.charAt(i)) ? Color.GREEN : Color.RED;
                spannable.setSpan(new ForegroundColorSpan(color), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        tvCauHoi.setText(spannable);
        tvCauHoi.setMovementMethod(new ScrollingMovementMethod());

    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
