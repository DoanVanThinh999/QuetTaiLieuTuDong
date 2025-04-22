package com.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ChiTietThongTinTaiKhoanActivity;
import com.Database1.MySQLite;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class TaiKhoanAdapter extends ArrayAdapter {
    Context context;
    int resource;
    ArrayList<TaiKhoan> listTaiKhoan;
    MySQLite mySQLite;
    public TaiKhoanAdapter(Context ctx,int res,ArrayList<TaiKhoan> lisTK){
        super(ctx,res);
        this.context = ctx;
        this.resource = res;
        this.listTaiKhoan = lisTK;
        mySQLite = new MySQLite(context, 1);
    }
    public int getCount(){
        return listTaiKhoan.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View customView = layoutInflater.inflate(resource,null);
        //
        TextView tvMaVaTenDN = customView.findViewById(R.id.tvMaVaTenDN);
        TextView tvHoTen = customView.findViewById(R.id.tvHoTen);
        TextView tvVaiTro = customView.findViewById(R.id.tvVaiTro);
        ImageView imgSuaTaiKhoan = customView.findViewById(R.id.imgSuaTaiKhoan);
        TaiKhoan taiKhoan = listTaiKhoan.get(position);
        tvMaVaTenDN.setText(taiKhoan.getId() + " - " + taiKhoan.getTenDangNhap());
        imgSuaTaiKhoan.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChiTietThongTinTaiKhoanActivity.class);

            intent.putExtra("tK", taiKhoan);

            context.startActivity(intent);
        });
        tvHoTen.setText(taiKhoan.getHoTen());
        if(taiKhoan.getVaiTro() == 1){
            tvVaiTro.setText("Quản trị viên");
        }else if(taiKhoan.getVaiTro() == 2){
            tvVaiTro.setText("Giáo viên");
        }
        else if(taiKhoan.getVaiTro() == 3) {
            tvVaiTro.setText("Học sinh");
        }
        else if(taiKhoan.getVaiTro() == 4) {
            tvVaiTro.setText("Trợ giảng");
        }
        else if(taiKhoan.getVaiTro() == 5) {
            tvVaiTro.setText("Tổng đài");
        }
        else{
            tvVaiTro.setText("Vai trò không tồn tại");
        }

        return customView;

    }
}
