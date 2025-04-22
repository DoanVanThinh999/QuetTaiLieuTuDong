package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class TaoVideoHuongDanNguoiDungActivity extends AppCompatActivity {
    private TaiKhoan taiKhoan;
    private Button btnChonVideo, btnSuaVideo;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    ImageButton btnFullscreen;
    boolean isFullScreen = false;
    TextView tvTitle;
    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<Intent> chonVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri videoUri = result.getData().getData();

                    // ✅ Sao chép video về bộ nhớ app
                    String localPath = copyUriToInternalStorage(videoUri); // Viết hàm này bên dưới

                    if (localPath != null) {
                        playVideo(Uri.fromFile(new File(localPath))); // Phát từ file local
                        getSharedPreferences("video_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("saved_video_uri", localPath)
                                .apply();
                        String fileName = new File(localPath).getName();
                        tvTitle.setText("🎬 " + fileName);
                    } else {
                        Toast.makeText(this, "Không thể lưu video", Toast.LENGTH_SHORT).show();
                    }
                }

            });

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_video_huong_dan_nguoi_dung);
        // ✅ Nếu có video đã lưu trước đó thì phát lại
        String savedUri = getSharedPreferences("video_prefs", MODE_PRIVATE)
                .getString("saved_video_uri", null);
        if (savedUri != null) {
            playVideo(Uri.parse(savedUri));
        }
        Intent intent = getIntent();
        taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        btnChonVideo = findViewById(R.id.btnChonVideo);
        playerView = findViewById(R.id.playerView);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        tvTitle = findViewById(R.id.tvTitle);
        btnSuaVideo = findViewById(R.id.btnSuaVideo);



        // Xử lý fullscreen toggle
        btnFullscreen.setOnClickListener(view -> {
            if (isFullScreen) {
                // Thoát fullscreen
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                tvTitle.setVisibility(View.VISIBLE);
                btnChonVideo.setVisibility(View.VISIBLE);
                btnSuaVideo.setVisibility(View.VISIBLE);
                showSystemUI();
                isFullScreen = false;
            } else {
                // Vào fullscreen
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                tvTitle.setVisibility(View.GONE);
                btnChonVideo.setVisibility(View.GONE);
                btnSuaVideo.setVisibility(View.GONE);
                hideSystemUI();
                isFullScreen = true;
            }
        });

        // Khởi tạo ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // Chọn video
        btnChonVideo.setOnClickListener(v -> openVideoPicker());

        // Sửa video
        btnSuaVideo.setOnClickListener(v -> {
            if (exoPlayer.getCurrentMediaItem() != null) {
                Uri currentUri = Uri.parse(exoPlayer.getCurrentMediaItem().playbackProperties.uri.toString());
                Intent intent1 = new Intent(this, ChinhSuaVideoActivity.class);
                intent1.putExtra("video_uri", currentUri.toString()); // ✅ dùng toString() rõ ràng
                intent1.putExtra("taiKhoan", taiKhoan);
                startActivity(intent1);
            } else {
                Toast.makeText(this, "Chưa có video nào để chỉnh sửa!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            if (input == null) return null;

            String fileName = "video_" + System.currentTimeMillis() + ".mp4";
            File outFile = new File(getCacheDir(), fileName);
            FileOutputStream output = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.close();
            input.close();

            return outFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        chonVideoLauncher.launch(intent);
    }
    private void playVideo(Uri videoUri) {
        try {
            // Kiểm tra có thể mở stream không
            getContentResolver().openInputStream(videoUri).close(); // nếu mở được là tồn tại

            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play();

            String fileName = videoUri.getLastPathSegment();
            tvTitle.setText("🎬 " + fileName);

            Toast.makeText(this, "🎬 Đang phát video", Toast.LENGTH_SHORT).show();

            // Lưu lại videoUri
            getSharedPreferences("video_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("saved_video_uri", videoUri.toString())
                    .apply();
        } catch (Exception e) {
            Toast.makeText(this, "⚠ Video không còn tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isFullScreen = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}
