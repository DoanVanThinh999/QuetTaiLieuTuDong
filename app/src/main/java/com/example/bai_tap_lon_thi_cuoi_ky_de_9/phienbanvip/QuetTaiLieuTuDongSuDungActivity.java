package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;

import static com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.EditActivity.bitmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.ChupAnhActivity;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.MainActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.EditActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.PdfExporter;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.PreviewActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.PublicVideoActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.PublicVideoListActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.ScanProcessor;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.ScannedDocViewerActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.TaoVideoHuongDanNguoiDungActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.TestActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.TracNghiemTuVideoActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class QuetTaiLieuTuDongSuDungActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private ImageView imageView, imgXemLSLuuAnh, imgLogout;
    private Bitmap inputBitmap;
    private TaiKhoan taiKhoan;
    private Button btnHDSD;
    private static final int REQUEST_EDIT_IMAGE = 123;
    private static final int REQUEST_PREVIEW_IMAGE = 321; // mã tùy chọn
    Bitmap originalBitmap;  // Biến để lưu ảnh gốc
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "❌ Không thể load OpenCV");
        } else {
            Log.d("OpenCV", "✅ OpenCV sẵn sàng!");
        }
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quet_tai_lieu_tu_dong_su_dung);
        imageView = findViewById(R.id.imageView);
        Button btnSelect = findViewById(R.id.btnSelect);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnEdit = findViewById(R.id.btnEdit);
        imgXemLSLuuAnh = findViewById(R.id.imgXemLSLuuAnh);
        btnHDSD = findViewById(R.id.btnHDSD);
        Intent intent = getIntent();
        taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        imgLogout = findViewById(R.id.imgLogout);
        imgLogout.setOnClickListener(view -> {
            Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, MainActivity.class);
            //Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, TestActivity.class);
            intent1.putExtra("taiKhoan", taiKhoan);
            startActivity(intent1); // Khởi chạy ChupAnhActivity
        });
        btnHDSD.setOnClickListener(view -> {
            Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, PublicVideoListActivity.class);
            //Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, TestActivity.class);
            intent1.putExtra("taiKhoan", taiKhoan);
            startActivity(intent1); // Khởi chạy ChupAnhActivity
        });


        imgXemLSLuuAnh.setOnClickListener(view -> {
            Intent intent1 = new Intent(QuetTaiLieuTuDongSuDungActivity.this, ScannedDocViewerActivity.class);
            intent1.putExtra("taiKhoan", taiKhoan);
            startActivity(intent1); // Khởi chạy ChupAnhActivity
        });

        btnSelect.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Chọn nguồn ảnh")
                    .setItems(new CharSequence[]{"Thư viện", "Máy ảnh"}, (dialog, which) -> {
                        if (which == 0) {
                            selectImage(); // chọn từ thư viện
                        } else {
                            captureImage(); // chụp ảnh
                        }
                    })
                    .show();
        });

        btnScan.setOnClickListener(v -> scanDocument());
        btnEdit.setOnClickListener(v -> openEditActivity());
    }
    private static final int REQUEST_CAPTURE = 999;

    private void captureImage() {
        Intent intent = new Intent(this, ChupAnhActivity.class);
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

    private void selectImage() {
        //Cho phép người dùng chọn ảnh từ thư viện
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI Đây là URI trỏ tới ảnh ngoài , như ảnh bạn chụp và lưu trong thư viện
        // . Nó sẽ mở giao diện chọn ảnh mặc định của hệ thống.
        startActivityForResult(intent, PICK_IMAGE);
        //Khởi chạy Intent đó, đồng thời gán mã PICK_IMAGE để biết đây là request chọn ảnh.
        // Khi người dùng chọn xong, kết quả sẽ trả về hàm onActivityResult() với mã tương ứng.
    }
    private void scanDocument() {
        if (inputBitmap == null) return;

        File file = new File(getExternalFilesDir(null), "originalBitmap.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            inputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Resize nếu quá lớn
        if (inputBitmap.getWidth() > 2000 || inputBitmap.getHeight() > 2000) {
            inputBitmap = Bitmap.createScaledBitmap(inputBitmap, inputBitmap.getWidth() / 3, inputBitmap.getHeight() / 3, true);
        }

        // 🧠 Thử detect lần đầu
        Bitmap cropped = ScanProcessor.detectAndCrop(this, inputBitmap);

        // ❌ Nếu fail → retry với lọc mạnh hơn
        if (cropped == null) {
            cropped = ScanProcessor.detectAndCrop(this, inputBitmap, true);
            if (cropped != null) {
                Toast.makeText(this, "Đã tự động lọc nhiễu và quét lại!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không phát hiện được tài liệu!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // ✅ Nếu quét thành công
        imageView.setImageBitmap(cropped);
        inputBitmap = cropped;

        PreviewActivity.previewBitmaps = new ArrayList<>(ScanProcessor.debugSteps);
        PreviewActivity.originalBitmap = inputBitmap;
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("taiKhoan", taiKhoan);
        intent.putExtra("originalBitmapUri", Uri.fromFile(file).toString());

        startActivityForResult(intent, REQUEST_PREVIEW_IMAGE);
    }



    private void openEditActivity() {
        if (inputBitmap == null) return;
        bitmap = inputBitmap;
        Intent editIntent = new Intent(this, EditActivity.class);
        startActivityForResult(editIntent, REQUEST_EDIT_IMAGE);  // Truyền mã yêu cầu
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Nếu người dùng chọn ảnh từ thư viện
            Uri selectedImage = data.getData();
            try {
                inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(inputBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Nếu ảnh được chụp từ camera
            Uri uri = Uri.parse(data.getStringExtra("imageUri"));
            try {
                inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(inputBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi đọc ảnh từ camera!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_EDIT_IMAGE && resultCode == RESULT_OK && data != null) {
            // Nhận Uri của ảnh đã chỉnh sửa từ EditActivity
            String editedImageUriString = data.getStringExtra("editedImageUri");
            Uri editedImageUri = Uri.parse(editedImageUriString);
            try {
                inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), editedImageUri);
                imageView.setImageBitmap(inputBitmap);  // Hiển thị ảnh đã chỉnh sửa
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_PREVIEW_IMAGE && resultCode == RESULT_OK && data != null) {
            // Lấy Uri từ kết quả trả về và cập nhật imageView
            String updatedImageUri = data.getStringExtra("updatedImageUri");
            if (updatedImageUri != null) {
                Uri uri = Uri.parse(updatedImageUri);
                try {
                    inputBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(inputBitmap);
                    Toast.makeText(this, "Ảnh đã được cập nhật từ Preview!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}