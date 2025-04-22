package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.unscramble;

import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameUnscrambleBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameManager;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;

import java.util.List;

public class GameUnscramble extends GameActivity {
    protected GameUnscrambleViewModel mVm;
    private GameUnscrambleBinding mB;
    private int mSelIndex = -1;
    private View mSelView = null;
    private List<DictionaryEntry> learned;

    @Override
    protected void initialize() {
        learned = DictionaryEntry.getLearned(this);
        mVm = new ViewModelProvider(this).get(GameUnscrambleViewModel.class);
        mB = DataBindingUtil.setContentView(this, R.layout.game_unscramble);
        mB.setLifecycleOwner(this);
        GameManager.manage(this, mB.gameCheck, mB.gameIndicator)
                .with(mVm)
                .withChecker(this::isCorrect, this::getCorrect)
                .withListener(this::onSubmit, this::onContinue);
    }

    @Override
    protected void create() {
        DictionaryEntry randomEntry = Arrays.pickRandom(learned);
        char[] input = Arrays.shuffle(randomEntry.getName().toCharArray());
        mVm.setEntry(randomEntry);
        mVm.setInput(input);
    }

    @Override
    protected void display() {
        ViewGroup viewGroup = mB.gameUnscrambleLetters;
        viewGroup.removeAllViews();
        char[] input = mVm.getInputValue();
        for (int i = 0; i < input.length; i++) {
            int j = i; // Truyền i vào j để khỏi bị lỗi final
            char c = input[i];
            View view = getLayoutInflater().inflate(R.layout.game_letter, viewGroup, false);;
            TextView textView = (TextView)view;
            textView.setText(String.valueOf(c));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLetterClick(view, j);
                }
            });
            viewGroup.addView(view);
        }
        if (mVm.getShowResultValue()) lock(); else unlock();
    }

    private void swap(int i, int j, View iView, View jView) {
        lock(); // Tắt hàm onClick để tránh người dùng đổi chữ quá nhanh gây lỗi
        mVm.swapInput(i, j);
        int dX = (int)(iView.getX() - jView.getX());
        int dY = (int)(iView.getY() - jView.getY());
        jView.animate()
                .translationX(dX)
                .translationY(dY)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .setDuration(getResources().getInteger(R.integer.game_unscramble_letter_swap_time))
                .start();
        iView.animate()
                .translationX(-dX)
                .translationY(-dY)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .setDuration(getResources().getInteger(R.integer.game_unscramble_letter_swap_time))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        iView.setTranslationX(0);
                        iView.setTranslationY(0);
                        jView.setTranslationX(0);
                        jView.setTranslationY(0);
                        unlock();
                        display();
                    }
                })
                .start();
    }

    @Override
    protected void start() {
        assertLearnedWords(learned, getResources().getInteger(R.integer.game_unscramble_min_learned_words), getResources().getInteger(R.integer.game_unscramble_min_length));
        if (mVm.getEntryValue() == null) create();
        display();
    }

    @Override
    protected void restart() {
        create();
        display();
    }

    @Override
    protected DictionaryEntry getCorrect() {
        return DictionaryEntry.find(this, String.valueOf(mVm.getInputValue()), false, null);
    }

    @Override
    protected void lock() {
        mSelIndex = -1;
        mSelView = null;
        ViewGroup viewGroup = mB.gameUnscrambleLetters;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setOnClickListener(null);
        }
    }

    @Override
    protected void unlock() {
        ViewGroup viewGroup = mB.gameUnscrambleLetters;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            int j = i;
            View child = viewGroup.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLetterClick(view, j);
                }
            });
        }
    }

    public void onLetterClick(View view, int j) {
        if (mSelIndex == -1) {
            view.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.game_letter_selected_300)));
            mSelIndex = j;
            mSelView = view;
            return;
        }
        mSelView.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.game_letter_500)));
        if (j != mSelIndex) swap(mSelIndex, j, mSelView, view);
        mSelIndex = -1;
    }
}
