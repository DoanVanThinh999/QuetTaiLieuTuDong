package com;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SuaLienHeActivity extends AppCompatActivity {

    ImageView imgAnhDaiDien;
    TextView tvTieuDe;
    EditText edtTenLienHe, edtSoDienThoai, edtEmail;
    Button btnChonAnh, btnSuaLienHe;
    Uri selectedImageUri; // Lưu URI của ảnh đại diện đã chọn
    String id; // ID của liên hệ cần sửa

    // Firebase Realtime Database
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference contactRef = firebaseDatabase.getReference().child("contacts");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_lien_he);

        // Liên kết giao diện
        imgAnhDaiDien = findViewById(R.id.imgAnhDaiDien);
        edtTenLienHe = findViewById(R.id.edtTenLienHe);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        btnSuaLienHe = findViewById(R.id.btnSuaLienHe);
        tvTieuDe = findViewById(R.id.tvTieuDe);

        // Lấy ID từ Intent
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        if (id != null && !id.isEmpty()) {
            loadContactData(id);
        } else {
            Toast.makeText(this, "Không tìm thấy liên hệ!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý chọn ảnh
        ActivityResultLauncher<String> chonAnhLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imgAnhDaiDien.setImageURI(uri);
                        }
                    }
                });

        btnChonAnh.setOnClickListener(view -> chonAnhLauncher.launch("image/*"));

        // Xử lý sự kiện nút sửa
        btnSuaLienHe.setOnClickListener(view -> updateContact());
    }

    private void loadContactData(String contactId) {
        contactRef.child(contactId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LienHe lienHe = snapshot.getValue(LienHe.class);
                if (lienHe != null) {
                    tvTieuDe.setText("Sửa liên hệ\n" + lienHe.getTenLienHe());
                    edtTenLienHe.setText(lienHe.getTenLienHe());
                    edtSoDienThoai.setText(lienHe.getSoDienThoai());
                    edtEmail.setText(lienHe.getEmail());

                    if (lienHe.getAnhDaiDien() != null && !lienHe.getAnhDaiDien().trim().isEmpty()) {
                        Glide.with(SuaLienHeActivity.this).load(lienHe.getAnhDaiDien()).into(imgAnhDaiDien);
                    }
                } else {
                    Toast.makeText(SuaLienHeActivity.this, "Liên hệ không tồn tại!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SuaLienHeActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateContact() {
        // Lấy thông tin từ các EditText
        String tenLienHe = edtTenLienHe.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String anhDaiDien = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        // Kiểm tra dữ liệu đầu vào
        if (tenLienHe.isEmpty() || soDienThoai.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật đối tượng LienHe
        LienHe updatedLienHe = new LienHe(id, tenLienHe, soDienThoai, email, anhDaiDien);

        // Lưu thay đổi lên Firebase
        contactRef.child(id).setValue(updatedLienHe).addOnSuccessListener(unused -> {
            Toast.makeText(SuaLienHeActivity.this, "Sửa liên hệ thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(SuaLienHeActivity.this, "Sửa liên hệ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}