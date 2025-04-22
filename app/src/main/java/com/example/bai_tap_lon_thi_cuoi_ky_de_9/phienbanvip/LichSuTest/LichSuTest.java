package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LichSuTest;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Question.Main_Question;

import java.util.ArrayList;

public class LichSuTest extends AppCompatActivity {
    ArrayList<LichSu> arrayList = null;
    Database DB = new Database(this);
    LichSuAdapter lichSuAdapter;
    ListView listView;
    RadioButton rA, rB, rC, rD;
    Main_Question main_question = new Main_Question();
    TextView tvTaiKhoanKH;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lich_su_test);
        listView = (ListView) findViewById(R.id.lvHistory);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");

        if (tvTaiKhoanKH != null) {
            tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }
        arrayList = new ArrayList<LichSu>();
        ActionBar ab = getSupportActionBar();
        //set mầu cho actionBar
        ab.setTitle("Lịch Sử Test");
        //Hiện nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lichSuAdapter = new LichSuAdapter(this, R.layout.item_lichsu_test, arrayList);
        load();
        listView.setAdapter(lichSuAdapter);
        getView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(LichSuTest.this, LichSuHien.class);
                Bundle b = new Bundle();
                LichSu lichsu = (LichSu) lichSuAdapter.getItem(position);
                b.putString("ID", lichsu.getIdcauhoi());
                b.putString("Time", lichsu.getTimeThi());
                b.putString("IDCheck", lichsu.getIdtich());
                in.putExtras(b);
                in.putExtra("taiKhoan", taiKhoan);
                startActivity(in);
            }
        });

    }

    public void getView() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);//chọn nhiều item trong ListView
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                //hiện só lượng đã chọn
                mode.setTitle(checkedCount + " /" + lichSuAdapter.getSelectedCount());
                lichSuAdapter.toggleSelection(position);//xóa item hiện trên listView
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_opption, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) { // Kiểm tra nếu hành động là xóa
                    SparseBooleanArray selected = lichSuAdapter.getSelectedIds(); // Lấy ra các vị trí đã check
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) { // Kiểm tra nếu vị trí này được chọn
                            LichSu lichsu = (LichSu) lichSuAdapter.getItem(selected.keyAt(i));
                            lichSuAdapter.remove(lichsu); // Xóa trong mảng ArrayList
                            delete(lichsu.getId()); // Xóa trong CSDL
                        }
                    }
                    mode.finish(); // Kết thúc chế độ ActionMode
                    return true;
                } else { // Nếu không phải hành động xóa
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
            }
        });

    }

    public void load() {
        Cursor cu = DB.getCursor("select * from LichSuTest");
        if (cu.moveToLast()) {
            do {
                LichSu lichsu = new LichSu();
                lichsu.setId(cu.getInt(0));
                lichsu.setNgay(cu.getString(1));
                lichsu.setIdcauhoi(cu.getString(2));
                lichsu.setIdtich(cu.getString(3));
                lichsu.setTimeThi(cu.getString(5));
                lichsu.setDiem(cu.getInt(4));
                arrayList.add(lichsu);
            } while (cu.moveToPrevious());

        }
    }
    //kiểm tra đấp án đúng  thì trả ra true ngược lại
    public boolean kiemtra(String a, String b) {
        if (a.contains(b.replaceAll("\\s+", ""))) {
            return true;
        } else {
            return false;
        }
    }
    public void delete(int id)
    {
        DB.ExecuteSQL("delete from LichSuTest where id = "+id+"");
    }
}