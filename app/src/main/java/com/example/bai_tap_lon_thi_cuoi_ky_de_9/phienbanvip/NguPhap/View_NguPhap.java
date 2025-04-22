package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.NguPhap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

public class View_NguPhap extends AppCompatActivity {
    TextView view_ten,view_CauKD,view_CauPD,view_CauNV,view_CachDung,view_ChuY;
    String ten;
    Database DB = new Database(this);
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_nguphap);
        view_ten=(TextView)findViewById(R.id.view_Ten);
        view_CauKD=(TextView)findViewById(R.id.view_CauKD);
        view_CauPD=(TextView)findViewById(R.id.view_CauPD);
        view_CauNV=(TextView)findViewById(R.id.view_CauNV);
        view_CachDung=(TextView)findViewById(R.id.view_CachDung);
        view_ChuY=(TextView)findViewById(R.id.view_ChuY);
        //Đọc dữ liệu từ csdl lên
        Intent in=getIntent();
        Bundle bundle=getIntent().getExtras();
        ten=bundle.getString("Ten");
        String ten=bundle.getString("Ten");
        //hiện tiêu đề;
        ActionBar ab = getSupportActionBar();
        //set mầu cho actionBar
        ab.setTitle("Ngữ Pháp");

        //Hiện nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(ten!=null){
            Cursor cursor = DB.getCursor("select * from Gramar where Ten='"+ten+"'");
            cursor.moveToFirst();
            view_ten.setText(cursor.getString(1));
            view_CauKD.setText(cursor.getString(2));
            view_CauPD.setText(cursor.getString(3));
            view_CauNV.setText(cursor.getString(4));
            view_CachDung.setText(cursor.getString(5));
            view_ChuY.setText(cursor.getString(6));
        }
    }
}
