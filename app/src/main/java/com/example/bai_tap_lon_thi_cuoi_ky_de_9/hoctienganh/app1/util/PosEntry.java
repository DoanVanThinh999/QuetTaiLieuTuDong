package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import android.content.Context;

import androidx.core.util.Pair;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Một từ loại, gồm tên từ loại (như danh từ, tính từ) và các bản dịch theo từ loại đó
 */
public class PosEntry extends Pair<String, String[]> {
    public PosEntry(String first, String[] second) {
        super(first, second);
    }

    public String getHeader() {
        return first;
    }

    public String[] getArray() {
        return second;
    }

    public static PosEntry[] from(Context context, DictionaryEntry entry) {
        List<PosEntry> posArray = new ArrayList<>();
        for (String[] posValue : new String[][]{
                {context.getString(R.string.dictionary_word_type_n), entry.getN()},
                {context.getString(R.string.dictionary_word_type_v), entry.getV()},
                {context.getString(R.string.dictionary_word_type_a), entry.getA()},
                {context.getString(R.string.dictionary_word_type_adv), entry.getAdv()},
                {context.getString(R.string.dictionary_word_type_prep), entry.getPrep()},
                {context.getString(R.string.dictionary_word_type_inj), entry.getInj()}
        })
            if (posValue[1] != null)
                posArray.add(new PosEntry(posValue[0], posValue[1].split("\\|")));
        return posArray.toArray(new PosEntry[0]);
    }
}