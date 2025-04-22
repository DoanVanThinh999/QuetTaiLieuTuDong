package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExporter {

    public static void saveImageAsPdf(Context context, Bitmap bitmap, String fileNameWithoutExtension) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                bitmap.getWidth(), bitmap.getHeight(), 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // ✅ Tạo file trong thư mục Download
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        // Thêm đuôi .pdf nếu chưa có
        String fileName = fileNameWithoutExtension.endsWith(".pdf") ?
                fileNameWithoutExtension : fileNameWithoutExtension + ".pdf";

        File file = new File(downloadsDir, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
            Toast.makeText(context, "Đã lưu PDF: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi lưu PDF", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

}

