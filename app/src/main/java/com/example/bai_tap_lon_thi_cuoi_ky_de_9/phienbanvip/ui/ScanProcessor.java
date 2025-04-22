package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ScanProcessor {

    // ✅ Giữ lại hàm gốc để tương thích
    public static Bitmap detectAndCrop(Context context, Bitmap bitmap) {
        return detectAndCrop(context, bitmap, false);
    }

    public static List<Bitmap> debugSteps = new ArrayList<>();
    public static Bitmap detectAndCrop(Context context, Bitmap bitmap, boolean retryMode) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        debugSteps.clear();
        debugSteps.add(bitmap.copy(Bitmap.Config.ARGB_8888, true));

        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);

        // 📌 Blur nhẹ hoặc mạnh tùy theo retryMode
        Imgproc.GaussianBlur(src, src, retryMode ? new Size(11, 11) : new Size(5, 5), 0);

        double median = computeMedian(src);  //Median > 200--> Quá chói, < 40 quá tối
        double lower = Math.max(0, (retryMode ? 0.05 : 0.1) * median); // lower = int(max(0, (1.0 - sigma) * v))
        double upper = Math.min(255, (retryMode ? 1.5 : 1.2) * median); // upper = int(min(255, (1.0 + sigma) * v))
        Log.d("CannyDebug", "=== ẢNH GỐC ===");
        Log.d("CannyDebug", "Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
        Log.d("CannyDebug", "RetryMode: " + retryMode + " | Median: " + median + ", Lower: " + lower + ", Upper: " + upper);


        Mat edge = new Mat();
        Imgproc.Canny(src, edge, lower, upper);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                retryMode ? new Size(11, 11) : new Size(9, 9)
        );
        Imgproc.morphologyEx(edge, edge, Imgproc.MORPH_CLOSE, kernel);

        Bitmap edgeBmp = Bitmap.createBitmap(edge.cols(), edge.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edge, edgeBmp);
        debugSteps.add(edgeBmp);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edge, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("CannyDebug", "=== CONTOUR GỐC ===");
        Log.d("CannyDebug", "Contours found: " + contours.size());
        double minArea = (bitmap.getWidth() * bitmap.getHeight()) * (retryMode ? 0.003 : 0.009);
        List<MatOfPoint> filteredContours = new ArrayList<>();
        Log.d("CannyDebug", "Filtered contours1: " + filteredContours.size());
        if (filteredContours.size() == 0 && !retryMode) {
            Log.d("CannyDebugBanDauNeuThua", "⚠️ Không tìm thấy contour đủ lớn → Thử lại với retryMode = true");

            // Thử lại detect với retryMode = true
            Bitmap retried = detectAndCrop(context, bitmap, true);

            if (retried == null) {
                Log.d("CannyDebugError", "❌ Retry thất bại — vẫn không tìm thấy contour hợp lệ.");
            } else {
                Log.d("CannyDebugFIX", "✅ Retry thành công — đã tìm thấy contour tài liệu!");
            }

            return retried;
        }


        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > minArea) {
                filteredContours.add(contour);
                Log.d("CannyDebug", "Filtered contours2: " + filteredContours.size());

            }
        }


        Log.d("CannyDebug", "=== SAU LỌC ===");
        Log.d("CannyDebug", "Filtered contours (> " + (int) minArea + "): " + filteredContours.size());
        filteredContours.sort((a, b) -> Double.compare(Imgproc.contourArea(b), Imgproc.contourArea(a)));
        Log.d("CannyDebug", "Filtered contours3: " + filteredContours.size());
        Bitmap contourBmp = drawContoursOnBitmap(bitmap, filteredContours);
        Log.d("CannyDebug", "Filtered contours4: " + filteredContours.size());
        debugSteps.add(contourBmp);

        MatOfPoint2f biggestQuad = null;
        double maxArea = -1;

        for (MatOfPoint c : filteredContours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(c.toArray()), approx, 0.02 * Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true), true);
            if (approx.total() >= 4) {
                double area = Imgproc.contourArea(c);
                if (area > maxArea) {
                    maxArea = area;
                    biggestQuad = approx;
                }
            }
            Log.d("CannyDebug", "Filtered contours5: " + filteredContours.size());

        }

        Bitmap cropped = null;
        if (biggestQuad != null) {
            cropped = warpPerspective(bitmap, biggestQuad);
            if (cropped != null) {
                debugSteps.add(cropped);
            }
        }

        if (cropped != null) {
            return restoreImageSharpness(cropped);
        }

        // 🧪 Nếu retry rồi mà vẫn không có contour → thử boost tương phản
        if (filteredContours.size() == 0 && retryMode) {
            Log.d("CannyDebug", "🧪 Boost contrast vì retryMode vẫn fail → Thử tự tăng tương phản");

            Mat contrasted = new Mat();
            src.convertTo(contrasted, -1, 1.8, -40);  // Tăng tương phản

            Mat edge2 = new Mat();
            Imgproc.Canny(contrasted, edge2, lower, upper);

            List<MatOfPoint> tempContours = new ArrayList<>();
            Imgproc.findContours(edge2, tempContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double minAreaBoost = (bitmap.getWidth() * bitmap.getHeight()) * 0.0025;
            for (MatOfPoint c : tempContours) {
                double area = Imgproc.contourArea(c);
                if (area > minAreaBoost) {
                    filteredContours.add(c);
                }
            }

            if (filteredContours.size() > 0) {
                debugSteps.add(drawContoursOnBitmap(bitmap, filteredContours));
                Log.d("CannyDebug", "✅ Boost contrast thành công — tìm thấy " + filteredContours.size() + " contour!");
            } else {
                Log.d("CannyDebug", "❌ Boost contrast cũng không giúp được.");
            }
        }
        // 📌 Nếu boost contrast vẫn không có gì — fallback crop ở giữa ảnh
        if (filteredContours.size() == 0) {
            Log.d("CannyDebug", "📌 Không tìm thấy contour — fallback crop giữa ảnh");

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int margin = Math.min(w, h) / 10;

            Bitmap fallbackCrop = Bitmap.createBitmap(bitmap, margin, margin, w - 2 * margin, h - 2 * margin);
            debugSteps.add(fallbackCrop);

            Toast.makeText(context, "Không nhận diện được tài liệu. Đã crop ở giữa ảnh.", Toast.LENGTH_SHORT).show();

            return restoreImageSharpness(fallbackCrop);
        }
        if (filteredContours.size() == 0) {
            Toast.makeText(context, "Không nhận diện được tài liệu. Vui lòng đưa ảnh gần hơn, rõ viền hơn.", Toast.LENGTH_LONG).show();
            return null;
        }

        return null;
    }


    // Restoring sharpness of the cropped image after processing
    private static Bitmap restoreImageSharpness(Bitmap bitmap) {
        // Apply sharpening
        Bitmap restoredBitmap = sharpenImage(bitmap);
        return restoredBitmap;
    }

    // A simple method to sharpen the image
    private static Bitmap sharpenImage(Bitmap bitmap) {
        Bitmap sharpened = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(sharpened);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        // Create a sharpen matrix
        ColorMatrix sharpenMatrix = new ColorMatrix();
        sharpenMatrix.setSaturation(1.5f); // Adjust as needed

        paint.setColorFilter(new ColorMatrixColorFilter(sharpenMatrix));

        // Apply the sharpening to the enhanced image
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return sharpened;
    }


    private static double computeMedian(Mat gray) {
        Mat flat = gray.reshape(0, 1);  // làm phẳng thành 1 hàng
        int total = (int) flat.total();
        byte[] data = new byte[total];
        flat.get(0, 0, data);
        int[] values = new int[total];
        for (int i = 0; i < total; i++) values[i] = data[i] & 0xFF; // Đưa về mảng 1 chiều
//        Dùng 0xFF? Vì byte trong Java có thể âm (từ -128 đến 127)
//        Nhưng giá trị pixel luôn là unsigned từ 0 → 255
//                & 0xFF sẽ chuyển byte âm → int dương tương ứng
//        Ví dụ: -1 (byte) → 255 (int)
//        ➡️ Đây là bước quan trọng để chuẩn hóa dữ liệu!
        java.util.Arrays.sort(values);
        return values[total / 2]; //Tính giá trị trung vị (median) sáng
    }

