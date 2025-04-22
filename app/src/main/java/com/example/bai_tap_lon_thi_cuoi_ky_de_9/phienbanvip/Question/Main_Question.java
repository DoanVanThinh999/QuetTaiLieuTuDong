package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main_Question extends AppCompatActivity {
    ListView listView;
    Adapter_Question adapter_question;
    ArrayList<Question> arrTest = null;
    ArrayList<Integer> ID;
    String IDtich;
    Database DB = new Database(this);
    Random random = new Random();
    Chronometer time;
    FloatingActionButton floatingActionButton;
    int d = 0, a = 0; // khởi tạo biến đếm
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    TextView tvTaiKhoanKH;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_layout);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");

        if (tvTaiKhoanKH != null) {
            tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }
        floatingActionButton = findViewById(R.id.FabTest);
        listView = findViewById(R.id.lvquiz);
        time = findViewById(R.id.chronometer2);
        time.start(); // bắt đầu chạy time

        ID = new ArrayList<>();
        arrTest = new ArrayList<Question>();
        adapter_question = new Adapter_Question(Main_Question.this, R.layout.item_layout_quiz, arrTest, a);
        listView.setAdapter(adapter_question);

        duyet();
        DoneTest();
    }

    public void DoneTest() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a == 0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main_Question.this);
                    dialog.setMessage("Bạn muốn kết thúc bài thi!");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            a++;
                            setMau(); // cập nhật lại đáp án và hiện trên listView
                            TinhDiem(); // gọi đến phương thức tính điểm
                            adapter_question = new Adapter_Question(Main_Question.this, R.layout.item_layout_quiz, arrTest, a);
                            listView.setAdapter(adapter_question);
                            Hien(d);
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.create().show();
                } else {
                    Hien(d);
                }
            }
        });
    }

    // Tính điểm
    public void TinhDiem() {
        IDtich = ""; // lấy ra vị trí đã tích
        d = 0; // Đặt lại điểm số

        for (int i = 0; i < arrTest.size(); i++) {
            Question questions = arrTest.get(i);
            if (questions.isSelectedA() && kiemtra(questions.getA().substring(0, 1), questions.getDung())) {
                d++;
                IDtich += "1,";
            }
            if (questions.isSelectedB() && kiemtra(questions.getB().substring(0, 1), questions.getDung())) {
                d++;
                IDtich += "2,";
            }
            if (questions.isSelectedC() && kiemtra(questions.getC().substring(0, 1), questions.getDung())) {
                d++;
                IDtich += "3,";
            }
            if (questions.isSelectedD() && kiemtra(questions.getD().substring(0, 1), questions.getDung())) {
                d++;
                IDtich += "4,";
            }
            if (!questions.isSelectedA() && !questions.isSelectedB() && !questions.isSelectedC() && !questions.isSelectedD()) {
                IDtich += "0,";
            }
        }
    }

    // Hiển thị điểm đã làm được
    public void Hien(int d) {
        time.stop(); // Dừng tính thời gian
        String idCauhoi = String.join(",", ID.stream().map(String::valueOf).toArray(String[]::new));
        Cursor cu = DB.getCursor("Select * from LichSuTest where IDCAUHOI = '" + idCauhoi + "'");

        if (cu.getCount() == 0) {
            WriteHistory(getIDHistory(), idCauhoi, IDtich, d);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(Main_Question.this);
        dialog.setCancelable(false);
        dialog.setTitle("Kết quả Test");
        dialog.setMessage("Điểm đạt : " + d + "/10" + "\n" + "Time: " + time.getText());
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Ghi dữ liệu lên Firebase
                uploadResultsToFirebase(d, time.getText().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                        finish();
                    }
                },3000);

            }
        });
        dialog.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.create().show();
    }

    private void uploadResultsToFirebase(int score, String timeTaken) {
        // Khởi tạo Firebase Database và Firebase Auth
        databaseReference = FirebaseDatabase.getInstance().getReference("results");
        auth = FirebaseAuth.getInstance();

        // Lấy email của người dùng hiện tại
        FirebaseUser user = auth.getCurrentUser();
        String email = user != null ? user.getEmail() : "unknown"; // Kiểm tra nếu người dùng đã đăng nhập

        // Tạo một đối tượng kết quả
        Result result = new Result(score + "/10 điểm", timeTaken, email,"Game Phiên bản Vip");

        // Ghi đối tượng này lên Realtime Database
        databaseReference.push().setValue(result).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Xử lý thành công
                Toast.makeText(Main_Question.this, "Kết quả đã được lưu!", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý lỗi
                Toast.makeText(Main_Question.this, "Lỗi khi lưu kết quả!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lớp Result để lưu trữ điểm, thời gian và email
    public class Result {
        public String score;
        public String timeTaken;
        public String email;
        public String typeGame;

        public Result() {
            // Constructor rỗng để Firebase có thể sử dụng
        }

        public Result(String score, String timeTaken, String email,String typeGame) {
            this.score = score;
            this.timeTaken = timeTaken;
            this.email = email;
            this.typeGame = typeGame;
        }
    }

    // Kiểm tra đáp án đúng thì trả ra true ngược lại
    public boolean kiemtra(String a, String b) {
        return a.contains(b.replaceAll("\\s+", ""));
    }

    public void duyet() {
        RanRom();
        for (int i = 0; i < ID.size(); i++) {
            load(ID.get(i)); // đưa dữ liệu vào listView theo ID
        }
        adapter_question.notifyDataSetChanged();
    }

    // Đọc đọc hỏi ở CSDL đưa vào ArrayList theo ID
    public void load(int ID) {
        Question test = new Question();
        Cursor cursor = DB.getCursor("select * from Question where ID = '" + ID + "'");
        if (cursor.moveToFirst()) {
            do {
                test.setId(cursor.getInt(0));
                test.setCauhoi(cursor.getString(1));
                test.setA(cursor.getString(2));
                test.setB(cursor.getString(3));
                test.setC(cursor.getString(4));
                test.setD(cursor.getString(5));
                test.setDung(cursor.getString(6));
                arrTest.add(test);
            } while (cursor.moveToNext());
        }
    }

    // RamRom 10 câu hỏi
    public ArrayList<Integer> RanRom() {
        int iNew;
        int dem = DB.GetCount("SELECT * FROM Question");
        for (int i = 0; i < 10; ) {
            iNew = random.nextInt(dem);
            if (!ID.contains(iNew) && iNew != 0) {
                i++;
                ID.add(iNew);
            }
        }
        return ID;
    }

    // Ghi lịch sử vào cơ sở dữ liệu
    public void WriteHistory(int id, String idCauhoi, String idtich, int diem) {
        DB.ExecuteSQL("INSERT INTO LichSuTest VALUES('" + (id + 1) + "','" + getDateTime() + "'," +
                "'" + idCauhoi + "', " +
                "'" + idtich + "', " +
                "" + diem + ",'" +
                "" + time.getText() + "')");
    }

    // Lấy ra id của phần tử cuối trong bảng lichsu
    public int getIDHistory() {
        int id = 0;
        Cursor cu = DB.getCursor("select id from LichSuTest");
        if (cu.moveToLast()) {
            id = cu.getInt(0);
        }
        return id;
    }

    // Lấy ra thời gian và ngày tháng
    public String getDateTime() {
        String time;
        Date date = new Date();
        String strDateFormat = "dd/MM/yyyy";
        String strDateFormat24 = "HH:mm:ss a";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat); // lấy ra ngày
        SimpleDateFormat sdft = new SimpleDateFormat(strDateFormat24); // lấy ra giờ
        time = sdf.format(date) + "   " + sdft.format(date);
        return time;
    }

    // Để sét màu cho đáp án đúng và đáp án sai
    public void setMau() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (int i = 0; i < visibleItemCount; i++) {
                    View v = listView.getChildAt(i);
                    if (v == null) continue; // Kiểm tra null

                    TextView IDCauHoi = v.findViewById(R.id.txtID);
                    RadioGroup rg = v.findViewById(R.id.radioGroup);
                    String DapAn = getDung(Integer.parseInt(IDCauHoi.getText().toString())); // lấy ra đáp án đúng từ id

                    // Khôi phục màu sắc mặc định cho tất cả các RadioButton
                    for (int j = 0; j < rg.getChildCount(); j++) {
                        RadioButton radioButton = (RadioButton) rg.getChildAt(j);
                        radioButton.setTextColor(getResources().getColor(R.color.defaultColor)); // Màu mặc định
                    }

                    int Check = rg.getCheckedRadioButtonId();

                    // Sét màu đáp án đã tích
                    if (Check != -1) {
                        RadioButton selectedRadioButton = v.findViewById(Check);
                        selectedRadioButton.setTextColor(getResources().getColor(R.color.colorSelect));
                    }

                    for (int j = 0; j < rg.getChildCount(); j++) {
                        RadioButton radioButton = (RadioButton) rg.getChildAt(j);
                        if (kiemtra(radioButton.getText().toString().substring(0, 1), DapAn)) {
                            setDung(j + 1);
                        }
                    }
                }
            }
        });
    }

    // Set màu đáp án đúng
    public void setDung(int i) {
        if (i - 1 < 0 || i - 1 >= listView.getChildCount()) return; // Kiểm tra chỉ số hợp lệ
        RadioButton radioButton = (RadioButton) ((RadioGroup) listView.getChildAt(i - 1).findViewById(R.id.radioGroup)).getChildAt(i - 1);
        radioButton.setTextColor(getResources().getColor(R.color.colordung));
    }

    // Trả ra đáp án đúng theo ID
    public String getDung(int id) {
        String dung = "";
        ArrayList<Question> ArrayQuestion = adapter_question.myArray; // gợi myArray
        for (Question question : ArrayQuestion) {
            if (question.getId() == id) {
                dung = question.getDung();
            }
        }
        return dung;
    }
}