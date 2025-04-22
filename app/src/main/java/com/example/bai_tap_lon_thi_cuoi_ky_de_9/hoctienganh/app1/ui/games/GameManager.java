package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Supplier;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameCheckBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameIndicatorBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception.NotEnoughItemsException;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Settings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameManager {
    private final GameIndicatorBinding mIb;
    private final GameCheckBinding mCb;
    private final Activity mAct;
    private MediaPlayer mCorrectPlayer, mIncorrectPlayer;
    private View.OnClickListener mSubmitListener, onContinueListener;
    private GameViewModel mVm;
    private Supplier<Boolean> mIsCorrect;
    private Supplier<DictionaryEntry> mGetCorrect;
    private final Settings mSettings;

    private GameManager(AppCompatActivity activity, GameCheckBinding checkBinding, GameIndicatorBinding indicatorBinding) {
        mAct = activity;
        mSettings = Settings.from(activity);
        mCb = checkBinding;
        mCb.setSelf(this);
        mIb = indicatorBinding;
        mIb.setSettings(mSettings);
        mIb.setSelf(this);

        activity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmLeave(null);
            }
        });

        if (!mSettings.getInGameSound()) return;
        mCorrectPlayer = MediaPlayer.create(activity, R.raw.correct);
        mIncorrectPlayer = MediaPlayer.create(activity, R.raw.incorrect);
    }

    public static GameManager manage(AppCompatActivity activity, GameCheckBinding binding, GameIndicatorBinding indicatorBinding) {
        return new GameManager(activity, binding, indicatorBinding);
    }

    public GameManager with(GameViewModel viewModel) {
        mVm = viewModel;
        if (mVm.getHeartsValue() == GameViewModel.FROM_SETTING) mVm.setHearts(mSettings.getMaxHearts());
        mIb.setViewModel(viewModel);
        mCb.setViewModel(viewModel);
        return this;
    }

    // Từ đúng không nhất thiết phải trùng với từ mà game đã đề ra
    // VD: Từ hiện tại là cat (trong game đoán chữ ghi là ca_), tui ghi car vẫn được
    public GameManager withChecker(Supplier<Boolean> isCorrect, Supplier<DictionaryEntry> getCorrect) {
        this.mIsCorrect = isCorrect;
        this.mGetCorrect = getCorrect;
        return this;
    }

    public GameManager withListener(View.OnClickListener onSubmitPressedListener, View.OnClickListener onContinuePressedListener) {
        this.mSubmitListener = onSubmitPressedListener;
        this.onContinueListener = onContinuePressedListener;
        return this;
    }

    public void check(View view) {
        mVm.setGameCheckState(true);
        if (mIsCorrect.get()) {
            correct(mGetCorrect.get());
            mVm.setProgress(GameViewModel.INCREMENT);
        }
        else {
            incorrect(mVm.getEntryValue());
            mVm.setHearts(GameViewModel.DECREMENT);
        }
        if (mSubmitListener != null) mSubmitListener.onClick(view);
    }

    public void confirmLeave(View view) {
        GameFinished.createConfirmLeaveDialog(mAct, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAct.finish();
                release();
            }
        }).show();
    }

    public void onContinue(View view) {
        mVm.setGameCheckState(false);
        if (mVm.getProgressValue() >= mSettings.getMaxProgress()) {
            finishGame(true, mVm.getElapsed());
            return;
        }
        if (mVm.getHeartsValue() <= 0) {
            finishGame(false, mVm.getElapsed());
            return;
        }
        if (onContinueListener != null) onContinueListener.onClick(view);
    }

    private void finishGame(boolean success, Date elapsed) {
        new GameFinished(mAct)
                .with(success, elapsed, mVm.getHeartsValue(), mVm.getProgressValue())
                .withListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mAct.finish();
                        release();
                    }
                })
                .createDialog()
                .show();
    }

    private void correct(DictionaryEntry correctEntry) {
        List<String> posArray = new ArrayList<>();
        for (String pos: new String[] {correctEntry.getN(), correctEntry.getV(), correctEntry.getA(), correctEntry.getAdv(), correctEntry.getPrep(), correctEntry.getInj()})
            if (pos != null) posArray.add(pos);
        mVm.setIsCorrect(true);
        mVm.setCheckTitle(Arrays.pickRandom(mAct.getResources().getStringArray(R.array.game_correct_titles)));
        try {
            mVm.setCheckSubText(Arrays.pickRandom(posArray));
        }
        catch (NotEnoughItemsException ignore) {}
        if (!mSettings.getInGameSound()) return;
        mCorrectPlayer.start();
    }

    private void incorrect(DictionaryEntry correctEntry) {
        mVm.setIsCorrect(false);
        mVm.setCheckTitle(Arrays.pickRandom(mAct.getResources().getStringArray(R.array.game_incorrect_titles)));
        mVm.setCheckSubText(correctEntry.getName());
        if (!mSettings.getInGameSound()) return;
        mIncorrectPlayer.start();
    }

    private void release() {
        if (!mSettings.getInGameSound()) return;
        mCorrectPlayer.release();
        mIncorrectPlayer.release();
    }
}
