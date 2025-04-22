package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception.NotEnoughWordsException;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;

import java.util.List;

public abstract class GameActivity extends AppCompatActivity {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();

        try {
            start();
        }
        catch (NotEnoughWordsException e) {
            e.printStackTrace();
            alertLearnedWordsNotEnough(e.getMinimumWordsToLearn(), e.getCurrentLearnedWords());
        }
    }

    /**
     * Chọn ra các từ có <code>length >= minLength</code> (modify mảng gốc) và báo lỗi khi người học học được ít hơn <code>minimum</code> từ
     */
    protected final void assertLearnedWords(List<DictionaryEntry> learned, int minimum, int minLength) {
        if (learned.size() < minimum)
            throw new NotEnoughWordsException(minimum, learned.size());
        if (minLength == 0) return;
        for (int i = learned.size() - 1; i >= 0; i--)
            if (learned.get(i).getName().length() < minLength)
                learned.remove(i);
        if (learned.size() < minimum)
            throw new NotEnoughWordsException(NotEnoughWordsException.UNKNOWN, learned.size());
    }

    /**
     * Được chạy khi mới mở activity, xoay màn hình, chạy trước start
     */
    protected abstract void initialize();

    /**
     * Báo "Không đủ vốn từ" và thoát
     */
    protected void alertLearnedWordsNotEnough(int minimumLearnedWords, int currentLearnedWords) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_learned_words_not_enough_title)
                .setMessage(minimumLearnedWords == NotEnoughWordsException.UNKNOWN ? getString(R.string.game_learned_words_not_enough_desc_unknown) : getString(R.string.game_learned_words_not_enough_desc, minimumLearnedWords, currentLearnedWords))
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /**
     * Báo "Không có kết nối" và đưa ra các lựa chọn để giải quyết
     */
    protected void alertNoInternet() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_quiz_no_internet_title)
                .setMessage(R.string.game_quiz_no_internet_text)
                .setCancelable(false)
                .setPositiveButton(R.string.game_quiz_no_internet_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        start();
                    }
                })
                .setNegativeButton(R.string.game_quiz_no_internet_ignore, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        restart();
                    }
                })
                .setNeutralButton(R.string.game_quiz_no_internet_abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    /**
     * Được chạy khi mới mở activity, xoay màn hình
     */
    protected abstract void start();

    /**
     * Được chạy sau mỗi lần trả lời, dùng để tạo từ mới
     */
    protected abstract void restart();

    /**
     * Khoá câu trả lời
     */
    protected abstract void lock();

    /**
     * Mở khoá câu trả lời
     */
    protected abstract void unlock();

    /**
     * Chỉnh giá trị cho viewModel
     */
    protected abstract void create();

    /**
     * Sử dụng viewModel đã có để render
     */
    protected abstract void display();
    protected boolean isCorrect() {
        return getCorrect() != null;
    }
    protected abstract DictionaryEntry getCorrect();
    protected void onSubmit(View view) {
        lock();
    }
    protected void onContinue(View view) {
        unlock();
        restart();
    }
}
