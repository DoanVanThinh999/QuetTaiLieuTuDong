package com;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.MainActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.MainTiengAnhActivity;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startIntent = new Intent(context, MainTiengAnhActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }
    }
}
