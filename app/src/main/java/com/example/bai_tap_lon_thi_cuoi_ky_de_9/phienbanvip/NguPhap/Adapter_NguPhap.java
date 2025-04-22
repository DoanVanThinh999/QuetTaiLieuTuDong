package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.NguPhap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class Adapter_NguPhap extends ArrayAdapter<NguPhap> {
    private Context context;
    private int resource;
    private ArrayList<NguPhap> arr;
    private ArrayAdapter<NguPhap>adp;
    public Adapter_NguPhap(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<NguPhap> arr) {
        super(context, resource, arr);
        this.context=context;
        this.resource=resource;
        this.arr=arr;
    }
    /*Thiết lập Hứng giá trị từ main*/
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView= LayoutInflater.from(context).inflate(R.layout.item_nguphap,parent,false);
        ImageView imageView=(ImageView)convertView.findViewById(R.id.imgNguPhap);
        final TextView txtTenNguPhap=(TextView)convertView.findViewById(R.id.txt_TenNguPhap);
        NguPhap nguPhap=arr.get(position);
        txtTenNguPhap.setText(nguPhap.getTen());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(v.getContext(),View_NguPhap.class);
                Bundle bundle=new Bundle();
                bundle.putString("Ten",txtTenNguPhap.getText().toString());
                in.putExtras(bundle);
                context.startActivity(in);
            }
        });
        return convertView;
    }
}
