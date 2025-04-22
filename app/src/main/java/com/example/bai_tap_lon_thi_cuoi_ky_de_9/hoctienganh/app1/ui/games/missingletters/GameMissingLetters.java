package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.missingletters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameMissingLettersBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameManager;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;

import java.util.List;

public class GameMissingLetters extends GameActivity {
    public static final float MISSING_LETTERS_PERCENT = 0.5f;
    public static final int MIN_LEARNED_WORDS = 10;
    private GameMissingLettersViewModel mVm;
    private GameMissingLettersBinding mB;
    private List<DictionaryEntry> learned;

    @Override
    protected void initialize() {
        learned = DictionaryEntry.getLearned(this);
        mVm = new ViewModelProvider(this).get(GameMissingLettersViewModel.class);
        mVm.getInput().observe(this, new Observer<char[]>() {
            @Override
            public void onChanged(char[] chars) {
                mVm.setCheckEnabled(!Arrays.contains(chars, '\0'));
            }
        });
        mB = DataBindingUtil.setContentView(this, R.layout.game_missing_letters);
        mB.setLifecycleOwner(this);
        GameManager.manage(this, mB.gameCheck, mB.gameIndicator)
                .with(mVm)
                .withChecker(this::isCorrect, this::getCorrect)
                .withListener(this::onSubmit, this::onContinue);
    }

    @Override
    protected DictionaryEntry getCorrect() {
        return DictionaryEntry.find(this, String.valueOf(mVm.getInputValue()), true, null);
    }

    @Override
    protected void create() {
        DictionaryEntry randomEntry = Arrays.pickRandom(learned);
        int lettersCount = randomEntry.getName().length();
        char[] input = randomEntry.getName().toCharArray();

        // Chuyển một số ký tự ngẫu nhiên thành '\0' (chữ chưa điền)
        int[] shuffledRange = Arrays.shuffle(Arrays.range(lettersCount));
        int[] hiddenIndexes = java.util.Arrays.copyOf(shuffledRange, (int)(lettersCount * MISSING_LETTERS_PERCENT));
        for (int index : hiddenIndexes) input[index] = '\0';

        mVm.setEntry(randomEntry);
        mVm.setInput(input);
        mVm.setDefaultInput(input);
    }

    @Override
    protected void display() {
        ViewGroup viewGroup = mB.gameMissingLettersLetters;
        viewGroup.removeAllViews();
        char[] defaultInput = mVm.getDefaultInputValue();
        for (int i = 0; i < defaultInput.length; i++) {
            char c = defaultInput[i];
            View view = getLayoutInflater().inflate(c == '\0' ? R.layout.game_letter_edittext : R.layout.game_letter, viewGroup, false);;
            if (c == '\0') {
                final int j = i; // Truyền i vào j để khỏi bị lỗi final
                boolean isLastEditText = Arrays.lastIndexOf(defaultInput, c) == i;
                boolean isFirstEditText = Arrays.indexOf(defaultInput, c) == i;
                EditText editText = (EditText)view;
                if (mVm.getInputValue()[i] != '\0') editText.setText(String.valueOf(mVm.getInputValue()[i]));
                editText.addTextChangedListener(new TextWatcher() {
                    public char c;
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        c = charSequence.length() == 0 ? '\0' : charSequence.charAt(i);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (!isFirstEditText || editable.length() != 0)
                            editText.onEditorAction(editable.length() == 0 ? EditorInfo.IME_ACTION_PREVIOUS : editText.getImeOptions());
                        mVm.setInputAt(j, c);
                        if (editable.length() <= 1) return;
                        editText.setText(String.valueOf(c));
                    }
                });
                if (isLastEditText) editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            else {
                TextView textView = (TextView)view;
                textView.setText(String.valueOf(c));
            }
            viewGroup.addView(view);
        }
        if (mVm.getShowResultValue()) lock(); else unlock();
    }

    @Override
    protected void start() {
        assertLearnedWords(learned, getResources().getInteger(R.integer.game_missing_letters_min_learned_words), getResources().getInteger(R.integer.game_missing_letters_min_length));
        if (mVm.getEntryValue() == null) create();
        display();
    }

    @Override
    protected void restart() {
        create();
        display();
    }

    @Override
    protected void lock() {
        ViewGroup viewGroup = mB.gameMissingLettersLetters;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof EditText) child.setEnabled(false);
        }
    }

    @Override
    protected void unlock() {
    }
}
