package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.CauHoi;
import com.Model.TaiKhoan;

import java.util.ArrayList;

public class ChonLoaiCauHoiActivity extends AppCompatActivity {
    private long markerTime;
    private int totalQuestions, currentIndex;
    private ArrayList<CauHoi> danhSachCauHoi;
    String videoUriStr;
    private TaiKhoan taiKhoan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoUriStr = getIntent().getStringExtra("video_uri");
        markerTime = getIntent().getLongExtra("marker_time", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 1);
        currentIndex = getIntent().getIntExtra("current_index", 0);
        danhSachCauHoi = (ArrayList<CauHoi>) getIntent().getSerializableExtra("ds_cau_hoi");
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        showQuizTypeDialog();
    }
    private void showQuizTypeDialog() {
        String[] quizOptions = {
                "Chọn 1 đáp án", "Chọn nhiều đáp án", "Điền khuyết",
                "Sắp xếp thứ tự", "Nối cột A - B", "Đúng / Sai"
        };

        danhSachCauHoi = (ArrayList<CauHoi>) getIntent().getSerializableExtra("ds_cau_hoi");
        if (danhSachCauHoi == null) {
            danhSachCauHoi = new ArrayList<>();
        }

        // 👉 Đảm bảo danh sách đủ số lượng phần tử
        while (danhSachCauHoi.size() <= currentIndex) {
            danhSachCauHoi.add(new CauHoi());
        }

        new AlertDialog.Builder(this)
                .setTitle("📌 Câu " + (currentIndex + 1) + " / " + totalQuestions)
                .setItems(quizOptions, (dialog, which) -> {
                    QuizType selectedType = QuizType.values()[which];

                    // ✅ Không còn NullPointerException nữa
                    danhSachCauHoi.get(currentIndex).quizType = selectedType.name();

                    Intent intent = new Intent(this, NhapCauHoiActivity.class);
                    intent.putExtra("marker_time", markerTime);
                    intent.putExtra("quiz_type", selectedType.name());
                    intent.putExtra("total_questions", totalQuestions);
                    intent.putExtra("current_index", currentIndex);
                    intent.putExtra("edit_mode", false);
                    intent.putExtra("taiKhoan", taiKhoan);
                    intent.putExtra("ds_cau_hoi", danhSachCauHoi); // 🔥 QUAN TRỌNG
                    intent.putExtra("video_uri", videoUriStr); // 🔁 TRUYỀN TIẾP SANG NHẬP CÂU HỎI
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
