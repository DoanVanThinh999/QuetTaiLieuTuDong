package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip;
/*
Tran Thanh Nhan 20/7/2020
* */
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class CustomListView extends ArrayAdapter<String> {

    private final Activity context;
    private ArrayList<String> itemName = new ArrayList<String>();
    private final Integer[] imgID;

    public CustomListView(Activity context, ArrayList itemName, Integer[] imgID)
    {
        super(context, R.layout.menu, itemName);
        this.context = context;
        this.itemName = itemName;
        this.imgID = imgID;
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.menu, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        txtTitle.setText(itemName.get(position));
        if (imgID.length > 1) {
            imageView.setImageResource(imgID[position]);
            return rowView;
        } else {
            imageView.setImageResource(imgID[0]);
            return rowView;
        }

    }

}
