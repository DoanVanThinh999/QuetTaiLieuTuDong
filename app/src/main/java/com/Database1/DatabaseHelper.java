// ✅ DatabaseHelper.java - Đã sửa đầy đủ để hỗ trợ MULTIPLE_CHOICE

package com.Database1;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.Nullable;

import com.Model.CauHoi;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quiz_app.db";
    private static final int DATABASE_VERSION = 20;

    // Tạo bảng câu hỏi (đã thêm dap_an_dung_list)
    private static final String CREATE_TABLE_QUESTIONS = "CREATE TABLE questions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "noi_dung TEXT," +
            "quiz_type TEXT," +
            "dap_an_list TEXT," +
            "dap_an_dung_list TEXT," +  // ✅ thêm để hỗ trợ MULTIPLE_CHOICE
            "chi_so_dung INTEGER," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");";
    private static final String CREATE_TABLE_MOC_CAU_HOI = "CREATE TABLE cau_hoi_theo_moc (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "marker_time INTEGER," +
            "video_path TEXT," +  // ✅ Thêm dòng này
            "noi_dung TEXT," +
            "quiz_type TEXT," +
            "dap_an_list TEXT," +
            "dap_an_dung_list TEXT," +
            "chi_so_dung INTEGER," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");";

    private static final String CREATE_TABLE_SEARCH_HISTORY = "CREATE TABLE IF NOT EXISTS search_history (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "query_tk TEXT" +
            ");";

    private static final String CREATE_TABLE_SO_LAN_XEM_VIDEO = "CREATE TABLE IF NOT EXISTS so_lan_xem_video (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "video_uri TEXT," +  // ✅ sửa lại đúng tên cột
            "so_lan_xem_hien_tai INTEGER DEFAULT 0" +
            ");";
    private static final String CREATE_TABLE_DIEM_XEM_VIDEO = "CREATE TABLE diem_xem_video (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "video_uri TEXT," +  // ✅ Thêm dòng này
            "lan_xem INTEGER," +
            "diem REAL"+
            ");";
    private static final String CREATE_TABLE_VIDEO_PUBLIC = "CREATE TABLE video_public (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "video_uri TEXT," +
            "video_title TEXT," +
            "cau_hoi_theo_moc BLOB," +
            "so_lan_xem TEXT," +
            "phuong_thuc_diem TEXT," +
            "xu_ly_khi_sai TEXT," +
            "phan_tram_diem_dung INTEGER," +
            "so_lan_tra_loi_lai INTEGER," +
            "tru_diem_moi_lan INTEGER," +   // ✅ Thêm dòng này
            "diem REAL" +
            ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_QUESTIONS);
        db.execSQL(CREATE_TABLE_DIEM_XEM_VIDEO);
        db.execSQL(CREATE_TABLE_VIDEO_PUBLIC );
        db.execSQL(CREATE_TABLE_MOC_CAU_HOI); // ← gọi bảng mới này
        db.execSQL(CREATE_TABLE_SEARCH_HISTORY); // 👈 Thêm dòng này
        db.execSQL(CREATE_TABLE_SO_LAN_XEM_VIDEO); // ✅ Gọi lệnh tạo bảng đúng
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS questions");
        db.execSQL("DROP TABLE IF EXISTS cau_hoi_theo_moc"); // ✅ Thêm dòng này
        db.execSQL("DROP TABLE IF EXISTS so_lan_xem_video"); // ✅ nếu cần reset version
        db.execSQL("DROP TABLE IF EXISTS diem_xem_video");
        db.execSQL("DROP TABLE IF EXISTS video_public");
        onCreate(db);
    }
    public boolean daTonTaiVideoPublic(String videoUri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM video_public WHERE video_uri = ?", new String[]{videoUri});
        boolean result = false;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0) > 0;
        }
        cursor.close();
        return result;
    }
    public boolean capNhatVideoPublic(String videoUri, String videoTitle, String soLanXem,
                                      String phuongThucDiem, String xuLyKhiSai, int phanTramDung,
                                      double diemLuu, @Nullable Integer soLanTraLoiLai, @Nullable Integer truDiemMoiLan) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("video_title", videoTitle);
            values.put("so_lan_xem", soLanXem);
            values.put("phuong_thuc_diem", phuongThucDiem);
            values.put("xu_ly_khi_sai", xuLyKhiSai);
            values.put("phan_tram_diem_dung", phanTramDung);
            values.put("diem", diemLuu);

            // ✅ Nếu là "Trả lời lại" mới update các cột này
            if ("Trả lời lại".equals(xuLyKhiSai) && soLanTraLoiLai != null && truDiemMoiLan != null) {
                values.put("so_lan_tra_loi_lai", soLanTraLoiLai);
                values.put("tru_diem_moi_lan", truDiemMoiLan);
            }

            int rows = db.update("video_public", values, "video_uri = ?", new String[]{videoUri});
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Thứ hai: Hàm lưu video public (bao gồm toàn bộ câu hỏi theo mốc)
    public boolean themVideoPublic(String videoUri, int userId, String phuongThucDiem, String soLanXemStr,
                                   int phanTramDung, String xuLyKhiSai, double diemLuu,
                                   HashMap<Long, ArrayList<CauHoi>> cauHoiTheoMoc,
                                   String videoTitle,int soLanTraLoiLai,
                                   int truDiemMoiLan) { // ✅ Thêm title ở cuối
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // ✅ Tạo bảng có thêm cột video_title
            db.execSQL("CREATE TABLE IF NOT EXISTS video_public (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "video_uri TEXT," +
                    "user_id INTEGER," +
                    "video_title TEXT," +  // ✅ THÊM DÒNG NÀY
                    "phuong_thuc_diem TEXT," +
                    "so_lan_xem TEXT," +
                    "phan_tram_diem_dung INTEGER," +
                    "xu_ly_khi_sai TEXT," +
                    "diem REAL," +
                    "so_lan_tra_loi_lai INTEGER" +
                    ")");

            ContentValues values = new ContentValues();
            values.put("video_uri", videoUri);
            values.put("user_id", userId);
            values.put("video_title", videoTitle); // ✅ Gán tên video vào DB
            values.put("phuong_thuc_diem", phuongThucDiem);
            values.put("so_lan_xem", soLanXemStr);
            values.put("phan_tram_diem_dung", phanTramDung);
            values.put("xu_ly_khi_sai", xuLyKhiSai);
            values.put("diem", diemLuu);
            if (xuLyKhiSai.equals("Trả lời lại")) {
                values.put("so_lan_tra_loi_lai", soLanTraLoiLai);
                values.put("tru_diem_moi_lan", truDiemMoiLan); // ✅ Thêm dòng này
            }

            long insertId = db.insert("video_public", null, values);

            if (insertId != -1) {
                for (Long markerTime : cauHoiTheoMoc.keySet()) {
                    ArrayList<CauHoi> dsCauHoi = cauHoiTheoMoc.get(markerTime);
                    for (CauHoi ch : dsCauHoi) {
                        this.addCauHoiTheoMoc(markerTime, videoUri, ch.noiDung, ch.quizType,
                                String.join(",", ch.dapAnList),
                                ch.dapAnDungList.toString().replace("[", "").replace("]", ""),
                                ch.chiSoDung);
                    }
                }
            }

            return insertId != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getStringFieldFromVideoPublic(String videoUri, String columnName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + columnName + " FROM video_public WHERE video_uri = ?", new String[]{videoUri});
        String result = "";
        if (cursor.moveToFirst()) result = cursor.getString(0);
        cursor.close();
        return result;
    }

    public int getIntFieldFromVideoPublic(String videoUri, String columnName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + columnName + " FROM video_public WHERE video_uri = ?", new String[]{videoUri});
        int result = 0;
        if (cursor.moveToFirst()) result = cursor.getInt(0);
        cursor.close();
        return result;
    }
    public ArrayList<String[]> getAllVideoUriWithTitle() {
        ArrayList<String[]> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT video_uri, video_title FROM video_public ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String uri = cursor.getString(0);
                String title = cursor.getString(1);
                list.add(new String[]{uri, title});
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }





    // Tăng số lần xem hoặc insert mới nếu chưa có
    public void tangSoLanXem(int userId, String videoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT so_lan_xem_hien_tai FROM so_lan_xem_video WHERE user_id = ? AND video_uri = ?",
                new String[]{String.valueOf(userId), videoUri});
        if (cursor.moveToFirst()) {
            int soLan = cursor.getInt(0) + 1;
            db.execSQL("UPDATE so_lan_xem_video SET so_lan_xem_hien_tai = ? WHERE user_id = ? AND video_uri = ?",
                    new Object[]{soLan, userId, videoUri});
        } else {
            db.execSQL("INSERT INTO so_lan_xem_video(user_id, video_uri, so_lan_xem_hien_tai) VALUES (?, ?, ?)",
                    new Object[]{userId, videoUri, 1});
        }
        cursor.close();
    }

    // Lấy số lần xem hiện tại
    public int getSoLanXem(int userId, String videoUri) {
        if (videoUri == null) {
            Log.e("DB_ERROR", "❌ videoUri null trong getSoLanXem");
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT so_lan_xem_hien_tai FROM so_lan_xem_video WHERE user_id = ? AND video_uri = ?",
                new String[]{String.valueOf(userId), videoUri});

        int result = 0;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    public void saveDiemLanXem(int userId, String videoUri, int lanXem, double diem) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS diem_xem_video (user_id INTEGER, video_uri TEXT, lan_xem INTEGER, diem REAL)");
        db.execSQL("INSERT INTO diem_xem_video(user_id, video_uri, lan_xem, diem) VALUES (?, ?, ?, ?)",
                new Object[]{userId, videoUri, lanXem, diem});
    }
    public double getDiemLanXem(int userId, String videoUri, int lanXem) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT diem FROM diem_xem_video WHERE user_id = ? AND video_uri = ? AND lan_xem = ?",
                new String[]{String.valueOf(userId), videoUri, String.valueOf(lanXem)});
        double diem = 0;
        if (c.moveToFirst()) diem = c.getDouble(0);
        c.close();
        return diem;
    }

    public double getDiemCaoNhat(int userId, String videoUri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(diem) FROM diem_xem_video WHERE user_id = ? AND video_uri = ?",
                new String[]{String.valueOf(userId), videoUri});
        double diem = 0;
        if (c.moveToFirst()) diem = c.getDouble(0);
        c.close();
        return diem;
    }

    public double getDiemTrungBinh(int userId, String videoUri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT AVG(diem) FROM diem_xem_video WHERE user_id = ? AND video_uri = ?",
                new String[]{String.valueOf(userId), videoUri});
        double diem = 0;
        if (c.moveToFirst()) diem = c.getDouble(0);
        c.close();
        return diem;
    }

    // Để đó tí fix tiêp
    public HashMap<Long, ArrayList<CauHoi>> getTatCaCauHoiTheoMoc(String videoPath) {
        HashMap<Long, ArrayList<CauHoi>> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("cau_hoi_theo_moc", null, "video_path = ?", new String[]{videoPath}, null, null, "marker_time ASC, timestamp ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long markerTime = cursor.getLong(cursor.getColumnIndex("marker_time"));
                @SuppressLint("Range") String noiDung = cursor.getString(cursor.getColumnIndex("noi_dung"));
                @SuppressLint("Range") String quizType = cursor.getString(cursor.getColumnIndex("quiz_type"));
                @SuppressLint("Range") String dapAnListStr = cursor.getString(cursor.getColumnIndex("dap_an_list"));
                @SuppressLint("Range") String dapAnDungListStr = cursor.getString(cursor.getColumnIndex("dap_an_dung_list"));  // ✅ BỔ SUNG DÒNG NÀY
                @SuppressLint("Range") int chiSoDung = cursor.getInt(cursor.getColumnIndex("chi_so_dung"));

                CauHoi cauHoi = new CauHoi();
                cauHoi.noiDung = noiDung;
                cauHoi.quizType = quizType;
                cauHoi.chiSoDung = chiSoDung;
                cauHoi.dapAnList = new ArrayList<>(Arrays.asList(dapAnListStr.split(",")));

                // MULTIPLE_CHOICE: chuyển về ArrayList<Boolean>
                if (quizType.equals("MULTIPLE_CHOICE")) {
                    ArrayList<Boolean> dungList = new ArrayList<>();
                    for (String s : dapAnDungListStr.split(",")) {
                        dungList.add(Boolean.parseBoolean(s.trim()));
                    }
                    cauHoi.dapAnDungList = dungList;
                }

                // ORDER_SEQUENCE: chuyển về ArrayList<Integer>
                else if (quizType.equals("ORDER_SEQUENCE")) {
                    ArrayList<Boolean> orderList = new ArrayList<>();
                    for (String s : dapAnDungListStr.split(",")) {
                        try {
                            orderList.add(Integer.parseInt(s.trim()) == cauHoi.dapAnList.indexOf(s.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                    cauHoi.dapAnDungList = orderList;
                }



                if (!map.containsKey(markerTime)) {
                    map.put(markerTime, new ArrayList<>());
                }
                map.get(markerTime).add(cauHoi);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return map;
    }


    public void saveSearchHistory(int userId, String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("query_tk", query);
        db.insert("search_history", null, values);
        db.close();
    }


    public ArrayList<Long> getAllMarkerTimesByVideoPath1(String videoPath) {
        ArrayList<Long> markerTimes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                true, // distinct
                "cau_hoi_theo_moc",
                new String[]{"marker_time"},
                "video_path = ?",
                new String[]{videoPath},
                null, null, "marker_time ASC", null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                long marker = cursor.getLong(cursor.getColumnIndex("marker_time"));
                markerTimes.add(marker);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return markerTimes;
    }


    public ArrayList<String> getSearchHistory(int userId) {
        ArrayList<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT query_tk FROM search_history WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                history.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return history;
    }

    public void deleteSearchHistory(int userId, String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("search_history", "user_id=? AND query=?", new String[]{String.valueOf(userId), query});
        db.close();
    }


    // ✅ Cập nhật câu hỏi
    public int updateQuestion(int questionId, String noiDung, String quizType, String dapAnList, int chiSoDung, String dapAnDungList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("noi_dung", noiDung);
        values.put("quiz_type", quizType);
        values.put("dap_an_list", dapAnList);
        values.put("chi_so_dung", chiSoDung);
        values.put("dap_an_dung_list", dapAnDungList);
        return db.update("questions", values, "id = ?", new String[]{String.valueOf(questionId)});
    }
    public long addCauHoiTheoMoc(long markerTime, String videoPath, String noiDung, String quizType, String dapAnList, String dapAnDungList, int chiSoDung) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marker_time", markerTime);
        values.put("video_path", videoPath);
        values.put("noi_dung", noiDung);
        values.put("quiz_type", quizType);
        values.put("dap_an_list", dapAnList);
        values.put("dap_an_dung_list", dapAnDungList);
        values.put("chi_so_dung", chiSoDung);
        return db.insert("cau_hoi_theo_moc", null, values);
    }
    public Cursor getCauHoiTheoVideoVaMoc(String videoPath, long markerTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                "cau_hoi_theo_moc",
                null,
                "video_path = ? AND marker_time = ?",
                new String[]{videoPath, String.valueOf(markerTime)},
                null, null, "timestamp ASC"
        );
    }

    public void logAllCauHoiTheoMoc() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cau_hoi_theo_moc", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String videoPath = cursor.getString(cursor.getColumnIndex("video_path"));
                @SuppressLint("Range") long marker = cursor.getLong(cursor.getColumnIndex("marker_time"));
                @SuppressLint("Range") String noiDung = cursor.getString(cursor.getColumnIndex("noi_dung"));
                @SuppressLint("Range") String quizType = cursor.getString(cursor.getColumnIndex("quiz_type"));
                @SuppressLint("Range") String dapAnList = cursor.getString(cursor.getColumnIndex("dap_an_list"));
                @SuppressLint("Range") String dapAnDungList = cursor.getString(cursor.getColumnIndex("dap_an_dung_list"));
                @SuppressLint("Range") int chiSoDung = cursor.getInt(cursor.getColumnIndex("chi_so_dung"));

                Log.d("CHECK_DB", "🎯 marker = " + marker + ", video = " + videoPath);
                Log.d("CHECK_DB", "  ➤ " + noiDung + " | Type: " + quizType);
                Log.d("CHECK_DB", "  📌 Đáp án: " + dapAnList);
                Log.d("CHECK_DB", "  ✅ Đáp án đúng (multi): " + dapAnDungList);
                Log.d("CHECK_DB", "  ☑️ Chỉ số đúng: " + chiSoDung);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.w("CHECK_DB", "⚠️ Không có dữ liệu trong bảng cau_hoi_theo_moc.");
        }
    }




    @SuppressLint("Range")
    public void upsertCauHoiTheoMoc(long markerTime, String videoPath, String noiDung, String quizType, String dapAnList, String dapAnDungList, int chiSoDung, int index) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Truy vấn tất cả câu hỏi tại marker đó
        Cursor cursor = db.query(
                "cau_hoi_theo_moc",
                new String[]{"id"},
                "video_path = ? AND marker_time = ?",
                new String[]{videoPath, String.valueOf(markerTime)},
                null, null, "timestamp ASC"
        );

        int idToUpdate = -1;

        if (cursor != null && cursor.moveToPosition(index)) {
            idToUpdate = cursor.getInt(cursor.getColumnIndex("id"));
        }

        ContentValues values = new ContentValues();
        values.put("marker_time", markerTime);
        values.put("video_path", videoPath);
        values.put("noi_dung", noiDung);
        values.put("quiz_type", quizType);
        values.put("dap_an_list", dapAnList);
        values.put("dap_an_dung_list", dapAnDungList);
        values.put("chi_so_dung", chiSoDung);

        if (idToUpdate != -1) {
            db.update("cau_hoi_theo_moc", values, "id = ?", new String[]{String.valueOf(idToUpdate)});
            Log.d("UPSERT", "🔁 Cập nhật câu hỏi ID = " + idToUpdate + " tại index = " + index);
        } else {
            db.insert("cau_hoi_theo_moc", null, values);
            Log.d("UPSERT", "➕ Thêm mới câu hỏi tại index = " + index);
        }

        if (cursor != null) cursor.close();
    }


    public int deleteCauHoiTheoMoc(String videoPath, long markerTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("cau_hoi_theo_moc", "video_path = ? AND marker_time = ?", new String[]{videoPath, String.valueOf(markerTime)});
    }

    public void removeDuplicateQuestionsByVideoAndMarker(String videoPath, long markerTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getCauHoiTheoVideoVaMoc(videoPath, markerTime);

        ArrayList<String> seen = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                String noiDung = cursor.getString(cursor.getColumnIndex("noi_dung"));

                @SuppressLint("Range")
                int id = cursor.getInt(cursor.getColumnIndex("id"));

                if (seen.contains(noiDung)) {
                    db.delete("cau_hoi_theo_moc", "id = ?", new String[]{String.valueOf(id)});
                    Log.d("REMOVE_DUPLICATE", "🗑 Xoá trùng: " + noiDung);
                } else {
                    seen.add(noiDung);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}
