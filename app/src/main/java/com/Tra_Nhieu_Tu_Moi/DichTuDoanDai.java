package com.Tra_Nhieu_Tu_Moi;

import java.io.Serializable;

public class DichTuDoanDai implements Serializable {
    String tuTra;
    String doanDich;
    public  DichTuDoanDai(){}

    public DichTuDoanDai(String tuTra, String doanDich) {
        this.tuTra = tuTra;
        this.doanDich = doanDich;
    }

    public String getTuTra() {
        return tuTra;
    }

    public void setTuTra(String tuTra) {
        this.tuTra = tuTra;
    }

    public String getDoanDich() {
        return doanDich;
    }

    public void setDoanDich(String doanDich) {
        this.doanDich = doanDich;
    }

    @Override
    public String toString() {
        return "DichTuDoanDai{" +
                "tuTra='" + tuTra + '\'' +
                ", doanDich='" + doanDich + '\'' +
                '}';
    }
}
