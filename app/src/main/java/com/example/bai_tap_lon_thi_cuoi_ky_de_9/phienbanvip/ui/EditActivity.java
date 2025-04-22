package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class EditActivity extends AppCompatActivity {
    boolean isImageModified = false;
    public static Bitmap bitmap;
    private ImageView imageView;
    private float contrast = 1.0f, brightness = 0f; //contrast, brightness: dùng để lưu giá trị điều chỉnh hình ảnh.
    Button btnExport, btnXacNhan;
    Button btnCrop;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        imageView = findViewById(R.id.imgEdit);
        imageView.setImageBitmap(bitmap);
        btnCrop = findViewById(R.id.btnCrop);
        SeekBar seekBrightness = findViewById(R.id.seekBrightness);
        SeekBar seekContrast = findViewById(R.id.seekContrast);
        btnXacNhan = findViewById(R.id.btnXacNhan);
        btnXacNhan.setVisibility(View.GONE);
        btnXacNhan.setOnClickListener(view -> {
            if (isImageModified) {
                // Áp dụng độ sáng và tương phản vào bitmap trước khi lưu
                Bitmap finalBitmap = applyContrastBrightness(bitmap, contrast, brightness);

                try {
                    File tempFile = new File(getExternalFilesDir(null), "edited_image.jpg");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();

                    Uri editedImageUri = Uri.fromFile(tempFile);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("editedImageUri", editedImageUri.toString());
                    setResult(RESULT_OK, resultIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finish();
        });






        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(view -> {
            exportToPdf();
        });
        Button btnRotate = findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(v -> {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(bitmap);
            isImageModified = true;
            btnXacNhan.setVisibility(View.VISIBLE);
        });
        btnCrop.setOnClickListener(v -> openCropActivity());
        seekBrightness.setOnSeekBarChangeListener(sliderChange((v) -> {
            //Độ sáng
            brightness = (v - 50) / 50f;
            // Giá trị thanh trượt v nằm trong phạm vi 0 đến 100.
            // Mục tiêu là để giá trị độ sáng nằm trong phạm vi [-1.0, 1.0].
            // Khi giá trị thanh trượt là 50, độ sáng sẽ là 0 (không thay đổi), giá trị nhỏ hơn 50 sẽ làm hình ảnh tối đi, còn giá trị lớn hơn 50 sẽ làm hình ảnh sáng lên.
            isImageModified = true;
            btnXacNhan.setVisibility(View.VISIBLE);
            applyFilter();
        }));

        seekContrast.setOnSeekBarChangeListener(sliderChange((v) -> {
            contrast = v / 50f;
            // v được chia cho 50 để điều chỉnh độ tương phản.
            // Nếu v là 50, độ tương phản sẽ là 1 (không thay đổi).
            // Nếu v nhỏ hơn 50, độ tương phản sẽ giảm, và nếu lớn hơn 50, độ tương phản sẽ tăng.
            isImageModified = true;
            btnXacNhan.setVisibility(View.VISIBLE);
            applyFilter();
        }));
    }
    private void openCropActivity() {
        try {
            File file = File.createTempFile("to_crop_", ".jpg", getExternalFilesDir(null));
            Uri imageUri = Uri.fromFile(file);

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));

            com.yalantis.ucrop.UCrop.Options options = new com.yalantis.ucrop.UCrop.Options();
            options.setFreeStyleCropEnabled(true); // ✅ Cho phép di chuyển tự do
            options.setToolbarTitle("Cắt ảnh");
            options.setToolbarColor(getResources().getColor(R.color.purple_500));
            options.setStatusBarColor(getResources().getColor(R.color.purple_700));
            options.setActiveControlsWidgetColor(getResources().getColor(R.color.teal_700));

            com.yalantis.ucrop.UCrop.of(imageUri, destinationUri)
                    .withOptions(options)
                    .withAspectRatio(0, 0) // không ép tỉ lệ
                    .withMaxResultSize(1080, 1920)
                    .start(this);
            isImageModified = true;
            btnXacNhan.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void exportToPdf() {
        if (bitmap == null) return;
        // Cho người dùng nhập tên
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt tên file PDF");

        final EditText input = new EditText(this);
        input.setHint("Tên file không chứa .pdf");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String tenFile = input.getText().toString().trim();
            if (!tenFile.isEmpty()) {
                PdfExporter.saveImageAsPdf(this, bitmap, tenFile);
            } else {
                Toast.makeText(this, "Vui lòng nhập tên file", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();


    }
    private void applyFilter() {
        ColorMatrix cm = new ColorMatrix();  // Tạo bộ lọc màu
        cm.set(new float[] {
                contrast, 0, 0, 0, brightness * 255,  // Cấu hình độ tương phản và độ sáng cho kênh đỏ
                0, contrast, 0, 0, brightness * 255,  // Cấu hình độ tương phản và độ sáng cho kênh xanh
                0, 0, contrast, 0, brightness * 255,  // Cấu hình độ tương phản và độ sáng cho kênh xanh dương
                0, 0, 0, 1, 0  // Kênh alpha (trong suốt)
        });

        // Áp dụng bộ lọc màu lên ảnh
        imageView.setColorFilter(new ColorMatrixColorFilter(cm));  // Đưa bộ lọc vào ảnh
    }
    private Bitmap applyContrastBrightness(Bitmap src, float contrast, float brightness) {
        Bitmap editedBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        Canvas canvas = new Canvas(editedBitmap);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix(new float[]{
                contrast, 0, 0, 0, brightness * 255,
                0, contrast, 0, 0, brightness * 255,
                0, 0, contrast, 0, brightness * 255,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return editedBitmap;
    }


    private SeekBar.OnSeekBarChangeListener sliderChange(final Consumer<Integer> consumer) {
        return new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                consumer.accept(progress);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == com.yalantis.ucrop.UCrop.REQUEST_CROP) {
            final Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(data);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