private static Point[] reorderPoints(Point[] pts) {
    Point[] ordered = new Point[4];

    // Tính tổng (x+y) và hiệu (y-x)
    List<Point> points = Arrays.asList(pts);

    ordered[0] = Collections.min(points, Comparator.comparingDouble(p -> p.x + p.y)); // Trái trên
    ordered[2] = Collections.max(points, Comparator.comparingDouble(p -> p.x + p.y)); // Phải dưới

    ordered[1] = Collections.min(points, Comparator.comparingDouble(p -> p.y - p.x)); // Phải trên
    ordered[3] = Collections.max(points, Comparator.comparingDouble(p -> p.y - p.x)); // Trái dưới

    return ordered;
}

    private static int indexOfMin(double[] arr) { // Tìm vị trí có giá trị nhỏ nhất trong mảng
        int minIdx = 0;
        for (int i = 1; i < arr.length; i++)
            if (arr[i] < arr[minIdx]) minIdx = i;
        return minIdx;
    }

    private static int indexOfMax(double[] arr) { // Tìm vị trí có giá trị lớn nhất trong mảng
        int maxIdx = 0;
        for (int i = 1; i < arr.length; i++)
            if (arr[i] > arr[maxIdx]) maxIdx = i;
        return maxIdx;
    }
    private static Bitmap debugOrderedPoints(Bitmap bmp, Point[] points) {
        Bitmap bmpCopy = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmpCopy);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setTextSize(40);

        for (int i = 0; i < points.length; i++) {
            canvas.drawText("P" + i, (float) points[i].x, (float) points[i].y, paint);
        }
        return bmpCopy;
    }

    private static Bitmap warpPerspective(Bitmap bitmap, MatOfPoint2f points) {//Từ 1 ảnh gốc
        // + 4 đỉnh (tài liệu đã phát hiện), ta biến đổi phối cảnh để cắt, xoay, và duỗi phẳng tờ giấy → giống như scan.

        //📌 Chuyển MatOfPoint2f sang mảng Point[]
        Point[] pts = points.toArray();
        Point[] ordered = reorderPoints(pts); //Gọi hàm reorderPoints để đảm bảo đúng thứ tự góc
        debugSteps.add(debugOrderedPoints(bitmap, ordered)); // Thêm ảnh debug này
        MatOfPoint2f src = new MatOfPoint2f(ordered); //📌 Tạo ma trận điểm nguồn từ ảnh gốc (4 điểm của tài liệu)

//        //Tạo ma trận điểm đích (chuẩn hình chữ nhật) → kích thước ảnh đích: 800 x 1000
//        MatOfPoint2f dst = new MatOfPoint2f(
//                new Point(0, 0), // Góc trên bên trái trong ảnh kết quả
//                new Point(800, 0), // Góc trên bên phải → ngang 800 pixels
//                new Point(800, 1000), // Góc dưới bên phải → cao 1000 pixels, ngang 800 pixels
//                new Point(0, 1000) //Góc dưới bên trái → cao 1000 pixels
//        );//==> Đây là cách “duỗi phẳng” ảnh tài liệu bằng cách ép nó vào khung vuông chuẩn
//        //        📌 Tạo ảnh kết quả với kích thước đích (1000px cao, 800px rộng)
////        Dùng CV_8UC4 để giữ 3 kênh màu (RGB)
//        Mat outputMat = new Mat(1000, 800, CvType.CV_8UC4);
        // ĐOẠN THÊM TÍ NỮA CÒN CHECK
        // 📌 Tính kích thước tài liệu thực tế dựa trên khoảng cách giữa các điểm đã sắp xếp
        double widthA = Math.hypot(ordered[2].x - ordered[3].x, ordered[2].y - ordered[3].y);
        double widthB = Math.hypot(ordered[1].x - ordered[0].x, ordered[1].y - ordered[0].y);
        double maxWidth = Math.max(widthA, widthB); // Chiều ngang lớn nhất

        double heightA = Math.hypot(ordered[1].x - ordered[2].x, ordered[1].y - ordered[2].y);
        double heightB = Math.hypot(ordered[0].x - ordered[3].x, ordered[0].y - ordered[3].y);
        double maxHeight = Math.max(heightA, heightB); // Chiều dọc lớn nhất

// 📌 Tạo ma trận điểm đích (khung chữ nhật chuẩn theo kích thước động)
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(maxWidth, 0),
                new Point(maxWidth, maxHeight),
                new Point(0, maxHeight)
        );

