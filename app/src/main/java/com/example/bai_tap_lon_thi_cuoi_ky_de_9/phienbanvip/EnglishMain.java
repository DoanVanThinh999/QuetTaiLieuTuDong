package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.LuyenPhatAmActivity;
import com.LuyenPhatAmThiNghiemActivity;
import com.LuyenPhatAmThiNghiemTichHopGemmiActivity;
import com.Model.TaiKhoan;
import com.QuanLyTaiKhoanActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.DangKyActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.MainActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LichSuTest.LichSuTest;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.NguPhap.Main_NguPhap;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Note.MainNote;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question.Main_Question;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu.GoCapTocNhanhBanPhimActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu.LuyenNgheChepChinhTaActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu.Search_tran;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu.Tra_Nhieu_Tu;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui.TaoVideoHuongDanNguoiDungActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.phat_am;

import java.io.IOException;
import java.util.ArrayList;


public class EnglishMain extends AppCompatActivity {

    ListView listView;
    Database DB = new Database(this);
    TextView tvTaiKhoanKH;
    ArrayList<String> arrayList = null;
    private FirebaseAuth mAuth;

    String[] item = {"Tra Từ Điển","Dịch Anh-Viet", "Luyện Tập", "Ghi Chú", "Ngữ Pháp", "Lịch sử Luyện Tập", "Luyện nghe","Luyện phát âm","Đăng xuất","Thí nghiệm phần nói"
    ,"Luyện nghe chép chính tả","Gõ nhanh bàn phím","Quét tài liệu tự động","Tạo video học liệu"};
    Integer[] icon = {R.drawable.timkiem,R.drawable.search ,R.drawable.tets, R.drawable.ghi_chu, R.drawable.book2, R.drawable.book,R.drawable.baseline_hearing_24,R.drawable.noi, R.drawable.exit,R.drawable.book3,R.drawable.english,R.drawable.go_cap_toc
    ,R.drawable.baseline_image_24,R.drawable.baseline_ondemand_video_24};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");

        if (tvTaiKhoanKH != null) {
            tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }

        try {
            DB.CopySDCard();
        } catch (IOException e) {
            e.printStackTrace();
        }

        arrayList = new ArrayList<>();
        //duyet mang
        for (String menu: item)
        {
            arrayList.add(menu);
        }
        ActionBar actionBar =getSupportActionBar();
        //set mau cho ActionBar

        actionBar.setTitle("KHOÁ HỌC TIẾNG ANH");
        CustomListView adapter = new CustomListView(this, arrayList, icon);
        listView = findViewById(R.id.listItem);
        listView.setAdapter(adapter);
        //chuyen man hinh nhan CSDL
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                {
                    Intent intent = new Intent(getApplication(), Search_tran.class);
                    //Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if(position == 1)
                {
                    Intent intent = new Intent(getApplication(), Tra_Nhieu_Tu.class);
                    //Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }

                if(position == 2)
                {
                    Intent intent = new Intent(getApplication(), Main_Question.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }

                if (position == 3) {
                    Intent in = new Intent(getApplication(), MainNote.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(in);
                }

                if(position == 4)
                {
                    Intent intent = new Intent(getApplication(), Main_NguPhap.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }

                if (position == 5) {
                    Intent intent = new Intent(getApplication(), LichSuTest.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 6) {
                    Intent intent = new Intent(getApplication(), phat_am.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 7) {
                    Intent intent = new Intent(getApplication(), LuyenPhatAmActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 8) {
                    mAuth.signOut();
                    Intent intentQuanLyTK = new Intent(getApplication(), MainActivity.class);
                    intentQuanLyTK.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intentQuanLyTK);
                    finish();
                }
                if (position == 9) {
                    Intent intent = new Intent(getApplication(), LuyenPhatAmThiNghiemTichHopGemmiActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 10) {
                    Intent intent = new Intent(getApplication(), LuyenNgheChepChinhTaActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 11) {
                    Intent intent = new Intent(getApplication(), GoCapTocNhanhBanPhimActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 12) {
                    Intent intent = new Intent(getApplication(), QuetTaiLieuTuDongSuDungActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }
                if (position == 13) {
                    Intent intent = new Intent(getApplication(), TaoVideoHuongDanNguoiDungActivity.class);
                    intent.putExtra("taiKhoan", taiKhoan);
                    startActivity(intent);
                }

            }
        });
    }
}