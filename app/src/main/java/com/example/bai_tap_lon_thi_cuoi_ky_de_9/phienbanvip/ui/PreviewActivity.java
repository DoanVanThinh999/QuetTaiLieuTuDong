package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import static com.example.bai_tap_lon_thi_cuoi_ky_de_9.BR.progress;
import static com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.EditActivity.bitmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DebugImageDatabase;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreviewActivity extends AppCompatActivity {
    public static Bitmap originalBitmap; // Biến để lưu ảnh gốc
    public static List<Bitmap> previewBitmaps = new ArrayList<>();
    private TaiKhoan taiKhoan;
    private Button btnSaveDebug, btnRestart;
    private ProgressBar progressBar;
    private TextView txtProgressPercent;
    ImageView imageView;  // Thêm ImageView để hiển thị ảnh
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        LinearLayout layout = findViewById(R.id.previewLayout);
        btnSaveDebug = findViewById(R.id.btnSaveDebug);
        // Ánh xạ ImageView từ layout
        imageView = findViewById(R.id.imageView);  // Đảm bảo rằng imageView đã được khởi tạo đúng
        progressBar = findViewById(R.id.progressBarSave);
        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        btnRestart = findViewById(R.id.btnRestart);
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        // Nhận ảnh gốc từ Intent
        // Lấy URI của ảnh gốc từ Intent
        // Lấy URI của ảnh gốc từ Intent
        String originalBitmapUriString = getIntent().getStringExtra("originalBitmapUri");
        if (originalBitmapUriString != null) {
            Uri originalBitmapUri = Uri.parse(originalBitmapUriString);
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), originalBitmapUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btnRestart.setOnClickListener(v -> {
            Intent resultIntent = new Intent();

            if (originalBitmap != null) {
                File imageFile = new File(getCacheDir(), "restarted_image.png");
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Uri imageUri = Uri.fromFile(imageFile);
                    resultIntent.putExtra("updatedImageUri", imageUri.toString());
                    setResult(RESULT_OK, resultIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    setResult(RESULT_CANCELED);
                }
            } else {
                Toast.makeText(this, "Không có ảnh gốc để cập nhật!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
            }

            finish();
        });



        btnSaveDebug.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            txtProgressPercent.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            txtProgressPercent.setText("0%");

            new Thread(() -> {
                DebugImageDatabase dbHelper = new DebugImageDatabase(this);
                int total = ScanProcessor.debugSteps.size();

                for (int i = 0; i < total; i++) {
                    Bitmap bmp = ScanProcessor.debugSteps.get(i);
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName = "step_" + timestamp + "_" + (i + 1) + ".png";

                    File file = FileUtils.getNewImageFile(this, fileName);

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    dbHelper.insertImage(fileName, file.getAbsolutePath(), timestamp, taiKhoan.getId());

                    int progress = (int) ((i + 1) / (float) total * 100);
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        txtProgressPercent.setText(progress + "%");
                    });
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "✔️ Lưu hoàn tất!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    txtProgressPercent.setVisibility(View.GONE);
                });
            }).start();
        });

        Button btnViewSaved = findViewById(R.id.btnViewSaved);
        btnViewSaved.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, ScannedDocViewerActivity.class);
            intent1.putExtra("taiKhoan", taiKhoan);
            startActivity(intent1);
        });

        for (Bitmap bmp : previewBitmaps) {
            ImageView img = new ImageView(this);
            img.setImageBitmap(bmp);
            img.setAdjustViewBounds(true);
            img.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(img);
        }

        previewBitmaps.clear();
    }
}
