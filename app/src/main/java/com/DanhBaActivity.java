package com;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DanhBaActivity extends AppCompatActivity {
    FloatingActionButton fabThemLienHe;
    ListView lvLienHe;
    ArrayList<LienHe> listLienHe;
    LienHeAdapter lienHeAdapter;

    // Firebase Database instance và tham chiếu
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference contactRef; // Sửa: Khởi tạo sau khi databaseReference có giá trị

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_ba); // Đảm bảo file XML tồn tại trong res/layout


        // Liên kết giao diện
        lvLienHe = findViewById(R.id.lvLienHe);
        fabThemLienHe = findViewById(R.id.fabThemLienHe); // Đúng cách liên kết ID
        listLienHe = new ArrayList<>();
        lienHeAdapter = new LienHeAdapter(this, R.layout.lv_item_lien_he, listLienHe);
        lvLienHe.setAdapter(lienHeAdapter);
        registerForContextMenu(lvLienHe);
        // FloatingActionButton thêm liên hệ mới
        fabThemLienHe.setOnClickListener(view -> {
            Intent intent = new Intent(DanhBaActivity.this, ThemLienHeActivity.class);
            startActivity(intent);
        });

        // Khởi tạo Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        contactRef = databaseReference.child("contacts"); // Khởi tạo contactRef tại đây



        // Đọc dữ liệu từ Realtime Database
        docDuLieu();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Lấy thông tin item được chọn
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        LienHe lienHe = lienHeAdapter.listLienHe.get(info.position);

        // Tạo tiêu đề cho menu
        String title = "Thao tác với: " + lienHe.getTenLienHe() + " - " + lienHe.getSoDienThoai();
        menu.setHeaderTitle(title);

        // Thêm các mục trong menu
        menu.add(0,v.getId(),0,"Gọi điện thoại");
        menu.add(0,v.getId(),1,"Nhắn tin");
        menu.add(0,v.getId(),2,"Sửa liên hệ");
        menu.add(0,v.getId(),3,"Xoá liên hệ");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        LienHe lienHe = lienHeAdapter.getListLienHe().get(info.position);
        switch (item.getOrder()){
            case 0:
                Uri phoneUri = Uri.parse("tel: " + lienHe.getSoDienThoai());
                Intent goiDienThoai = new Intent(Intent.ACTION_DIAL, phoneUri);
                startActivity(goiDienThoai);
                break;
            case 1:
                Uri smsURI = Uri.parse("smsto: " + lienHe.getSoDienThoai());
                Intent nhanTinIntent = new Intent(Intent.ACTION_SENDTO,smsURI);
                startActivity(nhanTinIntent);
                break;
            case 2:
                Intent intentSuaLienHe = new Intent(DanhBaActivity.this,SuaLienHeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", lienHe.getId());
                intentSuaLienHe.putExtras(bundle);
                startActivity(intentSuaLienHe);
                break;
            case 3:
                String thongDiep = "Bạn thực sự muốn xoá" + lienHe.tenLienHe + " - " + lienHe.getSoDienThoai() + "?";
                AlertDialog.Builder builder = new AlertDialog.Builder(DanhBaActivity.this);
                builder.setTitle("Xoá liên hệ");
                builder.setMessage(thongDiep);
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        contactRef.child(lienHe.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(DanhBaActivity.this,"Xoá thành công!",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(DanhBaActivity.this,"Xoá thất bại!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        docDuLieu();
                    }
                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create().show();
        }
        return true;
    }

    private void docDuLieu() {
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listLienHe.clear(); // Xóa dữ liệu cũ trước khi thêm mới
                for (DataSnapshot data : snapshot.getChildren()) {
                    LienHe lienHe = data.getValue(LienHe.class);
                    if (lienHe != null) {
                        listLienHe.add(lienHe); // Thêm dữ liệu mới
                    }
                    Log.d("FirebaseData", "Values: " + data.getValue());
                }
                lienHeAdapter.notifyDataSetChanged(); // Cập nhật giao diện
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Failed to read contacts", error.toException());
            }
        });
    }


}