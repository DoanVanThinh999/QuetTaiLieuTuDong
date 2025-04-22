package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.FragmentGamesBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.missingletters.GameMissingLetters;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.quiz.GameQuiz;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.games.unscramble.GameUnscramble;

public class GamesFragment extends Fragment {
    public static final int GAME_MISSING_LETTERS = 0, GAME_UNSCRAMBLE = 1, GAME_QUIZ = 2;
    private FragmentGamesBinding mB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mB = FragmentGamesBinding.inflate(inflater, container, false);
        mB.setFragment(this);
        return mB.getRoot();
    }

    public void startGame(int activityId) {
        Class<?>[] activities = {GameMissingLetters.class, GameUnscramble.class, GameQuiz.class};
        Intent intent = new Intent(getContext(), activities[activityId]);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mB = null;
    }
}