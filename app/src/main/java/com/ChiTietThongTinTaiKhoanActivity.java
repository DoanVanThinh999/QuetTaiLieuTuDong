package com;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Database1.MySQLite;
import com.Model.TaiKhoan;
import com.bumptech.glide.Glide;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChiTietThongTinTaiKhoanActivity extends AppCompatActivity {
    ImageView imgThoat;
    TextView tvTenMH, tvGiaNhap, tvGiaBan, tvTongBan, tvTongNhap, tvHangTrongKho, tvSoLuongTrong, tvSoLuongTrungBay, tvSoLuongGiaoDich,
            tvSoLuongGiaoDich1, tvSoLuongGiaoDich2, tvSoLuongGiaoDich3,tvSoLuongGiaoDich4, tvSoLuongGiaoDich5, tvSoLuongGiaoDich6,
            tvSoLuongGiaoDich7, tvSoLuongGiaoDich8, tvSoLuongGiaoDich9;
    // Firebase Database instance và tham chiếu
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference thongTinTaiKhoanRef; // Sửa: Khởi tạo sau khi databaseReference có giá trị
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_thong_tin_tai_khoan);
        // Ánh xạ các thành phần giao diện
        tvTenMH = findViewById(R.id.tvTenMH);
        tvGiaNhap = findViewById(R.id.tvGiaNhap);
        imgThoat = findViewById(R.id.imgThoat);
        tvGiaBan = findViewById(R.id.tvGiaBan);
        tvTongBan = findViewById(R.id.tvTongBan);
        tvTongNhap = findViewById(R.id.tvTongNhap);
        tvHangTrongKho = findViewById(R.id.tvHangTrongKho);
        tvSoLuongTrong = findViewById(R.id.tvSoLuongTrong);
        tvSoLuongTrungBay = findViewById(R.id.tvSoLuongTrungBay);
        tvSoLuongGiaoDich = findViewById(R.id.tvSoLuongGiaoDich);
        tvSoLuongGiaoDich1 = findViewById(R.id.tvSoLuongGiaoDich1);
        tvSoLuongGiaoDich2 = findViewById(R.id.tvSoLuongGiaoDich2);
        tvSoLuongGiaoDich3 = findViewById(R.id.tvSoLuongGiaoDich3);
        tvSoLuongGiaoDich4 = findViewById(R.id.tvSoLuongGiaoDich4);
        tvSoLuongGiaoDich5 = findViewById(R.id.tvSoLuongGiaoDich5);
        tvSoLuongGiaoDich6 = findViewById(R.id.tvSoLuongGiaoDich6);
        tvSoLuongGiaoDich7 = findViewById(R.id.tvSoLuongGiaoDich7);
        tvSoLuongGiaoDich8 = findViewById(R.id.tvSoLuongGiaoDich8);
        tvSoLuongGiaoDich9 = findViewById(R.id.tvSoLuongGiaoDich9);
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("tK");
        imgThoat.setOnClickListener(view -> {
            finish();
        });
        // Hiển thị thông tin mặt hàng
        if (taiKhoan != null) {
            tvTenMH.setText("Tên đăng nhập: " + taiKhoan.getTenDangNhap());
            tvGiaNhap.setText("Mật khẩu: " + taiKhoan.getMatKhau());
            tvGiaBan.setText("Họ tên: " + taiKhoan.getHoTen());
            tvTongBan.setText("Email: " + taiKhoan.getEmail());
            tvTongNhap.setText("Số điện thoại: " + taiKhoan.getSdt());
            tvHangTrongKho.setText("Địa điểm: " + taiKhoan.getDiaDiem());
            tvSoLuongTrong.setText("Trình độ: " + taiKhoan.getTrinhDo());
            tvSoLuongTrungBay.setText("Vai trò: " + taiKhoan.getVaiTro());
            tvSoLuongGiaoDich.setText("Điểm nghe tích lũy: " + taiKhoan.getDiemNgheTichLuy());
            tvSoLuongGiaoDich1.setText("Điểm nói tích lũy: " + taiKhoan.getDiemNoiTichLuy());
            tvSoLuongGiaoDich2.setText("Điểm đọc tích lũy: " + taiKhoan.getDiemDocTichLuy());
            tvSoLuongGiaoDich3.setText("Điểm viết tích lũy: " + taiKhoan.getDiemVietTichLuy());
            tvSoLuongGiaoDich4.setText("Điểm trung bình tích lũy: " + taiKhoan.getDiemTbTichLuy());
            tvSoLuongGiaoDich5.setText("Điểm nghe gần đây: " + taiKhoan.getDiemNgheGanDay());
            tvSoLuongGiaoDich6.setText("Điểm nói gần đây: " + taiKhoan.getDiemNoiGanDay());
            tvSoLuongGiaoDich7.setText("Điểm đọc gần đây: " + taiKhoan.getDiemDocGanDay());
            tvSoLuongGiaoDich8.setText("Điểm viết gần đây: " + taiKhoan.getDiemVietGanDay());
            tvSoLuongGiaoDich9.setText("Điểm trung bình gần đây: " + taiKhoan.getDiemTbGanDay());
        }
        // Khởi tạo Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        thongTinTaiKhoanRef = databaseReference.child("thongTinTaiKhoan"); // Khởi tạo contactRef tại đây
        // Nhận dữ liệu từ Intent
        Intent intent1 = getIntent();
        String tenDangNhap = intent1.getStringExtra("tenDangNhap");
        docThongTinTaiKhoan(tenDangNhap);
    }
    // Hàm đọc thông tin tài khoản từ Firebase
    private void docThongTinTaiKhoan(String tenDangNhap) {
        thongTinTaiKhoanRef.orderByChild("tenDangNhap").equalTo(tenDangNhap)
                .addValueEventListener(new ValueEventListener() { // Thay đổi từ addListenerForSingleValueEvent
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                TaiKhoan taiKhoan = data.getValue(TaiKhoan.class);
                                if (taiKhoan != null) {
                                    Log.d("FirebaseData", "Dữ liệu cập nhật: " + taiKhoan.getDiemVietTichLuy()); // Debug log
                                    hienThiThongTinTaiKhoan(taiKhoan);
                                }
                            }
                        } else {
                            Log.d("FirebaseData", "Không tìm thấy tài khoản với tên đăng nhập: " + tenDangNhap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Lỗi đọc dữ liệu tài khoản", error.toException());
                    }
                });
    }
    private void hienThiThongTinTaiKhoan(TaiKhoan taiKhoan) {
        tvTenMH.setText("Tên đăng nhập: " + taiKhoan.getTenDangNhap());
        tvGiaNhap.setText("Mật khẩu: " + taiKhoan.getMatKhau());
        tvGiaBan.setText("Họ tên: " + taiKhoan.getHoTen());
        tvTongBan.setText("Email: " + taiKhoan.getEmail());
        tvTongNhap.setText("Số điện thoại: " + taiKhoan.getSdt());
        tvHangTrongKho.setText("Địa điểm: " + taiKhoan.getDiaDiem());
        tvSoLuongTrong.setText("Trình độ: " + taiKhoan.getTrinhDo());
        tvSoLuongTrungBay.setText("Vai trò: " + taiKhoan.getVaiTro());
        tvSoLuongGiaoDich.setText("Điểm nghe tích lũy: " + taiKhoan.getDiemNgheTichLuy());
        tvSoLuongGiaoDich1.setText("Điểm nói tích lũy: " + taiKhoan.getDiemNoiTichLuy());
        tvSoLuongGiaoDich2.setText("Điểm đọc tích lũy: " + taiKhoan.getDiemDocTichLuy());
        tvSoLuongGiaoDich3.setText("Điểm viết tích lũy: " + taiKhoan.getDiemVietTichLuy());
        tvSoLuongGiaoDich4.setText("Điểm trung bình tích lũy: " + taiKhoan.getDiemTbTichLuy());
        tvSoLuongGiaoDich5.setText("Điểm nghe gần đây: " + taiKhoan.getDiemNgheGanDay());
        tvSoLuongGiaoDich6.setText("Điểm nói gần đây: " + taiKhoan.getDiemNoiGanDay());
        tvSoLuongGiaoDich7.setText("Điểm đọc gần đây: " + taiKhoan.getDiemDocGanDay());
        tvSoLuongGiaoDich8.setText("Điểm viết gần đây: " + taiKhoan.getDiemVietGanDay());
        tvSoLuongGiaoDich9.setText("Điểm trung bình gần đây: " + taiKhoan.getDiemTbGanDay());
    }
}