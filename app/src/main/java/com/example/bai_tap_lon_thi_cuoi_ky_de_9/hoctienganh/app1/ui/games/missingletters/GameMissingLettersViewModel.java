package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.missingletters;

import androidx.lifecycle.MutableLiveData;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameViewModel;


public class GameMissingLettersViewModel extends GameViewModel {
    private final MutableLiveData<char[]> defaultInput = new MutableLiveData<>();
    private final MutableLiveData<char[]> input = new MutableLiveData<>();

    public MutableLiveData<char[]> getDefaultInput() {
        return defaultInput;
    }

    public char[] getDefaultInputValue() {
        return defaultInput.getValue();
    }

    public void setDefaultInput(char[] defaultInput) {
        this.defaultInput.setValue(defaultInput);
    }

    public MutableLiveData<char[]> getInput() {
        return input;
    }

    public char[] getInputValue() {
        return input.getValue();
    }

    public void setInput(char[] input) {
        this.input.setValue(input);
    }

    public void setInputAt(int index, char input) {
        char[] array = getInputValue().clone();
        array[index] = input;
        setInput(array);
    }
}
