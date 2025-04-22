// PublicVideoListActivity.java
package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PublicVideoListActivity extends AppCompatActivity {

    private ListView listViewVideos;
    private Button btnThemVideo;
    private ArrayList<String> videoTitles = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> videoUris = new ArrayList<>();
    private TaiKhoan taiKhoan;
    private boolean isDeleteMode = false;
    private static final int REQUEST_CODE_THEM_VIDEO = 777;
    ImageView imgXoa, imgSetting;
    private int selectedPosition = -1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_video_list);
        imgXoa = findViewById(R.id.imgXoa);
        listViewVideos = findViewById(R.id.listViewPublicVideos);
        btnThemVideo = findViewById(R.id.btnThemVideo);
        imgSetting = findViewById(R.id.imgSetting);
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoTitles);
        listViewVideos.setAdapter(adapter);
        listViewVideos.setChoiceMode(ListView.CHOICE_MODE_NONE); // ban đầu không cho chọn
        btnThemVideo.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaoVideoHuongDanNguoiDungActivity.class);
            intent.putExtra("taiKhoan", taiKhoan);
            startActivityForResult(intent, REQUEST_CODE_THEM_VIDEO);
        });

        listViewVideos.setOnItemClickListener((adapterView, view, i, l) -> {
            if (isDeleteMode) return;  // ⛔ BỎ QUA nếu đang ở chế độ xoá
            selectedPosition = i; // ✅ Lưu vị trí dòng được chọn
            String videoUri = videoUris.get(i);
            DatabaseHelper db = new DatabaseHelper(this);
            HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUri);
            String soLanXem = db.getStringFieldFromVideoPublic(videoUri, "so_lan_xem");
            String phuongThucDiem = db.getStringFieldFromVideoPublic(videoUri, "phuong_thuc_diem");
            String xuLyKhiSai = db.getStringFieldFromVideoPublic(videoUri, "xu_ly_khi_sai");
            int phanTramDiemDung = db.getIntFieldFromVideoPublic(videoUri, "phan_tram_diem_dung");

            Intent intent = new Intent(this, PublicVideoActivity.class);
            intent.putExtra("video_uri", videoUri);
            intent.putExtra("taiKhoan", taiKhoan);
            intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);
            intent.putExtra("so_lan_xem", soLanXem);
            intent.putExtra("phuong_thuc_diem", phuongThucDiem);
            intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
            intent.putExtra("phan_tram_diem_dung", phanTramDiemDung);
            if (xuLyKhiSai.equals("Trả lời lại")) {
                intent.putExtra("so_lan_tra_loi_lai", db.getStringFieldFromVideoPublic(videoUri, "so_lan_tra_loi_lai"));
                intent.putExtra("tru_diem_moi_lan", db.getIntFieldFromVideoPublic(videoUri, "tru_diem_moi_lan"));
            }
            startActivity(intent);
        });
        imgXoa.setOnClickListener(v -> {
            if (!isDeleteMode) {
                // Bật chế độ xoá nhiều
                isDeleteMode = true;
                // chuyển sang layout có checkbox
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, videoTitles);
                listViewVideos.setAdapter(adapter);
                listViewVideos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                imgXoa.setImageResource(R.drawable.check); // đổi icon sang ✔️
                Toast.makeText(this, "📌 Chọn video cần xoá rồi ấn ✔ để xác nhận", Toast.LENGTH_SHORT).show();
            } else {
                // Xác nhận xoá
                new AlertDialog.Builder(this)
                        .setTitle("Xoá video?")
                        .setMessage("Bạn có chắc muốn xoá các video đã chọn?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            SparseBooleanArray checked = listViewVideos.getCheckedItemPositions();
                            DatabaseHelper db = new DatabaseHelper(this);

                            for (int i = videoTitles.size() - 1; i >= 0; i--) {
                                if (checked.get(i)) {
                                    String uri = videoUris.get(i);
                                    db.getWritableDatabase().delete("video_public", "video_uri = ?", new String[]{uri});
                                    videoTitles.remove(i);
                                    videoUris.remove(i);
                                    // ✅ Nếu đây là video đang lưu trong SharedPreferences thì xoá luôn
                                    String savedUri = getSharedPreferences("video_prefs", MODE_PRIVATE)
                                            .getString("saved_video_uri", null);
                                    if (savedUri != null && savedUri.equals(uri)) {
                                        getSharedPreferences("video_prefs", MODE_PRIVATE)
                                                .edit().remove("saved_video_uri").apply();
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();
                            listViewVideos.clearChoices();
                            listViewVideos.requestLayout();
                            listViewVideos.clearChoices();
                            listViewVideos.setChoiceMode(isDeleteMode
                                    ? ListView.CHOICE_MODE_MULTIPLE
                                    : ListView.CHOICE_MODE_NONE);

                            imgXoa.setImageResource(R.drawable.delete); // đổi lại icon thùng rác
                            isDeleteMode = false;

                            Toast.makeText(this, "🗑 Đã xoá!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Huỷ", (dialog, which) -> {
                            listViewVideos.clearChoices();
                            listViewVideos.setChoiceMode(isDeleteMode
                                    ? ListView.CHOICE_MODE_MULTIPLE
                                    : ListView.CHOICE_MODE_NONE);

                            imgXoa.setImageResource(R.drawable.delete);
                            isDeleteMode = false;
                        })
                        .show();
            }
        });
        imgSetting.setOnClickListener(v -> {
            if (isDeleteMode) {
                // Tắt chế độ xoá
                isDeleteMode = false;
                listViewVideos.clearChoices();
                listViewVideos.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                imgXoa.setImageResource(R.drawable.delete);
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoTitles);
                listViewVideos.setAdapter(adapter);
            }

            String[] options = {"✏️ Đổi tên video", "🛠️ Quay lại chỉnh sửa video"};
            new AlertDialog.Builder(this)
                    .setTitle("Chọn chức năng")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Toast.makeText(this, "📌 Chọn một video để đổi tên", Toast.LENGTH_SHORT).show();
                            listViewVideos.setOnItemClickListener((adapterView, view, i, l) -> {
                                String videoUri = videoUris.get(i);
                                DatabaseHelper db = new DatabaseHelper(this);

                                EditText input = new EditText(this);
                                input.setHint("Nhập tên mới");

                                new AlertDialog.Builder(this)
                                        .setTitle("Đổi tên video")
                                        .setView(input)
                                        .setPositiveButton("Lưu", (d, w) -> {
                                            String newTitle = input.getText().toString().trim();
                                            if (!newTitle.isEmpty()) {
                                                db.getWritableDatabase().execSQL(
                                                        "UPDATE video_public SET video_title = ? WHERE video_uri = ?",
                                                        new Object[]{newTitle, videoUri});
                                                loadDanhSachVideo();
                                                Toast.makeText(this, "✅ Đã đổi tên!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "❗ Tên không được để trống", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("Huỷ", null)
                                        .show();

                                // Khôi phục lại item click bình thường
                                khoiPhucItemClickMacDinh();
                            });
                        } else if (which == 1) {
                            Toast.makeText(this, "📌 Chọn video để quay lại chỉnh sửa", Toast.LENGTH_SHORT).show();
                            listViewVideos.setOnItemClickListener((adapterView, view, i, l) -> {
                                String videoUri = videoUris.get(i);
                                DatabaseHelper db = new DatabaseHelper(this);
                                HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUri);

                                Intent intent = new Intent(this, ChinhSuaVideoActivity.class);
                                intent.putExtra("video_uri", videoUri);
                                intent.putExtra("taiKhoan", taiKhoan);
                                intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);
                                startActivity(intent);

                                khoiPhucItemClickMacDinh(); // Khôi phục sau khi chỉnh sửa
                            });
                        }
                    }).show();
        });

        loadDanhSachVideo();
    }
    private void khoiPhucItemClickMacDinh() {
        listViewVideos.setOnItemClickListener((adapterView, view, i, l) -> {
            if (isDeleteMode) return;
            selectedPosition = i;
            String videoUri = videoUris.get(i);
            DatabaseHelper db = new DatabaseHelper(this);
            HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc = db.getTatCaCauHoiTheoMoc(videoUri);
            String soLanXem = db.getStringFieldFromVideoPublic(videoUri, "so_lan_xem");
            String phuongThucDiem = db.getStringFieldFromVideoPublic(videoUri, "phuong_thuc_diem");
            String xuLyKhiSai = db.getStringFieldFromVideoPublic(videoUri, "xu_ly_khi_sai");
            int phanTramDiemDung = db.getIntFieldFromVideoPublic(videoUri, "phan_tram_diem_dung");

            Intent intent = new Intent(this, PublicVideoActivity.class);
            intent.putExtra("video_uri", videoUri);
            intent.putExtra("taiKhoan", taiKhoan);
            intent.putExtra("cau_hoi_theo_moc", cauHoiTheoMoc);
            intent.putExtra("so_lan_xem", soLanXem);
            intent.putExtra("phuong_thuc_diem", phuongThucDiem);
            intent.putExtra("xu_ly_khi_sai", xuLyKhiSai);
            intent.putExtra("phan_tram_diem_dung", phanTramDiemDung);
            if (xuLyKhiSai.equals("Trả lời lại")) {
                intent.putExtra("so_lan_tra_loi_lai", db.getStringFieldFromVideoPublic(videoUri, "so_lan_tra_loi_lai"));
                intent.putExtra("tru_diem_moi_lan", db.getIntFieldFromVideoPublic(videoUri, "tru_diem_moi_lan"));
            }
            startActivity(intent);
        });
    }

    private void loadDanhSachVideo() {
        DatabaseHelper db = new DatabaseHelper(this);
        videoTitles.clear();
        videoUris.clear();
        ArrayList<String[]> danhSach = db.getAllVideoUriWithTitle(); // ← Đúng rồi

        for (String[] pair : danhSach) {
            String uri = pair[0];
            String title = pair[1];

            Log.d("DEBUG_VIDEO_LIST", "✅ URI = " + uri + ", TITLE = " + title); // Log debug

            videoUris.add(uri);
            videoTitles.add("🎬: " + (title == null || title.isEmpty() ? "Không tên" : title)); // Gộp thêm fallback
        }
        if (!isDeleteMode) {
            listViewVideos.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        adapter.notifyDataSetChanged();


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_THEM_VIDEO && resultCode == RESULT_OK) {
            Toast.makeText(this, "✅ Video đã được thêm!", Toast.LENGTH_SHORT).show();
            loadDanhSachVideo();
        }
    }
}
