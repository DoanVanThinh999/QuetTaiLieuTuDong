package com;

import java.io.Serializable;

public class LienHe implements Serializable {
    String id;
    String tenLienHe;
    String soDienThoai;
    String email;
    String anhDaiDien;
    public LienHe(){}

    public LienHe(String id, String tenLienHe, String soDienThoai, String email, String anhDaiDien) {
        this.id = id;
        this.tenLienHe = tenLienHe;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.anhDaiDien = anhDaiDien;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenLienHe() {
        return tenLienHe;
    }

    public void setTenLienHe(String tenLienHe) {
        this.tenLienHe = tenLienHe;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    @Override
    public String toString() {
        return "LienHe{" +
                "id='" + id + '\'' +
                ", tenLienHe='" + tenLienHe + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", email='" + email + '\'' +
                ", anhDaiDien='" + anhDaiDien + '\'' +
                '}';
    }
}

