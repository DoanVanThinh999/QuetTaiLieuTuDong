package com.Database1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.Model.DangBaiNghe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class LuuDangBaiNgheSQLite extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lichSuXuatBai.db";
    private static final int DATABASE_VERSION = 13;  // 🔼 Tăng version để áp dụng table mới
    private static final String TABLE_NAME = "lichSuXuatBaiHoc";

    public LuuDangBaiNgheSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "tenBai TEXT, " +
                "LoaiCau TEXT, " +
                "doanDich TEXT, " +
                "textContent TEXT, " +
                "speakerGenders TEXT, " +
                "accent TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN user_id INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textContent TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN speakerGenders TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN accent TEXT");
        }
        // Nếu bạn thêm cột mới sau này, nhớ kiểm tra oldVersion để tránh mất dữ liệu
    }
    // Lấy bản ghi theo fileName và userId
    public DangBaiNghe getDangBaiNgheByFileName(String fileName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        DangBaiNghe dangBaiNghe = null;

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE doanDich = ? AND user_id = ?",
                new String[]{fileName, String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            String tenBai = cursor.getString(cursor.getColumnIndexOrThrow("tenBai"));
            String loaiCau = cursor.getString(cursor.getColumnIndexOrThrow("LoaiCau"));
            String textContent = cursor.getString(cursor.getColumnIndexOrThrow("textContent"));
            String speakerGenders = cursor.getString(cursor.getColumnIndexOrThrow("speakerGenders"));
            String accent = cursor.getString(cursor.getColumnIndexOrThrow("accent"));

            dangBaiNghe = new DangBaiNghe(tenBai, loaiCau, fileName, textContent, speakerGenders, accent);

            cursor.close();
        }

        db.close();
        return dangBaiNghe;
    }

    // Thêm bản ghi
    public boolean insertLichSuTraTu(String tenBai, String loaiCau, String fileName, int userId,
                                     String textContent, String speakerGenders, String accent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tenBai", tenBai);
        values.put("LoaiCau", loaiCau);
        values.put("doanDich", fileName);
        values.put("user_id", userId);
        values.put("textContent", textContent);
        values.put("speakerGenders", speakerGenders);
        values.put("accent", accent);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }
    public void exportDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);

            // ✅ Thay vì dùng Environment.getExternalStorageDirectory()
            File exportDir = new File(context.getExternalFilesDir(null), "MyAppBackup");
            if (!exportDir.exists()) exportDir.mkdirs();

            File dstFile = new File(exportDir, "lichSuXuatBai_backup.db");

            FileChannel src = new FileInputStream(dbFile).getChannel();
            FileChannel dst = new FileOutputStream(dstFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Toast.makeText(context, "✅ Xuất DB thành công:\n" + dstFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "❌ Lỗi khi xuất DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("EXPORT_DB", "Lỗi: ", e);
        }
    }
    public void restoreDatabase(Context context) {
        try {
            File backupFile = new File(context.getExternalFilesDir(null), "MyAppBackup/lichSuXuatBai_backup.db");
            File dbFile = context.getDatabasePath(DATABASE_NAME);

            if (!backupFile.exists()) {
                Toast.makeText(context, "❌ File backup không tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ⚠️ Không cần gọi deleteDatabase 2 lần nữa, chỉ ghi đè
            if (dbFile.exists()) {
                dbFile.delete();
            }

            FileChannel src = new FileInputStream(backupFile).getChannel();
            FileChannel dst = new FileOutputStream(dbFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            // ✅ Kiểm tra file hợp lệ bằng cách mở rồi đóng
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READWRITE
            );
            db.close();

            Toast.makeText(context, "✅ Khôi phục DB thành công!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("RESTORE_DB", "Lỗi khôi phục DB: ", e);
            e.printStackTrace();
            Toast.makeText(context, "❌ Lỗi khôi phục DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }






    // Lấy dữ liệu theo user
    public List<DangBaiNghe> getLichSuTraTuByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DangBaiNghe> lichSuList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                String tenBai = cursor.getString(cursor.getColumnIndexOrThrow("tenBai"));
                String loaiCau = cursor.getString(cursor.getColumnIndexOrThrow("LoaiCau"));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow("doanDich"));
                String textContent = cursor.getString(cursor.getColumnIndexOrThrow("textContent"));
                String speakerGenders = cursor.getString(cursor.getColumnIndexOrThrow("speakerGenders"));
                String accent = cursor.getString(cursor.getColumnIndexOrThrow("accent"));

                DangBaiNghe baiNghe = new DangBaiNghe(tenBai, loaiCau, fileName, textContent, speakerGenders, accent);
                lichSuList.add(baiNghe);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lichSuList;
    }

    // Xóa bản ghi theo fileName và userId
    public boolean deleteByFileName(String fileName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, "doanDich = ? AND user_id = ?",
                new String[]{fileName, String.valueOf(userId)});
        Log.d("DELETE_DB", "Xóa DB: " + fileName + ", userId: " + userId + " → rowsDeleted: " + rowsDeleted);
        db.close();
        return rowsDeleted > 0;
    }

    // Kiểm tra file đã tồn tại chưa
    public boolean isFileExist(String fileName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_NAME + " WHERE doanDich = ? AND user_id = ?",
                new String[]{fileName, String.valueOf(userId)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }
}


//package com.Database1;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;
//
//import com.Model.DangBaiNghe;
//import com.Tra_Nhieu_Tu_Moi.DichTuDoanDai;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class LuuDangBaiNgheSQLite extends SQLiteOpenHelper {
//    private static final String DATABASE_NAME = "lichSuXuatBai.db";
//    private static final int DATABASE_VERSION = 12;
//
//    private static final String TABLE_NAME = "lichSuXuatBaiHoc";
//
//    public LuuDangBaiNgheSQLite(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "user_id INTEGER, " +
//                "tenBai TEXT, " + // tên bài: loại đề
//                "LoaiCau TEXT, " +  // tên file
//                "doanDich TEXT)";
//        db.execSQL(createTable);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion < 2) {
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN user_id INTEGER DEFAULT 0");
//        }
//        if (oldVersion < 3) {
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textContent TEXT");
//        }
//        if (oldVersion < 4) {
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN speakerGenders TEXT");
//        }
//        if (oldVersion < 5) {
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN accent TEXT");
//        }
//    }
//
//
//    // Hàm để chèn dữ liệu vào bảng
//    public boolean insertLichSuTraTu(String tenBai, String loaiCau, String fileName, int userId, String textContent, String speakerGenders,String accent) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("tenBai", tenBai);
//        values.put("LoaiCau", loaiCau);
//        values.put("doanDich", fileName);
//        values.put("user_id", userId);
//        values.put("textContent", textContent); // NEW
//        values.put("speakerGenders", speakerGenders);
//        values.put("accent", accent);
//        long result = db.insert(TABLE_NAME, null, values);
//        db.close();
//        return result != -1;
//    }
//
//
//
//    // Hàm lấy lịch sử tra từ theo user_id
//    public List<DangBaiNghe> getLichSuTraTuByUserId(int userId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        List<DangBaiNghe> lichSuList = new ArrayList<>();
//
//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY id DESC",
//                new String[]{String.valueOf(userId)});
//
//        if (cursor.moveToFirst()) {
//            do {
//                String tenBai = cursor.getString(2);     // tenBai TEXT
//                String loaiCau = cursor.getString(3);    // LoaiCau TEXT
//                String fileName = cursor.getString(4);   // doanDich TEXT
//                String textContent = cursor.getString(5);
//                String speakerGenders = cursor.getString(6);
//                String accent = cursor.getString(7);
//                lichSuList.add(new DangBaiNghe(tenBai, loaiCau, fileName,textContent,speakerGenders,accent));
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//        return lichSuList;
//    }
//    public boolean deleteByFileName(String fileName, int userId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        int rowsDeleted = db.delete(TABLE_NAME, "doanDich = ? AND user_id = ?", new String[]{fileName, String.valueOf(userId)});
//        Log.d("DELETE_DB", "Xóa trong DB: " + fileName + ", userId: " + userId + " → rowsDeleted: " + rowsDeleted);
//        db.close();
//        return rowsDeleted > 0;
//    }
//    public boolean isFileExist(String fileName, int userId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_NAME + " WHERE doanDich = ? AND user_id = ?",
//                new String[]{fileName, String.valueOf(userId)});
//        boolean exists = cursor.moveToFirst();
//        cursor.close();
//        db.close();
//        return exists;
//    }
//
//
//
//
//
//
//}
