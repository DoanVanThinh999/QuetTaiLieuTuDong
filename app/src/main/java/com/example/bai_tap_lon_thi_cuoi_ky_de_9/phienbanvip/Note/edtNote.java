package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Note;

/*
Tran Thanh Nhan 20/7/2020
* */

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class edtNote extends AppCompatActivity {
    Database DB = new Database(this);
    ArrayList<String> arr;
    EditText edtTenGhiChu, edtNoiDungGhiChu;
    String tenGhiChu, NoiDungGhiChu;
    String tile = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);
        edtTenGhiChu = (EditText) findViewById(R.id.edt_Tile);
        edtNoiDungGhiChu = (EditText) findViewById(R.id.edt_Note);
        Bul();
    }
    public void Bul(){
        //
        Bundle b=getIntent().getExtras();
        tile=b.getString("Tile");
        if(tile!=null) {
            Cursor cursor = DB.getCursor("select * from GhiChu where TenGhiChu =\"" + tile + "\"");
//        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            edtTenGhiChu.setText(cursor.getString(1));
            edtNoiDungGhiChu.setText(cursor.getString(2));
//        }while(cursor.moveToNext());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tickok, menu);
        return true;
    }
    /*item trong menu, xử lí sự kiện xác nhận insert, update, delete*/
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tickok) {
            Insert();
        }
        if(id==R.id.item_edit){
            Update();
            Toast.makeText(this, "Update thành công!", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(edtNote.this, MainNote.class);
            startActivity(in);
            finish();

        }
        if(id==R.id.item_delete){
            Delete();
            Intent in = new Intent(edtNote.this, MainNote.class);
            startActivity(in);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    /*Thêm bản ghi vào cơ sở dữ liệu*/

    public void Insert() {
        tenGhiChu = edtTenGhiChu.getText().toString();
        NoiDungGhiChu = edtNoiDungGhiChu.getText().toString();
        /*Kiểm tra bản ghi đã tồn tại hay chưa?*/

        int sbg = DB.GetCount("Select * from GhiChu where TenGhiChu=\"" + tenGhiChu + "\"");
        if (sbg == 1) {
            AlertDialog.Builder al = new AlertDialog.Builder(this);
            al.setTitle("Thông báo");
            al.setMessage("Tên ghi chú đã tồn tại. ");
            al.create().show();
        } else if (sbg == 0) {
            DB.ExecuteSQL("insert into GhiChu values(" + (getId()+1) + ",\"" + tenGhiChu + "\",\"" + NoiDungGhiChu + "\")");
            Intent in = new Intent(edtNote.this, MainNote.class);
            startActivity(in);
        }
    }
    //get ra id cua phân tử cuỗi cùng trong ghi chú
    public int getId()
    {
        int id = 0;
        Cursor cu = DB.getCursor("select id from GhiChu");
        if (cu.moveToLast())
        {
            id = cu.getInt(0);
        }
        return id;
    }

    public void Update(){
        tenGhiChu = edtTenGhiChu.getText().toString();
        NoiDungGhiChu = edtNoiDungGhiChu.getText().toString();
        DB.ExecuteSQL("update GhiChu set NDGhiChu=\""+NoiDungGhiChu+"\"where TenGhiChu=\""+tenGhiChu+"\"");

    }
    public void Delete(){
        tenGhiChu = edtTenGhiChu.getText().toString();
        DB.ExecuteSQL("delete from GhiChu where TenGhiChu=\""+tenGhiChu+"\"");
    }
}
