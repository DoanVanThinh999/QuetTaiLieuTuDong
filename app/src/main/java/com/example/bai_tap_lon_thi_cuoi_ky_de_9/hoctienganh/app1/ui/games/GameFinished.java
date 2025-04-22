package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameFinishedBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameFinished {
    private final Context mCtx;
    private final GameFinishedBinding mB;
    private boolean mIsSuccess;
    private MediaPlayer mSuccessPlayer, mFailPlayer;
    private DialogInterface.OnDismissListener mListener;
    private final Settings mSt;

    // Firebase Database Reference
    private DatabaseReference databaseReference;

    public GameFinished(Context context) {
        mCtx = context;
        mSt = Settings.from(context);
        mB = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.game_finished, null, false);
        mB.setSettings(mSt);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("game_results");

        if (!mSt.getInGameSound()) return;
        mFailPlayer = MediaPlayer.create(context, R.raw.fail);
        mSuccessPlayer = MediaPlayer.create(context, R.raw.success);
    }

    public GameFinished with(boolean success, Date elapsed, int hearts, int progress) {
        mIsSuccess = success;
        mB.setSuccess(success);
        mB.setTime(new SimpleDateFormat("mm:ss", Locale.US).format(elapsed));
        mB.setHearts(hearts);
        mB.setProgress(progress);

        // Ghi dữ liệu lên Firebase
        uploadDataToFirebase(elapsed, hearts, progress);
        return this;
    }

    private void uploadDataToFirebase(Date elapsed, int hearts, int progress) {
        String finishedTime = new SimpleDateFormat("mm:ss", Locale.US).format(elapsed);
        String perfection = hearts + " / " + mSt.getMaxHearts(); // Cách tính độ hoàn hảo
        String progressText = progress + " / " + mSt.getMaxProgress();

        // Lấy email của người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : "unknown"; // Kiểm tra nếu người dùng đã đăng nhập

        // Tạo đối tượng GameResult
        GameResult gameResult = new GameResult(finishedTime, perfection, progressText, email);

        // Ghi dữ liệu lên Firebase
        databaseReference.push().setValue(gameResult).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(mCtx, "Dữ liệu đã được lưu!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mCtx, "Lỗi khi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public GameFinished withListener(DialogInterface.OnDismissListener onDismissListener) {
        this.mListener = onDismissListener;
        return this;
    }

    public ViewGroup getView() {
        return (ViewGroup) mB.getRoot();
    }

    public AlertDialog.Builder createDialog() {
        if (mSt.getInGameSound())
            (mIsSuccess ? mSuccessPlayer : mFailPlayer).start();
        return new AlertDialog.Builder(mCtx)
                .setTitle(mIsSuccess ? R.string.game_finished_success_title : R.string.game_finished_failure_title)
                .setView(getView())
                .setPositiveButton("OK", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        release();
                        mListener.onDismiss(dialogInterface);
                    }
                });
    }

    public static AlertDialog.Builder createConfirmLeaveDialog(Context context, DialogInterface.OnClickListener onLeave) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.game_leave_title)
                .setMessage(R.string.game_leave_text)
                .setNegativeButton(R.string.game_leave_no, null)
                .setPositiveButton(R.string.game_leave_yes, onLeave);
    }

    private void release() {
        if (!mSt.getInGameSound()) return;
        mSuccessPlayer.release();
        mFailPlayer.release();
    }

    // Lớp GameResult để lưu trữ kết quả
    public static class GameResult {
        public String finishedTime;
        public String perfection;
        public String progress;
        public String email;

        public GameResult() {
            // Constructor rỗng để Firebase có thể sử dụng
        }

        public GameResult(String finishedTime, String perfection, String progress, String email) {
            this.finishedTime = finishedTime;
            this.perfection = perfection;
            this.progress = progress;
            this.email = email; // Thêm email vào đối tượng
        }
    }
}