package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.quiz;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.util.Predicate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.GameQuizBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameActivity;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.GameManager;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Speaker;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

public class GameQuiz extends GameActivity {
    public static final int QUIZ_MODE_TRANSLATE = 0, QUIZ_MODE_SYNONYM = 1, QUIZ_MODE_IMG_TO_WORD = 2, QUIZ_MODE_SOUND_TO_WORD = 3;
    /*
    0: Dịch tiếng anh sang tiếng việt
    1: Đồng nghĩa với từ nào?
    2: Nhìn hình đoán từ tiếng anh
    3: Nghe âm thanh và chọn từ tiếng anh
    */
    private GameQuizViewModel mVm;
    private GameQuizBinding mB;
    private NodeList quizzes;
    @Override
    protected void initialize() {
        InputStream is = getResources().openRawResource(R.raw.relationship);
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            quizzes = doc.getElementsByTagName("quiz");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVm = new ViewModelProvider(this).get(GameQuizViewModel.class);
        mB = DataBindingUtil.setContentView(this, R.layout.game_quiz);
        mB.setLifecycleOwner(this);
        mB.gameQuizAnswers.setOnCheckedChanged(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!compoundButton.isChecked()) return;
                int selected = Integer.parseInt(compoundButton.getTag().toString());
                mVm.setInput(selected);
            }
        });
        mB.setViewModel(mVm);
        GameManager.manage(this, mB.gameCheck, mB.gameIndicator)
                .with(mVm)
                .withChecker(this::isCorrect, this::getCorrect)
                .withListener(this::onSubmit, this::onContinue);
    }

    @Override
    protected DictionaryEntry getCorrect() {
        return mVm.getAnswerValue() == mVm.getInputValue() ? mVm.getEntryValue() : null;
    }

    @Override
    protected void create() {
        // Chọn 1 cái làm câu trả lời đúng
        int answer = (int)(Math.random() * 4);
        Element randomQuiz;
        String[] choicesArr = new String[4];
        int quizMode = Arrays.pickRandom(new int[] {QUIZ_MODE_IMG_TO_WORD, QUIZ_MODE_SOUND_TO_WORD});
        switch (quizMode) {
            case QUIZ_MODE_IMG_TO_WORD:
                // Chọn phần tử [type="sound"] ngẫu nhiên
                randomQuiz = getRandomQuizByType("img");

                // Chọn 4 cái bất kỳ
                choicesArr = Arrays.pickRandom(XmlUtil.getItems(randomQuiz), 4);
                mVm.setEntry(DictionaryEntry.find(this, choicesArr[answer], true, null));

                // Ghi tiêu đề câu hỏi, làm hình ảnh
                mVm.setQuestion(randomQuiz.getAttribute("label"));
                break;
            case QUIZ_MODE_SOUND_TO_WORD:
                // Chọn phần tử [type="sound"] ngẫu nhiên
                randomQuiz = getRandomQuizByType("sound");

                // Chọn 4 cái bất kỳ
                choicesArr = Arrays.pickRandom(XmlUtil.getItems(randomQuiz), 4);
                mVm.setEntry(DictionaryEntry.find(this, choicesArr[answer], true, null));

                // Ghi tiêu đề câu hỏi, làm âm thanh
                mVm.setQuestion(getString(R.string.game_quiz_sound_desc));
                break;
        }
        mVm.setInput(0);
        mVm.setAnswer(answer);
        mVm.setQuizMode(quizMode);
        mVm.setChoices(choicesArr);
    }
    @Override
    protected void display() {
        switch (mVm.getQuizModeValue()) {
            case QUIZ_MODE_IMG_TO_WORD:
                Glide.with(this).load(Uri.parse(mVm.getEntryValue().getImg())).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        alertNoInternet();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(mB.gameQuizImg);
                break;
            case QUIZ_MODE_SOUND_TO_WORD:
                Speaker speaker = Speaker.from(this, mB.gameQuizSpeak, mVm.getEntryValue().getSound());
                speaker.setOnSpeakStateListener(new Speaker.OnSpeakStateChangeListener() {
                    @Override
                    public void onSpeakStateChanged(ImageView view, int speakState) {
                        if (speakState == Speaker.SPEAK_ERROR) alertNoInternet();
                    }
                });
                mB.setSpeaker(speaker);
                break;
        }
    }

    private Element getRandomQuizByType(String type) {
        return Arrays.pickRandom(XmlUtil.toArray(quizzes, new Predicate<Element>() {
            @Override
            public boolean test(Element element) {
                return element.getAttribute("type").equals(type);
            }
        }));
    }

    @Override
    protected void start() {
        List<DictionaryEntry> learned = DictionaryEntry.getLearned(this);
        assertLearnedWords(learned, getResources().getInteger(R.integer.game_quiz_min_learned_words), 0);

        if (mVm.getEntryValue() == null) create();
        display();
    }

    @Override
    protected void restart() {
        create();
        display();
    }

    @Override
    protected void lock() {}

    @Override
    protected void unlock() {}
}
