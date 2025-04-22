package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.common.util.UnstableApi;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChinhSuaVideoActivity extends AppCompatActivity {
    private static final int REQUEST_CUT_VIDEO = 1;
    ImageView imgXemLaiNut;
    TextView tvVideoInfo, tvPercentText;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    Button btnThemMoc, btnCatVideo, btnChenVideo, btnKhoiPhuc, btnXoaMoc;
    private Uri videoUri, originalVideoUri;
    private final List<Long> markerPositions = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String inputVideoPath;
    private MarkerView markerView;
    private HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = new HashMap<>();
    private static final int REQUEST_CODE_UPDATE_CAU_HOI = 2000;
    private ActivityResultLauncher<Intent> updateCauHoiLauncher;
    private TaiKhoan taiKhoan;
    private ProgressBar loadingProgress;
    ProgressBar progressBarPercent;
    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_video);
        btnXoaMoc = findViewById(R.id.btnXoaMoc);
        tvPercentText = findViewById(R.id.tvPercentText);
        playerView = findViewById(R.id.playerView);
        btnThemMoc = findViewById(R.id.btnThemMoc);
        btnCatVideo = findViewById(R.id.btnCatVideo);
        btnChenVideo = findViewById(R.id.btnChenVideo);
        btnKhoiPhuc = findViewById(R.id.btnKhoiPhuc);
        tvVideoInfo = findViewById(R.id.tvVideoInfo);
        imgXemLaiNut = findViewById(R.id.ImgXemLaiNut);
        imgXemLaiNut.setVisibility(View.GONE);
        imgXemLaiNut.setOnClickListener(v -> showDialogXemLaiCauHoi());
        loadingProgress = findViewById(R.id.loadingProgress);
        progressBarPercent = findViewById(R.id.progressBarPercent);
        Intent intent = getIntent();
        taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        btnXoaMoc.setOnClickListener(view -> {
            DatabaseHelper db = new DatabaseHelper(this);
            ArrayList<Long> allMarkers = db.getAllMarkerTimesByVideoPath1(videoUri.toString());

            if (allMarkers.isEmpty()) {
                Toast.makeText(this, "Không có mốc nào để xoá!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inflate layout chứa danh sách CheckBox
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_xoa_moc, null);
            LinearLayout layoutCheckboxes = dialogView.findViewById(R.id.layoutCheckboxes);
            List<CheckBox> checkBoxList = new ArrayList<>();

            for (int i = 0; i < allMarkers.size(); i++) {
                long timeMs = allMarkers.get(i);
                CheckBox cb = new CheckBox(this);
                cb.setText("Mốc " + (i + 1) + ": " + (timeMs / 1000.0) + " giây");
                layoutCheckboxes.addView(cb);
                checkBoxList.add(cb);
            }

            new AlertDialog.Builder(this)
                    .setTitle("❌ Chọn mốc muốn xoá")
                    .setView(dialogView)
                    .setPositiveButton("Xoá", (dialog, which) -> {
                DatabaseHelper db99 = new DatabaseHelper(this);
                for (int i = checkBoxList.size() - 1; i >= 0; i--) {
                    if (checkBoxList.get(i).isChecked()) {
                        long timeToRemove = allMarkers.get(i);
                        db99.deleteCauHoiTheoMoc(videoUri.toString(), timeToRemove);
                    }
                }

                // cập nhật markerPositions từ DB sau khi xóa
                markerPositions.clear();
                markerPositions.addAll(db99.getAllMarkerTimesByVideoPath1(videoUri.toString()));
                markerView.setMarkers(markerPositions, exoPlayer.getDuration());
                updateMarkers();

                Toast.makeText(this, "Đã xoá mốc và dữ liệu liên quan!", Toast.LENGTH_SHORT).show();
            })

                    .setNegativeButton("Huỷ", null)
                    .show();
        });
        // Lấy Uri video từ Intent
        // Nhận đúng kiểu Uri
        String videoUriStr = getIntent().getStringExtra("video_uri");
        if (videoUriStr != null) {
            videoUri = Uri.parse(videoUriStr);
        } else {
            Toast.makeText(this, "Không tìm thấy video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        originalVideoUri = videoUri; // Lưu lại bản gốc

        // KHỞI TẠO exoPlayer TRƯỚC
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        // Thêm ngay dòng này để fix triệt để vấn đề ẩn marker khi ẩn hiện control
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> updateMarkers());
        executorService.execute(() -> {
            inputVideoPath = copyUriToFileWithProgress(videoUri);
            runOnUiThread(() -> {
                if (inputVideoPath == null || inputVideoPath.isEmpty()) {
                    Toast.makeText(this, "❌ Không thể đọc video", Toast.LENGTH_SHORT).show();
                    return;
                }

                hienThiThongTinVideoBanDau(inputVideoPath);
                MediaItem mediaItem = MediaItem.fromUri(videoUri);
                exoPlayer.setMediaItem(mediaItem);

                // ✅ Lấy markerPositions từ DB ngay khi mở video
                DatabaseHelper db = new DatabaseHelper(this);
                markerPositions.clear();
                markerPositions.addAll(db.getAllMarkerTimesByVideoPath1(videoUri.toString()));
                updateMarkers();
                markerView.setMarkers(markerPositions, exoPlayer.getDuration());

                exoPlayer.prepare();
                exoPlayer.play();
                loadingProgress.setVisibility(View.GONE);
                progressBarPercent.setVisibility(View.GONE);
                tvPercentText.setVisibility(View.GONE);
            });
        });
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateMarkers();
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                updateMarkers();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updateMarkers();
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                updateMarkers();
            }

            @Override
            public void onTimelineChanged(androidx.media3.common.Timeline timeline, int reason) {
                updateMarkers();
            }
        });

