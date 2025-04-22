package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LichSuTest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question.Adapter_Question;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question.Main_Question;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question.Question;

import java.util.ArrayList;

public class LichSuHien extends AppCompatActivity {
    String id, idcheck;
    Adapter_Question adapter_question;
    ListView listView;
    ArrayList<Question> arrTest = null;
    ArrayList<Integer> arr = null;
    Database DB = new Database(this);
    Chronometer time;
    RadioButton rA, rB, rC, rD;
    Main_Question mainQuestion = new Main_Question();
    TextView tvTaiKhoanKH;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lvquiz);
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");

        if (tvTaiKhoanKH != null) {
            tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }
        listView = (ListView) findViewById(R.id.lvquiz);
        arrTest = new ArrayList<Question>();
        //
        time = (Chronometer) findViewById(R.id.chronometer2);
        //
        Bundle b = getIntent().getExtras();
        id = b.getString("ID");
        String t = b.getString("Time");
        idcheck = b.getString("IDCheck");
        time.setText(t);
        //
        arr = new ArrayList<Integer>();
        adapter_question = new Adapter_Question(LichSuHien.this, R.layout.item_layout_quiz, arrTest, 2, arr);
        duyet(id);
        IdCheck(idcheck);
        listView.setAdapter(adapter_question);
        setMau();
    }

    public void load(int ID) {
        Question test = new Question();
        Cursor cu = DB.getCursor("select * from Question where ID = '" + ID + "'");
        if (cu.moveToFirst()) {
            do {
                test.setId(cu.getInt(0));
                test.setCauhoi(cu.getString(1));
                test.setA(cu.getString(2));
                test.setB(cu.getString(3));
                test.setC(cu.getString(4));
                test.setD(cu.getString(5));
                test.setDung(cu.getString(6));
                arrTest.add(test);
            } while (cu.moveToNext());
        }

    }
    public void duyet(String id) {
        String ID [] = id.split(",");
        for (int i = 0; i < ID.length; i++) {
            load(Integer.parseInt(ID[i]));// dưa dữ liệu nên listView theo id
        }
        listView.setAdapter(adapter_question);
    }
    public void IdCheck(String id) {
        String ID [] = id.split(",");
        for (int i = 0; i < ID.length; i++) {
            arr.add(Integer.valueOf(ID[i]));// đưa lịch suwaar check vào
        }
        listView.setAdapter(adapter_question);
    }
    public void setMau() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (int i = 0; i < visibleItemCount; i++) {
                    View view1 = listView.getChildAt(i);
                    TextView IDCauHoi = (TextView) view1.findViewById(R.id.txtID);
                    rA = (RadioButton) view1.findViewById(R.id.Radiobt_A);
                    rB = (RadioButton) view1.findViewById(R.id.Radiobt_B);
                    rC = (RadioButton) view1.findViewById(R.id.Radiobt_C);
                    rD = (RadioButton) view1.findViewById(R.id.Radiobt_D);
                    String DapAn = "";
                    DapAn = getDung(Integer.parseInt(IDCauHoi.getText().toString())); // Lấy ra đáp án đúng từ id
                    RadioGroup rg = (RadioGroup) view1.findViewById(R.id.radioGroup);
                    int Check = rg.getCheckedRadioButtonId();

                    // Sử dụng if-else thay cho switch-case
                    if (Check == R.id.Radiobt_A) {
                        rA.setTextColor(getResources().getColor(R.color.colorSelect));
                    } else if (Check == R.id.Radiobt_B) {
                        rB.setTextColor(getResources().getColor(R.color.colorSelect));
                    } else if (Check == R.id.Radiobt_C) {
                        rC.setTextColor(getResources().getColor(R.color.colorSelect));
                    } else if (Check == R.id.Radiobt_D) {
                        rD.setTextColor(getResources().getColor(R.color.colorSelect));
                    }

                    // Kiểm tra đáp án đúng
                    if (mainQuestion.kiemtra(rA.getText().toString().substring(0, 1), DapAn)) {
                        setDung(1);
                    }

                    if (mainQuestion.kiemtra(rB.getText().toString().substring(0, 1), DapAn)) {
                        setDung(2);
                    }

                    if (mainQuestion.kiemtra(rC.getText().toString().substring(0, 1), DapAn)) {
                        setDung(3);
                    }

                    if (mainQuestion.kiemtra(rD.getText().toString().substring(0, 1), DapAn)) {
                        setDung(4);
                    }
                }
            }
        });
    }
    public void setDung(int i) {
        switch (i) {
            case 1:
                rA.setTextColor(getResources().getColor(R.color.colordung));
                break;
            case 2:
                rB.setTextColor(getResources().getColor(R.color.colordung));
                break;
            case 3:
                rC.setTextColor(getResources().getColor(R.color.colordung));
                break;
            case 4:
                rD.setTextColor(getResources().getColor(R.color.colordung));
                break;
        }
    }
    public String getDung(int id) {
        String dung = "";
        ArrayList<Question> countryList = adapter_question.myArray;// gợi myArray
        for (int i = 0; i < countryList.size(); i++) {
            Question question = countryList.get(i);
            if (question.getId() == id)
            {
                dung = question.getDung();
            }
        }
        return dung;
    }
}