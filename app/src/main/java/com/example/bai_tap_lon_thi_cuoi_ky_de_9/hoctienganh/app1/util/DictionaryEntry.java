package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Predicate;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.MainTiengAnhActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Đại diện cho một từ tiếng Anh, có từ tiếng Anh, dịch nghĩa theo từ loại, hình ảnh và phiên âm cho từ đó
 */
public class DictionaryEntry {
    public static final int VIEW_TYPE_FLASHCARD = 0, VIEW_TYPE_COMPACT = 1, FILTER_NONE = 0, FILTER_FAVORITES = 1, FILTER_LEARNED = 2;
    private final Builder mB;
    private static final List<DictionaryEntry> sEntries = new ArrayList<>();

    private DictionaryEntry(Builder builder) {
        this.mB = builder;
    }

    public String getName() {
        return mB.mName;
    }

    public String getIpa() {
        return mB.mIpa;
    }

    public String getN() {
        return mB.mN;
    }

    public String getV() {
        return mB.mV;
    }

    public String getA() {
        return mB.mA;
    }

    public String getAdv() {
        return mB.mAdv;
    }

    public String getPrep() {
        return mB.mPrep;
    }

    public String getInj() {
        return mB.mInj;
    }

    public String getSound() {
        return mB.mSound;
    }

    public String getImg() {
        return mB.mImg;
    }

    private static void getEntries(Context context, List<DictionaryEntry> out, List<String> filter) {
        InputStream is = context.getResources().openRawResource(R.raw.dictionary);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            NodeList words = doc.getElementsByTagName("word");
            for (int i = 0; i < words.getLength(); i++) {
                Element elem = (Element)words.item(i);
                if (filter != null && !filter.contains(elem.getAttribute("name"))) continue;
                DictionaryEntry word = new Builder()
                        .setName(elem.getAttribute("name"))
                        .setIpa(elem.hasAttribute("ipa") ? elem.getAttribute("ipa") : null)
                        .setN(elem.hasAttribute("n") ? elem.getAttribute("n") : null)
                        .setV(elem.hasAttribute("v") ? elem.getAttribute("v") : null)
                        .setA(elem.hasAttribute("a") ? elem.getAttribute("a") : null)
                        .setAdv(elem.hasAttribute("adv") ? elem.getAttribute("adv") : null)
                        .setPrep(elem.hasAttribute("inj") ? elem.getAttribute("inj") : null)
                        .setInj(elem.hasAttribute("exc") ? elem.getAttribute("exc") : null)
                        .setSound(elem.hasAttribute("sound") ? elem.getAttribute("sound") : null)
                        .setImg(elem.hasAttribute("img") ? elem.getAttribute("img") : null)
                        .build();
                out.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tạo danh sách các từ
     * <br/>
     * Khi gọi hàm này, không nên thay đổi danh sách (bằng các hàm <code>set</code>, <code>remove</code> hoặc <code>clear</code>).
     * <br/>
     * Nhưng nếu phải thay đổi danh sách thì hãy clone danh sách trả về để tránh thay đổi danh sách gốc.
     */
    public static List<DictionaryEntry> getAll(Context context) {
        Log.d("getAll", "wordsList = " + sEntries);
        if (sEntries.size() != 0) return sEntries;
        Log.d("getAll", "Tạo wordsList");
        getEntries(context, sEntries, null);
        Log.d("getAll", "Trả về wordsList = " + sEntries);
        return sEntries;
    }

    public static List<DictionaryEntry> getAll(Context context, Predicate<DictionaryEntry> pred) {
        List<DictionaryEntry> filteredList = new ArrayList<>();
        for (DictionaryEntry entry: getAll(context)) {
            if (pred != null && !pred.test(entry)) continue;
            filteredList.add(entry);
        }
        return filteredList;
    }

    /**
     * Rút gọn cho <code>getAll(context, (entry) -> Settings.from(context).getLearnedWords().contains(entry.getName()))</code>
     */
    public static List<DictionaryEntry> getLearned(Context context) {
        List<String> learned = Settings.from(context).getLearnedWords();
        return getAll(context, new Predicate<DictionaryEntry>() {
            @Override
            public boolean test(DictionaryEntry entry) {
                return learned.contains(entry.getName());
            }
        });
    }

    /**
     * Rút gọn cho <code>getAll(context, (entry) -> Settings.from(context).getLearnedWords().contains(entry.getName()) && pred.test(entry))</code>
     */
    public static List<DictionaryEntry> getLearned(Context context, Predicate<DictionaryEntry> pred) {
        List<String> learned = Settings.from(context).getLearnedWords();
        return getAll(context, new Predicate<DictionaryEntry>() {
            @Override
            public boolean test(DictionaryEntry entry) {
                return learned.contains(entry.getName()) && pred.test(entry);
            }
        });
    }

    public static List<DictionaryEntry> getFavorites(Context context) {
        List<DictionaryEntry> favList = new ArrayList<>();
        Log.d("getFavorites", "favList = " + favList);
        Set<String> favorites = MainTiengAnhActivity.dictionaryStorage.getStringSet("favorites", new HashSet<>());
        getEntries(context, favList, new ArrayList<>(favorites));
        Log.d("getFavorites", "Trả về favList = " + favList);
        return favList;
    }

    public static List<String> getAllNames(Context context) {
        List<String> list = new ArrayList<>();
        for (DictionaryEntry entry : getAll(context))
            list.add(entry.getName());
        return list;
    }

    public static DictionaryEntry getRandom(Context context) {
        return Arrays.pickRandom(getAll(context));
    }

    public static DictionaryEntry getRandom(Context context, Predicate<DictionaryEntry> pred) {
        return Arrays.pickRandom(getAll(context, pred));
    }

    public static DictionaryEntry find(Context context, String word, boolean caseInsensitive, Predicate<DictionaryEntry> pred) {
        for (DictionaryEntry entry: getAll(context, pred)) {
            if (caseInsensitive ? !entry.getName().equalsIgnoreCase(word) : !entry.getName().equals(word)) continue;
            return entry;
        }
        return null;
    }

    public static DictionaryEntry find(Context context, String word, boolean caseInsensitive) {
        return find(context, word, caseInsensitive, null);
    }

    public static DictionaryEntry find(Context context, String word) {
        return find(context, word, false, null);
    }

    public static class Builder {
        private String mName, mIpa, mN, mV, mA, mAdv, mPrep, mInj, mSound, mImg;
        public Builder setName(String name) {
            this.mName = name;
            return this;
        }

        public Builder setIpa(String ipa) {
            this.mIpa = ipa;
            return this;
        }

        public Builder setN(String n) {
            this.mN = n;
            return this;
        }

        public Builder setV(String v) {
            this.mV = v;
            return this;
        }

        public Builder setA(String a) {
            this.mA = a;
            return this;
        }

        public Builder setAdv(String adv) {
            this.mAdv = adv;
            return this;
        }

        public Builder setPrep(String prep) {
            this.mPrep = prep;
            return this;
        }

        public Builder setInj(String inj) {
            this.mInj = inj;
            return this;
        }

        public Builder setSound(String sound) {
            this.mSound = sound;
            return this;
        }

        public Builder setImg(String img) {
            this.mImg = img;
            return this;
        }

        public DictionaryEntry build() {
            return new DictionaryEntry(this);
        }
    }
}
