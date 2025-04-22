package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CatVideoActivity extends AppCompatActivity {
    private Uri videoUri;
    private TextView tvVideoInfo;
    private EditText etStartTime, etEndTime;
    private Button btnCatVideo, btnLuuVideo;
    private String inputVideoPath;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private ExoPlayer exoPlayer;
    private final java.util.Stack<String> historyStack = new java.util.Stack<>();
    private Button btnKhoiPhucDoanTruocDo;
    private PlayerView playerViewXemLai;
    private ProgressBar progressBarXemLaiDoanCat;
    private TextView tvSaveIcon;
    private ExoPlayer previewPlayer;
    private String previewVideoPath = ""; // Đường dẫn file preview được sinh ra
    private SeekBar seekBarPreview;
    boolean isUpdatingProgress = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_video);
        
        videoUri = Uri.parse(getIntent().getStringExtra("video_uri"));
        tvVideoInfo = findViewById(R.id.tvVideoInfo);
        etStartTime = findViewById(R.id.etStartTime);
        seekBarPreview = findViewById(R.id.seekBarPreview);
        seekBarPreview.setVisibility(View.GONE);
        btnKhoiPhucDoanTruocDo = findViewById(R.id.btnKhoiPhucDoanTruocDo);
        btnKhoiPhucDoanTruocDo.setVisibility(View.GONE); // Ẩn ban đầu

        btnKhoiPhucDoanTruocDo.setOnClickListener(v -> {
            if (!historyStack.isEmpty()) {
                inputVideoPath = historyStack.pop(); // Lấy video trước đó
                playVideo(inputVideoPath);
                hienThiThongTinVideo(inputVideoPath);

                Toast.makeText(this, historyStack.isEmpty()
                        ? "🗃 Đã khôi phục về video ban đầu!"
                        : "↩️ Đã khôi phục phiên bản trước đó", Toast.LENGTH_SHORT).show();

                btnLuuVideo.setVisibility(View.VISIBLE); // luôn cho phép lưu lại sau khi khôi phục
            } else {
                Toast.makeText(this, "📦 Không còn bản khôi phục nào", Toast.LENGTH_SHORT).show();
            }
        });
        etEndTime = findViewById(R.id.etEndTime);
        btnCatVideo = findViewById(R.id.btnCatVideo);
        btnLuuVideo = findViewById(R.id.btnLuuVideo);
        progressBar = findViewById(R.id.progressBar);
        PlayerView playerView = findViewById(R.id.playerView);
        playerViewXemLai = findViewById(R.id.playerViewXemLai);
        progressBarXemLaiDoanCat = findViewById(R.id.progressBarXemLaiDoanCat);
        tvSaveIcon = findViewById(R.id.tvSaveIcon);
        tvSaveIcon.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xác nhận cắt")
                    .setMessage("Bạn có chắc muốn cắt đoạn đã preview không?")
                    .setPositiveButton("Cắt", (dialog, which) -> {
                        // Gọi lại catVideo() thật sự
                        tvSaveIcon.setVisibility(View.GONE);
                        playerViewXemLai.setVisibility(View.GONE);
                        if (previewPlayer != null) previewPlayer.release();
                        progressBarXemLaiDoanCat.setVisibility(View.GONE);
                        seekBarPreview.setVisibility(View.GONE);
                        isUpdatingProgress = false; // stop thread update
                        // Gọi hàm cắt thật:
                        thucHienCatVideoThucSu();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });


