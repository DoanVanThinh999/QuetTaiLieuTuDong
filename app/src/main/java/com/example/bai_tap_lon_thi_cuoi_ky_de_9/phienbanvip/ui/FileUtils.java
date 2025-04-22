package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FileUtils {

    public static File getAppImageFolder(Context context) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ScannedDocs");

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                // fallback to cache
                dir = new File(context.getCacheDir(), "ScannedDocs");
                dir.mkdirs();
            }
        }
        return dir;
    }

    public static File getNewImageFile(Context context, String fileName) {
        File folder = getAppImageFolder(context);
        return new File(folder, fileName);
    }



}
