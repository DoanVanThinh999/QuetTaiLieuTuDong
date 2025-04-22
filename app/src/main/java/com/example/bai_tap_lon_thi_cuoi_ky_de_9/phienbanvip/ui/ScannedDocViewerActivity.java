package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DebugImageDatabase;
import com.Model.TaiKhoan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScannedDocViewerActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private int currentIndex = 0;
    private ArrayList<String> imagePaths;
    private LinearLayout container, topBar;
    private ProgressBar progressBar;
    private TextView txtPercent;
    private DebugImageDatabase db;
    private int userId;
    private Context context;
    private boolean isMultiDeleteMode = false;
    private ArrayList<ImageView> selectedImages = new ArrayList<>();
    private ArrayList<File> selectedFiles = new ArrayList<>();
    private TextView btnConfirmDelete;
    private TextView btnDeleteAll;
    private TextView btnMultiDelete; // Thêm dòng này vào danh sách biến toàn cục của class
    private List<Integer> selectedSteps = new ArrayList<>();
    LinearLayout filterLayout;
    TextView stepLabel;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        db = new DebugImageDatabase(this);
        TaiKhoan taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        userId = taiKhoan != null ? taiKhoan.getId() : 0;
        imagePaths = db.getAllPaths(userId);

        ScrollView scrollView = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(container);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        progressBar.setMax(100);
        container.addView(progressBar);

        txtPercent = new TextView(this);
        txtPercent.setText("0%");
        txtPercent.setGravity(Gravity.CENTER_HORIZONTAL);
        container.addView(txtPercent);
        // Tạo layout ngang chứa Spinner + nút thùng rác
        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        topBar.setPadding(10, 10, 10, 10);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

// Label "Lọc theo bước:"
        stepLabel = new TextView(this);
        stepLabel.setText("Bước:");
        stepLabel.setPadding(10, 0, 10, 0);
        topBar.addView(stepLabel);

// Spinner
        spinner = new Spinner(this);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "1", "2", "3", "4","5"});
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinner.setLayoutParams(spinnerParams);
        topBar.addView(spinner);

// Spacer đẩy thùng rác về phải
        View space = new View(this);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 1, 1);
        topBar.addView(space, spaceParams);

// Nút thùng rác bên phải
        btnMultiDelete = new TextView(this); // Nếu đã khai báo ở ngoài thì bỏ "TextView" ở đây
        btnMultiDelete.setText("🗑️");
        btnMultiDelete.setTextSize(18);
        btnMultiDelete.setPadding(20, 10, 20, 10);
        btnMultiDelete.setOnClickListener(v -> toggleMultiDeleteMode());
        topBar.addView(btnMultiDelete);

