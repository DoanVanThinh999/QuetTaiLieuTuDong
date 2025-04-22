package com;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class ThemLienHeActivity extends AppCompatActivity {
    ImageView imgAnhDaiDien;
    EditText edtTenLienHe, edtSoDienThoai, edtEmail;
    Button btnChonAnh, btnThemLienHe;
    Uri selectedImageUri; // Lưu URI của ảnh đại diện đã chọn

    // Firebase Realtime Database
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference contactRef = firebaseDatabase.getReference().child("contacts");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_lien_he);

        // Liên kết giao diện
        imgAnhDaiDien = findViewById(R.id.imgAnhDaiDien);
        edtTenLienHe = findViewById(R.id.edtTenLienHe);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        btnThemLienHe = findViewById(R.id.btnThemLienHe);

        // Launcher để chọn ảnh đại diện
        ActivityResultLauncher<String> chonAnhLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            selectedImageUri = result;
                            imgAnhDaiDien.setImageURI(result);
                        }
                    }
                });

        // Chọn ảnh đại diện
        btnChonAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chonAnhLauncher.launch("image/*");
            }
        });

        // Thêm liên hệ
        btnThemLienHe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewContact();
            }
        });
    }



    private void addNewContact() {
        // Lấy thông tin từ các EditText
        String tenLienHe = edtTenLienHe.getText().toString();
        String soDienThoai = edtSoDienThoai.getText().toString();
        String email = edtEmail.getText().toString();
        String anhDaiDien = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        // Kiểm tra dữ liệu đầu vào
        if (tenLienHe.isEmpty() || soDienThoai.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng LienHe
        String id = UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên
        LienHe lienHe = new LienHe(id, tenLienHe, soDienThoai, email, anhDaiDien);

        // Lưu liên hệ vào Firebase
        contactRef.child(id).setValue(lienHe).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Thêm liên hệ thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity sau khi thêm thành công
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Thêm liên hệ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}