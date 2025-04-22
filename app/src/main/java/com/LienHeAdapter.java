package com;

import android.app.Activity;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class LienHeAdapter extends ArrayAdapter {
    Activity context;
    int resource;
    ArrayList<LienHe> listLienHe;
    public LienHeAdapter(Activity context,int resource,ArrayList<LienHe> listLienHe){
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.listLienHe = listLienHe;
    }

    @Override
    public int getCount() {
        return listLienHe.size();
    }
    public  ArrayList<LienHe> getListLienHe() {
        return  listLienHe;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View customView = inflater.inflate(resource,null);
        ImageView imgAnhDaiDien = customView.findViewById(R.id.imgAnhDaiDien);
        TextView tvTenLienHe = customView.findViewById(R.id.tvTenLienHe);
        TextView tvSoDienThoai = customView.findViewById(R.id.tvSoDienThoai);
        TextView tvEmail = customView.findViewById(R.id.tvEmail);
        LienHe lienHe = listLienHe.get(position);
        if(lienHe.getAnhDaiDien().trim().length() > 0){
            Glide.with(context.getBaseContext()).load(lienHe.getAnhDaiDien()).into(imgAnhDaiDien);
        }


        tvTenLienHe.setText(lienHe.getTenLienHe());
        tvSoDienThoai.setText(lienHe.getSoDienThoai());
        tvEmail.setText(lienHe.getEmail());
        return customView;
    }
}
