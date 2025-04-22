package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;

import java.util.Date;

public abstract class GameViewModel extends ViewModel {
    private final MutableLiveData<Integer> mHearts, mProgress;
    private final MutableLiveData<DictionaryEntry> mEntry;
    private final MutableLiveData<Date> mStartAt;
    private final MutableLiveData<Boolean> mShowResult, mIsCorrect, mCheckEnabled;
    private final MutableLiveData<String> mCheckTitle, mCheckSubText;

    public GameViewModel() {
        mHearts = new MutableLiveData<>(FROM_SETTING);
        mEntry = new MutableLiveData<>();
        mStartAt = new MutableLiveData<>(new Date());
        mProgress = new MutableLiveData<>(0);
        mShowResult = new MutableLiveData<>(false);
        mIsCorrect = new MutableLiveData<>(false);
        mCheckEnabled = new MutableLiveData<>(true);
        mCheckTitle = new MutableLiveData<>();
        mCheckSubText = new MutableLiveData<>();
    }

    public static final int FROM_SETTING = -4, DISABLED = -1, INCREMENT = -2, DECREMENT = -3;

    public MutableLiveData<Integer> getHearts() {
        return mHearts;
    }

    public int getHeartsValue() {
        return mHearts.getValue();
    }

    public void setHearts(int hearts) {
        mHearts.setValue(hearts == INCREMENT ? getHeartsValue() + 1
                : hearts == DECREMENT ? getHeartsValue() - 1
                : hearts
        );
    }

    public MutableLiveData<Integer> getProgress() {
        return mProgress;
    }

    public int getProgressValue() {
        return mProgress.getValue() == null
                ? 0
                : mProgress.getValue();
    }

    public void setProgress(int progress) {
        mProgress.setValue(progress == INCREMENT ? getProgressValue() + 1
                : progress == DECREMENT ? getProgressValue() - 1
                : progress
        );
    }

    public MutableLiveData<DictionaryEntry> getEntry() {
        return mEntry;
    }

    public DictionaryEntry getEntryValue() {
        return mEntry.getValue();
    }

    public void setEntry(DictionaryEntry entry) {
        mEntry.setValue(entry);
    }

    public Date getStartDateValue() {
        return mStartAt.getValue();
    }

    public Date getElapsed() {
        return new Date(System.currentTimeMillis() - getStartDateValue().getTime());
    }

    public MutableLiveData<Boolean> getShowResult() {
        return mShowResult;
    }

    public boolean getShowResultValue() {
        return mShowResult.getValue() == null ? false : mShowResult.getValue();
    }

    public void setGameCheckState(boolean showResult) {
        mShowResult.setValue(showResult);
    }

    public MutableLiveData<Boolean> getIsCorrect() {
        return mIsCorrect;
    }

    public boolean getIsCorrectValue() {
        return mIsCorrect.getValue() == null ? false : mShowResult.getValue();
    }

    public void setIsCorrect(boolean isCorrect) {
        mIsCorrect.setValue(isCorrect);
    }

    public MutableLiveData<Boolean> getCheckEnabled() {
        return mCheckEnabled;
    }

    public boolean getCheckEnabledValue() {
        return mCheckEnabled.getValue() == null ? false : mCheckEnabled.getValue();
    }

    public void setCheckEnabled(boolean checkEnabled) {
        mCheckEnabled.setValue(checkEnabled);
    }

    public MutableLiveData<String> getCheckTitle() {
        return mCheckTitle;
    }

    public String getCheckTitleValue() {
        return mCheckTitle.getValue();
    }

    public void setCheckTitle(String checkTitle) {
        mCheckTitle.setValue(checkTitle);
    }

    public MutableLiveData<String> getCheckSubText() {
        return mCheckSubText;
    }

    public String getCheckSubTextValue() {
        return mCheckSubText.getValue();
    }

    public void setCheckSubText(String subText) {
        mCheckSubText.setValue(subText);
    }
}
