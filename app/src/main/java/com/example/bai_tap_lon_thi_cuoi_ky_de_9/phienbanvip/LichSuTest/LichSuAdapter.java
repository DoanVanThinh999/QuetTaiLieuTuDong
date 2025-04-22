package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.LichSuTest;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class LichSuAdapter extends ArrayAdapter<LichSu> {
    Context context = null;
    ArrayList<LichSu> myArray = null;
    private SparseBooleanArray selectedListItemsIds;
    int a = 0;

    int layoutId;

    public LichSuAdapter(Context context, int layoutId, ArrayList<LichSu> arr) {
        super(context,layoutId, arr );
        this.context = context;
        this.layoutId = layoutId;
        this.selectedListItemsIds = new SparseBooleanArray();
        this.myArray = new ArrayList<LichSu>();
        this.myArray = arr;
    }
    public class Holder
    {
        TextView tvTme, tvDiem, tvNgay;
    }

//    @Override
//    public int getCount() {
//        return myArray.size();
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        final LichSu lichsu = myArray.get(position);
        convertView = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            holder= new Holder();
            convertView = mInflater.inflate(layoutId, null);
            holder.tvTme = (TextView) convertView.findViewById(R.id.tvTimethi);
            holder.tvNgay = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvDiem = (TextView) convertView.findViewById(R.id.tvdiem);
        }
        holder.tvTme.setText(lichsu.getTimeThi());
        holder.tvNgay.setText(lichsu.getNgay());
        holder.tvDiem.setText(String.valueOf(lichsu.getDiem()) + "/10");
        return convertView;
    }
    public void remove(LichSu lichSu) {
        myArray.remove(lichSu);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedListItemsIds.get(position));
    }

    public void removeSelection() {
        this.selectedListItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            selectedListItemsIds.put(position, value);
        else
            selectedListItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return myArray.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedListItemsIds;
    }
}