package com.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class CauHoi implements Serializable {
    //public String noiDung;
    public String noiDung = ""; // 👈 dòng này
    public String quizType;
    public ArrayList<String> dapAnList = new ArrayList<>();
    public ArrayList<Boolean> dapAnDungList = new ArrayList<>();
    public int chiSoDung = -1;
}


