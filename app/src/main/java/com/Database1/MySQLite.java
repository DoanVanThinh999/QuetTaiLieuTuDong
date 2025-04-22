package com.Database1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.Model.LichSu;
import com.Model.TaiKhoan;

import java.util.ArrayList;

public class MySQLite extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "banhang.sql";
    public SQLiteDatabase database;
    public MySQLite(Context context, int version) {
        super(context, DATABASE_NAME, null, 5);


        // Tạo bảng LICH_SU_TAI_KHOAN
        String sqlCreateHistoryTable = "CREATE TABLE IF NOT EXISTS LICH_SU_TAI_KHOAN (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "hanh_dong varchar(255), " +
                "chi_tiet varchar(255));";
        querySQL(sqlCreateHistoryTable);

        String sql = "CREATE TABLE IF NOT EXISTS " +
                "TAI_KHOAN ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten_dang_nhap VARCHAR(255), " +
                "mat_khau VARCHAR(255), " +
                "ho_ten VARCHAR(255), " +
                "email VARCHAR(255), " +
                "sdt VARCHAR(255), " +
                "dia_diem VARCHAR(255), " +
                "trinh_do VARCHAR(255), " +
                "vai_tro INTEGER, " +
                "diem_nghe_tich_luy VARCHAR(255), " +
                "diem_noi_tich_luy VARCHAR(255), " +
                "diem_doc_tich_luy VARCHAR(255), " +
                "diem_viet_tich_luy VARCHAR(255), " +
                "diem_tb_tich_luy VARCHAR(255), " +
                "diem_nghe_gan_day VARCHAR(255), " +
                "diem_noi_gan_day VARCHAR(255), " +
                "diem_doc_gan_day VARCHAR(255), " +
                "diem_viet_gan_day VARCHAR(255), " +
                "diem_tb_gan_day VARCHAR(255) );";
        querySQL(sql);

        // Thêm tài khoản mẫu
        themTaiKhoanMau("admin", "123456", "Nguyễn Văn A", "stu725102087@hnue.edu.vn", "0912345678", "Hà Nội", "A1", 1, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
        themTaiKhoanMau("giaovien", "123456", "Trần Thị H", "tranvanc@gmail.com", "0334567898", "Hà Nội", "A2", 2, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
        themTaiKhoanMau("hocsinh", "123456", "Đoàn Văn Thịnh", "stu725102089@hnue.edu.vn", "0334567898", "Nam Định", "B1", 3, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
        themTaiKhoanMau("trogiang", "123456", "Lê Văn Thịnh", "thinhdoan903@gmail.com", "0398718608", "Thanh Hoá", "B2", 4, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
        themTaiKhoanMau("tongDai", "123456", "Phan Thị Lệ Dung", "thinhdoan926@gmail.com", "0904743130", "Hà Nội", "C1", 5, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
    }


    // Thêm tài khoản mẫu vào cơ sở dữ liệu
    private void themTaiKhoanMau(
            String tenDangNhap,
            String matKhau,
            String hoTen,
            String email,
            String sdt,
            String diaDiem,
            String trinhDo,
            int vaiTro,
            String diemNgheTichLuy,
            String diemNoiTichLuy,
            String diemDocTichLuy,
            String diemVietTichLuy,
            String diemTbTichLuy,
            String diemNgheGanDay,
            String diemNoiGanDay,
            String diemDocGanDay,
            String diemVietGanDay,
            String diemTbGanDay
    ) {
        // Kiểm tra xem tài khoản đã tồn tại hay chưa
        String sqlCheck = "SELECT id FROM TAI_KHOAN WHERE ten_dang_nhap = '" + tenDangNhap + "';";
        Cursor cursor = getDataFromSQL(sqlCheck);
        int id = 0;
        while (cursor.moveToNext()) {
            id = cursor.getInt(0);
        }

        if (id <= 0) {
            // Câu lệnh SQL để thêm tài khoản với các giá trị điểm được truyền vào
            String sqlInsert = "INSERT INTO TAI_KHOAN (ten_dang_nhap, mat_khau, ho_ten, email, sdt, dia_diem, trinh_do, vai_tro, " +
                    "diem_nghe_tich_luy, diem_noi_tich_luy, diem_doc_tich_luy, diem_viet_tich_luy, diem_tb_tich_luy, " +
                    "diem_nghe_gan_day, diem_noi_gan_day, diem_doc_gan_day, diem_viet_gan_day, diem_tb_gan_day) " +
                    "VALUES ('" + tenDangNhap + "', '" + matKhau + "', '" + hoTen + "', '" + email + "', '" + sdt + "', '" + diaDiem + "', '" + trinhDo + "', " + vaiTro + ", " +
                    diemNgheTichLuy + ", " + diemNoiTichLuy + ", " + diemDocTichLuy + ", " + diemVietTichLuy + ", " + diemTbTichLuy + ", " +
                    diemNgheGanDay + ", " + diemNoiGanDay + ", " + diemDocGanDay + ", " + diemVietGanDay + ", " + diemTbGanDay + ");";

            // Thực thi câu lệnh SQL
            querySQL(sqlInsert);

            // Ghi lịch sử hoạt động
            luuLichSu("Thêm tài khoản", "Thêm tài khoản: " + tenDangNhap);
        }

        // Đóng con trỏ dữ liệu
        cursor.close();
    }








    // Ghi lịch sử vào bảng LICH_SU_TAI_KHOAN
    public void luuLichSu(String hanhDong, String chiTiet) {
        String sqlInsertHistory = "INSERT INTO LICH_SU_TAI_KHOAN (thoi_gian, hanh_dong, chi_tiet) " +
                "VALUES (DATETIME('now', 'localtime'), '" + hanhDong + "', '" + chiTiet + "');";
        querySQL(sqlInsertHistory);
    }

    public ArrayList<LichSu> xemLichSu() {
        ArrayList<LichSu> danhSachLichSu = new ArrayList<>();
        String sql = "SELECT thoi_gian, hanh_dong, chi_tiet FROM LICH_SU_TAI_KHOAN ORDER BY thoi_gian DESC;";
        Cursor cursor = getDataFromSQL(sql);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String thoiGian = cursor.getString(0); // Thời gian
                String hanhDong = cursor.getString(1); // Hành động
                String chiTiet = cursor.getString(2);  // Chi tiết

                // Tạo đối tượng LichSu và thêm vào danh sách
                danhSachLichSu.add(new LichSu(0, thoiGian, hanhDong, chiTiet));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return danhSachLichSu;
    }


    // Truy vấn không trả về kết quả
    public void querySQL(String sql) {
        database = getWritableDatabase();
        database.execSQL(sql);
    }

    // Truy vấn có trả về dữ liệu
    public Cursor getDataFromSQL(String sql) {
        database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }
    public boolean kiemTraDN(String tenDangNhap) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("TAI_KHOAN", new String[]{"id"}, "ten_dang_nhap = ?", new String[]{tenDangNhap}, null, null, null);

        boolean isExist = cursor.getCount() > 0; // Nếu tìm thấy tài khoản có tên đăng nhập trùng
        cursor.close();
        db.close();
        return isExist; // Trả về true nếu tên đăng nhập đã tồn tại, false nếu chưa có.
    }
    public void themTaiKhoanM(String tenDangNhap, String matKhau, String hoTen, String email, String soDienThoai,String diaDiem, String trinhDo, int vaiTro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ten_dang_nhap", tenDangNhap);
        values.put("mat_khau", matKhau);
        values.put("ho_ten", hoTen);
        values.put("email", email);
        values.put("sdt", soDienThoai);
        values.put("dia_diem", diaDiem);
        values.put("trinh_do", trinhDo);
        values.put("vai_tro", vaiTro);
        // Thêm thông tin điểm với giá trị mặc định 0.0
        values.put("diem_nghe_tich_luy", 0.0);
        values.put("diem_noi_tich_luy", 0.0);
        values.put("diem_doc_tich_luy", 0.0);
        values.put("diem_viet_tich_luy", 0.0);
        values.put("diem_tb_tich_luy", 0.0);
        values.put("diem_nghe_gan_day", 0.0);
        values.put("diem_noi_gan_day", 0.0);
        values.put("diem_doc_gan_day", 0.0);
        values.put("diem_viet_gan_day", 0.0);
        values.put("diem_tb_gan_day", 0.0);
        db.insert("TAI_KHOAN", null, values);
        luuLichSu("Đăng ký tài khoản", "Đăng ký tài khoản: " + tenDangNhap);
        db.close();
    }

    // Kiểm tra đăng nhập
    public TaiKhoan kiemTraDangNhap(String tenDangNhap, String matKhau) {
        // Tạo SQL query
        String sql;
        if (matKhau.isEmpty()) {
            // Chỉ kiểm tra tên đăng nhập
            sql = "SELECT * FROM TAI_KHOAN WHERE ten_dang_nhap = '" + tenDangNhap + "';";
        } else {
            // Kiểm tra cả tên đăng nhập và mật khẩu
            sql = "SELECT * FROM TAI_KHOAN WHERE ten_dang_nhap = '" + tenDangNhap + "' AND mat_khau = '" + matKhau + "';";
        }

        // Thực thi query
        Cursor cursor = getDataFromSQL(sql);
        TaiKhoan taiKhoan = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            String tenDN = cursor.getString(1);
            String mk = cursor.getString(2);
            String ht = cursor.getString(3);
            String email = cursor.getString(4);
            String sdt = cursor.getString(5);
            String diaDiem = cursor.getString(6);
            String trinhDo = cursor.getString(7);
            int vaiTro = cursor.getInt(8);

            // Lấy dữ liệu về điểm
            String diemNgheTichLuy = cursor.getString(9);
            String diemNoiTichLuy = cursor.getString(10);
            String diemDocTichLuy = cursor.getString(11);
            String diemVietTichLuy = cursor.getString(12);
            String diemTbTichLuy = cursor.getString(13);
            String diemNgheGanDay = cursor.getString(14);
            String diemNoiGanDay = cursor.getString(15);
            String diemDocGanDay = cursor.getString(16);
            String diemVietGanDay = cursor.getString(17);
            String diemTbGanDay = cursor.getString(18);

            // Khởi tạo đối tượng TaiKhoan với đầy đủ thông tin
            taiKhoan = new TaiKhoan(id, tenDN, mk, ht, email, sdt, diaDiem, trinhDo, vaiTro,
                    diemNgheTichLuy, diemNoiTichLuy, diemDocTichLuy, diemVietTichLuy, diemTbTichLuy,
                    diemNgheGanDay, diemNoiGanDay, diemDocGanDay, diemVietGanDay, diemTbGanDay);
        }
        cursor.close();
        return taiKhoan; // Trả về null nếu không tìm thấy
    }
    public boolean capNhatMatKhau(String tenDangNhap, String matKhauMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mat_khau", matKhauMoi);

        int rows = db.update("TAI_KHOAN", values, "ten_dang_nhap = ?", new String[]{tenDangNhap});
        db.close();
        return rows > 0; // Trả về true nếu cập nhật thành công
    }

    // Lấy danh sách tài khoản
    public ArrayList<TaiKhoan> docDuLieuTaiKhoan(String sql) {
        ArrayList<TaiKhoan> listTaiKhoan = new ArrayList<>();
        Cursor cursor = getDataFromSQL(sql);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String tenDN = cursor.getString(1);
            String mk = cursor.getString(2);
            String hoTen = cursor.getString(3);
            String email = cursor.getString(4);
            String sdt = cursor.getString(5);
            String dia_diem = cursor.getString(6);
            String trinh_do = cursor.getString(7);
            int vaiTro = cursor.getInt(8);
            // Lấy dữ liệu về điểm
            String diemNgheTichLuy = cursor.getString(9);
            String diemNoiTichLuy = cursor.getString(10);
            String diemDocTichLuy = cursor.getString(11);
            String diemVietTichLuy = cursor.getString(12);
            String diemTbTichLuy = cursor.getString(13);
            String diemNgheGanDay = cursor.getString(14);
            String diemNoiGanDay = cursor.getString(15);
            String diemDocGanDay = cursor.getString(16);
            String diemVietGanDay = cursor.getString(17);
            String diemTbGanDay = cursor.getString(18);
            // Khởi tạo đối tượng TaiKhoan với đầy đủ thông tin
            TaiKhoan taiKhoan = new TaiKhoan(id, tenDN, mk, hoTen, email, sdt, dia_diem, trinh_do, vaiTro,
                    diemNgheTichLuy, diemNoiTichLuy, diemDocTichLuy, diemVietTichLuy, diemTbTichLuy,
                    diemNgheGanDay, diemNoiGanDay, diemDocGanDay, diemVietGanDay, diemTbGanDay);
            listTaiKhoan.add(taiKhoan);
        }
        cursor.close();
        return listTaiKhoan;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TAI_KHOAN");
        db.execSQL("DROP TABLE IF EXISTS LICH_SU_TAI_KHOAN");
        onCreate(db);
    }
}
