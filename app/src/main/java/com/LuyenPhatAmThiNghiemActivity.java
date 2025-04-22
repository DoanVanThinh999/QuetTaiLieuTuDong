
package com;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.Model.TaiKhoan;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LuyenPhatAmThiNghiemActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final String AI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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"; // Replace with your OpenAI API key
    //private static final String GEMMI_API_URL = "https://api.gemmi.ai/v1/analyze"; // URL API của Gemmi
    //private static final String API_KEY = "AIzaSyDNJQLz2msYCDi4P39PlQhlOxw4GS0M5Z0"; // Thay bằng API key của Gemmi
    TextView edtNoiDung, tvTaiKhoanKH, tvCauHoi, tvPhanTich, tvDiemSo, tvTimer;
    ImageButton btnSpeak;
    ScrollView scrollView, scrollView3;
    Button btnSubmit;
    private final OkHttpClient client = new OkHttpClient();
    private String recordedAnswer = ""; // Lưu câu trả lời đã ghi âm
    private CountDownTimer countDownTimer; // Đồng hồ đếm ngược
    private boolean isTimerRunning = false; // Trạng thái đồng hồ

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_phat_am_thi_nghiem);

        edtNoiDung = findViewById(R.id.edtNoiDung);
        tvTimer = findViewById(R.id.tvTimer);
        btnSpeak = findViewById(R.id.btnSpeak);
        tvTaiKhoanKH = findViewById(R.id.tvTaiKhoanKH);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        tvPhanTich = findViewById(R.id.tvPhanTich);
        tvDiemSo = findViewById(R.id.tvDiemSo);
        scrollView = findViewById(R.id.scrollView);
        btnSubmit = findViewById(R.id.btnSubmit);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        scrollView3 = findViewById(R.id.scrollView3);
        scrollView3.post(() -> scrollView3.fullScroll(ScrollView.FOCUS_DOWN));

        Intent intent = getIntent();
        TaiKhoan taiKhoan = (TaiKhoan) intent.getSerializableExtra("taiKhoan");
        tvTaiKhoanKH.setText("Xin chào, " + (taiKhoan != null ? taiKhoan.getTenDangNhap() : "Quản trị viên"));

        btnSpeak.setOnClickListener(view -> {
            if (!isTimerRunning) {
                startTimer();
                speak();
            } else {
                Toast.makeText(this, "Đang trong thời gian ghi âm. Vui lòng chờ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(view -> {
            if (!recordedAnswer.isEmpty()) {
                String question = tvCauHoi.getText().toString();
                analyzeAnswer(question, recordedAnswer); // Gửi câu trả lời đã ghi âm để chấm
            } else {
                Toast.makeText(this, "Vui lòng ghi âm câu trả lời trước khi nộp.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, speak something");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000);

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) { // 30 giây
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Thời gian còn lại: " + millisUntilFinished / 1000 + " giây");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Hết thời gian!");
                isTimerRunning = false;
                Toast.makeText(LuyenPhatAmThiNghiemActivity.this, "Hết thời gian ghi âm!", Toast.LENGTH_SHORT).show();
            }
        };
        isTimerRunning = true;
        countDownTimer.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            tvTimer.setText("Thời gian đã dừng.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                stopTimer(); // Dừng đồng hồ khi nói xong
                recordedAnswer = findClosestWord(results); // Lưu câu trả lời đã ghi âm
                edtNoiDung.setText(recordedAnswer);
            }
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu giọng nói. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
    private String findClosestWord(ArrayList<String> options) {
        if (options.isEmpty()) return "";

        String bestMatch = options.get(0); // Mặc định lấy kết quả đầu tiên
        int minDistance = Integer.MAX_VALUE;

        for (String option : options) {
            int distance = levenshteinDistance(bestMatch.toLowerCase(), option.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = option;
            }
        }
        return bestMatch;
    }

    // Hàm tính khoảng cách Levenshtein giữa hai chuỗi
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private void analyzeAnswer(String question, String answer) {
        JSONObject jsonRequest = new JSONObject();
        try {
            //jsonRequest.put("model", "gpt-4");
            jsonRequest.put("model", "gpt-4o-mini");  // Model phù hợp
            JSONArray messages = new JSONArray();

            messages.put(new JSONObject().put("role", "system").put("content",
                    "Bạn là một giám khảo IELTS chuyên nghiệp. Bạn sẽ chấm điểm bài nói của thí sinh dựa trên tiêu chí IELTS (Fluency, Vocabulary, Grammar, Pronunciation) theo thang điểm 0-9.\n" +
                            "Ngoài ra, bạn sẽ kiểm tra xem câu trả lời có phù hợp với câu hỏi không, phân tích lỗi sai bằng tiếng Việt, và đề xuất một câu trả lời chuẩn hơn với từ vựng phong phú hơn.\n" +
                            "Cuối cùng, hiển thị số điểm IELTS theo từng tiêu chí."
            ));

            messages.put(new JSONObject().put("role", "user").put("content",
                    "Câu hỏi: " + question + "\n" +
                            "Câu trả lời của thí sinh: " + answer + "\n" +
                            "Phân tích: \n" +
                            "- Câu trả lời có đúng với câu hỏi không?\n" +
                            "- Những lỗi sai về ngữ pháp, từ vựng, phát âm là gì? (viết bằng tiếng Việt)\n" +
                            "- Một câu trả lời chuẩn hơn sẽ như thế nào?\n" +
                            "- Chấm điểm IELTS (Fluency, Vocabulary, Grammar, Pronunciation) theo thang điểm 0-9."
            ));

            jsonRequest.put("messages", messages);
            jsonRequest.put("max_tokens", 500);
            jsonRequest.put("temperature", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonRequest.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(AI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        sendRequest(request, 0);
    }

    private void sendRequest(Request request, int retryCount) {
        final int MAX_RETRIES = 3;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (retryCount < MAX_RETRIES) {
                    sendRequest(request, retryCount + 1);
                } else {
                    runOnUiThread(() -> Toast.makeText(LuyenPhatAmThiNghiemActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        runOnUiThread(() -> parseResponse(responseBody));
                    } else if (response.code() == 429 && retryCount < MAX_RETRIES) {
                        Log.e("Retry", "Thử lại lần: " + (retryCount + 1));
                        Thread.sleep(2000); // Chờ 2 giây
                        sendRequest(request, retryCount + 1);
                    } else {
                        runOnUiThread(() -> Toast.makeText(LuyenPhatAmThiNghiemActivity.this, "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            String analysisText = choices.getJSONObject(0).getJSONObject("message").getString("content").trim();

            // Phân tách nội dung phản hồi
            String[] sections = analysisText.split("\n- "); // Tách theo dấu "- "

            // Lấy dữ liệu quan trọng
            String relevanceAnalysis = sections.length > 1 ? sections[1] : "Không xác định.";
            String errorAnalysis = sections.length > 2 ? sections[2] : "Không có lỗi nào được chỉ ra.";
            String improvedAnswer = sections.length > 3 ? sections[3] : "Không có gợi ý câu trả lời mới.";
            String ieltsScores = sections.length > 4 ? sections[4] : "Không có điểm số.";

            // Hiển thị phân tích bằng tiếng Việt
            String finalAnalysis = "🔹 **Đánh giá sự phù hợp:** " + relevanceAnalysis + "\n\n"
                    + "🔹 **Lỗi sai:** " + errorAnalysis + "\n\n"
                    + "🔹 **Câu trả lời tốt hơn (English):** " + improvedAnswer + "\n\n"
                    + "🔹 **Điểm IELTS:** " + ieltsScores;

            runOnUiThread(() -> {
                tvPhanTich.setText(finalAnalysis);

                // Cập nhật điểm IELTS
                String extractedScore = extractScore(ieltsScores);
                tvDiemSo.setText("Điểm IELTS: " + extractedScore);
            });

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                tvPhanTich.setText("Phân tích: Lỗi trong xử lý phản hồi.");
                tvDiemSo.setText("Điểm IELTS: Không xác định.");
            });
        }
    }

    // Hàm trích xuất điểm số từ phản hồi AI
    private String extractScore(String text) {
        // Tìm số hợp lệ dạng 4, 5.0, 6.5, 7.5, ...
        Pattern pattern = Pattern.compile("\\b[0-9](\\.5|\\.0)?\\b");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(); // Lấy số hợp lệ đầu tiên
        }

        return "Không xác định"; // Nếu không tìm thấy số hợp lệ
    }
}