package com.Model;

public class LichSu {
    int id;
    String thoiGian;
    String hanhDong;
    String chiTiet;
    public LichSu(){ }
    public LichSu(int id, String thoiGian, String hanhDong, String chiTiet) {
        this.id = id;
        this.thoiGian = thoiGian;
        this.hanhDong = hanhDong;
        this.chiTiet = chiTiet;
    }

    public int getId() {
        return id;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public String getChiTiet() {
        return chiTiet;
    }
}
