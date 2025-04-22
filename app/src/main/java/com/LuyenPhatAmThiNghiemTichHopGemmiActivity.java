package com;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.text.textclassifier.TextClassifier;
import com.google.mediapipe.tasks.text.textclassifier.TextClassifierResult;
import com.google.mediapipe.tasks.components.containers.Classifications;
import com.google.mediapipe.tasks.components.containers.Category; // Chú ý class này!



import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LuyenPhatAmThiNghiemTichHopGemmiActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final String MODEL_PATH = "bert_classifier.tflite";

    private TextView edtNoiDung, tvTaiKhoanKH, tvCauHoi, tvPhanTich, tvDiemSo, tvTimer;
    private ImageButton btnSpeak;
    private ScrollView scrollView, scrollView3;
    private Button btnSubmit;

    private String recordedAnswer = "";
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    private TextClassifier textClassifier;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_phat_am_thi_nghiem_tich_hop_gemmi);

        edtNoiDung = findViewById(R.id.edtNoiDung);
        tvTimer = findViewById(R.id.tvTimer);
        btnSpeak = findViewById(R.id.btnSpeak);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        tvPhanTich = findViewById(R.id.tvPhanTich);
        tvDiemSo = findViewById(R.id.tvDiemSo);
        scrollView = findViewById(R.id.scrollView);
        btnSubmit = findViewById(R.id.btnSubmit);
        scrollView3 = findViewById(R.id.scrollView3);

        TaiKhoan taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));

        setupClassifier();

        btnSpeak.setOnClickListener(view -> {
            if (!isTimerRunning) {
                startTimer();
                speak();
            } else {
                Toast.makeText(this, "Vui lòng chờ hết ghi âm!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(view -> {
            if (!recordedAnswer.isEmpty()) {
                analyzeAnswer(recordedAnswer);
            } else {
                Toast.makeText(this, "Vui lòng ghi âm câu trả lời trước khi nộp.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClassifier() {
        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_PATH)
                .build();

        TextClassifier.TextClassifierOptions options = TextClassifier.TextClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .build();

        textClassifier = TextClassifier.createFromOptions(this, options);
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Thời gian còn lại: " + millisUntilFinished / 1000 + " giây");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Hết thời gian!");
                isTimerRunning = false;
            }
        };
        isTimerRunning = true;
        countDownTimer.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            tvTimer.setText("Thời gian đã dừng.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                stopTimer();
                recordedAnswer = results.get(0);
                edtNoiDung.setText(recordedAnswer);
            }
        }
    }

    private void analyzeAnswer(String answer) {
        TextClassifierResult result = textClassifier.classify(answer);

        float positiveScore = 0f;
        float negativeScore = 0f;

        // Lấy danh sách kết quả phân loại từ model
        List<Classifications> classificationsList = result.classificationResult().classifications();
        for (Classifications classifications : classificationsList) {
            for (Category category : classifications.categories()) {
                if (category.categoryName().equalsIgnoreCase("positive")) {
                    positiveScore = category.score();
                } else if (category.categoryName().equalsIgnoreCase("negative")) {
                    negativeScore = category.score();
                }
            }
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(String.format(Locale.US, "positive: %.2f%%\nnegative: %.2f%%",
                positiveScore * 100, negativeScore * 100));

        tvPhanTich.setText(resultBuilder.toString());

        // Chuyển positiveScore sang điểm IELTS
        float ieltsScore = convertToIELTS(positiveScore);
        tvDiemSo.setText(String.format(Locale.US, "Điểm IELTS: %.1f", ieltsScore));
    }


    private float convertToIELTS(float positiveScore) {
        float percentage = positiveScore * 100;

        if (percentage >= 95) {
            return 9.0f;
        } else if (percentage >= 90) {
            return 8.5f;
        } else if (percentage >= 85) {
            return 8.0f;
        } else if (percentage >= 80) {
            return 7.5f;
        } else if (percentage >= 70) {
            return 7.0f;
        } else if (percentage >= 65) {
            return 6.5f;
        } else if (percentage >= 60) {
            return 6.0f;
        } else if (percentage >= 55) {
            return 5.5f;
        } else if (percentage >= 50) {
            return 5.0f;
        } else if (percentage >= 40) {
            return 4.5f;
        } else if (percentage >= 30) {
            return 4.0f;
        } else if (percentage >= 20) {
            return 3.5f;
        } else if (percentage >= 15) {
            return 3.0f;
        } else if (percentage >= 10) {
            return 2.5f;
        } else if (percentage >= 5) {
            return 2.0f;
        } else {
            return 1.0f;  // Rất thấp
        }
    }


}