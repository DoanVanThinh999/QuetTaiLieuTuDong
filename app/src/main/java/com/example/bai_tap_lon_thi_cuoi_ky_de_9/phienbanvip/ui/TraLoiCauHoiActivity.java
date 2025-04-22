package com.example.bai_tap_lon_thi_cuoi_ky_de_9.phienbanvip.ui;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.Model.CauHoi;
import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TraLoiCauHoiActivity extends AppCompatActivity {
    private LinearLayout layoutQuizContainer;
    private TextView tvCauHoi;
    private Button btnTraLoi;
    private TextView tvThongBao;
    private ArrayList<CauHoi> danhSachCauHoi;
    private int index = 0;
    private Object currentUserAnswer = null;
    private int soCauDung = 0;
    private int[] soLanSaiMoiCau;
    TaiKhoan taiKhoan;
    int soLanTraLoiLai, truDiemMoiLan;
    String xuLyKhiSai;
    private boolean daTraLoi = false;
    private int soLanSaiHienTai = 0;
    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tra_loi_cau_hoi);

        layoutQuizContainer = findViewById(R.id.layoutQuizContainer);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        btnTraLoi = findViewById(R.id.btnTraLoi);
        tvThongBao = findViewById(R.id.tvThongBao);
        taiKhoan = (TaiKhoan) getIntent().getSerializableExtra("taiKhoan");
        xuLyKhiSai = getIntent().getStringExtra("xu_ly_khi_sai");
        soLanTraLoiLai = getIntent().getIntExtra("so_lan_tra_loi_lai", Integer.MAX_VALUE);
        truDiemMoiLan = getIntent().getIntExtra("tru_diem_moi_lan", 0);
        danhSachCauHoi = (ArrayList<CauHoi>) getIntent().getSerializableExtra("cau_hoi_theo_moc");
        if (danhSachCauHoi == null || danhSachCauHoi.isEmpty()) {
            Toast.makeText(this, "Không có câu hỏi nào", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        soLanSaiMoiCau = new int[danhSachCauHoi.size()];
        btnTraLoi.setOnClickListener(v -> {
            if (!daTraLoi) {
                xuLyTraLoi();
            } else {
                daTraLoi = false;
                btnTraLoi.setText("Trả lời");
                // 🧠 Nếu là chế độ "quay lại mốc"
                if ("quay_lai_moc".equals(btnTraLoi.getTag())) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("so_cau_dung", 0);
                    resultIntent.putExtra("tai_khoan", taiKhoan);
                    resultIntent.putExtra("tong_cau", 1);
                    resultIntent.putExtra("marker_time", getIntent().getLongExtra("marker_time", -1));
                    resultIntent.putExtra("tra_loi_dung", false);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    return;
                }
                index++; // ✅ Di chuyển dòng index++ vào đây để sang câu mới
                tvThongBao.setVisibility(GONE);
                if (index < danhSachCauHoi.size()) {
                    hienThiCauHoi(danhSachCauHoi.get(index));
                } else {
                    // Hoàn tất quiz
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("so_cau_dung", soCauDung);
                    resultIntent.putExtra("tai_khoan", taiKhoan);
                    resultIntent.putExtra("tong_cau", danhSachCauHoi.size());
                    resultIntent.putExtra("marker_time", getIntent().getLongExtra("marker_time", -1));
                    resultIntent.putExtra("tra_loi_dung", soCauDung == danhSachCauHoi.size());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });


        hienThiCauHoi(danhSachCauHoi.get(index));
    }
    private void hienThiCauHoi(CauHoi cauHoi) {
        tvCauHoi.setText(cauHoi.noiDung);
        btnTraLoi.setEnabled(false);
        layoutQuizContainer.removeAllViews();
        layoutQuizContainer.setBackgroundColor(Color.parseColor("#1AFF0000"));
        layoutQuizContainer.setVisibility(ViewGroup.VISIBLE);
        QuizRenderer.render(this, layoutQuizContainer, cauHoi, userAnswer -> {
            currentUserAnswer = userAnswer;
            btnTraLoi.setEnabled(true);
        });
    }
    private void xuLyTraLoi() {
        CauHoi cauHoi = danhSachCauHoi.get(index);
        boolean dung = checkCorrectAnswer(cauHoi, currentUserAnswer);
        float diemDatDuoc = tinhDiemPhanDung(cauHoi, currentUserAnswer); // ✅ đúng một phần
        if (dung) {
            soCauDung++;
            tvThongBao.setText("✅ Đúng rồi!");
            QuizRenderer.renderKetQuaSauTraLoi(this, layoutQuizContainer, cauHoi, currentUserAnswer);
            daTraLoi = true;
            btnTraLoi.setText("Tiếp tục");
            tvThongBao.setVisibility(GONE);
            return;

        }else if (diemDatDuoc > 0f) {
            tvThongBao.setText("🔶 Đúng một phần! (" + Math.round(diemDatDuoc * 100) + "%)");
            QuizRenderer.renderKetQuaSauTraLoi(this, layoutQuizContainer, cauHoi, currentUserAnswer);
            daTraLoi = true;
            btnTraLoi.setText("Tiếp tục");
            String dapAnDung1Phan = "";
            if (cauHoi.quizType.equals("MULTIPLE_CHOICE")) {
                ArrayList<String> dapAnTextList = new ArrayList<>();
                for (int i = 0; i < cauHoi.dapAnDungList.size(); i++) {
                    if (Boolean.TRUE.equals(cauHoi.dapAnDungList.get(i))) {
                        dapAnTextList.add(cauHoi.dapAnList.get(i));
                    }
                }
                dapAnDung1Phan = dapAnTextList.toString();
            }
            // ✅ BỔ SUNG DÒNG NÀY:
            tvThongBao.setVisibility(View.VISIBLE);

            tvThongBao.setText("❌ Sai rồi!\nĐáp án đúng: " + dapAnDung1Phan);
            //tvThongBao.setVisibility(GONE);
            return;

        }
        else {
            // Kiểm tra nếu đang ở chế độ "Trả lời lại"
            String xuLyKhiSai = getIntent().getStringExtra("xu_ly_khi_sai");
            int soLanTraLoiLai = getIntent().getIntExtra("so_lan_tra_loi_lai", Integer.MAX_VALUE);
            int truDiemMoiLan = getIntent().getIntExtra("tru_diem_moi_lan", 0);

            if (xuLyKhiSai != null && xuLyKhiSai.equals("Trả lời lại")) {
                if (soLanSaiMoiCau[index] < soLanTraLoiLai) {
                    soLanSaiMoiCau[index]++;
                    int diemBiTru = truDiemMoiLan * soLanSaiMoiCau[index];
                    tvThongBao.setText("❌ Sai rồi! Trừ " + diemBiTru + "% điểm. Còn " + (soLanTraLoiLai - soLanSaiMoiCau[index]) + " lượt");
                    return; // Không tăng index, cho trả lời lại
                } else {
                    tvThongBao.setText("❌ Sai và đã hết lượt trả lời lại!");
                    daTraLoi = true;
                    btnTraLoi.setText("Tiếp tục");
                    return;
                }
            } else if (xuLyKhiSai.equals("Xem đáp án rồi tiếp tục")) {
                String dapAnDung = "";

                if (cauHoi.quizType.equals("FILL_IN_THE_BLANK") || cauHoi.quizType.equals("SINGLE_CHOICE") || cauHoi.quizType.equals("TRUE_FALSE")) {
                    dapAnDung = cauHoi.dapAnList.get(cauHoi.chiSoDung);
                }

                else if (cauHoi.quizType.equals("MULTIPLE_CHOICE")) {
                    ArrayList<String> dapAnTextList = new ArrayList<>();
                    for (int i = 0; i < cauHoi.dapAnDungList.size(); i++) {
                        if (Boolean.TRUE.equals(cauHoi.dapAnDungList.get(i))) {
                            dapAnTextList.add(cauHoi.dapAnList.get(i));
                        }
                    }
                    dapAnDung = dapAnTextList.toString();
                }

                else if (cauHoi.quizType.equals("ORDER_SEQUENCE")) {
                    ArrayList<String> orderList = new ArrayList<>();
                    for (Object o : cauHoi.dapAnDungList) {
                        int index = Integer.parseInt(String.valueOf(o)) - 1;
                        if (index >= 0 && index < cauHoi.dapAnList.size()) {
                            orderList.add(cauHoi.dapAnList.get(index));
                        }
                    }
                    dapAnDung = orderList.toString();
                }

                else {
                    dapAnDung = cauHoi.dapAnDungList.toString();
                }

                // ✅ BỔ SUNG DÒNG NÀY:
                tvThongBao.setVisibility(View.VISIBLE);

                tvThongBao.setText("❌ Đúng một phần!\nĐáp án đúng chính xác hơn là: " + dapAnDung);

                QuizRenderer.renderKetQuaSauTraLoi(this, layoutQuizContainer, cauHoi, currentUserAnswer);
                daTraLoi = true;
                btnTraLoi.setText("Tiếp tục");
                return;
            }


            else if (xuLyKhiSai.equals("Quay lại mốc")) {
                tvThongBao.setText("❌ Sai rồi! Bạn sẽ phải quay lại mốc trước.");
                QuizRenderer.renderKetQuaSauTraLoi(this, layoutQuizContainer, cauHoi, currentUserAnswer);
                daTraLoi = true;
                btnTraLoi.setText("Tiếp tục");

                // Lưu lại flag đặc biệt để xử lý ở bước tiếp theo
                btnTraLoi.setTag("quay_lai_moc");
                return;
            }


        }

        if (index < danhSachCauHoi.size()) {
            hienThiCauHoi(danhSachCauHoi.get(index));
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("so_cau_dung", soCauDung);
            resultIntent.putExtra("tai_khoan", taiKhoan);
            resultIntent.putExtra("tong_cau", danhSachCauHoi.size());
            resultIntent.putExtra("marker_time", getIntent().getLongExtra("marker_time", -1));
            resultIntent.putExtra("tra_loi_dung", soCauDung == danhSachCauHoi.size());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
    private float tinhDiemPhanDung(CauHoi cauHoi, Object userAnswer) {
        try {
            if (cauHoi.quizType.equals("FILL_IN_THE_BLANK")) {
                String userInput = ((String) userAnswer).trim().toLowerCase();
                String correct = cauHoi.dapAnList.get(cauHoi.chiSoDung).trim().toLowerCase();
                return userInput.contains(correct) ? 1.0f : 0f;
            }

            else if (cauHoi.quizType.equals("MULTIPLE_CHOICE")) {
                ArrayList<Boolean> userList = (ArrayList<Boolean>) userAnswer;
                ArrayList<Boolean> correctList = cauHoi.dapAnDungList;

                int total = correctList.size();
                int dung = 0;
                for (int i = 0; i < total; i++) {
                    if (Objects.equals(userList.get(i), correctList.get(i))) {
                        dung++;
                    }
                }
                return total == 0 ? 0f : ((float) dung / total);
            }

            else if (cauHoi.quizType.equals("ORDER_SEQUENCE")) {
                ArrayList<Integer> userList = (ArrayList<Integer>) userAnswer;

                ArrayList<Integer> correctList = new ArrayList<>();
                for (Object obj : cauHoi.dapAnDungList) {
                    correctList.add(Integer.parseInt(String.valueOf(obj)));
                }

                int total = correctList.size();
                int dung = 0;
                for (int i = 0; i < total; i++) {
                    if (Objects.equals(userList.get(i), correctList.get(i))) {
                        dung++;
                    }
                }
                return total == 0 ? 0f : ((float) dung / total);
            }

            else if (cauHoi.quizType.equals("MATCHING_PAIRS")) {
                HashMap<String, String> userMap = (HashMap<String, String>) userAnswer;

                int total = cauHoi.dapAnList.size();
                int dung = 0;
                for (int i = 0; i < total; i++) {
                    String key = cauHoi.dapAnList.get(i);
                    String correctVal = String.valueOf(cauHoi.dapAnDungList.get(i));
                    String userVal = userMap.getOrDefault(key, "");
                    if (userVal.equalsIgnoreCase(correctVal)) {
                        dung++;
                    }
                }
                return total == 0 ? 0f : ((float) dung / total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    private boolean checkCorrectAnswer(CauHoi cauHoi, Object userAnswer) {
        try {
            switch (cauHoi.quizType) {
                case "SINGLE_CHOICE":
                case "TRUE_FALSE":
                    return (int) userAnswer == cauHoi.chiSoDung;

                case "FILL_IN_THE_BLANK":
                    return ((String) userAnswer).trim().equalsIgnoreCase(cauHoi.dapAnList.get(cauHoi.chiSoDung).trim());

                case "MULTIPLE_CHOICE":
                    ArrayList<Boolean> userList = (ArrayList<Boolean>) userAnswer;
                    ArrayList<Boolean> correctList = cauHoi.dapAnDungList;
                    if (userList.size() != correctList.size()) return false;
                    for (int i = 0; i < userList.size(); i++) {
                        if (!Objects.equals(userList.get(i), correctList.get(i))) return false;
                    }
                    return true;

                case "ORDER_SEQUENCE":
                    ArrayList<Integer> userOrder = (ArrayList<Integer>) userAnswer;
                    ArrayList<Integer> correctOrder = new ArrayList<>();
                    for (Object obj : cauHoi.dapAnDungList) {
                        correctOrder.add(Integer.parseInt(String.valueOf(obj)));
                    }
                    return userOrder.equals(correctOrder);

                case "MATCHING_PAIRS":
                    HashMap<String, String> userPairs = (HashMap<String, String>) userAnswer;
                    HashMap<String, String> correctPairs = new HashMap<>();
                    for (int i = 0; i < cauHoi.dapAnList.size(); i++) {
                        correctPairs.put(cauHoi.dapAnList.get(i), String.valueOf(cauHoi.dapAnDungList.get(i)));
                    }
                    return userPairs.equals(correctPairs);

                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}