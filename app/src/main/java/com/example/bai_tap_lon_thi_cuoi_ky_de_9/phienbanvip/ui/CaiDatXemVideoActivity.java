package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CaiDatXemVideoActivity extends AppCompatActivity {
    private Spinner spinnerSoLanXem, spinnerTinhDiem, spinnerXuLySai;
    private SeekBar seekBarPhanTramDiem;
    private TextView tvPhanTram;
    private Button btnBatDau, btnXuatViDeoChoMnXem;
    private String videoUri;
    private TaiKhoan taiKhoan;
    HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc;
    private Spinner spinnerSoLanTraLoiLai;
    private SeekBar seekBarTruDiem;
    private TextView tvTruDiem;
    private LinearLayout layoutTraLoiLaiOptions;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat_xem_video);
        spinnerSoLanTraLoiLai = findViewById(R.id.spinnerSoLanTraLoiLai);
        seekBarTruDiem = findViewById(R.id.seekBarTruDiem);
        tvTruDiem = findViewById(R.id.tvTruDiem);
        layoutTraLoiLaiOptions = findViewById(R.id.layoutTraLoiLaiOptions);
        btnXuatViDeoChoMnXem = findViewById(R.id.btnXuatViDeoChoMnXem);


// Hiện ẩn tùy chọn theo spinner xử lý sai

        spinnerSoLanXem = findViewById(R.id.spinnerSoLanXem);
        spinnerTinhDiem = findViewById(R.id.spinnerTinhDiem);
        spinnerXuLySai = findViewById(R.id.spinnerXuLySai);
        seekBarPhanTramDiem = findViewById(R.id.seekBarPhanTram);
        tvPhanTram = findViewById(R.id.tvPhanTram);
        btnBatDau = findViewById(R.id.btnBatDau);

        videoUri = getIntent().getStringExtra("video_uri");
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        cauHoiTheoMoc = (HashMap<Long, ArrayList<CauHoi>>) getIntent().getSerializableExtra("cau_hoi_theo_moc");

        // Khởi tạo spinner
        String[] lanXemOptions = {"1", "2", "3", "Không giới hạn"};
        String[] phuongThucTinhDiem = {"Lần đầu tiên", "Điểm cao nhất", "Trung bình các lần"};
        String[] xuLyKhiSai = {"Trả lời lại", "Quay lại mốc", "Xem đáp án rồi tiếp tục"};

        spinnerSoLanXem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lanXemOptions));
        spinnerTinhDiem.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, phuongThucTinhDiem));
        spinnerXuLySai.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, xuLyKhiSai));

        seekBarPhanTramDiem.setMax(100);
        seekBarPhanTramDiem.setProgress(30); // mặc định 30%
        tvPhanTram.setText("Cho điểm phần đúng: 30%");

        seekBarPhanTramDiem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPhanTram.setText("Cho điểm phần đúng: " + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        spinnerXuLySai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                layoutTraLoiLaiOptions.setVisibility(selected.equals("Trả lời lại") ? View.VISIBLE : View.GONE);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        // Tạo danh sách số lần cho phép: 1–10 + "Vô hạn"
        ArrayList<String> lanTraLoiOptions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) lanTraLoiOptions.add(String.valueOf(i));
        lanTraLoiOptions.add("Vô hạn");
        spinnerSoLanTraLoiLai.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lanTraLoiOptions));

        seekBarTruDiem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTruDiem.setText("Trừ " + progress + "% mỗi lần sai");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnBatDau.setOnClickListener(v -> {
            Intent intent = new Intent(this, TracNghiemTuVideoActivity.class);
            intent.putExtra("video_uri", videoUri);
            intent.putExtra("taiKhoan", taiKhoan);
            DatabaseHelper db = new DatabaseHelper(this);
            HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUri);
            intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);
            intent.putExtra("so_lan_xem", spinnerSoLanXem.getSelectedItem().toString());
            intent.putExtra("phuong_thuc_diem", spinnerTinhDiem.getSelectedItem().toString());
            intent.putExtra("xu_ly_khi_sai", spinnerXuLySai.getSelectedItem().toString());
            intent.putExtra("phan_tram_diem_dung", seekBarPhanTramDiem.getProgress());
            if (spinnerXuLySai.getSelectedItem().toString().equals("Trả lời lại")) {
                intent.putExtra("so_lan_tra_loi_lai", spinnerSoLanTraLoiLai.getSelectedItem().toString());
                intent.putExtra("tru_diem_moi_lan", seekBarTruDiem.getProgress());
            }
            startActivity(intent);
        });
        btnXuatViDeoChoMnXem.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(this);
            String videoUriStr = videoUri.toString();
            boolean laChinhSua = db.daTonTaiVideoPublic(videoUriStr);

            if (laChinhSua) {
                // ✅ Nếu đã có → chỉ cập nhật, không cần hỏi lại tên
                HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUriStr);

                // Cấu hình hiện tại
                String soLanXem = spinnerSoLanXem.getSelectedItem().toString();
                String phuongThucDiem = spinnerTinhDiem.getSelectedItem().toString();
                String xuLyKhiSaiValue = spinnerXuLySai.getSelectedItem().toString();
                int phanTramDung = seekBarPhanTramDiem.getProgress();

                double diemLuu = phuongThucDiem.equals("Lần đầu tiên") ? db.getDiemLanXem(taiKhoan.getId(), videoUriStr, 1)
                        : phuongThucDiem.equals("Điểm cao nhất") ? db.getDiemCaoNhat(taiKhoan.getId(), videoUriStr)
                        : db.getDiemTrungBinh(taiKhoan.getId(), videoUriStr);

                String tenVideoCu = db.getStringFieldFromVideoPublic(videoUriStr, "video_title");

                boolean success = db.capNhatVideoPublic(
                        videoUriStr,
                        tenVideoCu, // ✅ Giữ nguyên tên cũ
                        soLanXem,
                        phuongThucDiem,
                        xuLyKhiSaiValue,
                        phanTramDung,
                        diemLuu,
                        xuLyKhiSaiValue.equals("Trả lời lại") ? Integer.parseInt(spinnerSoLanTraLoiLai.getSelectedItem().toString()) : null,
                        xuLyKhiSaiValue.equals("Trả lời lại") ? seekBarTruDiem.getProgress() : null
                );

                if (success) {
                    Toast.makeText(this, "✅ Đã cập nhật video!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, PublicVideoListActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan); // ✅ BỔ SUNG DÒNG NÀY
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "❌ Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }

            } else {
                // ✅ Chưa có → phải nhập tên
                EditText input = new EditText(this);
                input.setHint("Nhập tên video");

                new AlertDialog.Builder(this)
                        .setTitle("Đặt tên cho video")
                        .setView(input)
                        .setPositiveButton("Lưu", (dialog, which) -> {
                            String tenVideo = input.getText().toString().trim();
                            if (tenVideo.isEmpty()) {
                                Toast.makeText(this, "❗ Vui lòng nhập tên video!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUriStr);
                            String soLanXem = spinnerSoLanXem.getSelectedItem().toString();
                            String phuongThucDiem = spinnerTinhDiem.getSelectedItem().toString();
                            String xuLyKhiSaiValue = spinnerXuLySai.getSelectedItem().toString();
                            int phanTramDung = seekBarPhanTramDiem.getProgress();

                            double diemLuu = phuongThucDiem.equals("Lần đầu tiên") ? db.getDiemLanXem(taiKhoan.getId(), videoUriStr, 1)
                                    : phuongThucDiem.equals("Điểm cao nhất") ? db.getDiemCaoNhat(taiKhoan.getId(), videoUriStr)
                                    : db.getDiemTrungBinh(taiKhoan.getId(), videoUriStr);

                            boolean success = db.themVideoPublic(
                                    videoUriStr,
                                    taiKhoan.getId(),
                                    phuongThucDiem,
                                    soLanXem,
                                    phanTramDung,
                                    xuLyKhiSaiValue,
                                    diemLuu,
                                    cauHoiTheoMoc,
                                    tenVideo,
                                    Integer.parseInt(spinnerSoLanTraLoiLai.getSelectedItem().toString()),
                                    seekBarTruDiem.getProgress()
                            );

                            if (success) {
                                Toast.makeText(this, "✅ Đã lưu video mới!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, PublicVideoListActivity.class);
                                intent.putExtra("taiKhoan", taiKhoan); // ✅ BỔ SUNG DÒNG NÀY
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "❌ Lỗi khi lưu video mới!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });
    }
}
