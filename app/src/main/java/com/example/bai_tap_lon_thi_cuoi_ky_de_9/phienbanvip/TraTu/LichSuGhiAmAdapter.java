package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Database1.LuuDangBaiNgheSQLite;
import com.Model.DangBaiNghe;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LuyenNgheChiTietDoanHoiThoaiActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TTSVoiceSelectionActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LichSuGhiAmAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> filePaths;
    private LayoutInflater inflater;
    ArrayList<String> loaiCauList;
    String textContent;
    private TaiKhoan taiKhoan;
    ArrayList<String> textContentList; // THÊM DÒNG NÀY
    public LichSuGhiAmAdapter(Context context, ArrayList<String> filePaths, ArrayList<String> loaiCauList, String textContent, TaiKhoan taiKhoan, ArrayList<String> textContentList) {
        this.context = context;
        this.filePaths = filePaths;
        this.loaiCauList = loaiCauList;
        this.textContent = textContent;
        this.taiKhoan = taiKhoan;
        this.textContentList = textContentList;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return filePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return filePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvFileName,  tvLoaiCau;
        Button btnDelete, btnBatDau;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_ghi_am, parent, false);
            holder = new ViewHolder();
            holder.tvFileName = convertView.findViewById(R.id.tvFileName);

            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            holder.btnBatDau = convertView.findViewById(R.id.btnBatDau);
            holder.tvLoaiCau = convertView.findViewById(R.id.tvLoaiCau);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String path = filePaths.get(position);
        File file = new File(path);
        holder.tvLoaiCau.setText("Loại câu: " + loaiCauList.get(position));
        holder.tvFileName.setText(file.getName());
        // 👉 Đặt lại listener mỗi lần getView
        holder.btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc chắn muốn xoá file này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        Log.d("DELETE_DEBUG", "===> Bắt đầu xử lý xóa ở vị trí: " + position);

                        LuuDangBaiNgheSQLite db = new LuuDangBaiNgheSQLite(context);
                        String fileName = file.getName();

                        boolean fileExisted = file.exists();
                        boolean fileDeleted = false;

                        if (fileExisted) {
                            fileDeleted = file.delete();
                            Log.d("DELETE_DEBUG", "Đã xóa file khỏi bộ nhớ: " + file.getAbsolutePath());
                        } else {
                            Log.e("DELETE_DEBUG", "File KHÔNG TỒN TẠI: " + file.getAbsolutePath());
                        }

                        boolean dbDeleted = db.deleteByFileName(fileName, taiKhoan.getId());
                        Log.d("DELETE_DEBUG", "Xoá DB: " + fileName + " → " + dbDeleted);

                        if (fileDeleted || dbDeleted) {
                            filePaths.remove(position);
                            loaiCauList.remove(position);
                            textContentList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Đã xóa khỏi danh sách!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Không thể xoá!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });





        holder.btnBatDau.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Chọn giọng đọc");

            String[] options = {"Anh - Mỹ", "Anh - Anh"};
            builder.setItems(options, (dialog, which) -> {
                String accent = (which == 0) ? "us" : "uk";
                String loaiCau = loaiCauList.get(position);
                String textDaLuu = textContentList.get(position);
                String filePath = filePaths.get(position);

                // 👇 THÊM đoạn này mới chính xác tuyệt đối:
                LuuDangBaiNgheSQLite db = new LuuDangBaiNgheSQLite(context);
                DangBaiNghe currentItem = db.getDangBaiNgheByFileName(new File(filePath).getName(), taiKhoan.getId());
                String genderJson = currentItem != null ? currentItem.getSpeakerGenders() : "";

                Intent intent;
                if ("Hội thoại".equalsIgnoreCase(loaiCau)) {
                    //intent = new Intent(context, LuyenNgheChiTietDoanHoiThoaiActivity.class);
                    intent = new Intent(context, TichDoanVanHoiThoai.class);
                } else {
                    intent = new Intent(context, LuyenNgheChiTietActivity.class);
                }

                intent.putExtra("filePath", filePath);
                intent.putExtra("textContent", textDaLuu);
                intent.putExtra("taiKhoan", taiKhoan);
                intent.putExtra("accent", accent);
                intent.putExtra("speakerGenders", genderJson); // ✅ Chính xác

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

            builder.show();
        });




        return convertView;
    }
    public ArrayList<String> getFilePaths() {
        return filePaths;
    }

    public ArrayList<String> getLoaiCauList() {
        return loaiCauList;
    }

    public ArrayList<String> getTextContentList() {
        return textContentList;
    }

}
