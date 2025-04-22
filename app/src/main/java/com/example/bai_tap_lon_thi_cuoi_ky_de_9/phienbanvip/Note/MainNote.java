package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Note;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainNote extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ArrayList<Note> arr = new ArrayList<Note>();
    Custom_Adapter_Note adp = null;
    Database DB = new Database(this);
    FloatingActionButton fabAdd;

    GridView gvHienThi;
    EditText edt_Tile, edt_Note;
    TextView tvTaiKhoanKH;

    // Định nghĩa giá trị số cố định cho delete
    private static final int DELETE_NOTE_ACTION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_note_layout);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Ghi Chú");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fabAdd = findViewById(R.id.Fab);
        edt_Tile = findViewById(R.id.edt_Tile);
        edt_Note = findViewById(R.id.edt_Note);
        gvHienThi = findViewById(R.id.gv_ghichu);
        arr = new ArrayList<Note>();
        adp = new Custom_Adapter_Note(this, R.layout.item_ghi_chu_layout, arr);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");

        if (tvTaiKhoanKH != null) {
            tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }
        gvHienThi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(MainNote.this, edtNote.class);
                Bundle b = new Bundle();
                Note note = (Note) adp.getItem(position);
                b.putString("ID", String.valueOf(note.getId()));
                b.putString("Tile", note.getTile());
                b.putString("Note", note.getNote());
                in.putExtras(b);
                startActivity(in);
            }
        });
        load();
        getView();
        gvHienThi.setAdapter(adp);
        Add();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timkiem_layout, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    public void getView() {
        gvHienThi.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gvHienThi.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = gvHienThi.getCheckedItemCount();
                mode.setTitle(checkedCount + " /" + adp.getSelectedCount());
                adp.toggleSelection(position);
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
                // Sử dụng giá trị số cố định để xử lý xóa
                if (item.getItemId() == R.id.delete) {
                    SparseBooleanArray selected = adp.getSelectedIds();
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            Note note = (Note) adp.getItem(selected.keyAt(i));
                            adp.remove(note);
                            delete(note.getId());
                        }
                    }
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
            }
        });
    }

    public void load() {
        Cursor cur = DB.getCursor("select * from GhiChu");
        if (cur.moveToFirst()) {
            do {
                arr.add(new Note(cur.getInt(0), cur.getString(1), cur.getString(2)));
            } while (cur.moveToNext());
        }
    }

    public void Add() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), edtNote.class);
                Bundle bundle = new Bundle();
                bundle.putString("Tile", null);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void delete(int id) {
        DB.ExecuteSQL("delete from GhiChu where id = \"" + id + "\"");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Cursor cursor = DB.getCursor("Select * from GhiChu where TenGhiChu like \"%"+newText+"%\"");
        adp.clear();
        if (cursor.moveToFirst()) {
            do {
                arr.add(new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        gvHienThi.setAdapter(adp);
        adp.notifyDataSetChanged();
        return true;
    }
}