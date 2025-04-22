package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TracNghiemTuVideoActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private Uri videoUri;
    TaiKhoan taiKhoan;
    private HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc;
    private String xuLyKhiSai;
    private int phanTramDiemDung;
    private int soLanXem = Integer.MAX_VALUE;
    String phuongThucDiem;
    private int soLanTraLoiLai = Integer.MAX_VALUE;
    private int truDiemMoiLan = 0;
    private int soLanXemHienTai = 0;
    private long[] sortedMarkers;
    private int currentMarkerIndex = 0;
    double soCauDung = 0; // ✅ từ int → double
    int tongCau = 0;

    private Set<Long> daTraLoiMoc = new HashSet<>();
    private Handler handler = new Handler();
    private static final int REQUEST_CODE_TRA_LOI = 1001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trac_nghiem_video);

        playerView = findViewById(R.id.playerView);

        Intent intent = getIntent();
        videoUri = Uri.parse(intent.getStringExtra("video_uri"));
        taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        cauHoiTheoMoc = (HashMap<Long, ArrayList<CauHoi>>) intent.getSerializableExtra("cau_hoi_theo_moc");
        xuLyKhiSai = intent.getStringExtra("xu_ly_khi_sai");
        phanTramDiemDung = intent.getIntExtra("phan_tram_diem_dung", 0);
        String soLanXemStr = intent.getStringExtra("so_lan_xem");
        soLanXem = soLanXemStr.equals("Không giới hạn") ? Integer.MAX_VALUE : Integer.parseInt(soLanXemStr);

        phuongThucDiem = intent.getStringExtra("phuong_thuc_diem");

        if (xuLyKhiSai.equals("Trả lời lại")) {
            String lanTraLoiLaiStr = intent.getStringExtra("so_lan_tra_loi_lai");
            soLanTraLoiLai = lanTraLoiLaiStr.equals("Vô hạn") ? Integer.MAX_VALUE : Integer.parseInt(lanTraLoiLaiStr);
            truDiemMoiLan = intent.getIntExtra("tru_diem_moi_lan", 0);
        }
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri));
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    // ✔✔ KHI VIDEO XEM HẾT 1 LẦN LÀ KẾT THÚc
                    soLanXemHienTai++; // ✅ BẮT BUỘC PHẢI CÓ
                    DatabaseHelper db = new DatabaseHelper(TracNghiemTuVideoActivity.this);
                    db.tangSoLanXem(taiKhoan.getId(), videoUri.toString());
                    double diem = ((double) soCauDung / tongCau) * 100;
                    db.saveDiemLanXem(taiKhoan.getId(), videoUri.toString(), db.getSoLanXem(taiKhoan.getId(), videoUri.toString()), diem);
                    Intent intent = new Intent(TracNghiemTuVideoActivity.this, ManHinhKetThucActivity.class);
                    intent.putExtra("tong_cau", tongCau);
                    intent.putExtra("so_cau_dung", soCauDung);
                    intent.putExtra("video_uri", videoUri.toString());
                    intent.putExtra("tai_khoan", taiKhoan);

                    // ✔ Truyền lại cấu hình
                    intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
                    intent.putExtra("so_lan_xem", String.valueOf(soLanXem));
                    intent.putExtra("phuong_thuc_diem", phuongThucDiem);
                    intent.putExtra("phan_tram_diem_dung", phanTramDiemDung);
                    intent.putExtra("so_lan_tra_loi_lai", String.valueOf(soLanTraLoiLai));
                    intent.putExtra("tru_diem_moi_lan", truDiemMoiLan);

                    startActivity(intent);
                    finish();
                }
            }
        });




        sortedMarkers = cauHoiTheoMoc.keySet().stream().sorted().mapToLong(Long::longValue).toArray();
        // luôn update marker khi hiển thị/ẩn thanh điều khiển
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> updateMarkers());

        // Gọi ngay khi khởi tạo
        updateMarkers();
        handler.postDelayed(checkMarkerRunnable, 300);
        checkHoanThanhVaKetThuc();
    }
    private void checkHoanThanhVaKetThuc() {
        if (daTraLoiMoc.size() == sortedMarkers.length && soLanXemHienTai >= soLanXem) {
            double diem = ((double) soCauDung / tongCau) * 100;
            DatabaseHelper db = new DatabaseHelper(this);
            int soLanDaXem = db.getSoLanXem(taiKhoan.getId(), videoUri.toString());
            db.saveDiemLanXem(taiKhoan.getId(), videoUri.toString(), soLanDaXem + 1, diem);

            Intent intent = new Intent(this, ManHinhKetThucActivity.class); // hoặc class nào bạn muốn
            intent.putExtra("tong_cau", tongCau);
            intent.putExtra("so_cau_dung", soCauDung);
            intent.putExtra("video_uri", videoUri.toString());
            intent.putExtra("tai_khoan", taiKhoan);

            // ✅ TRUYỀN THÊM TOÀN BỘ CẤU HÌNH GỐC:
            intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
            intent.putExtra("so_lan_xem", String.valueOf(soLanXem));
            intent.putExtra("phuong_thuc_diem", phuongThucDiem);
            intent.putExtra("phan_tram_diem_dung", phanTramDiemDung);
            intent.putExtra("so_lan_tra_loi_lai", String.valueOf(soLanTraLoiLai));
            intent.putExtra("tru_diem_moi_lan", truDiemMoiLan);
            startActivity(intent);
            finish();
        }
    }

    private boolean dangTraLoiCauHoi = false;

    private int lastCheckedMarkerIndex = -1;

    private final Runnable checkMarkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (dangTraLoiCauHoi) {
                handler.postDelayed(this, 300);
                return;
            }

            if (exoPlayer == null || !exoPlayer.isPlaying()) {
                handler.postDelayed(this, 300);
                return;
            }

            long currentPosition = exoPlayer.getCurrentPosition();

            // Duyệt từng mốc, chứ không chỉ currentMarkerIndex
            for (int i = 0; i < sortedMarkers.length; i++) {
                long marker = sortedMarkers[i];

                if (daTraLoiMoc.contains(marker)) continue;

                if (Math.abs(currentPosition - marker) <= 700) {
                    Log.d("QUIZ_MARKER", "🎯 Hiển thị câu hỏi tại mốc: " + marker);
                    dangTraLoiCauHoi = true;
                    currentMarkerIndex = i; // cập nhật chính xác mốc đang hỏi

                    exoPlayer.pause();

                    Intent intent = new Intent(TracNghiemTuVideoActivity.this, TraLoiCauHoiActivity.class);
                    intent.putExtra("video_uri", videoUri.toString());
                    intent.putExtra("marker_time", marker);
                    intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc.get(marker));
                    intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
                    intent.putExtra("phan_tram_diem_dung", phanTramDiemDung);
                    intent.putExtra("taiKhoan", taiKhoan);
                    intent.putExtra("so_lan_tra_loi_lai", soLanTraLoiLai);
                    intent.putExtra("tru_diem_moi_lan", truDiemMoiLan);
                    startActivityForResult(intent, REQUEST_CODE_TRA_LOI);
                    return;
                }

                // Nếu vượt quá mốc mà chưa trả lời → Seek lại mốc
                if (currentPosition > marker + 1500 && !daTraLoiMoc.contains(marker)) {
                    Log.w("QUIZ_SKIP", "⛔ Vượt mốc " + marker + " chưa trả lời → Seek lại");
                    exoPlayer.seekTo(marker - 200); // seek nhẹ về trước mốc
                    break;
                }
            }

            handler.postDelayed(this, 300);
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TRA_LOI && resultCode == RESULT_OK && data != null) {
            int cauDung = data.getIntExtra("so_cau_dung", 0);
            int tong = data.getIntExtra("tong_cau", 0);
            long marker = data.getLongExtra("marker_time", -1);
            boolean dung = data.getBooleanExtra("tra_loi_dung", false); // 🔥 THÊM DÒNG NÀY

            if (!dung && xuLyKhiSai.equals("Quay lại mốc")) {
                if (currentMarkerIndex <= 0) {
                    exoPlayer.seekTo(0); // Nếu đang ở mốc đầu tiên → tua lại từ đầu
                    Log.d("MARKER_BACK", "Sai ở mốc đầu tiên → xem lại từ đầu");
                } else {
                    currentMarkerIndex--; // Lùi về mốc trước
                    long markerTruoc = sortedMarkers[currentMarkerIndex];
                    exoPlayer.seekTo(markerTruoc - 300); // Seek nhẹ về trước 300ms
                    Log.d("MARKER_BACK", "Sai → quay lại mốc trước: " + markerTruoc);
                }
                dangTraLoiCauHoi = false;
                handler.postDelayed(checkMarkerRunnable, 300);
                return;
            }

            Log.d("MARKER_DEBUG", "Đang ở mốc: " + currentMarkerIndex + " - " + marker);
            Log.d("MARKER_DEBUG_BanDau", "➡️ Số câu đúng: " + cauDung + " / " + tong);
            double diemCong = 0;
            if (cauDung == tong) {
                diemCong = 1;
            } else if (cauDung > 0) {
                diemCong = (cauDung / (double) tong) + (phanTramDiemDung / 100.0) * ((tong - cauDung) / (double) tong);
            }

            Log.d("MARKER_DEBUG_DIEM_CONG1", "➕ Điểm cộng lần này: " + diemCong);
            Log.d("MARKER_DEBUG_DIEM_CONG2", "⏱️ Trước cộng: soCauDung = " + soCauDung + ", tongCau = " + tongCau);


            soCauDung += diemCong;
            tongCau += tong;
            Log.d("MARKER_DEBUG_SAU_CONG", "✅ Sau cộng: soCauDung = " + soCauDung + ", tongCau = " + tongCau);
            daTraLoiMoc.add(marker);
            currentMarkerIndex++;

            dangTraLoiCauHoi = false; // ✅ đánh dấu đã xử lý xong mốc này
            updateMarkers(); // ← Gọi khi trả lời xong câu hỏi
            exoPlayer.play();
            // ✅ Check nếu đã trả lời hết + hết lượt xem
            checkHoanThanhVaKetThuc();
            handler.postDelayed(checkMarkerRunnable, 300); // đảm bảo tiếp tục check
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void updateMarkers() {
        PlayerControlView controlView = playerView.findViewById(androidx.media3.ui.R.id.exo_controller);
        if (controlView != null) {
            DefaultTimeBar timeBar = controlView.findViewById(androidx.media3.ui.R.id.exo_progress);
            if (timeBar != null) {
                int markerCount = sortedMarkers.length;
                long[] adMarkers = new long[markerCount];
                boolean[] playedAdGroups = new boolean[markerCount];

                for (int i = 0; i < markerCount; i++) {
                    adMarkers[i] = sortedMarkers[i];
                    playedAdGroups[i] = false; // Luôn hiển thị marker
                }

                timeBar.setAdGroupTimesMs(adMarkers, playedAdGroups, markerCount);
                timeBar.setAdMarkerColor(Color.parseColor("#1E7D22"));
                timeBar.invalidate(); // Refresh giao diện ngay lập tức
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) exoPlayer.release();
        handler.removeCallbacksAndMessages(null);
    }
}