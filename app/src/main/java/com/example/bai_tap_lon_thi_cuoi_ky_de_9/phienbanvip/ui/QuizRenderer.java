package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.Model.CauHoi;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.*;

public class QuizRenderer {

    public interface AnswerCallback {
        void onAnswerSelected(Object userAnswer);
    }

    public static void render(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        container.removeAllViews();

        switch (cauHoi.quizType) {
            case "SINGLE_CHOICE":
                renderSingleChoice(context, container, cauHoi, callback);

                break;

            case "TRUE_FALSE":
                renderTrueFalse(context, container, cauHoi, callback);
                break;

            case "MULTIPLE_CHOICE":
                renderMultipleChoice(context, container, cauHoi, callback);
                break;

            case "FILL_IN_THE_BLANK":
                renderFillInBlank(context, container, cauHoi, callback);
                break;

            case "ORDER_SEQUENCE":
                renderOrderSequence(context, container, cauHoi, callback);
                break;

            case "MATCHING_PAIRS":
                renderMatchingPairs(context, container, cauHoi, callback);
                break;

            default:
                TextView tv = new TextView(context);
                tv.setText("[❗] Không hỗ trợ loại câu hỏi này: " + cauHoi.quizType);
                tv.setTextColor(Color.RED);
                container.addView(tv);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void renderSingleChoice(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        Log.d("QUIZ_RENDER", "📌 renderSingleChoice - số lượng đáp án: " + cauHoi.dapAnList.size());
        RadioGroup rg = new RadioGroup(context);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setFocusable(true);
        rg.setFocusableInTouchMode(true);
        rg.setClickable(true); // ✅
        for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
            MaterialRadioButton rb = new MaterialRadioButton(context);
            rb.setText(cauHoi.dapAnList.get(i));
            //rb.setId(View.generateViewId()); // ✅ Tạo ID duy nhất hợp lệ hệ thống
            rb.setTag(i); // 🔥 dùng tag để lưu index
            rb.setTextColor(Color.DKGRAY);
            rb.setFocusable(true);
            rb.setFocusableInTouchMode(true);
            rb.setEnabled(true);
            rg.setClickable(true); // ✅
            rg.addView(rb);
        }
        Log.d("QUIZ_CHECK", "⛳️ RadioGroup enabled = " + rg.isEnabled() + ", focusable = " + rg.isFocusable());
        rg.setOnTouchListener((v, e) -> {
            Log.d("QUIZ_TOUCH", "✋ Người dùng chạm vào RadioGroup");
            return false;
        });
        rg.post(() -> {
            rg.setOnCheckedChangeListener((group, checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                if (radioButton != null && radioButton.getTag() != null) {
                    int selectedIndex = (int) radioButton.getTag();
                    Log.d("QUIZ_RENDER_Ngon", "🟢 Người dùng chọn đáp án index: " + selectedIndex);
                    callback.onAnswerSelected(selectedIndex);
                } else {
                    Log.e("QUIZ_RENDER_Error", "❌ Không tìm thấy RadioButton hoặc tag null!");
                }
            });
        });



        Log.d("QUIZ_CHECK", "⛳️ RadioGroup enabled = " + rg.isEnabled() + ", focusable = " + rg.isFocusable());

        rg.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                Log.d("QUIZ_RENDER", "✅ RadioButton added: " + ((MaterialRadioButton) child).getText());
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(rg);
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void renderTrueFalse(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        RadioGroup rg = new RadioGroup(context);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setClickable(true); // ✅
        String[] options = {"Đúng", "Sai"};
        for (int i = 0; i < options.length; i++) {
            MaterialRadioButton rb = new MaterialRadioButton(context);
            rb.setText(options[i]);
            //rb.setId(View.generateViewId()); // ✅ Tạo ID duy nhất hợp lệ hệ thống
            rb.setTag(i); // 🔥 dùng tag để lưu index
            rb.setTextColor(Color.DKGRAY);
            rb.setFocusable(true);
            rb.setFocusableInTouchMode(true);
            rg.setClickable(true); // ✅
            rb.setEnabled(true);
            rg.addView(rb);
        }

        rg.setOnTouchListener((v, e) -> {
            Log.d("QUIZ_TOUCH", "✋ Người dùng chạm vào RadioGroup");
            return false;
        });
        rg.post(() -> {
            rg.setOnCheckedChangeListener((group, checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                if (radioButton != null && radioButton.getTag() != null) {
                    int selectedIndex = (int) radioButton.getTag();
                    Log.d("QUIZ_RENDER_Ngon", "🟢 Người dùng chọn đáp án index: " + selectedIndex);
                    callback.onAnswerSelected(selectedIndex);
                } else {
                    Log.e("QUIZ_RENDER_Error", "❌ Không tìm thấy RadioButton hoặc tag null!");
                }
            });
        });


        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(rg);
    }

    private static void renderMultipleChoice(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        ArrayList<Boolean> answers = new ArrayList<>(Collections.nCopies(cauHoi.dapAnList.size(), false));

        for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
            int index = i;
            CheckBox cb = new CheckBox(context);
            cb.setText(cauHoi.dapAnList.get(i));
            cb.setTextColor(Color.DKGRAY);
            cb.setFocusable(true);
            cb.setFocusableInTouchMode(true);
            cb.setClickable(true);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                answers.set(index, isChecked);
                callback.onAnswerSelected(new ArrayList<>(answers));
            });
            container.requestLayout();
            container.invalidate();
            container.addView(cb);
        }
    }

    private static void renderFillInBlank(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        EditText edt = new EditText(context);
        edt.setHint("Điền vào chỗ trống");
        edt.setInputType(InputType.TYPE_CLASS_TEXT);
        edt.setFocusable(true);
        edt.setFocusableInTouchMode(true);
        edt.setClickable(true); // 👈 Cho phép nhập liệu
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(edt);

        Button submit = new Button(context);
        submit.setText("Xác nhận");
        submit.setOnClickListener(v -> {
            callback.onAnswerSelected(edt.getText().toString().trim());
        });
        container.requestLayout();
        container.invalidate();
        container.addView(submit);
    }

    private static void renderOrderSequence(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        TextView tv = new TextView(context);
        tv.setText("(Sắp xếp - chọn thứ tự bằng số)");
        tv.setTextColor(Color.GRAY);
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(tv);

        LinearLayout orderLayout = new LinearLayout(context);
        orderLayout.setOrientation(LinearLayout.VERTICAL);

        for (String item : cauHoi.dapAnList) {
            EditText et = new EditText(context);
            et.setHint("Thứ tự của: " + item);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.setClickable(true); // 👈 Đảm bảo nhập số được
            orderLayout.addView(et);
        }
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(orderLayout);

        Button submit = new Button(context);
        submit.setText("Xác nhận");
        submit.setOnClickListener(v -> {
            ArrayList<Integer> order = new ArrayList<>();
            for (int i = 0; i < orderLayout.getChildCount(); i++) {
                EditText et = (EditText) orderLayout.getChildAt(i);
                try {
                    order.add(Integer.parseInt(et.getText().toString().trim()));
                } catch (NumberFormatException e) {
                    order.add(-1);
                }
            }
            callback.onAnswerSelected(order);
        });
        container.requestLayout();
        container.invalidate();
        container.addView(submit);
    }

    private static void renderMatchingPairs(Context context, LinearLayout container, CauHoi cauHoi, AnswerCallback callback) {
        TextView tv = new TextView(context);
        tv.setText("(Ghép cặp mô phỏng bằng EditText)");
        tv.setTextColor(Color.GRAY);
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(tv);

        LinearLayout pairLayout = new LinearLayout(context);
        pairLayout.setOrientation(LinearLayout.VERTICAL);

        for (String left : cauHoi.dapAnList) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView leftText = new TextView(context);
            leftText.setText(left);
            leftText.setPadding(8, 8, 8, 8);
            leftText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            EditText rightInput = new EditText(context);
            rightInput.setHint("Ghép với...");
            rightInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
            rightInput.setFocusable(true);
            rightInput.setFocusableInTouchMode(true);
            rightInput.setClickable(true); // 👈 Đảm bảo nhập văn bản được
            row.addView(leftText);
            row.addView(rightInput);
            pairLayout.addView(row);
        }
        container.requestLayout();
        container.invalidate();
        //container.bringToFront(); // quan trọng
        container.addView(pairLayout);

        Button submit = new Button(context);
        submit.setText("Xác nhận");
        submit.setOnClickListener(v -> {
            HashMap<String, String> userMatches = new HashMap<>();
            for (int i = 0; i < pairLayout.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) pairLayout.getChildAt(i);
                TextView left = (TextView) row.getChildAt(0);
                EditText right = (EditText) row.getChildAt(1);
                userMatches.put(left.getText().toString(), right.getText().toString().trim());
            }
            callback.onAnswerSelected(userMatches);
        });
        container.requestLayout();
        container.invalidate();
        container.addView(submit);
    }
    public static void renderKetQuaSauTraLoi(Context context, LinearLayout container, CauHoi cauHoi, Object userAnswer) {
        // Gọi lại render bình thường, nhưng không gắn callback (vì chỉ hiển thị kết quả)
        render(context, container, cauHoi, ans -> {});

        // Áp dụng màu sau khi render
        if (cauHoi.quizType.equals("SINGLE_CHOICE") || cauHoi.quizType.equals("TRUE_FALSE")) {
            if (!(userAnswer instanceof Integer)) return;

            int userIndex = (int) userAnswer;
            int dungIndex = cauHoi.chiSoDung;

            RadioGroup rg = (RadioGroup) container.getChildAt(0);
            for (int i = 0; i < rg.getChildCount(); i++) {
                MaterialRadioButton rb = (MaterialRadioButton) rg.getChildAt(i);
                if (i == dungIndex) {
                    rb.setTextColor(Color.parseColor("#1E7D22")); // Xanh lá: đúng
                } else if (i == userIndex && userIndex != dungIndex) {
                    rb.setTextColor(Color.RED); // Đỏ: người chọn sai
                }
            }
        }

        else if (cauHoi.quizType.equals("MULTIPLE_CHOICE")) {
            if (!(userAnswer instanceof ArrayList)) return;

            @SuppressWarnings("unchecked")
            ArrayList<Boolean> userList = (ArrayList<Boolean>) userAnswer;

            for (int i = 0; i < container.getChildCount(); i++) {
                View view = container.getChildAt(i);
                if (!(view instanceof CheckBox)) continue;

                CheckBox cb = (CheckBox) view;

                boolean isDapAnDung = cauHoi.dapAnDungList.size() > i && (Boolean) cauHoi.dapAnDungList.get(i);

                if (isDapAnDung) {
                    cb.setTextColor(Color.parseColor("#1E7D22")); // Xanh lá nếu đúng
                } else if (userList.get(i)) {
                    cb.setTextColor(Color.RED); // Đỏ nếu người dùng chọn sai
                }

            }
        }
        else if (cauHoi.quizType.equals("FILL_IN_THE_BLANK")) {
            if (!(userAnswer instanceof String)) return;
            String userInput = ((String) userAnswer).trim().toLowerCase();
            String correct = cauHoi.dapAnList.get(cauHoi.chiSoDung).trim().toLowerCase();

            EditText et = (EditText) container.getChildAt(0);
            if (userInput.contains(correct)) {
                et.setTextColor(Color.parseColor("#1E7D22")); // Xanh lá nếu đúng
            } else {
                et.setTextColor(Color.RED); // Đỏ nếu sai
            }
        }
        else if (cauHoi.quizType.equals("ORDER_SEQUENCE")) {
            if (!(userAnswer instanceof ArrayList)) return;
            @SuppressWarnings("unchecked")
            ArrayList<Integer> userOrder = (ArrayList<Integer>) userAnswer;

            LinearLayout groupLayout = (LinearLayout) container.getChildAt(1); // vì [0] là TextView hướng dẫn
            for (int i = 0; i < userOrder.size(); i++) {
                EditText et = (EditText) groupLayout.getChildAt(i);
                int userVal = userOrder.get(i);
                if (userVal == i + 1) {
                    et.setTextColor(Color.parseColor("#1E7D22")); // đúng vị trí
                } else {
                    et.setTextColor(Color.RED); // sai thứ tự
                }
            }
        }
        else if (cauHoi.quizType.equals("MATCHING_PAIRS")) {
            if (!(userAnswer instanceof HashMap)) return;
            @SuppressWarnings("unchecked")
            HashMap<String, String> userPairs = (HashMap<String, String>) userAnswer;

            LinearLayout pairLayout = (LinearLayout) container.getChildAt(1); // [0] là TextView hướng dẫn
            for (int i = 0; i < pairLayout.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) pairLayout.getChildAt(i);
                TextView left = (TextView) row.getChildAt(0);
                EditText right = (EditText) row.getChildAt(1);

                String key = left.getText().toString().trim();
                String userValue = right.getText().toString().trim();
                String correctValue = String.valueOf(cauHoi.dapAnDungList.get(i));

                if (userValue.equalsIgnoreCase(correctValue)) {
                    right.setTextColor(Color.parseColor("#1E7D22")); // xanh nếu đúng
                } else {
                    right.setTextColor(Color.RED); // đỏ nếu sai
                }
            }
        }


    }

}