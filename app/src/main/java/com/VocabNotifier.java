package com;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.MainActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.MainTiengAnhActivity;

import java.util.Random;

public class VocabNotifier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("VocabNotifier", "Báo thức đã kích hoạt!");
        String[] reminders = {
                "Bạn cần học chăm chỉ hơn!",
                "Bạn quên kiến thức rồi sao? Hãy chứng minh chúng mình sai đi!",
                "Kiến thức là sức mạnh! Học ngay đi nào!",
                "Hôm nay bạn đã học từ mới chưa?",
                "Mình cá là bạn chưa học đủ đâu đấy!",
                "Nếu không học hôm nay, bạn sẽ hối hận vào ngày mai!"
        };

        // Chọn một câu ngẫu nhiên
        Random random = new Random();
        String message = reminders[random.nextInt(reminders.length)];

        // Hiển thị thông báo
        showNotification(context, message);
    }

    private void showNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "vocab_channel";

        NotificationChannel channel = new NotificationChannel(channelId, "Vocab Reminder", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Kênh nhắc học từ vựng");
        notificationManager.createNotificationChannel(channel);

        Intent intent = new Intent(context, MainTiengAnhActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.book3)
                .setContentTitle("Lời nhắc học từ vựng")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
    }
}
