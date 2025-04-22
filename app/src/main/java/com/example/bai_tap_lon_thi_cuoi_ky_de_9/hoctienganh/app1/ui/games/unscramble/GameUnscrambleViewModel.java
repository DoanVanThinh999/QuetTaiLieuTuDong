package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.unscramble;

import androidx.lifecycle.MutableLiveData;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameViewModel;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;


public class GameUnscrambleViewModel extends GameViewModel {
    private MutableLiveData<char[]> mInput = new MutableLiveData<>();

    public MutableLiveData<char[]> getInput() {
        return mInput;
    }

    public char[] getInputValue() {
        return mInput.getValue();
    }

    public void setInput(char[] input) {
        mInput.setValue(input);
    }

    public void swapInput(int i, int j) {
        char[] array = getInputValue().clone();
        Arrays.swap(array, i, j);
        setInput(array);
    }
}
