package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.NguPhap;
/*
Tran Thanh Nhan 20/7/2020
* */
public class NguPhap {

    int Id;
    String Ten, CauKD, CauPD, CauNV, CachDung, ChuY;

    public NguPhap() {

    }

    public NguPhap(int id,String ten, String cauKD, String cauPD, String cauNV, String cachDung, String chuY) {
        Id=id;
        Ten = ten;
        CauKD = cauKD;
        CauPD = cauPD;
        CauNV = cauNV;
        CachDung = cachDung;
        ChuY = chuY;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTen() {
        return Ten;
    }

    public void setTen(String ten) {
        Ten = ten;
    }

    public String getCauKD() {
        return CauKD;
    }

    public void setCauKD(String cauKD) {
        CauKD = cauKD;
    }

    public String getCauPD() {
        return CauPD;
    }

    public void setCauPD(String cauPD) {
        CauPD = cauPD;
    }

    public String getCauNV() {
        return CauNV;
    }

    public void setCauNV(String cauNV) {
        CauNV = cauNV;
    }

    public String getCachDung() {
        return CachDung;
    }

    public void setCachDung(String cachDung) {
        CachDung = cachDung;
    }

    public String getChuY() {
        return ChuY;
    }

    public void setChuY(String chuY) {
        ChuY = chuY;
    }
}