// Thêm topBar vào layout
        container.addView(topBar);

        btnConfirmDelete = new TextView(this);
        btnConfirmDelete.setText("✅");
        btnConfirmDelete.setGravity(Gravity.CENTER_HORIZONTAL);
        btnConfirmDelete.setTextSize(16);
        btnConfirmDelete.setPadding(0, 10, 0, 30);
        btnConfirmDelete.setVisibility(TextView.GONE); // ẨN BAN ĐẦU
        btnConfirmDelete.setOnClickListener(v -> confirmMultiDelete());
        container.addView(btnConfirmDelete);
        btnDeleteAll = new TextView(this);
        btnDeleteAll.setText("🧨 DELETE ALL");
        btnDeleteAll.setGravity(Gravity.CENTER_HORIZONTAL);
        btnDeleteAll.setTextSize(16);
        btnDeleteAll.setPadding(0, 0, 0, 30);
        btnDeleteAll.setVisibility(TextView.GONE); // ẨN BAN ĐẦU
        btnDeleteAll.setOnClickListener(v -> deleteAllImages());
        container.addView(btnDeleteAll);

        setContentView(scrollView);

        if (imagePaths.isEmpty()) {
            Toast.makeText(context, "Không có ảnh đã lưu!", Toast.LENGTH_SHORT).show();
            return;
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                selectedSteps.clear();
                if (!selected.equals("All")) {
                    selectedSteps.add(Integer.parseInt(selected));
                }
                reloadImagesByStepFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        loadNextImage();
    }
    private void toggleMultiDeleteMode() {
        isMultiDeleteMode = !isMultiDeleteMode;
        Toast.makeText(context, isMultiDeleteMode ? "Chọn ảnh để xoá" : "Thoát chế độ xoá nhiều", Toast.LENGTH_SHORT).show();
        btnConfirmDelete.setVisibility(isMultiDeleteMode ? TextView.VISIBLE : TextView.GONE);
        btnDeleteAll.setVisibility(isMultiDeleteMode ? TextView.VISIBLE : TextView.GONE);
        // Clear lựa chọn nếu tắt chế độ
        if (!isMultiDeleteMode) {
            for (ImageView iv : selectedImages) {
                iv.setAlpha(1.0f);
            }
            selectedImages.clear();
            selectedFiles.clear();
        }
    }
    private void deleteAllImages() {
        new AlertDialog.Builder(context)
                .setTitle("Xoá tất cả?")
                .setMessage("Bạn chắc chắn muốn xoá toàn bộ ảnh đã lưu?")
                .setPositiveButton("Xoá hết", (dialog, which) -> {
                    for (String path : imagePaths) {
                        File file = new File(path);
                        if (file.exists()) file.delete();
                        db.deleteByPath(path, userId);
                    }
                    container.removeAllViews(); // xoá UI ảnh
                    selectedFiles.clear();
                    selectedImages.clear();
                    imagePaths.clear();

                    Toast.makeText(context, "Đã xoá tất cả ảnh", Toast.LENGTH_SHORT).show();

                    isMultiDeleteMode = false;
                    btnConfirmDelete.setVisibility(TextView.GONE);
                    btnDeleteAll.setVisibility(TextView.GONE);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadNextImage() {
        if (currentIndex >= imagePaths.size()) {
            progressBar.setVisibility(ProgressBar.GONE);
            txtPercent.setText("Kho lưu trữ vùng tài liệu");
            return;
        }

        String path = imagePaths.get(currentIndex);
        File file = new File(path);

        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                ImageView img = new ImageView(context);
                img.setImageBitmap(bitmap);
                img.setAdjustViewBounds(true);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 30, 0, 30);
                img.setLayoutParams(params);

                container.addView(img);

                img.setOnClickListener(v -> {
                    if (isMultiDeleteMode) {
                        if (selectedImages.contains(img)) {
                            img.setAlpha(1.0f); // bỏ chọn
                            selectedImages.remove(img);
                            selectedFiles.remove(file);
                        } else {
                            img.setAlpha(0.5f); // highlight chọn
                            selectedImages.add(img);
                            selectedFiles.add(file);
                        }
                    } else {
                        showDialog(file, bitmap, img);
                    }
                });

            }
        }

        currentIndex++;
        int percent = (int) ((currentIndex / (float) imagePaths.size()) * 100);
        progressBar.setProgress(percent);
        txtPercent.setText(percent + "%");

        // Tiếp tục load ảnh sau 30ms để tránh ANR
        handler.postDelayed(this::loadNextImage, 30);
    }

    private void showDialog(File file, Bitmap bitmap, ImageView imgView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ảnh: " + file.getName());

        ImageView bigView = new ImageView(context);
        bigView.setImageBitmap(bitmap);
        bigView.setAdjustViewBounds(true);

        ScrollView scroll = new ScrollView(context);
        scroll.addView(bigView);
        builder.setView(scroll);

        builder.setPositiveButton("❌ Xóa ảnh", (dialog, which) -> {
            boolean deleted = file.delete();
            if (deleted) {
                db.deleteByPath(file.getAbsolutePath(), userId);
                container.removeView(imgView);
                Toast.makeText(context, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Không thể xóa ảnh!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
    private void confirmMultiDelete() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(context, "Chưa chọn ảnh nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Xoá nhiều ảnh?")
                .setMessage("Bạn có chắc muốn xoá " + selectedFiles.size() + " ảnh?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        File file = selectedFiles.get(i);
                        ImageView img = selectedImages.get(i);
                        if (file.exists() && file.delete()) {
                            db.deleteByPath(file.getAbsolutePath(), userId);
                            container.removeView(img);
                        }
                    }
                    selectedFiles.clear();
                    selectedImages.clear();
                    Toast.makeText(context, "Đã xoá ảnh đã chọn!", Toast.LENGTH_SHORT).show();
                    isMultiDeleteMode = false;
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
    private void reloadImagesByStepFilter() {
        container.removeAllViews();
        currentIndex = 0;

        // ⚠️ Gỡ topBar khỏi parent nếu đã có
        if (topBar.getParent() != null) {
            ((ViewGroup) topBar.getParent()).removeView(topBar);
        }

        // Hiển thị lại các nút control
        container.addView(progressBar);
        container.addView(txtPercent);
        container.addView(topBar); // Safe để add lại
        container.addView(btnConfirmDelete);
        container.addView(btnDeleteAll);

        // Lấy danh sách ảnh tương ứng step
        ArrayList<String> allPaths = db.getAllPaths(userId);
        if (selectedSteps.isEmpty()) {
            imagePaths = allPaths;
        } else {
            imagePaths = filterPathsBySteps(allPaths, selectedSteps);
        }

        if (imagePaths.isEmpty()) {
            Toast.makeText(context, "Không có ảnh cho các bước đã chọn!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadNextImage();
    }

    private ArrayList<String> filterPathsBySteps(List<String> paths, List<Integer> steps) {
        ArrayList<String> filtered = new ArrayList<>();
        for (String path : paths) {
            for (int step : steps) {
                // Match chính xác kiểu file kết thúc bằng _<step>.png, ví dụ: _1.png
                if (path.matches(".*_" + step + "\\.png$")) {
                    filtered.add(path);
                    break;
                }
            }
        }
        return filtered;
    }


}
