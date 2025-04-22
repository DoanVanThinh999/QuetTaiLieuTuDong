package com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.io.File;
import java.io.IOException;

public class ChupAnhActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imgPreview;
    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chup_anh);

        imgPreview = findViewById(R.id.imgPreview);

        try {
            // 🔥 Tạo file ảnh
            photoFile = File.createTempFile("captured_", ".jpg", getExternalFilesDir(null));
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

            // 🔥 Mở camera, yêu cầu ảnh lưu vào file
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // ✅ Đọc ảnh gốc từ file
            Bitmap original = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            Bitmap processed = enhanceImage(original);

            imgPreview.setImageBitmap(processed);

            // Trả ảnh về Activity trước đó
            Intent result = new Intent();
            result.putExtra("imageUri", photoUri.toString());
            setResult(RESULT_OK, result);
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap enhanceImage(Bitmap bitmap) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix matrix = new ColorMatrix(new float[]{
                1.2f, 0, 0, 0, 15,
                0, 1.2f, 0, 0, 15,
                0, 0, 1.2f, 0, 15,
                0, 0, 0, 1, 0
        });

        ColorMatrix saturation = new ColorMatrix();
        saturation.setSaturation(1.3f);

        matrix.postConcat(saturation);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));

        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }
}


//package com;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.widget.ImageView;
//import androidx.appcompat.app.AppCompatActivity;
//import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
//
//public class ChupAnhActivity extends AppCompatActivity {
//    private static final int CAMERA_REQUEST = 1888;
//    private ImageView imgPreview;
//    Bitmap capturedBitmap;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chup_anh);
//        imgPreview = findViewById(R.id.imgPreview);
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap original = (Bitmap) data.getExtras().get("data");
//
//            // ✅ Tăng chất lượng ảnh sau khi chụp
//            Bitmap processed = enhanceImage(original);
//
//            capturedBitmap = processed;
//            imgPreview.setImageBitmap(processed);
//
//            // Trả ảnh về Activity trước đó
//            Intent result = new Intent();
//            result.putExtra("captured", processed);
//            setResult(RESULT_OK, result);
//            finish();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private Bitmap enhanceImage(Bitmap bitmap) {
//        // Tạo ảnh mới cùng kích thước
//        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//
//        // ✅ Matrix tăng độ sáng và tương phản rõ rệt
//        ColorMatrix brightnessContrast = new ColorMatrix(new float[]{
//                1.25f, 0, 0, 0, 15,
//                0, 1.25f, 0, 0, 15,
//                0, 0, 1.25f, 0, 15,
//                0, 0, 0, 1, 0
//        });
//
//        // ✅ Tăng saturation cho màu sống động
//        ColorMatrix saturation = new ColorMatrix();
//        saturation.setSaturation(1.4f);
//
//        // Kết hợp 2 hiệu ứng
//        brightnessContrast.postConcat(saturation);
//        paint.setColorFilter(new ColorMatrixColorFilter(brightnessContrast));
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//
//        // ➕ Thêm sharpen sau cùng
//        return sharpenImage(result);
//    }
//
//    private Bitmap sharpenImage(Bitmap bitmap) {
//        // Cách đơn giản: áp lại saturation nhẹ + draw lại để tăng chi tiết
//        Bitmap sharpened = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(sharpened);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//
//        ColorMatrix sharpenMatrix = new ColorMatrix();
//        sharpenMatrix.setSaturation(1.2f); // nhẹ hơn để tránh noise
//
//        paint.setColorFilter(new ColorMatrixColorFilter(sharpenMatrix));
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//
//        return sharpened;
//    }
//}

//package com;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.provider.MediaStore;
//
//import android.widget.ImageView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//
//import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
//
//public class ChupAnhActivity extends AppCompatActivity {
//    private static final int CAMERA_REQUEST = 1888;
//    private ImageView imgPreview;
//    Bitmap capturedBitmap;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chup_anh);
//        imgPreview = findViewById(R.id.imgPreview);
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap original = (Bitmap) data.getExtras().get("data");
//
//            // ➕ Tăng độ nét & độ bão hòa (để giống ảnh chụp màn hình)
//            Bitmap processed = enhanceImage(original);
//
//            capturedBitmap = processed;
//            imgPreview.setImageBitmap(processed);
//
//            // Trả ảnh về activity trước đó
//            Intent result = new Intent();
//            result.putExtra("captured", processed);
//            setResult(RESULT_OK, result);
//            finish();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private Bitmap enhanceImage(Bitmap bitmap) {
//        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//
//        // 💡 Điều chỉnh sáng & tương phản nhẹ
//        ColorMatrix brightnessContrast = new ColorMatrix(new float[]{
//                1.05f, 0, 0, 0, 9,
//                0, 1.05f, 0, 0, 9,
//                0, 0, 1.05f, 0, 9,
//                0, 0, 0, 1, 0
//        });
//
//        // 🎨 Saturation chỉ 1 tí cho màu tự nhiên
//        ColorMatrix saturation = new ColorMatrix();
//        saturation.setSaturation(1.15f); // nhẹ hơn 8%
//
//        // 🔍 Không sharpen luôn để tránh noise nếu ánh sáng yếu
//        brightnessContrast.postConcat(saturation);
//        paint.setColorFilter(new ColorMatrixColorFilter(brightnessContrast));
//
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        //return result;
//        return sharpenImage(result);
//    }
//    // A simple method to sharpen the image
//    private Bitmap sharpenImage(Bitmap bitmap) {
//        Bitmap sharpened = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(sharpened);
//        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//
//        // Create a sharpen matrix
//        ColorMatrix sharpenMatrix = new ColorMatrix();
//        sharpenMatrix.setSaturation(1.5f); // Adjust as needed
//
//        paint.setColorFilter(new ColorMatrixColorFilter(sharpenMatrix));
//
//        // Apply the sharpening to the enhanced image
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//
//        return sharpened;
//    }
//
//
//
//
//
//
//
//
//
//}
