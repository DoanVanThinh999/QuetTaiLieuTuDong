package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.dictionary.CompactListItem;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.dictionary.DictionaryListItem;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.dictionary.FlashCard;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;

import java.util.List;

public class DictionaryAdapter extends ArrayAdapter<DictionaryEntry> {
    private DictionaryListItem.Factory mFact;
    public DictionaryAdapter(Context context, List<DictionaryEntry> list) {
        super(context, 0, list);
    }

    public void setListViewType(int i) {
        mFact = i == DictionaryEntry.VIEW_TYPE_COMPACT ? new CompactListItem.Factory() : new FlashCard.Factory();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return mFact.create(getContext(), getItem(position)).getView();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}