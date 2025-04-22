package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.ActivityMainBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.ActivityMainTiengAnhBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.VocabNotifier;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.EnglishMain;

import java.util.Calendar;

public class MainTiengAnhActivity extends AppCompatActivity {

    private ActivityMainTiengAnhBinding mB;
    public static SharedPreferences dictionaryStorage;
    TextView tvXinChao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvXinChao = findViewById(R.id.tvXinChao);
        mB = ActivityMainTiengAnhBinding.inflate(getLayoutInflater());
        setContentView(mB.getRoot());
        Intent intent1 = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent1.getSerializableExtra("taiKhoan");
        if (mB.tvXinChao != null) {
            mB.tvXinChao.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));
        }
        if (dictionaryStorage == null) dictionaryStorage = getSharedPreferences("dictionary", MODE_PRIVATE);


        mB.btnPhienbanvip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentQuanLyTK = new Intent(MainTiengAnhActivity.this, EnglishMain.class);
                intentQuanLyTK.putExtra("taiKhoan", taiKhoan);
                startActivity(intentQuanLyTK);
            }
        });

        NotificationChannelCompat channel = new NotificationChannelCompat.Builder("dictionary", NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Dictionary")
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN).build())
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(channel);

        // Nhắc từ vựng sau 30 phút
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.MINUTE, 30); // Cộng thêm 30 phút

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, VocabNotifier.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30 * 60 * 1000, pIntent);

        // Kiểm tra và yêu cầu quyền thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        scheduleReminder();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dictionary, R.id.navigation_games, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
       // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mB.navView, navController);
    }

    private void scheduleReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, VocabNotifier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        long triggerTime = System.currentTimeMillis() + (60 * 1000); // 1 phút sau
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        Log.d("MainActivity", "Báo thức đã được đặt sau 1 phút");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelReminder(); // Nếu người dùng mở ứng dụng, hủy báo thức
    }

    private void cancelReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, VocabNotifier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d("MainActivity", "Hủy báo thức vì người dùng mở ứng dụng");
    }
}