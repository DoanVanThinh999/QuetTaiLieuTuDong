package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.QuetTaiLieuTuDongSuDungActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class ManHinhKetThucActivity extends AppCompatActivity {
    TextView tvKetQua, tvThongTinCauHinh;
    Button btnQuayLai, btnThoat;
    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_hinh_ket_thuc);
        tvThongTinCauHinh = findViewById(R.id.tvThongTinCauHinh);
        tvKetQua = findViewById(R.id.tvKetQua);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        btnThoat = findViewById(R.id.btnThoat);

        Intent intent = getIntent();
        int tongCau = intent.getIntExtra("tong_cau", 0);
        double soCauDung = intent.getDoubleExtra("so_cau_dung", 0);
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("tai_khoan");
        String videoUri = intent.getStringExtra("video_uri");
        String soLanXemStr = intent.getStringExtra("so_lan_xem");
        // Chuyển "2147483647" thành "Không giới hạn" nếu là MAX_VALUE
        String hienSoLanXem = soLanXemStr.equals(String.valueOf(Integer.MAX_VALUE)) ? "Không giới hạn" : soLanXemStr;
        String phuongThucDiem = intent.getStringExtra("phuong_thuc_diem");
        int phanTramDung = intent.getIntExtra("phan_tram_diem_dung", 0);

        DatabaseHelper db = new DatabaseHelper(this);
        int soLanDaXem = db.getSoLanXem(taiKhoan.getId(), videoUri);

        double diem = 0;


// ✅ Tạo bảng điểm lưu theo người dùng và video URI nếu chưa có
        if (phuongThucDiem.equals("Lần đầu tiên")) {
            diem = db.getDiemLanXem(taiKhoan.getId(), videoUri, 1); // điểm lần đầu
        } else if (phuongThucDiem.equals("Điểm cao nhất")) {
            diem = db.getDiemCaoNhat(taiKhoan.getId(), videoUri);
        } else if (phuongThucDiem.equals("Trung bình các lần")) {
            diem = db.getDiemTrungBinh(taiKhoan.getId(), videoUri);
        }


        tvKetQua.setText(
                "✅ Tài khoản: " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Ẩn") +
                        "\n📊 Tổng số câu: " + tongCau +
                        "\n✅ Trả lời đúng: " + soCauDung +
                        "\n🏆 Điểm: " + String.format("%.1f", diem) + "%"
        );
        tvThongTinCauHinh.setText(
                "📅 Số lần xem cho phép: " + hienSoLanXem +
                        "\n👁️ Số lần bạn đã xem: " + soLanDaXem +
                        "\n📐 Phương thức tính điểm: " + phuongThucDiem +
                        "\n🧮 Cho phép đúng 1 phần mỗi câu: " + phanTramDung + "%"
        );



        //btnQuayLai.setOnClickListener(v -> finish());
        btnThoat.setOnClickListener(v -> {
            Intent intent1 = new Intent(ManHinhKetThucActivity.this, QuetTaiLieuTuDongSuDungActivity.class);
            //Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, TestActivity.class);
            intent1.putExtra("taiKhoan", taiKhoan);
            startActivity(intent1); // Khởi chạy ChupAnhActivity
        });
        btnQuayLai.setOnClickListener(v -> {
            // ✅ CHẶN nếu vượt quá số lần xem
            boolean duocPhepXem = true;
            if (!soLanXemStr.equals("Không giới hạn")) {
                int soLanXemToiDa = Integer.parseInt(soLanXemStr);
                if (soLanDaXem >= soLanXemToiDa) {
                    Toast.makeText(this, "❌ Bạn đã vượt quá số lần xem được phép!", Toast.LENGTH_LONG).show();
                    duocPhepXem = false;
                }
            }

            if (!duocPhepXem) return;

            // ✅ Nếu được phép xem lại → khởi động lại
            Intent intent9 = new Intent(this, TracNghiemTuVideoActivity.class);
            intent9.putExtra("video_uri", videoUri);
            intent9.putExtra("taiKhoan", taiKhoan);

            // Lấy lại câu hỏi từ database
            HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUri);
            intent9.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);

            // 🔁 TRUYỀN LẠI TOÀN BỘ CẤU HÌNH GỐC:
            intent9.putExtra("so_lan_xem", soLanXemStr);
            intent9.putExtra("phuong_thuc_diem", phuongThucDiem);
            intent9.putExtra("xu_ly_khi_sai", intent.getStringExtra("xu_ly_khi_sai"));
            intent9.putExtra("phan_tram_diem_dung", phanTramDung);

            if ("Trả lời lại".equals(intent.getStringExtra("xu_ly_khi_sai"))) {
                intent9.putExtra("so_lan_tra_loi_lai", intent.getStringExtra("so_lan_tra_loi_lai"));
                intent9.putExtra("tru_diem_moi_lan", intent.getIntExtra("tru_diem_moi_lan", 0));
            }

            startActivity(intent9);
            finish();
        });



    }
}
