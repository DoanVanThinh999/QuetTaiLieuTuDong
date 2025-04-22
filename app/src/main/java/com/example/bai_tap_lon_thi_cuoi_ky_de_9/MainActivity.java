package com.example.bai_tap_lon_thi_cuoi_ky_de_9;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ChupAnhActivity;
import com.DanhBaActivity;
import com.Database1.MySQLite;
import com.Model.TaiKhoan;
import com.QuanLyTaiKhoanActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.MainTiengAnhActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.VocabNotifier;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LichSuTest.LichSuTest;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.QuetTaiLieuTuDongSuDungActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu.TraTu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText edtTenDangNhap, edtMatKhau;
    Button btnDangNhap, btnDangNhapGoogle;
    ImageView   btnTogglePassword;
    LinearLayout  layout_forgot_password;
    TextView tvDangKy;
    ScrollView scrollView1;
    private FirebaseAuth mAuth;
    MySQLite mySQLite;
    boolean isPasswordVisible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtTenDangNhap = findViewById(R.id.edtTenDangNhap);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        // Ánh xạ Spinner

        layout_forgot_password = findViewById(R.id.layout_forgot_password);
        btnDangNhapGoogle = findViewById(R.id.btnDangNhapGoogle);
        tvDangKy = findViewById(R.id.tvDangKy);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        scrollView1 = findViewById(R.id.scrollView1);
        scrollView1.post(() -> scrollView1.fullScroll(ScrollView.FOCUS_DOWN));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    edtMatKhau.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                } else {
                    // Hiển thị mật khẩu
                    edtMatKhau.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                }
                isPasswordVisible = !isPasswordVisible;
                // Di chuyển con trỏ về cuối chuỗi
                edtMatKhau.setSelection(edtMatKhau.getText().length());
            }
        });
        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra trạng thái đăng nhập
        checkUserLoggedIn();

        tvDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentXemTaiLie = new Intent(MainActivity.this, DangKyActivity.class);
                startActivity(intentXemTaiLie);
            }
        });
        layout_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onClickForgotPassword();
                resetPassword();
            }
        });


        // Khởi tạo SQLite
        mySQLite = new MySQLite(this, 1);


        // Xử lý sự kiện Đăng nhập
        handleLoginEvent();

        btnDangNhapGoogle.setOnClickListener(view -> {
            loginUser();
        });
        scheduleReminder();
    }
    private void scheduleReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, VocabNotifier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        long triggerTime = System.currentTimeMillis() + (60 * 1000); // 1 phút sau
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelReminder(); // Nếu người dùng mở ứng dụng, hủy báo thức
    }

    private void cancelReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, VocabNotifier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private void checkUserLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Người dùng đã đăng nhập, chuyển đến MainActivity
            startActivity(new Intent(MainActivity.this, MainTiengAnhActivity.class));
            finish(); // Đóng AuthActivity
        }
    }

    private void resetPassword() {
        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        // Inflate layout dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);

        // Lấy các thành phần trong dialog
        EditText emailEditText = view.findViewById(R.id.emailEditText);
        Button resetPasswordButton = view.findViewById(R.id.resetPasswordButton);

        // Tạo dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý sự kiện khi nhấn nút Reset Password
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Reset password email sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    private void handleLoginEvent() {
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtTenDangNhap.getText().toString().trim();
                String password = edtMatKhau.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    TaiKhoan taiKhoan = mySQLite.kiemTraDangNhap(email, password);
                    if (taiKhoan != null && taiKhoan.getId() > 0) {
                        navigateToRoleBasedActivity(taiKhoan);
                    } else {
                        Toast.makeText(MainActivity.this, "Tài khoản hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                    }
                    //String email = emailEditText.getText().toString().trim();
                    //String password = passwordEditText.getText().toString().trim();
                  //loginUser();


                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void navigateToRoleBasedActivity(TaiKhoan taiKhoan) {
        Intent intent;
        if (taiKhoan.getVaiTro() == 1) { // Vai trò quản trị
            intent = new Intent(MainActivity.this, QuanLyTaiKhoanActivity.class);
        } else if (taiKhoan.getVaiTro() == 2) { // Vai trò khách hàng
            //intent = new Intent(MainActivity.this, MainTiengAnhActivity.class);
            intent = new Intent(MainActivity.this, MainTiengAnhActivity.class);
        }
        else if(taiKhoan.getVaiTro() == 3 || taiKhoan.getVaiTro() == 1){
            intent = new Intent(MainActivity.this, QuetTaiLieuTuDongSuDungActivity.class);
        }
        else if(taiKhoan.getVaiTro() == 4 || taiKhoan.getVaiTro() == 1){
            intent = new Intent(MainActivity.this, TraTu.class);
        }
        else if(taiKhoan.getVaiTro() == 5 || taiKhoan.getVaiTro() == 1){
            intent = new Intent(MainActivity.this, DanhBaActivity.class);
        }
        else {
            Toast.makeText(this, "Vai trò không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("taiKhoan", taiKhoan);
        startActivity(intent);
    }

    private void loginUser() {
        String email = edtTenDangNhap.getText().toString().trim();
        String password = edtMatKhau.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            //mySQLite.luuLichSu("Xóa tài khoản", "Đã xóa tài khoản: " + taiKhoan.getTenDangNhap());
                            // Chuyển đến màn hình chính
                            startActivity(new Intent(MainActivity.this, MainTiengAnhActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}