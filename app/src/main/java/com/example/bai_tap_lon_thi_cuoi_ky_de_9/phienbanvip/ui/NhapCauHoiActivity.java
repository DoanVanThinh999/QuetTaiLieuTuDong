package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.Database1.DatabaseHelper;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import java.util.ArrayList;
import java.util.Arrays;
public class NhapCauHoiActivity extends AppCompatActivity {
    private TextView tvQuizType, tvMarkerTime, tvProgress;
    Button btnNext, btnQuayLaiChinhSua, btnThemDapAn, btnBotDapAn;
    EditText etCauHoi;
    private LinearLayout answerContainer;
    private RadioGroup radioGroup;
    private final int MIN_OPTIONS = 2;
    private final int MAX_OPTIONS = 20;
    private ArrayList<CauHoi> finalDanhSachCauHoi1;
    private int finalCurrentIndex;
    private String finalQuizType;
    String videoUriStr;
    private long markerTime;
    private TaiKhoan taiKhoan;
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhap_cau_hoi);
        btnThemDapAn = findViewById(R.id.btnThemDapAn);
        btnBotDapAn = findViewById(R.id.btnBotDapAn);
        tvQuizType = findViewById(R.id.tvQuizType);
        tvMarkerTime = findViewById(R.id.tvMarkerTime);
        tvProgress = findViewById(R.id.tvProgress);
        btnNext = findViewById(R.id.btnNext);
        btnQuayLaiChinhSua = findViewById(R.id.btnQuayLaiChinhSua);
        videoUriStr = getIntent().getStringExtra("video_uri");
        etCauHoi = findViewById(R.id.etCauHoi);
        answerContainer = findViewById(R.id.answerContainer);
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        // ✅ LẤY DỮ LIỆU TỪ INTENT (đưa lên đầu để tránh lỗi)
        String quizType = getIntent().getStringExtra("quiz_type");
        markerTime = getIntent().getLongExtra("marker_time", 0);
        int totalQuestions = getIntent().getIntExtra("total_questions", 1);
        int currentIndex = getIntent().getIntExtra("current_index", 0);

        ArrayList<CauHoi> danhSachCauHoi = (ArrayList<CauHoi>) getIntent().getSerializableExtra("ds_cau_hoi");
        if (danhSachCauHoi == null) {
            danhSachCauHoi = new ArrayList<>();
        }
        while (danhSachCauHoi.size() < totalQuestions) {
            danhSachCauHoi.add(new CauHoi()); // ✅ Không bao giờ để null
        }
        finalDanhSachCauHoi1 = danhSachCauHoi;

        CauHoi cauHoiHienTai = null;

        if (danhSachCauHoi != null && currentIndex < danhSachCauHoi.size()) {
            cauHoiHienTai = danhSachCauHoi.get(currentIndex);
        }

        if (quizType != null && !quizType.equals("null")) {
            finalQuizType = quizType;
            if (cauHoiHienTai != null) {
                cauHoiHienTai.quizType = finalQuizType; // 🛠 gán lại nếu thiếu
            }
        } else if (cauHoiHienTai != null && cauHoiHienTai.quizType != null) {
            finalQuizType = cauHoiHienTai.quizType;
        } else {
            Toast.makeText(this, "❌ Không xác định được loại câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvQuizType.setText("📌 Loại câu hỏi: " + finalQuizType);
        finalCurrentIndex = currentIndex;
        if (danhSachCauHoi == null) {
            danhSachCauHoi = new ArrayList<>();
        }
        if (finalDanhSachCauHoi1 != null && currentIndex < finalDanhSachCauHoi1.size()) {
            cauHoiHienTai = finalDanhSachCauHoi1.get(currentIndex);
        }
        if (cauHoiHienTai != null && cauHoiHienTai.quizType != null) {
            finalQuizType = cauHoiHienTai.quizType; // Gán lại quizType đúng đã lưu
            tvQuizType.setText("📌 Loại câu hỏi: " + finalQuizType);
        } else {
            tvQuizType.setText("📌 Loại câu hỏi: " + finalQuizType); // fallback
        }
        tvMarkerTime.setText("⏱️ Mốc thời gian: " + (markerTime / 1000.0) + " giây");
        tvProgress.setText("Câu " + (finalCurrentIndex + 1) + " / " + totalQuestions);

        btnNext.setOnClickListener(v -> {
            if (!saveQuestionFromUI(finalQuizType, finalCurrentIndex, finalDanhSachCauHoi1)) return;

            boolean isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            if (isEditMode) {
                // ✅ Chỉ setResult + finish nếu đang chỉnh sửa
                Intent result = new Intent();
                result.putExtra("current_index", finalCurrentIndex);
                result.putExtra("taiKhoan",taiKhoan);
                result.putExtra("cau_hoi", finalDanhSachCauHoi1.get(finalCurrentIndex));
                setResult(RESULT_OK, result);
                finish();
                return;
            }

            // ⬇️ Nếu không ở edit_mode thì xử lý logic bình thường
            if (finalCurrentIndex + 1 < totalQuestions) {
                CauHoi cauHoiTiepTheo = finalDanhSachCauHoi1.get(finalCurrentIndex + 1);
                Intent intent;
                if (cauHoiTiepTheo.quizType != null && !cauHoiTiepTheo.quizType.equals("null")) {
                    intent = new Intent(this, NhapCauHoiActivity.class);
                    intent.putExtra("quiz_type", cauHoiTiepTheo.quizType);
                } else {
                    intent = new Intent(this, ChonLoaiCauHoiActivity.class);
                }
                intent.putExtra("marker_time", markerTime);
                intent.putExtra("total_questions", totalQuestions);
                intent.putExtra("current_index", finalCurrentIndex + 1);
                intent.putExtra("ds_cau_hoi", finalDanhSachCauHoi1);
                intent.putExtra("taiKhoan",taiKhoan);
                intent.putExtra("video_uri", getIntent().getStringExtra("video_uri")); // ✅ THÊM DÒNG NÀY
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "🎉 Đã thêm xong tất cả câu hỏi cho mốc!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, DanhSachCauHoiActivity.class);
                intent.putExtra("marker_time", markerTime);
                intent.putExtra("total_questions", totalQuestions);
                intent.putExtra("ds_cau_hoi", finalDanhSachCauHoi1);
                intent.putExtra("taiKhoan", taiKhoan);
                intent.putExtra("video_uri", getIntent().getStringExtra("video_uri")); // ✅ THÊM DÒNG NÀY
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });


        btnQuayLaiChinhSua.setOnClickListener(v -> {
            Log.d("btnQuayLaiChinhSua", "👉 Đang xử lý quay lại câu trước");
            if (finalCurrentIndex > 0) {
                Log.d("btnQuayLaiChinhSua", "🧠 Gọi saveQuestionFromUI() cho câu index " + finalCurrentIndex);
                Log.d("btnQuayLaiChinhSua", " Chuẩn bị quay về câu trước");
                Log.d("btnQuayLaiChinhSua", "📋 Size danh sách: " + finalDanhSachCauHoi1.size());
                Log.d("btnQuayLaiChinhSua", "📋 Index cần lấy: " + (finalCurrentIndex - 1));
                // 👉 Lấy câu hỏi trước đó trong danh sách đã lưu tạm
                CauHoi cauHoiTruoc = finalDanhSachCauHoi1.get(finalCurrentIndex - 1);
                Log.d("btnQuayLaiChinhSua", "➡️ Câu hỏi trước: " + cauHoiTruoc.noiDung);
                Intent intent = new Intent(this, NhapCauHoiActivity.class);
                intent.putExtra("marker_time", markerTime);
                intent.putExtra("quiz_type", cauHoiTruoc.quizType);
                intent.putExtra("total_questions", totalQuestions);
                intent.putExtra("current_index", finalCurrentIndex - 1);
                intent.putExtra("ds_cau_hoi", finalDanhSachCauHoi1);
                intent.putExtra("edit_mode", true); // ✅ Đừng quên cái này
                intent.putExtra("video_uri", getIntent().getStringExtra("video_uri")); // ✅ THÊM DÒNG NÀY
                intent.putExtra("taiKhoan",taiKhoan);
                startActivity(intent);
                finish();

            } else {
                Log.d("btnQuayLaiChinhSua", "❌ Đang ở câu đầu tiên, không thể quay lại");
                Toast.makeText(this, "❌ Đây là câu đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });
        btnThemDapAn.setOnClickListener(v -> {
            switch (finalQuizType) {
                case "SINGLE_CHOICE":
                    if (radioGroup.getChildCount() < MAX_OPTIONS) {
                        addRadioButtonWithEditText(radioGroup.getChildCount());
                    } else {
                        Toast.makeText(this, "Tối đa " + MAX_OPTIONS + " đáp án!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "MULTIPLE_CHOICE":
                    if (answerContainer.getChildCount() < MAX_OPTIONS) {
                        addCheckBoxWithEditText(answerContainer.getChildCount());
                    } else {
                        Toast.makeText(this, "Tối đa " + MAX_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "FILL_IN_THE_BLANK":
                    if (answerContainer.getChildCount() < MAX_OPTIONS) {
                        addBlankOption(answerContainer.getChildCount());
                    } else {
                        Toast.makeText(this, "Tối đa " + MAX_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "ORDER_SEQUENCE":
                    if (answerContainer.getChildCount() < MAX_OPTIONS) {
                        addStepOption(answerContainer.getChildCount());
                    } else {
                        Toast.makeText(this, "Tối đa " + MAX_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case "MATCHING_PAIRS":
                    if (answerContainer.getChildCount() < MAX_OPTIONS) {
                        addMatchingPairRow(answerContainer.getChildCount());
                    } else {
                        Toast.makeText(this, "Tối đa " + MAX_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "TRUE_FALSE":
                    Toast.makeText(this, "Loại này không cần thêm đáp án!", Toast.LENGTH_SHORT).show();
                    return;
                // TODO: Sau này thêm MULTIPLE_CHOICE, ORDER_SEQUENCE, MATCHING_PAIRS...
                default:
                    Toast.makeText(this, "❓ Loại câu hỏi chưa hỗ trợ nút +", Toast.LENGTH_SHORT).show();
            }
        });
        btnBotDapAn.setOnClickListener(v -> {
            switch (finalQuizType) {
                case "SINGLE_CHOICE":
                    if (radioGroup.getChildCount() > MIN_OPTIONS) {
                        radioGroup.removeViewAt(radioGroup.getChildCount() - 1);
                    } else {
                        Toast.makeText(this, "Phải có ít nhất " + MIN_OPTIONS + " đáp án!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "MULTIPLE_CHOICE":
                    if (answerContainer.getChildCount() > MIN_OPTIONS) {
                        answerContainer.removeViewAt(answerContainer.getChildCount() - 1);
                    } else {
                        Toast.makeText(this, "Phải có ít nhất " + MIN_OPTIONS + " đáp án!", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case "FILL_IN_THE_BLANK":
                    if (answerContainer.getChildCount() > MIN_OPTIONS) {
                        answerContainer.removeViewAt(answerContainer.getChildCount() - 1);
                    } else {
                        Toast.makeText(this, "Phải có ít nhất " + MIN_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "ORDER_SEQUENCE":
                    if (answerContainer.getChildCount() > MIN_OPTIONS) {
                        answerContainer.removeViewAt(answerContainer.getChildCount() - 1);
                    } else {
                        Toast.makeText(this, "Phải có ít nhất " + MIN_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "MATCHING_PAIRS":
                    if (answerContainer.getChildCount() > MIN_OPTIONS) {
                        answerContainer.removeViewAt(answerContainer.getChildCount() - 1);
                    } else {
                        Toast.makeText(this, "Phải có ít nhất " + MIN_OPTIONS + " ô trống!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "TRUE_FALSE":
                    Toast.makeText(this, "Không thể xoá đáp án trong loại Đúng/Sai!", Toast.LENGTH_SHORT).show();
                    return;
                default:
                    Toast.makeText(this, "❓ Loại câu hỏi chưa hỗ trợ nút –", Toast.LENGTH_SHORT).show();
            }
        });
        loadCauHoiIfEditMode();
        new DatabaseHelper(this).logAllCauHoiTheoMoc();
    }
    private void loadCauHoiIfEditMode() {
        if (getIntent().getBooleanExtra("edit_mode", false)) {
            CauHoi cauHoi = finalDanhSachCauHoi1.get(finalCurrentIndex);
            if (cauHoi != null && cauHoi.quizType != null) {
                finalQuizType = cauHoi.quizType;  // Gán lại quizType nếu cần
                tvQuizType.setText("📌 Loại câu hỏi: " + finalQuizType);
                etCauHoi.setText(cauHoi.noiDung);

                // ✅ GỌI HIỂN THỊ UI TƯƠNG ỨNG SAU KHI ĐÃ BIẾT quizType
                capNhatUITheoLoaiCauHoi(finalQuizType, cauHoi);
            }
        } else {
            // Nếu không ở edit mode, hiển thị giao diện rỗng ban đầu
            switch (finalQuizType) {
                case "SINGLE_CHOICE":
                    hienThiCacDapAnTracNghiem(4);
                    break;
                case "MULTIPLE_CHOICE":
                    hienThiCacDapAnMultipleChoice(4);
                    break;
                case "FILL_IN_THE_BLANK":
                    hienThiCacOChuaBlank(2);
                    break;
                case "ORDER_SEQUENCE":
                    hienThiCacBuocSapXep(3);
                    break;
                case "MATCHING_PAIRS":
                    hienThiCacCapGhep(3);
                    break;
                case "TRUE_FALSE":
                    hienThiDungSai();
                    break;
            }
        }
    }

    private void capNhatUITheoLoaiCauHoi(String finalQuizType, CauHoi cauHoi) {
        if ("SINGLE_CHOICE".equals(finalQuizType)) {
            hienThiCacDapAnTracNghiem(cauHoi.dapAnList.size());
            for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                if (i < radioGroup.getChildCount()) { // Kiểm tra số lượng View trong RadioGroup
                    LinearLayout row = (LinearLayout) radioGroup.getChildAt(i);
                    EditText editText = (EditText) row.getChildAt(1);
                    RadioButton rb = (RadioButton) row.getChildAt(0);

                    if (editText != null) {
                        editText.setText(cauHoi.dapAnList.get(i)); // Cập nhật lại đáp án
                    }
                    if (rb != null && i == cauHoi.chiSoDung) {
                        rb.setChecked(true); // Cập nhật đáp án đúng
                    }
                }
            }
        }
        else if ("MULTIPLE_CHOICE".equals(finalQuizType)) {
            hienThiCacDapAnMultipleChoice(cauHoi.dapAnList.size());
            for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                if (i < answerContainer.getChildCount()) { // Kiểm tra số lượng View trong answerContainer
                    LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                    CheckBox checkBox = (CheckBox) row.getChildAt(0);
                    EditText editText = (EditText) row.getChildAt(1);

                    if (editText != null) {
                        editText.setText(cauHoi.dapAnList.get(i)); // Cập nhật lại đáp án
                    }
                    if (checkBox != null) {
                        checkBox.setChecked(cauHoi.dapAnDungList.get(i)); // Cập nhật trạng thái của CheckBox
                    }
                }
            }
        }
        else if ("FILL_IN_THE_BLANK".equals(finalQuizType)) {
            hienThiCacOChuaBlank(cauHoi.dapAnList.size());
            for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                if (i < answerContainer.getChildCount()) { // Kiểm tra số lượng View trong answerContainer
                    LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                    EditText editText = (EditText) row.getChildAt(1);

                    if (editText != null) {
                        editText.setText(cauHoi.dapAnList.get(i)); // Cập nhật lại đáp án
                    }
                }
            }
        }
        else if ("ORDER_SEQUENCE".equals(finalQuizType)) {
            // Hiển thị các bước sắp xếp
            hienThiCacBuocSapXep(cauHoi.dapAnList.size());

            for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                if (i < answerContainer.getChildCount()) { // Kiểm tra số lượng View trong answerContainer
                    LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                    EditText editText = (EditText) row.getChildAt(1);

                    if (editText != null) {
                        editText.setText(cauHoi.dapAnList.get(i)); // Cập nhật lại đáp án cho từng bước
                    }
                }
            }
        }

        else if ("MATCHING_PAIRS".equals(finalQuizType)) {
            hienThiCacCapGhep(cauHoi.dapAnList.size());
            for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                if (i < answerContainer.getChildCount()) { // Kiểm tra số lượng View trong answerContainer
                    LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                    EditText editA = (EditText) row.getChildAt(1);
                    EditText editB = (EditText) row.getChildAt(2);
                    String[] parts = cauHoi.dapAnList.get(i).split(":::", -1);

                    if (editA != null && editB != null && parts.length == 2) {
                        editA.setText(parts[0]);
                        editB.setText(parts[1]);
                    }
                }
            }
        }

        else if ("TRUE_FALSE".equals(finalQuizType)) {
            hienThiDungSai();
            if (cauHoi.chiSoDung == 0) {
                RadioButton rbTrue = (RadioButton) ((RadioGroup) answerContainer.getChildAt(0)).getChildAt(0);
                if (rbTrue != null) {
                    rbTrue.setChecked(true);
                }
            } else if (cauHoi.chiSoDung == 1) {
                RadioButton rbFalse = (RadioButton) ((RadioGroup) answerContainer.getChildAt(0)).getChildAt(1);
                if (rbFalse != null) {
                    rbFalse.setChecked(true);
                }
            }
        }

    }
    private boolean saveQuestionFromUI(String finalQuizType, int currentIndex, ArrayList<CauHoi> danhSachCauHoi) {
        String dapAnListText = "";
        StringBuilder dapAnDungList = new StringBuilder();
        //CauHoi cauHoi = new CauHoi();
        // ✅ LẤY ĐÚNG OBJECT GỐC
        CauHoi cauHoi = danhSachCauHoi.get(currentIndex);
        cauHoi.noiDung = etCauHoi.getText().toString();

        if (cauHoi.noiDung.trim().isEmpty()) {
            Toast.makeText(this, "❌ Bạn chưa nhập nội dung câu hỏi!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ("SINGLE_CHOICE".equals(finalQuizType)) {
            ArrayList<String> answers = new ArrayList<>();
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) radioGroup.getChildAt(i);
                EditText editText = (EditText) row.getChildAt(1);
                RadioButton rb = (RadioButton) row.getChildAt(0);
                String ans = editText.getText().toString().trim();
                answers.add(ans);
                if (rb.isChecked()) cauHoi.chiSoDung = i;
            }
            cauHoi.dapAnList = answers;
            dapAnListText = String.join(",", answers);

        } else if ("MULTIPLE_CHOICE".equals(finalQuizType)) {
            ArrayList<String> answers = new ArrayList<>();
            ArrayList<Boolean> checkeds = new ArrayList<>();
            for (int i = 0; i < answerContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                CheckBox cb = (CheckBox) row.getChildAt(0);
                EditText et = (EditText) row.getChildAt(1);
                answers.add(et.getText().toString().trim());
                checkeds.add(cb.isChecked());
            }
            cauHoi.dapAnList = answers;
            cauHoi.dapAnDungList = checkeds;
            dapAnListText = String.join(",", answers);
            for (boolean b : checkeds) dapAnDungList.append(b).append(",");

        } else if ("FILL_IN_THE_BLANK".equals(finalQuizType)) {
            int soBlank = cauHoi.noiDung.split("___", -1).length - 1;
            if (answerContainer.getChildCount() != soBlank) {
                hienThiCacOChuaBlank(soBlank);
                Toast.makeText(this, "⚠️ Số ô trống không khớp nội dung. Đã cập nhật lại ô nhập.", Toast.LENGTH_SHORT).show();
                return false;
            }
            ArrayList<String> answers = new ArrayList<>();
            for (int i = 0; i < soBlank; i++) {
                LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                EditText et = (EditText) row.getChildAt(1);
                String text = et.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa nhập đáp án cho ô số " + (i + 1), Toast.LENGTH_SHORT).show();
                    return false;
                }
                answers.add(text);
            }
            cauHoi.dapAnList = answers;
            dapAnListText = String.join(",", answers);

        } else if ("ORDER_SEQUENCE".equals(finalQuizType)) {
            ArrayList<String> steps = new ArrayList<>();
            for (int i = 0; i < answerContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                EditText et = (EditText) row.getChildAt(1);
                String text = et.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa nhập nội dung bước " + (i + 1), Toast.LENGTH_SHORT).show();
                    return false;
                }
                steps.add(text);
            }
            cauHoi.dapAnList = steps;
            dapAnListText = String.join(",", steps);

        } else if ("MATCHING_PAIRS".equals(finalQuizType)) {
            ArrayList<String> pairs = new ArrayList<>();
            for (int i = 0; i < answerContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) answerContainer.getChildAt(i);
                EditText editA = (EditText) row.getChildAt(1);
                EditText editB = (EditText) row.getChildAt(2);
                String a = editA.getText().toString().trim();
                String b = editB.getText().toString().trim();
                if (a.isEmpty() || b.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa nhập đầy đủ cặp " + (i + 1), Toast.LENGTH_SHORT).show();
                    return false;
                }
                pairs.add(a + ":::" + b);
            }
            cauHoi.dapAnList = pairs;
            dapAnListText = String.join(",", pairs);

        } else if ("TRUE_FALSE".equals(finalQuizType)) {
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "❗ Bạn chưa chọn Đúng hoặc Sai!", Toast.LENGTH_SHORT).show();
                return false;
            }

            RadioButton rbTrue = (RadioButton) ((RadioGroup) answerContainer.getChildAt(0)).getChildAt(0);
            cauHoi.chiSoDung = rbTrue.isChecked() ? 0 : 1;

            // ✅ THÊM DÒNG NÀY để tránh bị null dapAnList
            ArrayList<String> answers = new ArrayList<>();
            answers.add("Đúng");
            answers.add("Sai");
            cauHoi.dapAnList = answers;

            dapAnListText = "Đúng,Sai";
        }

        Log.d("DEBUG-LUU", "🔐 Trước khi set: " + cauHoi.noiDung);
        cauHoi.quizType = finalQuizType;
        danhSachCauHoi.set(currentIndex, cauHoi);
        Log.d("DEBUG-LUU", "✅ Sau khi set: " + danhSachCauHoi.get(currentIndex).noiDung);

        danhSachCauHoi.set(currentIndex, cauHoi); // Rồi mới set
        // Cập nhật database
        new DatabaseHelper(this)
                .updateQuestion(currentIndex, cauHoi.noiDung, finalQuizType, dapAnListText, cauHoi.chiSoDung, dapAnDungList.toString());
        Log.d("DEBUG-LUU", "Lưu: " + cauHoi.noiDung + " | Đáp án: " + dapAnListText + " | Đáp án đúng (index): " + cauHoi.chiSoDung);
        // ✅ Lưu vào bảng cau_hoi_theo_moc
        new DatabaseHelper(this).addCauHoiTheoMoc(
                markerTime,
                videoUriStr != null ? videoUriStr : "", // fallback nếu null
                cauHoi.noiDung,
                finalQuizType,
                dapAnListText,
                dapAnDungList.toString(),
                cauHoi.chiSoDung
        );
        Log.d("DEBUG_DB_FULL", "📌 markerTime = " + markerTime);
        Log.d("DEBUG_DB_FULL", "📁 videoUriStr = " + videoUriStr);
        Log.d("DEBUG_DB_FULL", "📝 noiDung = " + cauHoi.noiDung);
        Log.d("DEBUG_DB_FULL", "📚 quizType = " + finalQuizType);
        Log.d("DEBUG_DB_FULL", "🔢 dapAnList = " + dapAnListText);
        Log.d("DEBUG_DB_FULL", "✅ dapAnDungList = " + dapAnDungList);
        Log.d("DEBUG_DB_FULL", "⭐ chiSoDung = " + cauHoi.chiSoDung);
        Log.d("DEBUG_DB", "💾 Đã lưu vào bảng cau_hoi_theo_moc: marker = "
                + markerTime + ", video = " + videoUriStr);

        if (cauHoi.noiDung == null || cauHoi.noiDung.trim().isEmpty()) {
            Log.e("SAVE", "⚠️ Câu hỏi không có nội dung!");
        }
        if (cauHoi.dapAnList == null || cauHoi.dapAnList.isEmpty()) {
            Log.e("SAVE", "⚠️ Danh sách đáp án rỗng hoặc null!");
        }

        return true;
    }
    private void hienThiDungSai() {
        answerContainer.removeAllViews();

        RadioGroup tfGroup = new RadioGroup(this);
        tfGroup.setOrientation(RadioGroup.VERTICAL);
        tfGroup.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        RadioButton rbTrue = new RadioButton(this);
        rbTrue.setText("✅ Đúng");
        rbTrue.setId(View.generateViewId());

        RadioButton rbFalse = new RadioButton(this);
        rbFalse.setText("❌ Sai");
        rbFalse.setId(View.generateViewId());

        tfGroup.addView(rbTrue);
        tfGroup.addView(rbFalse);

        radioGroup = tfGroup;
        answerContainer.addView(tfGroup);

    }

    private void hienThiCacCapGhep(int count) {
        answerContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            addMatchingPairRow(i);
        }
    }
    // Utility tránh lỗi NULL khi đọc từ Cursor
    public static String safeGetString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index == -1 || cursor.isNull(index)) {
            Log.w("safeGetString", "Cột không tồn tại hoặc NULL: " + columnName);
            return "";
        }
        return cursor.getString(index);
    }


    private void addMatchingPairRow(int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Label
        TextView label = new TextView(this);
        label.setText("Cặp " + (index + 1) + ": ");

        // Cột A
        EditText editA = new EditText(this);
        editA.setHint("Cột A");
        editA.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        // Cột B
        EditText editB = new EditText(this);
        editB.setHint("Cột B");
        editB.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        row.addView(label);
        row.addView(editA);
        row.addView(editB);

        answerContainer.addView(row);
    }

    private void hienThiCacBuocSapXep(int count) {
        answerContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            addStepOption(i);
        }
    }

    private void addStepOption(int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView label = new TextView(this);
        label.setText((index + 1) + ": ");

        EditText editText = new EditText(this);
        editText.setHint("Nội dung bước " + (index + 1));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));

        row.addView(label);
        row.addView(editText);
        answerContainer.addView(row);
    }
    private void hienThiCacDapAnMultipleChoice(int count) {
        answerContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            addCheckBoxWithEditText(i);
        }
    }
    private void addCheckBoxWithEditText(int index) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(View.generateViewId());
        if (checkBox.getParent() != null) {
            ((ViewGroup) checkBox.getParent()).removeView(checkBox); // Loại bỏ checkBox khỏi parent hiện tại trước khi thêm vào
        }
        EditText editText = new EditText(this);
        editText.setHint("Đáp án " + (index + 1));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));
        wrapper.addView(checkBox);
        wrapper.addView(editText);
        answerContainer.addView(wrapper);
    }
    private void hienThiCacOChuaBlank(int count) {
        answerContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            addBlankOption(i);
        }
    }
    private void addBlankOption(int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView label = new TextView(this);
        label.setText("Ô " + (index + 1) + ": ");

        EditText editText = new EditText(this);
        editText.setHint("Đáp án đúng cho chỗ trống " + (index + 1));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));
        row.addView(label);
        row.addView(editText);
        answerContainer.addView(row);
    }
    private void hienThiCacDapAnTracNghiem(int count) {
        answerContainer.removeAllViews(); // Clear giao diện trước
        radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < count; i++) {
            addRadioButtonWithEditText(i);
        }
        answerContainer.addView(radioGroup);
    }
    private void addRadioButtonWithEditText(int index) {
        // Wrapper chứa RadioButton + EditText
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.HORIZONTAL);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // RadioButton
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(View.generateViewId());

        // Đảm bảo không bị lỗi "already has a parent"
        if (radioButton.getParent() != null) {
            ((ViewGroup) radioButton.getParent()).removeView(radioButton);
        }

        // EditText
        EditText editText = new EditText(this);
        editText.setHint("Đáp án " + (index + 1));
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ));
        wrapper.addView(radioButton);
        wrapper.addView(editText);
        // Thêm dòng này để đảm bảo chỉ được chọn 1 radio duy nhất
        radioButton.setOnClickListener(v -> {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View view = radioGroup.getChildAt(i);
                if (view instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) view;
                    RadioButton rb = (RadioButton) layout.getChildAt(0);
                    rb.setChecked(rb == radioButton);
                }
            }
        });
        radioGroup.addView(wrapper);
    }
}
