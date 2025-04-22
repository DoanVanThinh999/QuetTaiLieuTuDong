package com.example.bai_tap_lon_thi_cuoi_ky_de_9;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.MySQLite;
import com.Model.TaiKhoan;
import com.QuanLyTaiKhoanActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DangKyActivity extends AppCompatActivity {
    EditText tvTenDangNhap, tvMK,edtHoTen, edtEMAIL,edtSDT, edtDiaDiem, edtTrinhDo;
    Button btnDK, btnThoat;
    private FirebaseAuth mAuth;
    ImageView btnTogglePassword;
    boolean isPasswordVisible = false;
    MySQLite db;
    // Firebase Database instance và tham chiếu
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference thongTinTaiKhoanRef;
    ScrollView scrollView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        mAuth = FirebaseAuth.getInstance();
        db = new MySQLite(this, 1);
        // Khởi tạo MySQLite

        // Khởi tạo các View
        btnDK = findViewById(R.id.btnDK);
        btnThoat = findViewById(R.id.btnThoat);
        tvTenDangNhap = findViewById(R.id.tvTenDangNhap);
        tvMK = findViewById(R.id.tvMK);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtEMAIL = findViewById(R.id.edtEMAIL);
        edtSDT = findViewById(R.id.edtSDT);
        edtDiaDiem = findViewById(R.id.edtDiaDiem);
        edtTrinhDo = findViewById(R.id.edtTrinhDo);
        scrollView2 = findViewById(R.id.scrollView2);
        scrollView2.post(() -> scrollView2.fullScroll(ScrollView.FOCUS_DOWN));
        scrollView2.post(() -> scrollView2.fullScroll(View.FOCUS_UP));
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    tvMK.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                } else {
                    // Hiển thị mật khẩu
                    tvMK.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                }
                isPasswordVisible = !isPasswordVisible;
                // Di chuyển con trỏ về cuối chuỗi
                tvMK.setSelection(tvMK.getText().length());
            }
        });
        // Xử lý khi nhấn nút Đăng ký
        btnDK.setOnClickListener(view -> {
            themTaiKhoan();
            // registerUser();
        });

        // Xử lý khi nhấn nút Thoát
        btnThoat.setOnClickListener(view -> {
            finish();
        });

        // Khởi tạo Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        thongTinTaiKhoanRef = databaseReference.child("thongTinTaiKhoan"); // Khởi tạo contactRef tại đây
    }

    private void registerUser() {
        String email = tvTenDangNhap.getText().toString().trim();
        String password = tvMK.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DangKyActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                       // Intent intent = new Intent(DangKyActivity.this, MainActivity.class);
                       // intent.putExtra("ten_dang_nhap", tenDangNhap);
                       // startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(DangKyActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void themTaiKhoan() {
        String tenDangNhap = tvTenDangNhap.getText().toString().trim();
        String matKhau = tvMK.getText().toString().trim();
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEMAIL.getText().toString().trim();
        String soDienThoai = edtSDT.getText().toString().trim();
        String diaDiem = edtDiaDiem.getText().toString().trim();
        String trinhDo = edtTrinhDo.getText().toString().trim();

        // Kiểm tra điều kiện mật khẩu
        if (!isValidPassword(matKhau)) {
            Toast.makeText(DangKyActivity.this, "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ cái, số, ký tự đặc biệt và ít nhất một chữ in hoa.", Toast.LENGTH_LONG).show();
            return;
        }
        //Kiểm tra điều kiện số điện thoại
        if(!isValidPhone(soDienThoai)){
            Toast.makeText(DangKyActivity.this, "Số điện thoại có 10 ký tự và phải gồm các ký tự số với số 0 đứng đầu.", Toast.LENGTH_LONG).show();
            return;
        }
        if(!isValidTrinhDo(trinhDo)){
            Toast.makeText(DangKyActivity.this, "Trình độ chỉ có A1, A2, B1, B2, C1, C2", Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra xem tên đăng nhập đã tồn tại trong cơ sở dữ liệu chưa
        if (db.kiemTraDN(tenDangNhap)) {
            Toast.makeText(DangKyActivity.this, "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!tenDangNhap.isEmpty() && !matKhau.isEmpty() && !hoTen.isEmpty() && !email.isEmpty() && !soDienThoai.isEmpty()
        && !diaDiem.isEmpty() && !trinhDo.isEmpty()) {
            int vaiTro = 3;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận Đăng ký");
            builder.setMessage("Em có chắc chắn với đăng ký của mình chưa? Hãy đưa ra thông tin thật chính xác để chúng tôi có phương pháp dạy học cho em được tốt nhất.");

            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                // Thêm tài khoản vào cơ sở dữ liệu
                db.themTaiKhoanM(tenDangNhap, matKhau, hoTen, email, soDienThoai, diaDiem, trinhDo,vaiTro);

                // Đăng ký thành công, chuyển sang MainActivity
                Intent intent = new Intent(DangKyActivity.this, MainActivity.class);
                intent.putExtra("ten_dang_nhap", tenDangNhap);
                startActivity(intent);
                // Đồng bộ lên Firebase
                String firebaseKey = thongTinTaiKhoanRef.push().getKey(); // Lấy khóa duy nhất từ Firebase
                if (firebaseKey != null) {
                    // Tạo đối tượng TaiKhoan với id là firebaseKey
                    TaiKhoan taiKhoanMoi = new TaiKhoan(firebaseKey, tenDangNhap, matKhau, hoTen, email, soDienThoai, diaDiem, trinhDo, vaiTro,
                            "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
                    thongTinTaiKhoanRef.child(firebaseKey).setValue(taiKhoanMoi)
                            .addOnSuccessListener(aVoid -> Toast.makeText(DangKyActivity.this, "Đã thêm tài khoản lên Firebase", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(DangKyActivity.this, "Lỗi đồng bộ Firebase", Toast.LENGTH_SHORT).show());
                }
                finish();
                Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

            });

            builder.setNegativeButton("Hủy", (dialog, which) -> {
                // Hủy bỏ, đóng hộp thoại
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(DangKyActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isValidTrinhDo(String trinhDo) {
        // Kiểm tra nếu không thuộc các giá trị hợp lệ
        if (!trinhDo.equals("A1") &&
                !trinhDo.equals("A2") &&
                !trinhDo.equals("B1") &&
                !trinhDo.equals("B2") &&
                !trinhDo.equals("C1") &&
                !trinhDo.equals("C2")) {
            return false;
        }
        return true;
    }

    private boolean isValidPhone(String numberPhone) {
        // Kiểm tra độ dài của số điện thoại phải là 10
        if (numberPhone.length() != 10) {
            return false;
        }
        if (numberPhone.charAt(0) != '0') {
            return false;
        }
        for (char c : numberPhone.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }
    // Kiểm tra mật khẩu hợp lệ
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (isSpecialCharacter(c)) hasSpecialChar = true;
        }

        return hasUpperCase && hasDigit && hasSpecialChar;
    }

    // Kiểm tra ký tự đặc biệt trong mật khẩu
    private boolean isSpecialCharacter(char c) {
        String specialChars = "!@#$%^&*(),.?\":{}|<>"; // Danh sách ký tự đặc biệt
        return specialChars.indexOf(c) != -1;
    }
}