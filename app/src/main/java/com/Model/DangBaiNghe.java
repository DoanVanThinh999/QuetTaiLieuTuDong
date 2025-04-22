package com.Model;

import java.io.Serializable;

public class DangBaiNghe implements Serializable {
    private String tenBai;
    private String loaiCau;
    private String fileName;
    private String textContent;
    private String speakerGenders;
    private String accent;
    public DangBaiNghe(String tenBai, String loaiCau, String fileName, String textContent,String speakerGenders,String accent) {

        this.tenBai = tenBai;
        this.loaiCau = loaiCau;
        this.fileName = fileName;
        this.textContent = textContent;
        this.speakerGenders = speakerGenders;
        this.accent = accent;
    }

    public String getSpeakerGenders() {
        return speakerGenders;
    }

    public void setSpeakerGenders(String speakerGenders) {
        this.speakerGenders = speakerGenders;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getTenBai() {
        return tenBai;
    }

    public void setTenBai(String tenBai) {
        this.tenBai = tenBai;
    }

    public String getLoaiCau() {
        return loaiCau;
    }

    public void setLoaiCau(String loaiCau) {
        this.loaiCau = loaiCau;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        return "DangBaiNghe{" +
                "tenBai='" + tenBai + '\'' +
                ", loaiCau='" + loaiCau + '\'' +
                ", fileName='" + fileName + '\'' +
                ", textContent='" + textContent + '\'' +
                '}';
    }
}
