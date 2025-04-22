package com.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CauHoiAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> data;
    private ListView listView;
    private boolean isMultiDeleteMode;

    public CauHoiAdapter(Context context, ArrayList<String> data, ListView listView, boolean isMultiDeleteMode) {
        super(context, android.R.layout.simple_list_item_multiple_choice, data);
        this.context = context;
        this.data = data;
        this.listView = listView;
        this.isMultiDeleteMode = isMultiDeleteMode;
    }

    public void setMultiDeleteMode(boolean isMultiDeleteMode) {
        this.isMultiDeleteMode = isMultiDeleteMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = super.getView(position, convertView, parent);

        if (isMultiDeleteMode) {
            if (listView.isItemChecked(position)) {
                rowView.setBackgroundColor(Color.parseColor("#FFCDD2")); // đỏ nhạt nếu được chọn
            } else {
                rowView.setBackgroundColor(Color.parseColor("#FFF9C4")); // vàng nhạt mặc định khi bật xoá nhiều
            }
        } else {
            rowView.setBackgroundColor(Color.TRANSPARENT); // trở lại như cũ
        }

        return rowView;
    }
}
