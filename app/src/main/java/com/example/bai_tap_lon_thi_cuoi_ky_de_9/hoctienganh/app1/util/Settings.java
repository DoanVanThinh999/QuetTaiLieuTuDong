package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Settings {
    public static final String SETTING_IN_GAME_SOUND = "sound",
            SETTING_FAVORITES = "favorites",
            SETTING_LIST_VIEW_TYPE = "list_view_type",
            SETTING_VOCAB_NOTIFICATION = "vocab_notification",
            SETTING_MAX_HEARTS = "max_hearts",
            SETTING_MAX_PROGRESS = "max_progress",
            SETTING_LEARNED_WORDS = "learned_words";
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEd;

    private Settings(Context context) {
        mSp = context.getSharedPreferences("dictionary", Context.MODE_PRIVATE);
        mEd = mSp.edit();
    }

    public static Settings from(Context context) {
        return new Settings(context);
    }

    public boolean getInGameSound() {
        return mSp.getBoolean(SETTING_IN_GAME_SOUND, true);
    }

    public void setInGameSound(boolean inGameSound) {
        mEd.putBoolean(SETTING_IN_GAME_SOUND, inGameSound).commit();
    }

    public int getListViewType() {
        return mSp.getInt(SETTING_LIST_VIEW_TYPE, DictionaryEntry.VIEW_TYPE_FLASHCARD);
    }

    public void setListViewType(int listViewType) {
        mEd.putInt(SETTING_LIST_VIEW_TYPE, listViewType).commit();
    }

    public int getMaxHearts() {
        return mSp.getInt(SETTING_MAX_HEARTS, 5);
    }

    public void setMaxHearts(int maxHearts) {
        mEd.putInt(SETTING_MAX_HEARTS, maxHearts).commit();
    }

    public int getMaxProgress() {
        return mSp.getInt(SETTING_MAX_PROGRESS, 10);
    }

    public void setMaxProgress(int maxProgress) {
        mEd.putInt(SETTING_MAX_PROGRESS, maxProgress).commit();
    }

    public boolean getVocabNotification() {
        return mSp.getBoolean(SETTING_VOCAB_NOTIFICATION, true);
    }

    public void setVocabNotification(boolean vocabNotification) {
        mEd.putBoolean(SETTING_VOCAB_NOTIFICATION, vocabNotification).commit();
    }

    public List<String> getLearnedWords() {
        return new ArrayList<>(mSp.getStringSet(SETTING_LEARNED_WORDS, new HashSet<>()));
    }

    public void setLearnedWords(List<String> learnedWords) {
        mEd.putStringSet(SETTING_LEARNED_WORDS, new HashSet<>(learnedWords)).commit();
    }

    public void addLearnedWord(String learnedWord) {
        List<String> learnedWords = getLearnedWords();
        learnedWords.add(learnedWord);
        mEd.putStringSet(SETTING_LEARNED_WORDS, new HashSet<>(learnedWords)).commit();
    }

    public void removeLearnedWord(String learnedWord) {
        List<String> learnedWords = getLearnedWords();
        learnedWords.remove(learnedWord);
        mEd.putStringSet(SETTING_LEARNED_WORDS, new HashSet<>(learnedWords)).commit();
    }

    public List<String> getFavorites() {
        return new ArrayList<>(mSp.getStringSet(SETTING_FAVORITES, new HashSet<>()));
    }

    public void setFavorites(List<String> favorites) {
        mEd.putStringSet(SETTING_FAVORITES, new HashSet<>(favorites)).commit();
    }

    public void addFavorite(String favorite) {
        List<String> favorites = getFavorites();
        favorites.add(favorite);
        mEd.putStringSet(SETTING_FAVORITES, new HashSet<>(favorites)).commit();
    }

    public void removeFavorite(String favorite) {
        List<String> favorites = getFavorites();
        favorites.remove(favorite);
        mEd.putStringSet(SETTING_FAVORITES, new HashSet<>(favorites)).commit();
    }

    public void set(String setting, int value) {
        mEd.putInt(setting, value).commit();
    }

    public void get(String setting, int defaultValue) {
        mSp.getInt(setting, defaultValue);
    }

    public void clear() {mEd.clear().commit();}
}
