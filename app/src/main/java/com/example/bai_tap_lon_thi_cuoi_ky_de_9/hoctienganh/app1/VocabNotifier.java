package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.arch.core.util.Function;
import androidx.core.app.NotificationCompat;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.MainTiengAnhActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.PosEntry;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhắc nhở từ mới hoặc ôn lại từ cũ mỗi ngày
 */
public class VocabNotifier extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        notifyWord(context);
    }

    public static void notifyWord(Context context) {
        Intent openApp = new Intent(context, MainTiengAnhActivity.class);
        PendingIntent pOpenApp = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_MUTABLE);
        List<String> learnedWords = Settings.from(context).getLearnedWords();
        DictionaryEntry entry;

        if (!Settings.from(context).getVocabNotification()) return;

        boolean notifyNewWord = Math.random() < 0.5;
        if (learnedWords.size() == 0) // Chưa học từ gì, nên gặp từ nào cũng mới
            notifyNewWord = true;
        if (DictionaryEntry.getAll(context).size() == learnedWords.size()) // Đã học hết từ, nên chỉ có từ cũ
            notifyNewWord = false;
        if (notifyNewWord) {
            List<String> wordsNotLearned = new ArrayList<>(DictionaryEntry.getAllNames(context));
            wordsNotLearned.removeAll(learnedWords);
            entry = DictionaryEntry.find(context, Arrays.pickRandom(wordsNotLearned));
        }
        else entry = DictionaryEntry.find(context, Arrays.pickRandom(learnedWords));

        PosEntry[] posArray = PosEntry.from(context, entry);
        String joinedPos = String.join("\n", Arrays.toStringArray(posArray, new Function<PosEntry, String>() {
            @Override
            public String apply(PosEntry posEntry) {
                return String.format("%s: %s", posEntry.getHeader(), String.join("|", posEntry.getArray()));
            }
        }));

        Notification notification = new NotificationCompat.Builder(context, "dictionary")
                .setContentTitle(notifyNewWord ? context.getString(R.string.notification_new_word_title, entry.getName()) : context.getString(R.string.notification_old_word_title, entry.getName()))
                .setContentText(joinedPos)
                .setSmallIcon(R.drawable.book)
                .setContentIntent(pOpenApp)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(joinedPos))
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int)System.currentTimeMillis(), notification);
    }
}
