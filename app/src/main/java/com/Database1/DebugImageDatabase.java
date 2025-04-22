package com.Database1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DebugImageDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "debug_images.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "debug_images";

    public DebugImageDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS debug_images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "image_path TEXT," +
                "filepath TEXT," +
                "timestamp TEXT," +
                "user_id INTEGER DEFAULT 0" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nếu cần sửa DB khi nâng cấp version:
        db.execSQL("DROP TABLE IF EXISTS debug_images");
        onCreate(db);
    }


    public void insertImage(String filename, String filepath, String timestamp, int user_id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE filepath = ? AND user_id = ?", new String[]{filepath, String.valueOf(user_id)});
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put("image_path", filename);
            values.put("filepath", filepath);
            values.put("timestamp", timestamp);
            values.put("user_id", user_id);
            db.insert(TABLE_NAME, null, values);
        }
        cursor.close();
    }


    public void deleteByPath(String filepath, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "filepath = ? AND user_id = ?", new String[]{filepath, String.valueOf(userId)});
    }


    public ArrayList<String> getAllPaths(int userId) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT filepath FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }



}
