package com;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.Database1.MySQLite;
import com.Model.LichSu;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.DangKyActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.MainActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.List;

public class LichSuAdapter extends ArrayAdapter<LichSu> {

    // Constructor nhận context, layout resource, và danh sách các đối tượng
    public LichSuAdapter(Context context, int resource, List<LichSu> objects) {
        super(context, resource, objects);  // gọi constructor của ArrayAdapter
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Nếu convertView null, tạo mới view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_lich_su, parent, false);
        }

        // Lấy đối tượng LichSu tại vị trí position
        LichSu lichSu = getItem(position);

        // Ánh xạ các TextView từ layout
        TextView tvThoiGian = convertView.findViewById(R.id.tvThoiGian);
        TextView tvHanhDong = convertView.findViewById(R.id.tvHanhDong);
        TextView tvChiTiet = convertView.findViewById(R.id.tvChiTiet);
        ImageView imgXoaLS =convertView.findViewById(R.id.imgXoaLS);
        imgXoaLS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LichSuAdapter.this.getContext());
                builder.setTitle("Xác nhận xoá");
                builder.setMessage("Bạn chắc chắn xoá sự kiện này?");

                builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                    if (lichSu != null) {
                        // Xóa lịch sử từ cơ sở dữ liệu
                        MySQLite mySQLite = new MySQLite(getContext(), 1);
                        mySQLite.querySQL("DELETE FROM LICH_SU_TAI_KHOAN WHERE id = " + lichSu.getId() + ";");

                        // Xóa khỏi danh sách và cập nhật ListView
                        remove(lichSu);
                        notifyDataSetChanged();

                        Toast.makeText(getContext(), "Đã xóa sự kiện lịch sử", Toast.LENGTH_SHORT).show();
                    }

                });

                builder.setNegativeButton("Hủy", (dialog, which) -> {
                    // Hủy bỏ, đóng hộp thoại
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        // Kiểm tra lichSu có null không trước khi gán giá trị
        if (lichSu != null) {
            tvThoiGian.setText(lichSu.getThoiGian());
            tvHanhDong.setText(lichSu.getHanhDong());
            tvChiTiet.setText(lichSu.getChiTiet());
        }

        // Trả về convertView đã được ánh xạ dữ liệu
        return convertView;
    }
}