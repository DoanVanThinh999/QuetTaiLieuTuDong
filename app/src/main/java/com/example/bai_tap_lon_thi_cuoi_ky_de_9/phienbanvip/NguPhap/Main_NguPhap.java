package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.NguPhap;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class Main_NguPhap extends AppCompatActivity implements SearchView.OnQueryTextListener {
    Database DB = new Database(this);
    ArrayList<NguPhap> arrayList;
    Adapter_NguPhap adapterNguPhap;
    ListView listView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lv_nguphap);
        //hiện tiêu đề;
        ActionBar ab = getSupportActionBar();
        //set mầu cho actionBar
        ab.setTitle("Ngữ Pháp");
        //Hiện nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.lvnguphap);
        arrayList = new ArrayList<NguPhap>();
        adapterNguPhap = new Adapter_NguPhap(this,R.layout.item_nguphap, arrayList);
        listView.setAdapter(adapterNguPhap);
        load();
        adapterNguPhap.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timkiem_layout ,menu);
        MenuItem item=menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    public void load() {
        Cursor cur = DB.getCursor("Select * from Gramar");
        int count =cur.getCount();
        if (cur.moveToFirst()) {
            do {
                arrayList.add(new NguPhap(count++, cur.getString(1), cur.getString(2),
                        cur.getString(3),cur.getString(4),cur.getString(5),cur.getString(6)));
            } while (cur.moveToNext());
        }
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Cursor cur = DB.getCursor("Select * from Gramar where Ten like '%"+newText+"%'");
        int count =cur.getCount();
        /*Làm mới lại adp*/
        adapterNguPhap.clear();

        if(cur.moveToFirst()){

            do{
                arrayList.add(new NguPhap(count++, cur.getString(1), cur.getString(2),
                        cur.getString(3),cur.getString(4),cur.getString(5),cur.getString(6)));
            }while (cur.moveToNext());

        }
        listView.setAdapter(adapterNguPhap);
        adapterNguPhap.notifyDataSetChanged();
        return true;

    }
}
