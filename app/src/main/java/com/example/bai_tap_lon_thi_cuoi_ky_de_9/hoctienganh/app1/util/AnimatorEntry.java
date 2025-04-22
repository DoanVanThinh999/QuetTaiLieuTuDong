package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;

import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

/**
 * Tạo animator và chỉnh target
 */
public class AnimatorEntry extends Pair<Integer, View> {
    public AnimatorEntry(@AnimatorRes int animatorRes, @NonNull View target) {
        super(animatorRes, target);
    }

    public int getAnimatorRes() {
        return first;
    }

    public View getTarget() {
        return second;
    }

    public AnimatorSet toSet(Context context) {
        AnimatorSet animSet = (AnimatorSet)AnimatorInflater.loadAnimator(context, first);
        animSet.setTarget(second);
        return animSet;
    }
}