// Ẩn ban đầu
        playerViewXemLai.setVisibility(View.GONE);
        progressBarXemLaiDoanCat.setVisibility(View.GONE);
        tvSaveIcon.setVisibility(View.GONE);

        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        executorService.execute(() -> {
            inputVideoPath = copyUriToFile(videoUri);
            runOnUiThread(() -> {
                hienThiThongTinVideo(inputVideoPath);
                playVideo(inputVideoPath);
            });
        });
        btnCatVideo.setOnClickListener(v -> catVideo());
        btnLuuVideo.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("cutVideoUri", Uri.fromFile(new File(inputVideoPath)).toString());
            setResult(RESULT_OK, intent);
            finish();
        });
    }
    private void extractAudioToWav(String videoPath, File outputWav, Runnable onFinish) {
        String cmd = "-y -i \"" + videoPath + "\" -ac 1 -ar 44100 -vn -f wav \"" + outputWav.getAbsolutePath() + "\"";
        executorService.execute(() -> {
            Session session = FFmpegKit.execute(cmd);
            runOnUiThread(() -> {
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    onFinish.run();
                } else {
                    Toast.makeText(this, "❌ Trích xuất audio thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void catVideo() {
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thời gian bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }
        double startSec = convertTimeToSeconds(startTime);
        double endSec = convertTimeToSeconds(endTime);
        if (startSec >= endSec) {
            Toast.makeText(this, "Thời gian kết thúc phải lớn hơn bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(this, "Đang tạo preview", "Vui lòng chờ...", true);

        File cachePreview = new File(getCacheDir(), "preview_cut.mp4");
        previewVideoPath = cachePreview.getAbsolutePath();

        String cmdPreview = "-y -i \"" + inputVideoPath + "\" -ss " + startTime + " -to " + endTime + " -c copy \"" + previewVideoPath + "\"";

        executorService.execute(() -> {
            Session session = FFmpegKit.execute(cmdPreview);
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    // Hiển thị playerViewXemLai
                    playerViewXemLai.setVisibility(View.VISIBLE);
                    tvSaveIcon.setVisibility(View.VISIBLE);
                    playPreview(previewVideoPath);
                    Toast.makeText(this, "🎬 Xem trước đoạn cắt, nhấn 💾 để xác nhận", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ Tạo preview thất bại", Toast.LENGTH_LONG).show();
                }
            });
        });


    }
    private void thucHienCatVideoThucSu(){
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thời gian bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }
        double startSec = convertTimeToSeconds(startTime);
        double endSec = convertTimeToSeconds(endTime);
        if (startSec >= endSec) {
            Toast.makeText(this, "Thời gian kết thúc phải lớn hơn bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        File cacheDir = getCacheDir();
        File partBefore = new File(cacheDir, "before_cut.mp4");
        File partAfter = new File(cacheDir, "after_cut.mp4");
        File concatFile = new File(cacheDir, "concat.txt");
        String finalOutput = getExternalFilesDir(null) + "/video_final_" + getTimeStamp() + ".mp4";
        executorService.execute(() -> {
            try {
                historyStack.push(inputVideoPath); // lưu lại trước khi cắt
                boolean hasBeforePart = startSec > 0;
                // 1. Cắt phần trước
                if (hasBeforePart) {
                    String cmd1 = "-y -i \"" + inputVideoPath + "\" -t " + startTime + " -c copy \"" + partBefore.getAbsolutePath() + "\"";
                    Session s1 = FFmpegKit.execute(cmd1);
                    Log.d("FFMPEG-CMD1", s1.getAllLogsAsString());
                    if (!ReturnCode.isSuccess(s1.getReturnCode())) throw new RuntimeException("❌ Cắt phần trước thất bại");
                }
                // 2. Cắt phần sau
                String cmd2 = "-y -i \"" + inputVideoPath + "\" -ss " + endTime + " -c copy \"" + partAfter.getAbsolutePath() + "\"";
                Session s2 = FFmpegKit.execute(cmd2);
                Log.d("FFMPEG-CMD2", s2.getAllLogsAsString());
                if (!ReturnCode.isSuccess(s2.getReturnCode())) throw new RuntimeException("❌ Cắt phần sau thất bại");
                // 3. Ghi concat.txt
                FileOutputStream fos = new FileOutputStream(concatFile);
                if (hasBeforePart) fos.write(("file '" + partBefore.getAbsolutePath().replace("'", "'\\''") + "'\n").getBytes());
                fos.write(("file '" + partAfter.getAbsolutePath().replace("'", "'\\''") + "'\n").getBytes());
                fos.close();
                // 4. Ghép lại
                String cmd3 = "-y -f concat -safe 0 -i \"" + concatFile.getAbsolutePath() + "\" -c copy \"" + finalOutput + "\"";
                FFmpegKit.executeAsync(cmd3, session -> runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (ReturnCode.isSuccess(session.getReturnCode())) {
                        File out = new File(finalOutput);
                        if (out.exists() && out.length() > 0) {
                            inputVideoPath = finalOutput;
                            playVideo(finalOutput);
                            hienThiThongTinVideo(finalOutput);
                            btnKhoiPhucDoanTruocDo.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "✅ Đã cắt bỏ đoạn " + startTime + " → " + endTime, Toast.LENGTH_LONG).show();
                            btnLuuVideo.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "❌ File sau khi ghép không tồn tại", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("FFMPEG-CMD3-FAIL", session.getAllLogsAsString());
                        Toast.makeText(this, "❌ Ghép video thất bại", Toast.LENGTH_LONG).show();
                    }
                }));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "⚠️ Lỗi trong quá trình cắt video", Toast.LENGTH_SHORT).show();
                });
            }
        });

    }
    private void playPreview(String path) {
        if (previewPlayer != null) previewPlayer.release();

        previewPlayer = new ExoPlayer.Builder(this).build();
        playerViewXemLai.setPlayer(previewPlayer);

        MediaItem item = MediaItem.fromUri(Uri.fromFile(new File(path)));
        previewPlayer.setMediaItem(item);
        previewPlayer.prepare();
        previewPlayer.play();

        // Hiện progressBar khi xem preview
        progressBarXemLaiDoanCat.setVisibility(View.VISIBLE);
        seekBarPreview.setVisibility(View.VISIBLE);

        // Update tiến trình theo video
        isUpdatingProgress = true;
        executorService.execute(() -> {
            while (isUpdatingProgress && previewPlayer != null) {
                runOnUiThread(() -> {
                    if (previewPlayer != null && previewPlayer.getDuration() > 0) {
                        long position = previewPlayer.getCurrentPosition();
                        long duration = previewPlayer.getDuration();
                        int progressPercent = (int) (100.0 * position / duration);
                        progressBarXemLaiDoanCat.setProgress(progressPercent);
                    }
                });

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
            }
        });


        // Cho phép seek bằng SeekBar
        seekBarPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && previewPlayer != null && previewPlayer.getDuration() > 0) {
                    long seekTo = (long) (progress / 100.0 * previewPlayer.getDuration());
                    previewPlayer.seekTo(seekTo);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Auto tua lại đầu khi hết
        previewPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    previewPlayer.seekTo(0);
                    previewPlayer.pause();
                }
            }
        });
    }



    private double convertTimeToSeconds(String timeString) {
        try {
            String[] parts = timeString.split(":");
            if (parts.length != 3) return 0;
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return hours * 3600 + minutes * 60 + seconds;
        } catch (Exception e) {
            return 0;
        }
    }
    private void playVideo(String path) {
        Uri uri = Uri.fromFile(new File(path));
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void hienThiThongTinVideo(String path) {
        executorService.execute(() -> {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(path);
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long durationMillis = Long.parseLong(durationStr);
                long durationSeconds = durationMillis / 1000;
                runOnUiThread(() -> tvVideoInfo.setText("Độ dài video: " + durationSeconds + " giây"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Không thể lấy thông tin video", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private String copyUriToFile(Uri uri) {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            String fileName = getFileNameFromUri(uri);
            File tempFile = new File(getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int len;
            long totalRead = 0;
            long fileSize = resolver.openAssetFileDescriptor(uri, "r").getLength();
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
                totalRead += len;
                int progress = (int) (100.0 * totalRead / fileSize);
                runOnUiThread(() -> progressBar.setProgress(progress));
            }
            outputStream.close();
            inputStream.close();
            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "Không thể xử lý video", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
            return "";
        }
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String name = "temp_video.mp4";
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) name = cursor.getString(nameIndex);
            cursor.close();
        }
        return name;
    }
    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) exoPlayer.release();
    }
}