package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.PosEntry;

import java.util.ArrayList;

public class PosAdapter extends ArrayAdapter<PosEntry> {
    public PosAdapter(Context context, ArrayList<PosEntry> entries) {
        super(context, 0, entries);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertedView = LayoutInflater.from(getContext()).inflate(R.layout.word_pos, viewGroup, false);
        TextView header = convertedView.findViewById(R.id.text_pos_header);
        header.setText(getItem(i).getHeader());
        TextView list = convertedView.findViewById(R.id.text_pos_list);
        list.setEnabled(false);
        list.setText(String.join("\n\n", getItem(i).getArray()));
        return convertedView;
    }
}
