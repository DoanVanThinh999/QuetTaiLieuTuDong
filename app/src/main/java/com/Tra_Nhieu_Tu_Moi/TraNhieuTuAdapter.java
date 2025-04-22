package com.Tra_Nhieu_Tu_Moi;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import java.util.List;
import java.util.Locale;

public class TraNhieuTuAdapter extends BaseAdapter {
    private Context context;
    private List<DichTuDoanDai> dataList;
    private TextToSpeech textToSpeech; // Đối tượng TextToSpeech

    public TraNhieuTuAdapter(Context context, List<DichTuDoanDai> dataList) {
        this.context = context;
        this.dataList = dataList;

        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Ngôn ngữ không được hỗ trợ!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Khởi tạo TextToSpeech thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_tra_tu_doan_dai, parent, false);
            holder = new ViewHolder();

            holder.tvTuTra = convertView.findViewById(R.id.tvTuTra);
            holder.tvDoanDich = convertView.findViewById(R.id.tvDoanDich);
            holder.btnPlayResult1 = convertView.findViewById(R.id.btnPlayResult1);
            holder.btnPlayResult = convertView.findViewById(R.id.btnPlayResult);
            holder.scrollView1 = convertView.findViewById(R.id.scrollView1);
            holder.scrollView2 = convertView.findViewById(R.id.scrollView2);

            holder.scrollView1.post(() -> holder.scrollView1.fullScroll(ScrollView.FOCUS_DOWN));
            holder.scrollView2.post(() -> holder.scrollView2.fullScroll(ScrollView.FOCUS_DOWN));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DichTuDoanDai item = dataList.get(position);
        holder.tvTuTra.setText(item.getTuTra());
        holder.tvDoanDich.setText(item.getDoanDich());

        holder.btnPlayResult1.setOnClickListener(v -> {
            String textToRead = holder.tvTuTra.getText().toString().trim();
            if (!textToRead.isEmpty()) {
                if (isEnglish(textToRead)) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    textToSpeech.setLanguage(new Locale("vi"));
                }

                if (!textToSpeech.isSpeaking()) {
                    holder.btnPlayResult1.setImageResource(android.R.drawable.ic_media_pause);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "TEXT_1");
                    }
                } else {
                    textToSpeech.stop();
                    holder.btnPlayResult1.setImageResource(android.R.drawable.ic_media_play);
                }
            } else {
                Toast.makeText(context, "Không có nội dung để đọc!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnPlayResult.setOnClickListener(v -> {
            String textToRead = holder.tvDoanDich.getText().toString().trim();
            if (!textToRead.isEmpty()) {
                if (isEnglish(textToRead)) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    textToSpeech.setLanguage(new Locale("vi"));
                }

                if (!textToSpeech.isSpeaking()) {
                    holder.btnPlayResult.setImageResource(android.R.drawable.ic_media_pause);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "TEXT_2");
                    }
                } else {
                    textToSpeech.stop();
                    holder.btnPlayResult.setImageResource(android.R.drawable.ic_media_play);
                }
            } else {
                Toast.makeText(context, "Không có nội dung để đọc!", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    public void destroyTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private boolean isEnglish(String text) {
        return text.matches("^[a-zA-Z0-9\\s,.!?]+$");
    }

    static class ViewHolder {
        TextView tvTuTra;
        TextView tvDoanDich;
        ImageButton btnPlayResult1;
        ImageButton btnPlayResult;
        ScrollView scrollView1;
        ScrollView scrollView2;
    }
}
