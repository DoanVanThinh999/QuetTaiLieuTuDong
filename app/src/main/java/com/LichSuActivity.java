package com;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Database1.MySQLite;
import com.Model.LichSu;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class LichSuActivity extends AppCompatActivity {
    ListView lvLichSu;
    LichSuAdapter lichSuAdapter;
    ArrayList<LichSu> lichSuList;
    MySQLite mySQLite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);
        lvLichSu = findViewById(R.id.lvLichSu);
        mySQLite = new MySQLite(this, 1);

        // Lấy danh sách lịch sử từ cơ sở dữ liệu
        lichSuList = mySQLite.xemLichSu();

        // Cài đặt Adapter
        lichSuAdapter = new LichSuAdapter(this, R.layout.item_lich_su, lichSuList);
        lvLichSu.setAdapter(lichSuAdapter);
        hienThiLichSu();
    }
    private void hienThiLichSu() {
        // Gọi danh sách lịch sử từ cơ sở dữ liệu
        ArrayList<LichSu> danhSachLichSu = mySQLite.xemLichSu();

        // Cài đặt Adapter
        lichSuAdapter = new LichSuAdapter(this, R.layout.item_lich_su, danhSachLichSu);
        lvLichSu.setAdapter(lichSuAdapter);
    }
}
