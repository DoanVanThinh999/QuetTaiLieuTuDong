package com;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Adapter.TaiKhoanAdapter;
import com.Database1.MySQLite;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class QuanLyTaiKhoanActivity extends AppCompatActivity {
    Button btnThemTaiKhoan, btnTimKiem, btnXemLichSu;
    EditText edtTimKiem;
    ListView lvTaiKhoan;
    TaiKhoanAdapter adapterTaiKhoan;
    ArrayList<TaiKhoan> listTaiKhoan;
    MySQLite mySQLite;
    String sql = "";
    // Firebase Database instance và tham chiếu
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference thongTinTaiKhoanRef; // Sửa: Khởi tạo sau khi databaseReference có giá trị
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_tai_khoan);
        btnThemTaiKhoan = findViewById(R.id.btnThemTaiKhoan);
        btnTimKiem = findViewById(R.id.btnTimKiem);
        edtTimKiem = findViewById(R.id.edtTimKiem);
        lvTaiKhoan = findViewById(R.id.lvTaiKhoan);
        btnXemLichSu = findViewById(R.id.btnXemLichSu);

        mySQLite = new MySQLite(QuanLyTaiKhoanActivity.this, 1);
        capNhatDanhSachTK();

        btnThemTaiKhoan.setOnClickListener(view -> themTaiKhoan());
        btnTimKiem.setOnClickListener(view -> timKiemTaiKhoan());
        btnXemLichSu.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyTaiKhoanActivity.this, LichSuActivity.class);
            startActivity(intent);
        });

        lvTaiKhoan.setOnItemLongClickListener((adapterView, view, i, l) -> {
            xoaTaiKhoan(i);
            return true;
        });

        lvTaiKhoan.setOnItemClickListener((adapterView, view, position, l) -> suaTaiKhoan(position));
        // Khởi tạo Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        thongTinTaiKhoanRef = databaseReference.child("thongTinTaiKhoan"); // Khởi tạo contactRef tại đây
        mySQLite = new MySQLite(QuanLyTaiKhoanActivity.this, 1);

        // Gọi hàm đồng bộ dữ liệu
        dongBoDuLieuFirebase();
    }
    private void dongBoDuLieuFirebase() {
        // Lấy dữ liệu từ SQLite
        String sql = "SELECT * FROM TAI_KHOAN";
        ArrayList<TaiKhoan> listTaiKhoan = mySQLite.docDuLieuTaiKhoan(sql);

        // Đẩy dữ liệu lên Firebase
        for (TaiKhoan taiKhoan : listTaiKhoan) {
            String key = thongTinTaiKhoanRef.push().getKey(); // Tạo một khóa Firebase duy nhất
            if (key != null) {
                thongTinTaiKhoanRef.child(key).setValue(taiKhoan).addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseSync", "Đồng bộ tài khoản thành công: " + taiKhoan.getTenDangNhap());
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseSync", "Lỗi đồng bộ tài khoản: " + taiKhoan.getTenDangNhap(), e);
                });
            }
        }
    }

    private void capNhatDanhSachTK() {
        sql = "SELECT * FROM TAI_KHOAN";
        listTaiKhoan = mySQLite.docDuLieuTaiKhoan(sql);
        adapterTaiKhoan = new TaiKhoanAdapter(QuanLyTaiKhoanActivity.this, R.layout.tai_khoan, listTaiKhoan);
        lvTaiKhoan.setAdapter(adapterTaiKhoan);
    }

    private void themTaiKhoan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(QuanLyTaiKhoanActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.them_thong_tin_tai_khoan, null);

        EditText tvTenDangNhap = dialogView.findViewById(R.id.tvTenDangNhap);
        EditText tvMK = dialogView.findViewById(R.id.tvMK);
        EditText edtHoTen = dialogView.findViewById(R.id.edtHoTen);
        EditText edtEMAIL = dialogView.findViewById(R.id.edtEMAIL);
        EditText edtSDT = dialogView.findViewById(R.id.edtSDT);
        EditText edtDiaDiem = dialogView.findViewById(R.id.edtDiaDiem);
        EditText edtTrinhDo = dialogView.findViewById(R.id.edtTrinhDo);
        EditText edtVT = dialogView.findViewById(R.id.edtVT);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ScrollView ScroolView1 = dialogView.findViewById(R.id.ScroolView1);


        builder.setView(dialogView);
        builder.setTitle("THÊM THÔNG TIN TÀI KHOẢN");

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String tenDangNhap = tvTenDangNhap.getText().toString().trim();
            String matKhau = tvMK.getText().toString().trim();
            String hoTen = edtHoTen.getText().toString().trim();
            String email = edtEMAIL.getText().toString().trim();
            String sdt = edtSDT.getText().toString().trim();
            String diaDiem = edtDiaDiem.getText().toString().trim();
            String trinhDo = edtTrinhDo.getText().toString().trim();
            String vaiTroStr = edtVT.getText().toString().trim();

            if (!tenDangNhap.isEmpty() && !matKhau.isEmpty() && !hoTen.isEmpty() && !email.isEmpty() && !sdt.isEmpty() && !diaDiem.isEmpty() && !trinhDo.isEmpty() && !vaiTroStr.isEmpty()) {
                int vaiTro;
                try {
                    vaiTro = Integer.parseInt(vaiTroStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(QuanLyTaiKhoanActivity.this, "Vai trò phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra tài khoản đã tồn tại
                String sqlCheck = "SELECT * FROM TAI_KHOAN WHERE ten_dang_nhap = '" + tenDangNhap + "';";
                ArrayList<TaiKhoan> existingTaiKhoan = mySQLite.docDuLieuTaiKhoan(sqlCheck);

                if (!existingTaiKhoan.isEmpty()) {
                    Toast.makeText(QuanLyTaiKhoanActivity.this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    // Thêm tài khoản mới
                    String sqlInsert = "INSERT INTO TAI_KHOAN (ten_dang_nhap, mat_khau, ho_ten, email, sdt, dia_diem, trinh_do, vai_tro, " +
                            "diem_nghe_tich_luy, diem_noi_tich_luy, diem_doc_tich_luy, diem_viet_tich_luy, diem_tb_tich_luy, " +
                            "diem_nghe_gan_day, diem_noi_gan_day, diem_doc_gan_day, diem_viet_gan_day, diem_tb_gan_day) " +
                            "VALUES ('" + tenDangNhap + "', '" + matKhau + "', '" + hoTen + "', '" + email + "', '" + sdt + "', '" + diaDiem + "', '" + trinhDo + "', " + vaiTro + ", " +
                            "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);";

                    mySQLite.querySQL(sqlInsert);
                    // Đồng bộ lên Firebase
                    String firebaseKey = thongTinTaiKhoanRef.push().getKey(); // Lấy khóa duy nhất từ Firebase
                    if (firebaseKey != null) {
                        // Tạo đối tượng TaiKhoan với id là firebaseKey
                        TaiKhoan taiKhoanMoi = new TaiKhoan(firebaseKey, tenDangNhap, matKhau, hoTen, email, sdt, diaDiem, trinhDo, vaiTro,
                                "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
                        thongTinTaiKhoanRef.child(firebaseKey).setValue(taiKhoanMoi)
                                .addOnSuccessListener(aVoid -> Toast.makeText(QuanLyTaiKhoanActivity.this, "Đã thêm tài khoản lên Firebase", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(QuanLyTaiKhoanActivity.this, "Lỗi đồng bộ Firebase", Toast.LENGTH_SHORT).show());
                    }
                    mySQLite.luuLichSu("Thêm tài khoản", "Đã thêm tài khoản: " + tenDangNhap);
                    capNhatDanhSachTK();
                    Toast.makeText(QuanLyTaiKhoanActivity.this, "Tài khoản mới đã được thêm", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuanLyTaiKhoanActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
        // Cho ScrollView cuộn lên đầu sau khi show
        ScroolView1.post(() -> ScroolView1.fullScroll(View.FOCUS_UP));
    }

    private void xoaTaiKhoan(int position) {
        TaiKhoan taiKhoan = listTaiKhoan.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(QuanLyTaiKhoanActivity.this);
        dialog.setTitle("Xóa tài khoản");
        dialog.setMessage("Bạn có chắc muốn xóa tài khoản: " + taiKhoan.getTenDangNhap() + " không?");

        dialog.setPositiveButton("Đồng ý", (dialogInterface, which) -> {
            String sqlDelete = "DELETE FROM TAI_KHOAN WHERE id = " + taiKhoan.getId() + ";";
            mySQLite.querySQL(sqlDelete);
            // Xóa tài khoản trên Firebase
            thongTinTaiKhoanRef.orderByChild("tenDangNhap").equalTo(taiKhoan.getTenDangNhap()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        data.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(QuanLyTaiKhoanActivity.this, "Đã xóa tài khoản trên Firebase", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(QuanLyTaiKhoanActivity.this, "Lỗi khi xóa tài khoản trên Firebase", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Lỗi xóa tài khoản trên Firebase", error.toException());
                }
            });
            mySQLite.luuLichSu("Xóa tài khoản", "Đã xóa tài khoản: " + taiKhoan.getTenDangNhap());
            capNhatDanhSachTK();
            Toast.makeText(QuanLyTaiKhoanActivity.this, "Đã xóa tài khoản: " + taiKhoan.getTenDangNhap(), Toast.LENGTH_SHORT).show();
        });

        dialog.setNegativeButton("Hủy", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.create().show();
    }

    private void suaTaiKhoan(int position) {
        TaiKhoan taiKhoan = listTaiKhoan.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(QuanLyTaiKhoanActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.sua_tai_khoan, null);

        EditText tvMaVaTenDN = dialogView.findViewById(R.id.tvMaVaTenDN);
        EditText edtMK = dialogView.findViewById(R.id.edtMK);
        EditText edtVaiTro = dialogView.findViewById(R.id.edtVaiTro);

        // Đặt giá trị hiện tại vào các trường nhập liệu
        tvMaVaTenDN.setText(taiKhoan.getTenDangNhap());
        edtMK.setText(taiKhoan.getMatKhau());

        // Kiểm tra và chuyển đổi giá trị vai trò thành chuỗi hiển thị
        String vaiTroText;
        switch (taiKhoan.getVaiTro()) {
            case 1:
                vaiTroText = "Quản trị viên";
                break;
            case 2:
                vaiTroText = "Giáo viên";
                break;
            case 3:
                vaiTroText = "Học sinh";
                break;
            case 4:
                vaiTroText = "Trợ giảng";
                break;
            case 5:
                vaiTroText = "Tổng đài";
                break;
            default:
                vaiTroText = "Khác";
                break;
        }
        edtVaiTro.setText(vaiTroText);

        builder.setView(dialogView);
        builder.setTitle("Sửa tài khoản");

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String tenDangNhap = tvMaVaTenDN.getText().toString().trim();
            String matKhau = edtMK.getText().toString().trim();
            String vaiTro = edtVaiTro.getText().toString().trim();

            if (!tenDangNhap.isEmpty() && !matKhau.isEmpty() && !vaiTro.isEmpty()) {
                // Kiểm tra nếu tên đăng nhập mới đã tồn tại
                String sqlCheck = "SELECT * FROM TAI_KHOAN WHERE ten_dang_nhap = '" + tenDangNhap + "' AND id != " + taiKhoan.getId() + ";";
                ArrayList<TaiKhoan> existingTaiKhoan = mySQLite.docDuLieuTaiKhoan(sqlCheck);

                if (!existingTaiKhoan.isEmpty()) {
                    Toast.makeText(QuanLyTaiKhoanActivity.this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    // Chuyển vai trò từ chuỗi về giá trị tương ứng
                    int vaiTroValue;
                    switch (vaiTro) {
                        case "Quản trị viên":
                            vaiTroValue = 1;
                            break;
                        case "Giáo viên":
                            vaiTroValue = 2;
                            break;
                        case "Học sinh":
                            vaiTroValue = 3;
                            break;
                        case "Trợ giảng":
                            vaiTroValue = 4;
                            break;
                        case "Tổng đài":
                            vaiTroValue = 5;
                            break;
                        default:
                            vaiTroValue = 0;
                            break;
                    }

                    String sqlUpdate = "UPDATE TAI_KHOAN SET ten_dang_nhap = '" + tenDangNhap + "', mat_khau = '" + matKhau + "', vai_tro = " + vaiTroValue + " WHERE id = " + taiKhoan.getId() + ";";
                    mySQLite.querySQL(sqlUpdate);
                    // Cập nhật trên Firebase
                    thongTinTaiKhoanRef.orderByChild("tenDangNhap").equalTo(taiKhoan.getTenDangNhap()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                data.getRef().child("matKhau").setValue(matKhau);
                                data.getRef().child("vaiTro").setValue(vaiTroValue);
                                Toast.makeText(QuanLyTaiKhoanActivity.this, "Đã cập nhật tài khoản trên Firebase", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FirebaseError", "Lỗi cập nhật Firebase", error.toException());
                        }
                    });
                    mySQLite.luuLichSu("Sửa tài khoản", "Đã sửa tài khoản: " + taiKhoan.getTenDangNhap() + " thành " + tenDangNhap
                    + "\n" + "Mật khẩu: "+ taiKhoan.getMatKhau() + "\n" + "Vai trò: " + taiKhoan.getVaiTro());
                    capNhatDanhSachTK();
                    Toast.makeText(QuanLyTaiKhoanActivity.this, "Tài khoản đã được cập nhật", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuanLyTaiKhoanActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void timKiemTaiKhoan() {
        String tkhoa = edtTimKiem.getText().toString().trim();
        if (!tkhoa.isEmpty()) {
            String sqlSearch = "SELECT * FROM TAI_KHOAN WHERE ten_dang_nhap LIKE '%" + tkhoa + "%';";
            listTaiKhoan = mySQLite.docDuLieuTaiKhoan(sqlSearch);
            adapterTaiKhoan = new TaiKhoanAdapter(QuanLyTaiKhoanActivity.this, R.layout.tai_khoan, listTaiKhoan);
            lvTaiKhoan.setAdapter(adapterTaiKhoan);
        } else {
            capNhatDanhSachTK();
        }
    }

}