// Thêm đầy đủ import cho AnalyticsListener ở đây!
        exoPlayer.addAnalyticsListener(new AnalyticsListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onPositionDiscontinuity(
                    AnalyticsListener.EventTime eventTime,
                    Player.PositionInfo oldPosition,
                    Player.PositionInfo newPosition,
                    int reason) {
                updateMarkers();
            }

            @Override
            public void onPlaybackStateChanged(@SuppressLint("UnsafeOptInUsageError") AnalyticsListener.EventTime eventTime, int state) {
                updateMarkers();
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onTimelineChanged(AnalyticsListener.EventTime eventTime, int reason) {
                updateMarkers();
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onVideoSizeChanged(AnalyticsListener.EventTime eventTime, VideoSize videoSize) {
                updateMarkers();
            }
        });




        markerView = findViewById(R.id.markerView);
        btnThemMoc.setOnClickListener(v -> {
            long currentTime = exoPlayer.getCurrentPosition();
            // ✅ Lưu vào database luôn
            DatabaseHelper db = new DatabaseHelper(this);
            db.addCauHoiTheoMoc(currentTime, videoUri.toString(), "", "", "", "", -1);
            // cập nhật markerPositions từ DB
            markerPositions.clear();
            markerPositions.addAll(db.getAllMarkerTimesByVideoPath1(videoUri.toString()));
            updateMarkers();
            markerView.setMarkers(markerPositions, exoPlayer.getDuration());
            showInputQuestionCountDialog(currentTime);
        });


        btnCatVideo.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, CatVideoActivity.class);
            intent1.putExtra("video_uri", videoUri.toString());
            startActivityForResult(intent, REQUEST_CUT_VIDEO);
        });

        btnChenVideo.setOnClickListener(v -> {
            Intent intent2 = new Intent(this, ChenVideoActivity.class);
            intent2.putExtra("video_uri", videoUri.toString());
            intent2.putExtra("currentTime", exoPlayer.getCurrentPosition());
            startActivity(intent2);
        });
        updateCauHoiLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("CHECK_RECEIVE", "📥 Đã vào updateCauHoiLauncher");
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        long marker = result.getData().getLongExtra("marker_time", -1);
                        ArrayList<CauHoi> updatedList =
                                (ArrayList<CauHoi>) result.getData().getSerializableExtra("ds_cau_hoi");
                        for (int i = 0; i < updatedList.size(); i++) {
                            Log.d("CHECK_RECEIVE", "🔄 Câu " + i + ": " + updatedList.get(i).noiDung); // ← THÊM DÒNG NÀY
                        }
                        if (marker != -1 && updatedList != null) {
                            for (CauHoi ch : updatedList) {
                                if (ch.noiDung == null) ch.noiDung = "";
                                Log.d("DEBUG_UPDATE", "✅ Câu sau cập nhật: " + ch.noiDung);
                            }

                            // ✅ Nếu có key gần đúng thì xoá key cũ để tránh trùng
                            ArrayList<Long> keyTrung = new ArrayList<>();
                            for (Long key : cauHoiTheoMoc.keySet()) {
                                if (Math.abs(key - marker) <= 10) {
                                    keyTrung.add(key);
                                }
                            }
                            for (Long key : keyTrung) {
                                cauHoiTheoMoc.remove(key);
                            }

// 🔁 Gán lại bằng marker đúng
                            cauHoiTheoMoc.put(marker, updatedList);
                            if (!markerPositions.contains(marker)) {
                                markerPositions.add(marker);
                            }

                            Log.d("CHECK_UPDATE", "✅ Đã gán updatedList vào marker = " + marker);

// 🧩 In lại để kiểm chứng
                            for (int i = 0; i < updatedList.size(); i++) {
                                Log.d("CHECK_AFTER_PUT", "🧩 Marker " + marker + " - Câu " + i + ": " + updatedList.get(i).noiDung);
                            }
                            Log.d("CHECK_RECEIVE", "📥 Nhận lại từ DanhSachCauHoiActivity: code = " + result.getResultCode());



                            imgXemLaiNut.setVisibility(View.VISIBLE);
                            showDialogXemLaiCauHoi();
                        }
                    }
                }
        );
        btnKhoiPhuc.setOnClickListener(v -> {
            Intent intent22 = new Intent(ChinhSuaVideoActivity.this, CaiDatXemVideoActivity.class);
            intent22.putExtra("video_uri", videoUri.toString());
            intent22.putExtra("taiKhoan", taiKhoan); // Serializable
            intent22.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc); // 🧠 THÊM DÒNG NÀY
            startActivity(intent22);
        });

    }

    private void showInputQuestionCountDialog(long markerTime) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số lượng câu hỏi cho mốc này");

        new AlertDialog.Builder(this)
                .setTitle("🟢 Mốc " + (markerTime / 1000.0) + " giây")
                .setMessage("Bạn muốn thêm bao nhiêu câu hỏi?")
                .setView(input)
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        int questionCount = Integer.parseInt(value);

                        // ✅ Nếu mốc chưa tồn tại thì tạo mới danh sách câu hỏi
                        if (!cauHoiTheoMoc.containsKey(markerTime)) {
                            ArrayList<CauHoi> danhSachMoi = new ArrayList<>();
                            for (int i = 0; i < questionCount; i++) {
                                CauHoi ch = new CauHoi();
                                ch.noiDung = "";  // ✅ đảm bảo không bị null
                                danhSachMoi.add(ch);
                            }
                            cauHoiTheoMoc.put(markerTime, danhSachMoi);
                        }

                        openQuizEditor(markerTime, questionCount, 0);
                        imgXemLaiNut.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    @SuppressLint({"SetTextI18n", "Range"})
    private void showDialogXemLaiCauHoi() {
        if (markerPositions.isEmpty()) {
            Toast.makeText(this, "Chưa có mốc nào để xem lại!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("DEBUG_MOC", "📋 Toàn bộ key hiện có trong cauHoiTheoMoc:");
        for (Long k : cauHoiTheoMoc.keySet()) {
            Log.d("DEBUG_MOC", "   - " + k);
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_xem_lai_moc, null);
        LinearLayout layout = dialogView.findViewById(R.id.layoutDanhSachCauHoiMoc);

        layout.removeAllViews(); // 🔁 Luôn clear layout trước khi add lại

        for (int i = 0; i < markerPositions.size(); i++) {
            long timeMs = markerPositions.get(i);
            ArrayList<CauHoi> dsCauHoi = cauHoiTheoMoc.get(timeMs);
            Log.d("CHECK_NOIDUNG", "==> Kiểm tra nội dung cho mốc " + timeMs);
            if (dsCauHoi != null) {
                for (int j = 0; j < dsCauHoi.size(); j++) {
                    CauHoi ch = dsCauHoi.get(j);
                    Log.d("CHECK_FIX", "Câu hỏi " + j + ": noiDung = '" + ch.noiDung + "'");
                }
            }

            boolean daNhap = false;
            if (dsCauHoi != null) {
                for (CauHoi ch : dsCauHoi) {
                    if (ch != null && ch.noiDung != null && !ch.noiDung.trim().isEmpty())
                    {
                        daNhap = true;
                        break;
                    }
                }
            }
            TextView tv = new TextView(this);
            tv.setPadding(16, 16, 16, 16);
            tv.setTextSize(16);
            tv.setTextColor(Color.BLACK);
            tv.setText("📍 Mốc " + (i + 1) + ": " + (timeMs / 1000.0) + " giây");
            int finalI = i;
            Log.d("DEBUG_MOC", "🔍 Mốc " + i + ": " + timeMs + ", có " + (dsCauHoi != null ? dsCauHoi.size() : 0) + " câu");
            tv.setOnClickListener(view -> {
                //ArrayList<CauHoi> ds = cauHoiTheoMoc.get(markerPositions.get(finalI));
                DatabaseHelper db = new DatabaseHelper(this);
                Cursor cursor = db.getCauHoiTheoVideoVaMoc(videoUri.toString(), markerPositions.get(finalI));
                ArrayList<CauHoi> ds = new ArrayList<>();

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        CauHoi ch = new CauHoi();
                        ch.noiDung = cursor.getString(cursor.getColumnIndex("noi_dung"));
                        ch.quizType = cursor.getString(cursor.getColumnIndex("quiz_type"));
                        ch.chiSoDung = cursor.getInt(cursor.getColumnIndex("chi_so_dung"));

                        String dapAnListStr = cursor.getString(cursor.getColumnIndex("dap_an_list"));
                        String dapAnDungListStr = cursor.getString(cursor.getColumnIndex("dap_an_dung_list"));
                        ch.dapAnList = new ArrayList<>(java.util.Arrays.asList(dapAnListStr.split(",")));
                        ch.dapAnDungList = new ArrayList<>();
                        for (String b : dapAnDungListStr.split(",")) {
                            ch.dapAnDungList.add(Boolean.parseBoolean(b.trim()));
                        }

                        ds.add(ch);
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                if (ds == null) {
                    Toast.makeText(this, "Chưa có câu hỏi nào cho mốc này!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, DanhSachCauHoiActivity.class);
                intent.putExtra("marker_time", markerPositions.get(finalI));
                intent.putExtra("total_questions", ds.size());
                intent.putExtra("ds_cau_hoi", ds);
                intent.putExtra("taiKhoan",taiKhoan);
                intent.putExtra("video_uri", videoUri.toString()); // ✅ THÊM DÒNG NÀY
                updateCauHoiLauncher.launch(intent);
            });

            layout.addView(tv);
        }
        new AlertDialog.Builder(this)
                .setTitle("👀 Xem lại câu hỏi theo mốc")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .show();
    }
    private void openQuizEditor(long markerTime, int totalQuestions, int currentIndex) {
        ArrayList<CauHoi> dsCauHoi;

        // ✅ Kiểm tra xem mốc đã có câu hỏi chưa
        if (cauHoiTheoMoc.containsKey(markerTime)) {
            dsCauHoi = cauHoiTheoMoc.get(markerTime);
            if (dsCauHoi == null) {
                dsCauHoi = new ArrayList<>();
            }
        } else {
            dsCauHoi = new ArrayList<>();
            for (int i = 0; i < totalQuestions; i++) {
                CauHoi ch = new CauHoi();
                ch.noiDung = "";  // ✅ đảm bảo không bị null
                dsCauHoi.add(ch);
            }
            cauHoiTheoMoc.put(markerTime, dsCauHoi); // ✅ Chỉ gán nếu chưa có
        }

        Intent intent = new Intent(this, ChonLoaiCauHoiActivity.class);
        intent.putExtra("marker_time", markerTime);
        intent.putExtra("total_questions", totalQuestions);
        intent.putExtra("current_index", currentIndex);
        intent.putExtra("video_uri", videoUri.toString()); // 🔥 THÊM DÒNG NÀY
        intent.putExtra("ds_cau_hoi", dsCauHoi);
        intent.putExtra("taiKhoan", taiKhoan);
        startActivity(intent);
    }
    @OptIn(markerClass = UnstableApi.class)
    private void updateMarkers() {
        DatabaseHelper db = new DatabaseHelper(this);
        markerPositions.clear();
        markerPositions.addAll(db.getAllMarkerTimesByVideoPath1(videoUri.toString()));

        PlayerControlView controlView = playerView.findViewById(androidx.media3.ui.R.id.exo_controller);
        if (controlView != null) {
            DefaultTimeBar timeBar = controlView.findViewById(androidx.media3.ui.R.id.exo_progress);
            if (timeBar != null) {
                int markerCount = markerPositions.size();
                long[] adMarkers = new long[markerCount];
                boolean[] playedAdGroups = new boolean[markerCount]; // Tạo mảng boolean này (bắt buộc!)

                for (int i = 0; i < markerCount; i++) {
                    adMarkers[i] = markerPositions.get(i);
                    playedAdGroups[i] = false; // Luôn false để luôn hiển thị marker
                }

                // Set vào timeBar
                timeBar.setAdGroupTimesMs(adMarkers, playedAdGroups, markerCount);
                timeBar.setAdMarkerColor(Color.parseColor("#1E7D22"));
                timeBar.invalidate(); // Refresh giao diện ngay lập tức
            }
        }
    }


    private String copyUriToFileWithProgress(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream == null) return "";

            String fileName = getFileNameFromUri(uri);
            File tempFile = new File(getCacheDir(), fileName);

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int len;
            int copied = 0;
            int total = getContentLengthFromUri(uri);
            runOnUiThread(() -> {
                loadingProgress.setVisibility(View.VISIBLE);
                progressBarPercent.setVisibility(View.VISIBLE);
                tvPercentText.setVisibility(View.VISIBLE);
            });
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
                copied += len;
                final int progress = total > 0 ? (int) (((float) copied / total) * 100) : 0;
                runOnUiThread(() -> {
                    loadingProgress.setProgress(progress);
                    progressBarPercent.setProgress(progress);
                    tvPercentText.setText("Đang tải: " + progress + "%"); // ✅ FIX HERE
                });
            }

            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private int getContentLengthFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                int size = (int) cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
            cursor.close();
        }
        return -1; // unknown size
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String name = "temp_video.mp4";
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                name = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CUT_VIDEO && resultCode == RESULT_OK) {
            String cutVideoUri = data.getStringExtra("cutVideoUri");
            if (cutVideoUri != null) {
                videoUri = Uri.parse(cutVideoUri);
                originalVideoUri = videoUri;
                hienThiThongTinVideo(videoUri.getPath());

                MediaItem mediaItem = MediaItem.fromUri(videoUri);
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            } else {
                Toast.makeText(this, "Lỗi cắt video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hienThiThongTinVideoBanDau(String path) {
        if (path == null || path.isEmpty()) {
            Toast.makeText(this, "Video không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMillis = Long.parseLong(durationStr);
            long durationSeconds = durationMillis / 1000;
            tvVideoInfo.setText("Độ dài video: " + durationSeconds + " giây");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lấy thông tin video", Toast.LENGTH_SHORT).show();
        }
    }

    private void hienThiThongTinVideo(String path) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMillis = Long.parseLong(durationStr);
            long durationSeconds = durationMillis / 1000;
            tvVideoInfo.setText("Độ dài video: " + durationSeconds + " giây");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể lấy thông tin video", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
