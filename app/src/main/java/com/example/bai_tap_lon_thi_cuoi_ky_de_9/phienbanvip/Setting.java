package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

public class Setting extends AppCompatActivity {
    Button btxoaTest, btxoasTratu;
    TextView textView;
    Database DB = new Database(this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ActionBar ab = getSupportActionBar();
        //set mầu cho actionBar
        ab.setTitle("Setting");
        //Hiện nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //
        btxoasTratu = (Button) findViewById(R.id.btxoatratu);
        btxoaTest = (Button) findViewById(R.id.btxoaTest);
        textView = (TextView) findViewById(R.id.tvThongtin);
        btxoasTratu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.ExecuteSQL("DELETE FROM LichSuTraTu");
                Toast.makeText(Setting.this, "Xóa Thành công", Toast.LENGTH_SHORT).show();
            }
        });
        btxoaTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.ExecuteSQL("DELETE FROM LichSuTest");
                Toast.makeText(Setting.this, "Xóa Thành công", Toast.LENGTH_SHORT).show();
            }
        });
        String s = "\n"+"\nPhiên Bản: 1.0"+"\n"+"";
        textView.setText(s);
    }
}
