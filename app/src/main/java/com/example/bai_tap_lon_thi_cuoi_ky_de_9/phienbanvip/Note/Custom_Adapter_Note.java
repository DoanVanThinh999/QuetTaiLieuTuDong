package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.Note;

/*
Tran Thanh Nhan 20/7/2020
* */

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.ArrayList;

public class Custom_Adapter_Note extends ArrayAdapter<Note> {
    private Context context;
    private int resource;
    private ArrayList<Note> arr;
    private ArrayAdapter<Note>adp;
    /**/
    private SparseBooleanArray selectedListItemsIds;

    /*Khai báo contrustor*/
    public Custom_Adapter_Note(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Note> arr) {
        super(context, resource, arr);
        this.context=context;
        this.resource=resource;
        this.selectedListItemsIds=new SparseBooleanArray();
        this.arr=arr;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView= LayoutInflater.from(context).inflate(R.layout.item_ghi_chu_layout,parent,false);
        ImageView img=(ImageView)convertView.findViewById(R.id.img);
        TextView txtTile=(TextView)convertView.findViewById(R.id.txt_Tile);
        Note note=arr.get(position);
        txtTile.setText(note.getTile());
        GridView gv=(GridView)convertView.findViewById(R.id.gv_ghichu);

        /*convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView txtTile=(TextView)v.findViewById(R.id.txt_Tile);

                Intent in =new Intent(v.getContext(),edtNote.class);

                Bundle bundle=new Bundle();
                bundle.putString("Ten",txtTile.getText().toString());
                in.putExtras(bundle);
                context.startActivity(in);
            }
        });*/
        return convertView;
    }


    public void remove(Note note) {
        arr.remove(note);
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
        return arr.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedListItemsIds;
    }
}
