package com.Tra_Nhieu_Tu_Moi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TraNhieuTuMoiSQLite extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lichSuTraTu.db";
    private static final int DATABASE_VERSION = 2; // Tăng version lên 2

    private static final String TABLE_NAME = "lichSuTraTu";

    public TraNhieuTuMoiSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "tenBai TEXT, " +
                "doanDich TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Thêm cột user_id nếu chưa có
            String alterTable = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN user_id INTEGER DEFAULT 0";
            db.execSQL(alterTable);
        }
    }

    // Hàm để chèn dữ liệu vào bảng
    public boolean insertLichSuTraTu(String tenBai, String doanDich, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tenBai", tenBai);

        contentValues.put("doanDich", doanDich);
        contentValues.put("user_id", userId);
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    // Hàm lấy lịch sử tra từ theo user_id
    public List<DichTuDoanDai> getLichSuTraTuByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<DichTuDoanDai> lichSuList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                // Lấy giá trị các cột từ cursor
                String tenBai = cursor.getString(2); // tenBai = cột thứ 3
                String doanDich = cursor.getString(3); // doanDich = cột thứ 4
                lichSuList.add(new DichTuDoanDai(tenBai, doanDich));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lichSuList;
    }


    public boolean deleteLichSuTraTu(String tuTra, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, "tenBai = ? AND user_id = ?", new String[]{tuTra, String.valueOf(userId)});
        db.close();
        return rowsDeleted > 0; // Trả về true nếu xóa thành công
    }
}

