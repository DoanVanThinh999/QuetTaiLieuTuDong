package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.google.android.material.radiobutton.MaterialRadioButton;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        LinearLayout container = findViewById(R.id.layoutQuizContainer);

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < 3; i++) {
            MaterialRadioButton rb = new MaterialRadioButton(this);
            rb.setText("Đáp án " + (i + 1));
            rb.setTag(i);
            rg.addView(rb);
        }

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            View rb = group.findViewById(checkedId);
            if (rb != null && rb.getTag() != null) {
                int idx = (int) rb.getTag();
                Log.d("QUIZ_RENDER_Ngon", "🟢 Người dùng chọn: " + idx);
                Toast.makeText(this, "Chọn: " + idx, Toast.LENGTH_SHORT).show();
            }
        });

        container.addView(rg);
    }
}
