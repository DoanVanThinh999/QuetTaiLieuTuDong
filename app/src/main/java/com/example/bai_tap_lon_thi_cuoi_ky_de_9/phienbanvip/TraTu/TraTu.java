package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.TraTu;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Database;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Note.edtNote;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.Locale;

public class TraTu extends AppCompatActivity implements SearchView.OnQueryTextListener {
    TextView textView;
    TextToSpeech textToSpeech;
    String worl;
    String mean;
    Database DB = new Database(this);

    // Định nghĩa các giá trị số cố định cho các menu item

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tra_tu);
        textView = findViewById(R.id.tvhientratu);
        Bundle b = getIntent().getExtras();
        worl = b.getString("key_Word");
        mean = b.getString("key_Mean");
        Lichsu(worl);
        textView.setText(mean);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(worl);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_tratu_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = item.getTitle().toString();

        if (title.equals("Thêm Ghi Chú")) {
            addNote();
        } else if (title.equals("search")) {
            searchItem();
        } else if (title.equals("doc")) {
            textToSpeech.speak(worl, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(this, "Đang đọc", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNote() {
        Bundle b = getIntent().getExtras();
        worl = b.getString("key_Word");
        mean = b.getString("key_Mean");
        mean = android.database.DatabaseUtils.sqlEscapeString(mean);
        int sbg = DB.GetCount("select * from GhiChu where TenGhiChu=\"" + worl.trim() + "\"");
        int count = DB.GetCount("Select * from GhiChu");
        count++;

        if (sbg == 1) {
            AlertDialog.Builder al = new AlertDialog.Builder(this);
            al.setTitle("Thông báo");
            al.setMessage("Tên ghi chú đã tồn tại.");
            al.create().show();
        } else {
            DB.ExecuteSQL("Insert into GhiChu values(" + count + ",\"" + worl.trim() + "\",\"" + mean + "\")");
            Toast.makeText(this, "Đã Thêm", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchItem() {
        Intent in = new Intent(getApplicationContext(), Search_tran.class);
        finish();
        startActivity(in);
    }

    public void Lichsu(String a) {
        Cursor c = DB.getCursor("select * from LichSuTraTu");
        int leng = c.getCount();
        if (leng < 50) {
            setLS(a, leng);
        } else {
            DB.ExecuteSQL("delete from LichSuTraTu where ID = 1");
            setLS(a, leng);
        }
    }

    public void setLS(String a, int i) {
        Cursor c = DB.getCursor("select * from LichSuTraTu where work = '" + a + "'");
        int leng = c.getCount();
        if (leng == 0) {
            DB.ExecuteSQL("insert into LichSuTraTu values(" + (i + 1) + ",\"" + a + "\")");
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}