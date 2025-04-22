package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Adapter.CauHoiAdapter;
import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.Arrays;

public class DanhSachCauHoiActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<CauHoi> danhSachCauHoi = new ArrayList<>();
    private int totalQuestions;
    private long markerTime;
    private String videoUriStr;
    private Button btnCapNhat;
    TaiKhoan taiKhoan;
    ArrayList<String> danhSachNoiDung; // Dùng để lọc
    private static final int REQUEST_CODE_EDIT = 3000;
    private boolean isMultiDeleteMode = false;
    private CauHoiAdapter adapter;
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    TextView tvSoLuongDaChon;
    LinearLayout layoutChucNangXoaNhieu ;
    Button btnChonTatCa ;
    Button btnBoChonTatCa;
    ImageView imgThem;
    AutoCompleteTextView edtTimKiem;
    @SuppressLint({"MissingInflatedId", "Range"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_cau_hoi);
        imgThem = findViewById(R.id.imgThem);
        tvSoLuongDaChon = findViewById(R.id.tvSoLuongDaChon);
        btnChonTatCa = findViewById(R.id.btnChonTatCa);
        btnBoChonTatCa = findViewById(R.id.btnBoChonTatCa);
        btnCapNhat = findViewById(R.id.btnCapNhatCauHoi);
        layoutChucNangXoaNhieu = findViewById(R.id.layoutChucNangXoaNhieu);
        layoutChucNangXoaNhieu.setVisibility(View.GONE);
        tvSoLuongDaChon.setVisibility(View.GONE);
        listView = findViewById(R.id.listViewCauHoi);
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        ImageView btnXoaCauHoiDaChon = findViewById(R.id.btnXoaCauHoiDaChon);
        btnChonTatCa.setOnClickListener(v -> {
            if (!isMultiDeleteMode) return; // chỉ hoạt động khi đang trong chế độ xoá nhiều

            for (int i = 0; i < listView.getCount(); i++) {
                listView.setItemChecked(i, true);
            }
            adapter.notifyDataSetChanged(); // 💡 THÊM VÀO CUỐI mỗi btnChonTatCa / btnBoChonTatCa

            tvSoLuongDaChon.setText("📝 Đã chọn: " + listView.getCheckedItemCount());

        });
        btnBoChonTatCa.setOnClickListener(v -> {
            if (!isMultiDeleteMode) return; // chỉ hoạt động khi đang trong chế độ xoá nhiều

            for (int i = 0; i < listView.getCount(); i++) {
                listView.setItemChecked(i, false);
            }
            adapter.notifyDataSetChanged(); // 💡 THÊM VÀO CUỐI mỗi btnChonTatCa / btnBoChonTatCa
            tvSoLuongDaChon.setText("📝 Đã chọn: " + listView.getCheckedItemCount());
        });
        int selectedCount = listView.getCheckedItemCount();
        tvSoLuongDaChon.setText("Đã chọn: " + selectedCount);
        btnXoaCauHoiDaChon.setOnClickListener(v -> {
            if (!isMultiDeleteMode) {
                // 👉 Bắt đầu chế độ chọn nhiều
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                isMultiDeleteMode = true;
                adapter.setMultiDeleteMode(true); // 💡 THÊM VÀO ĐÂY
                btnXoaCauHoiDaChon.setImageResource(R.drawable.check); // đổi thành icon xác nhận
                layoutChucNangXoaNhieu.setVisibility(View.VISIBLE);
                tvSoLuongDaChon.setVisibility(View.VISIBLE);
                tvSoLuongDaChon.setText("📝 Đã chọn: 0");
                Toast.makeText(this, "👉 Chọn các câu hỏi cần xoá", Toast.LENGTH_SHORT).show();

            } else {
                // 👉 Xác nhận xoá
                selectedItems.clear();
                for (int i = 0; i < listView.getCount(); i++) {
                    if (listView.isItemChecked(i)) {
                        selectedItems.add(i);
                    }
                }

                if (selectedItems.isEmpty()) {
                    Toast.makeText(this, "⚠️ Chưa chọn câu hỏi nào!", Toast.LENGTH_SHORT).show();

                    // 👉 Reset về trạng thái bình thường luôn
                    listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                    isMultiDeleteMode = false;
                    btnXoaCauHoiDaChon.setImageResource(R.drawable.delete);
                    tvSoLuongDaChon.setVisibility(View.GONE);
                    layoutChucNangXoaNhieu.setVisibility(View.GONE);
                    adapter.setMultiDeleteMode(false); // ✅

                    return;
                }


                new AlertDialog.Builder(this)
                        .setTitle("❌ Xác nhận xoá?")
                        .setMessage("Bạn muốn xoá " + selectedItems.size() + " câu hỏi đã chọn?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            for (int i = selectedItems.size() - 1; i >= 0; i--) {
                                danhSachCauHoi.remove((int) selectedItems.get(i));
                            }
                            updateListView();

                            // 🔁 Reset lại trạng thái
                            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                            isMultiDeleteMode = false;
                            btnXoaCauHoiDaChon.setImageResource(R.drawable.delete); // trở lại icon thùng rác
                            adapter.setMultiDeleteMode(false); // 💡 THÊM VÀO ĐÂY
                            tvSoLuongDaChon.setVisibility(View.GONE);
                            layoutChucNangXoaNhieu.setVisibility(View.GONE);
                            Toast.makeText(this, "✅ Đã xoá!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });


        imgThem.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("➕ Nhập số lượng câu muốn thêm");

            final String[] soLuongOptions = {"1", "2", "3", "4", "5"};
            builder.setItems(soLuongOptions, (dialog, which) -> {
                int soLuongThem = Integer.parseInt(soLuongOptions[which]);
                int viTriBatDau = danhSachCauHoi.size(); // 👉 Bắt đầu từ câu kế tiếp

                for (int i = 0; i < soLuongThem; i++) {
                    Intent intent = new Intent(this, ChonLoaiCauHoiActivity.class);
                    intent.putExtra("marker_time", markerTime);
                    intent.putExtra("video_uri", videoUriStr);
                    intent.putExtra("total_questions", totalQuestions + soLuongThem);
                    intent.putExtra("current_index", viTriBatDau + i);
                    intent.putExtra("ds_cau_hoi", danhSachCauHoi);
                    intent.putExtra("taiKhoan",taiKhoan);
                    startActivity(intent);
                }

                finish(); // Đóng activity hiện tại để refresh lại sau khi thêm
            });

            builder.show();
        });
        edtTimKiem = findViewById(R.id.edtTimKiem);
        danhSachNoiDung = new ArrayList<>();
        DatabaseHelper db = new DatabaseHelper(this);
        ArrayList<String> lichSuTimKiem = db.getSearchHistory(taiKhoan.getId());
        ArrayAdapter<String> lichSuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lichSuTimKiem);
        ((AutoCompleteTextView) edtTimKiem).setAdapter(lichSuAdapter);
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();

                if (!query.trim().isEmpty()) {
                    db.saveSearchHistory(taiKhoan.getId(), query); // ✅ dùng db chứ không phải gọi trực tiếp
                }

                ArrayList<CauHoi> danhSachLoc = new ArrayList<>();
                for (CauHoi ch : danhSachCauHoi) {
                    if (ch.noiDung != null && ch.noiDung.toLowerCase().contains(query)) {
                        danhSachLoc.add(ch);
                    }
                }

                ArrayList<String> danhSachLocNoiDung = new ArrayList<>();
                for (CauHoi ch : danhSachLoc) {
                    String cau = (ch.noiDung != null && !ch.noiDung.trim().isEmpty()) ? ch.noiDung : "(Chưa nhập)";
                    String display = "Câu " + (danhSachCauHoi.indexOf(ch) + 1) + ": " + cau;
                    danhSachLocNoiDung.add(display);
                }

                adapter = new CauHoiAdapter(DanhSachCauHoiActivity.this, danhSachLocNoiDung, listView, isMultiDeleteMode);
                listView.setAdapter(adapter);
            }
        });



        // 🔍 Lấy thông tin videoUri & markerTime
        videoUriStr = getIntent().getStringExtra("video_uri");
        markerTime = getIntent().getLongExtra("marker_time", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 1);

        if (videoUriStr == null) {
            Toast.makeText(this, "❌ video_uri bị null!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 📥 Load câu hỏi từ DB

        Cursor cursor = db.getCauHoiTheoVideoVaMoc(videoUriStr, markerTime);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                CauHoi ch = new CauHoi();
                ch.noiDung = cursor.getString(cursor.getColumnIndex("noi_dung"));
                ch.quizType = cursor.getString(cursor.getColumnIndex("quiz_type"));
                ch.chiSoDung = cursor.getInt(cursor.getColumnIndex("chi_so_dung"));

                String dapAnListStr = cursor.getString(cursor.getColumnIndex("dap_an_list"));
                String dapAnDungListStr = cursor.getString(cursor.getColumnIndex("dap_an_dung_list"));

                ch.dapAnList = new ArrayList<>(Arrays.asList(dapAnListStr.split(",")));

                ch.dapAnDungList = new ArrayList<>();
                for (String b : dapAnDungListStr.split(",")) {
                    ch.dapAnDungList.add(Boolean.parseBoolean(b.trim()));
                }

                danhSachCauHoi.add(ch);
            } while (cursor.moveToNext());
            cursor.close();
        }





        updateListView();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isMultiDeleteMode) {
                int x = listView.getCheckedItemCount();
                tvSoLuongDaChon.setText("📝 Đã chọn: " + x);
            } else {
                // Nếu không phải chế độ xoá, thì chỉnh sửa như cũ
                CauHoi cauHoi = danhSachCauHoi.get(position);
                Intent intent = new Intent(this, NhapCauHoiActivity.class);
                intent.putExtra("current_index", position);
                intent.putExtra("total_questions", totalQuestions);
                intent.putExtra("marker_time", markerTime);
                intent.putExtra("quiz_type", getQuizTypeFromCauHoi(cauHoi));
                intent.putExtra("edit_mode", true);
                intent.putExtra("ds_cau_hoi", danhSachCauHoi);
                intent.putExtra("video_uri", videoUriStr);
                intent.putExtra("taiKhoan",taiKhoan);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });

        btnCapNhat.setOnClickListener(v -> handleCapNhatCauHoi());
        updateListView(); // cập nhật `danhSachNoiDung`
    }
    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Xoá câu hỏi");

        builder.setMessage("Bạn có chắc muốn xoá câu hỏi này không?\n" + danhSachCauHoi.get(position).noiDung);

        builder.setPositiveButton("Xoá", (dialog, which) -> {
            danhSachCauHoi.remove(position);
            updateListView();
            Toast.makeText(this, "Đã xoá câu hỏi!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Huỷ", null);
        builder.show();
    }

    private void updateListView() {
        danhSachNoiDung.clear();
        for (int i = 0; i < danhSachCauHoi.size(); i++) {
            CauHoi ch = danhSachCauHoi.get(i);
            String cau = (ch != null && ch.noiDung != null && !ch.noiDung.trim().isEmpty()) ? ch.noiDung : "(Chưa nhập)";
            String display = "Câu " + (i + 1) + ": " + cau;
            danhSachNoiDung.add(display);
        }
        adapter = new CauHoiAdapter(this, danhSachNoiDung, listView, isMultiDeleteMode);
        listView.setAdapter(adapter);
        adapter.setMultiDeleteMode(isMultiDeleteMode); // 💡 THÊM VÀO ĐÂY
        // Nếu không ở chế độ xoá nhiều, tắt chọn nhiều
        if (!isMultiDeleteMode) {
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        if (isMultiDeleteMode) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            tvSoLuongDaChon.setVisibility(View.VISIBLE);
            layoutChucNangXoaNhieu.setVisibility(View.VISIBLE);
            tvSoLuongDaChon.setText("📝 Đã chọn: " + listView.getCheckedItemCount());
        } else {
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            tvSoLuongDaChon.setVisibility(View.GONE);
            layoutChucNangXoaNhieu.setVisibility(View.GONE);
        }

    }

    private String getQuizTypeFromCauHoi(CauHoi ch) {
        return ch.quizType != null ? ch.quizType : "SINGLE_CHOICE";
    }
    private void handleCapNhatCauHoi() {
        DatabaseHelper db = new DatabaseHelper(this);

        // Kiểm tra xem có ít nhất 1 câu hỏi hợp lệ không
        boolean hasValidCauHoi = false;
        for (CauHoi c : danhSachCauHoi) {
            if (c != null && c.noiDung != null && !c.noiDung.trim().isEmpty()) {
                hasValidCauHoi = true;
                break;
            }
        }

        // Nếu không có câu hỏi hợp lệ, vẫn trả về để không mất dữ liệu
        if (!hasValidCauHoi) {
            Toast.makeText(this, "Không có câu hỏi nào để cập nhật", Toast.LENGTH_SHORT).show();

            Intent result = new Intent();
            result.putExtra("marker_time", markerTime);
            result.putExtra("ds_cau_hoi", danhSachCauHoi);
            result.putExtra("taiKhoan", taiKhoan);
            result.putExtra("video_uri", videoUriStr);
            setResult(RESULT_OK, result);
            finish();
            return;
        }

        // Lưu bình thường nếu có chỉnh sửa
        Cursor existingCursor = db.getCauHoiTheoVideoVaMoc(videoUriStr, markerTime);
        ArrayList<String> existingNoiDungList = new ArrayList<>();

        if (existingCursor != null && existingCursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String existingNoiDung = existingCursor.getString(existingCursor.getColumnIndex("noi_dung"));
                existingNoiDungList.add(existingNoiDung);
            } while (existingCursor.moveToNext());
            existingCursor.close();
        }

        for (int i = 0; i < danhSachCauHoi.size(); i++) {
            CauHoi c = danhSachCauHoi.get(i);
            if (c.noiDung != null && !c.noiDung.trim().isEmpty()) {
                String dapAnList = String.join(",", c.dapAnList != null ? c.dapAnList : new ArrayList<>());
                String dapAnDungList = c.dapAnDungList != null ? c.dapAnDungList.toString() : "";
                db.upsertCauHoiTheoMoc(markerTime, videoUriStr, c.noiDung, c.quizType, dapAnList, dapAnDungList, c.chiSoDung, i);
            }
        }

        db.removeDuplicateQuestionsByVideoAndMarker(videoUriStr, markerTime);

        // Trả kết quả về ChinhSuaVideoActivity
        Intent result = new Intent();
        result.putExtra("marker_time", markerTime);
        result.putExtra("ds_cau_hoi", danhSachCauHoi);
        result.putExtra("taiKhoan", taiKhoan);
        result.putExtra("video_uri", videoUriStr);
        setResult(RESULT_OK, result);
        finish();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK && data != null) {
            int currentIndex = data.getIntExtra("current_index", -1);
            CauHoi updated = (CauHoi) data.getSerializableExtra("cau_hoi");
            if (currentIndex != -1 && updated != null) {
                danhSachCauHoi.set(currentIndex, updated);
                updateListView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
