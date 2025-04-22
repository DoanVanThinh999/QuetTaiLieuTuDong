// PublicVideoActivity.java
package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PublicVideoActivity extends AppCompatActivity {
    private TextView tvSoLanXem, tvDaLam, tvPhuongThucDiem, tvDiemLuu;
    private Button btnBatDau;
    private static final int REQUEST_CODE_TRA_LOI_PUBLIC = 1010;

    private String videoUri;
    private TaiKhoan taiKhoan;
    private HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc;

    private String soLanXemStr, phuongThucDiem, xuLyKhiSai;
    private int phanTramDung;
    private String soLanTraLoiLai;
    private int truDiemMoiLan;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_video);

        tvSoLanXem = findViewById(R.id.tvSoLanXem);
        tvDaLam = findViewById(R.id.tvDaLam);
        tvPhuongThucDiem = findViewById(R.id.tvPhuongThucDiem);
        tvDiemLuu = findViewById(R.id.tvDiemLuu);
        btnBatDau = findViewById(R.id.btnBatDau);

        videoUri = getIntent().getStringExtra("video_uri");
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        cauHoiTheoMoc = (HashMap<Long, ArrayList<CauHoi>>) getIntent().getSerializableExtra("cau_hoi_theo_moc");
        soLanXemStr = getIntent().getStringExtra("so_lan_xem");
        phuongThucDiem = getIntent().getStringExtra("phuong_thuc_diem");
        xuLyKhiSai = getIntent().getStringExtra("xu_ly_khi_sai");
        phanTramDung = getIntent().getIntExtra("phan_tram_diem_dung", 0);
        soLanTraLoiLai = getIntent().getStringExtra("so_lan_tra_loi_lai");
        truDiemMoiLan = getIntent().getIntExtra("tru_diem_moi_lan", 0);

        DatabaseHelper db = new DatabaseHelper(this);
        int soLanDaLam = db.getSoLanXem(taiKhoan.getId(), videoUri);
        double diem = db.getDiemCaoNhat(taiKhoan.getId(), videoUri);

        tvSoLanXem.setText("Số lần xem cho phép: " + soLanXemStr);
        tvDaLam.setText("Số lần bạn đã làm: " + soLanDaLam);
        tvPhuongThucDiem.setText("Phương thức tính điểm: " + phuongThucDiem);
        tvDiemLuu.setText("Điểm đã được lưu: " + (diem == 0 ? "Không" : diem + "%"));

        btnBatDau.setOnClickListener(v -> {
            Intent intent = new Intent(PublicVideoActivity.this, TracNghiemTuVideoActivity.class);
            intent.putExtra("video_uri", videoUri);
            intent.putExtra("taiKhoan", taiKhoan);
            intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);
            intent.putExtra("so_lan_xem", soLanXemStr);
            intent.putExtra("phuong_thuc_diem", phuongThucDiem);
            intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
            intent.putExtra("phan_tram_diem_dung", phanTramDung);
            if (xuLyKhiSai.equals("Trả lời lại")) {
                intent.putExtra("so_lan_tra_loi_lai", soLanTraLoiLai);
                intent.putExtra("tru_diem_moi_lan", truDiemMoiLan);
            }
            startActivityForResult(intent, REQUEST_CODE_TRA_LOI_PUBLIC);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TRA_LOI_PUBLIC && resultCode == RESULT_OK) {
            Toast.makeText(this, "✅ Bạn đã hoàn thành video!", Toast.LENGTH_SHORT).show();
        }
    }
}