// 📌 Tạo ảnh đầu ra đúng với kích thước thật
        Mat outputMat = new Mat((int) maxHeight, (int) maxWidth, CvType.CV_8UC4);
        //Chuyển Bitmap ảnh gốc thành Mat để OpenCV xử lý
        Mat srcMat = new Mat();
        Utils.bitmapToMat(bitmap, srcMat);
//        📌 Tính ma trận biến đổi phối cảnh
//        src là 4 điểm thực tế trên ảnh gốc
//        dst là khung chữ nhật chuẩn
        Mat transform = Imgproc.getPerspectiveTransform(src, dst); //==> ma trận giúp “uốn cong” hoặc “nắn thẳng” ảnh gốc → khớp với khung chuẩn

//        📌 Dùng ma trận transform để biến đổi ảnh
//        Ảnh đầu ra sẽ là tài liệu được cắt đúng khung + duỗi phẳng
        Imgproc.warpPerspective(srcMat, outputMat, transform, outputMat.size());
//        📌 Chuyển ảnh kết quả từ Mat → Bitmap để hiển thị hoặc lưu
//        Trả về ảnh đã được xử lý
        Bitmap out = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, out);
        return out;
    }

    public static Bitmap drawContoursOnBitmap(Bitmap baseBitmap, List<MatOfPoint> contours) { // Mục đích: nhận vào một ảnh gốc + danh sách contour → vẽ từng contour lên ảnh, tô màu:
        // Tạo bản sao của ảnh gốc để vẽ lên
        Bitmap bmpWithContours = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmpWithContours); // Ý nghĩa: Không vẽ trực tiếp lên ảnh gốc, tạo 1 Canvas để vẽ 1 API của Android

        //Chuẩn bị cọ vẽ (Paint)
        Paint paint = new Paint();
        paint.setStrokeWidth(4); // Viền dày 4px
        paint.setStyle(Paint.Style.STROKE);

        //Dùng để viết chữ ghi số thứ tự và diện tích của contour
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(18f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        //Duyệt qua từng contour
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            Point[] points = contour.toArray();

            // ✅ Kiểm tra số đỉnh bằng approxPolyDP nhằm xác định được số đỉnh thật
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double peri = Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.08 * peri, true);
            int vertexCount = (int) approx.total(); // số đỉnh thực sự
            //Ý nghĩa:
            //giúp đơn giản hóa contour → đếm được số đỉnh

            // Tạo một đường viền khép kín từ các điểm contour
            // Android không có sẵn hàm vẽ MatOfPoint, nên dùng Path
            Path path = new Path();
            if (points.length > 0) {

                path.moveTo((float) points[0].x, (float) points[0].y);
                for (int j = 1; j < points.length; j++) {
                    path.lineTo((float) points[j].x, (float) points[j].y);
                }
                path.close();

                // Đổi màu nếu contour có 4 đỉnh (nghi ngờ là tài liệu)
                //paint.setColor(points.length == 4 ? Color.GREEN : Color.RED);
                paint.setColor(vertexCount >= 4 ? Color.GREEN : Color.RED);
                canvas.drawPath(path, paint);

                // 🎯 Tính trung tâm để đặt text, Tính tâm contour → vẽ chữ
                double centerX = 0, centerY = 0;
                for (Point p : points) {
                    centerX += p.x;
                    centerY += p.y;
                }
                centerX /= points.length;
                centerY /= points.length; // Tính trung bình cộng các tọa độ điểm → ra vị trí chính giữa contour

                // 🎯 Ghi text: số thứ tự + diện tích
                double area = Imgproc.contourArea(contour);
                String label = "#" + (i + 1) + " (area: " + (int) area + ")";
                canvas.drawText(label, (float) centerX, (float) centerY, textPaint);
            }
        }
        return bmpWithContours;
    }


//    public static void saveDebugImages(Context context, List<Bitmap> images) {
//        for (int i = 0; i < images.size(); i++) {
//            String filename = "step_" + (i + 1) + ".png";
//            File file = FileUtils.getNewImageFile(context, filename);
//            try (FileOutputStream out = new FileOutputStream(file)) {
//                images.get(i).compress(Bitmap.CompressFormat.PNG, 100, out);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }



}


