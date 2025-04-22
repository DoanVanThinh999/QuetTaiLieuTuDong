package com.Model;

import java.io.Serializable;

public class TaiKhoan implements Serializable{
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private String sdt;
    private String diaDiem;
    private String trinhDo;
    private int vaiTro;
    private String diemNgheTichLuy;
    private String diemNoiTichLuy;
    private String diemDocTichLuy;
    private String diemVietTichLuy;
    private String diemTbTichLuy;
    private String diemNgheGanDay;
    private String diemNoiGanDay;
    private String diemDocGanDay;
    private String diemVietGanDay;
    private String diemTbGanDay;

    // Constructor không tham số (bắt buộc cho Firebase)
    public TaiKhoan(String firebaseKey, String tenDangNhap, String matKhau, String hoTen, String email, String soDienThoai, String diaDiem, String trinhDo, int vaiTro, String diemNgheTichLuy, String diemNoiTichLuy, String diemDocTichLuy, String diemVietTichLuy, String diemTbTichLuy, String diemNgheGanDay, String diemNoiGanDay, String diemDocGanDay, String diemVietGanDay, String diemTbGanDay) {
        // Để trống
    }

    // Constructor đầy đủ tham số
    public TaiKhoan(int id, String tenDangNhap, String matKhau, String hoTen, String email, String sdt, String diaDiem,
                    String trinhDo, int vaiTro, String diemNgheTichLuy, String diemNoiTichLuy, String diemDocTichLuy,
                    String diemVietTichLuy, String diemTbTichLuy, String diemNgheGanDay, String diemNoiGanDay,
                    String diemDocGanDay, String diemVietGanDay, String diemTbGanDay) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.email = email;
        this.sdt = sdt;
        this.diaDiem = diaDiem;
        this.trinhDo = trinhDo;
        this.vaiTro = vaiTro;
        this.diemNgheTichLuy = diemNgheTichLuy;
        this.diemNoiTichLuy = diemNoiTichLuy;
        this.diemDocTichLuy = diemDocTichLuy;
        this.diemVietTichLuy = diemVietTichLuy;
        this.diemTbTichLuy = diemTbTichLuy;
        this.diemNgheGanDay = diemNgheGanDay;
        this.diemNoiGanDay = diemNoiGanDay;
        this.diemDocGanDay = diemDocGanDay;
        this.diemVietGanDay = diemVietGanDay;
        this.diemTbGanDay = diemTbGanDay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public String getTrinhDo() {
        return trinhDo;
    }

    public void setTrinhDo(String trinhDo) {
        this.trinhDo = trinhDo;
    }

    public int getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(int vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getDiemNgheTichLuy() {
        return diemNgheTichLuy;
    }

    public void setDiemNgheTichLuy(String diemNgheTichLuy) {
        this.diemNgheTichLuy = diemNgheTichLuy;
    }

    public String getDiemDocTichLuy() {
        return diemDocTichLuy;
    }

    public void setDiemDocTichLuy(String diemDocTichLuy) {
        this.diemDocTichLuy = diemDocTichLuy;
    }

    public String getDiemNoiTichLuy() {
        return diemNoiTichLuy;
    }

    public void setDiemNoiTichLuy(String diemNoiTichLuy) {
        this.diemNoiTichLuy = diemNoiTichLuy;
    }

    public String getDiemVietTichLuy() {
        return diemVietTichLuy;
    }

    public void setDiemVietTichLuy(String diemVietTichLuy) {
        this.diemVietTichLuy = diemVietTichLuy;
    }

    public String getDiemTbTichLuy() {
        return diemTbTichLuy;
    }

    public void setDiemTbTichLuy(String diemTbTichLuy) {
        this.diemTbTichLuy = diemTbTichLuy;
    }

    public String getDiemNgheGanDay() {
        return diemNgheGanDay;
    }

    public void setDiemNgheGanDay(String diemNgheGanDay) {
        this.diemNgheGanDay = diemNgheGanDay;
    }

    public String getDiemNoiGanDay() {
        return diemNoiGanDay;
    }

    public void setDiemNoiGanDay(String diemNoiGanDay) {
        this.diemNoiGanDay = diemNoiGanDay;
    }

    public String getDiemDocGanDay() {
        return diemDocGanDay;
    }

    public void setDiemDocGanDay(String diemDocGanDay) {
        this.diemDocGanDay = diemDocGanDay;
    }

    public String getDiemVietGanDay() {
        return diemVietGanDay;
    }

    public void setDiemVietGanDay(String diemVietGanDay) {
        this.diemVietGanDay = diemVietGanDay;
    }

    public String getDiemTbGanDay() {
        return diemTbGanDay;
    }

    public void setDiemTbGanDay(String diemTbGanDay) {
        this.diemTbGanDay = diemTbGanDay;
    }
}