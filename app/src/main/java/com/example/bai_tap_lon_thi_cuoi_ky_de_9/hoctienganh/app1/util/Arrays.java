package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.arch.core.util.Function;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception.NotEnoughItemsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Thêm các hàm thuật toán cho lớp <code>Arrays</code> (như lập dãy số liên tiếp, đổi vị trí, hoán ngẫu nhiên)
 */
public final class Arrays {
    private Arrays() {}

    private static final Random sRandom = new Random();

    /**
     * Lập dãy số từ 0 tới <code>max</code> - 1
     */
    @NonNull
    public static int[] range(int max) {
        int[] array = new int[max];
        for (int i = 0; i < max; i++) array[i] = i;
        return array;
    }

    /**
     * Lập dãy số từ <code>min</code> tới <code>max</code> - 1
     */
    @NonNull
    public static int[] range(int min, int max) {
        int[] array = new int[max - min];
        for (int i = min; i < max; i++) array[i] = i;
        return array;
    }

    // --- Đổi chỗ ---
    /**
     * Đổi chỗ 2 phần tử trong mảng
     */
    @NonNull
    public static int[] swap(@NonNull @Size(min=1) int[] array, int i, int j) {
        if (array.length < 1) throw new NotEnoughItemsException(i, 1);
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        return array;
    }

    /**
     * Đổi chỗ 2 phần tử trong mảng
     */
    @NonNull
    public static char[] swap(@NonNull @Size(min=1) char[] array, int i, int j) {
        if (array.length < 1) throw new NotEnoughItemsException(i, 1);
        char temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        return array;
    }

    /**
     * Đổi chỗ 2 phần tử trong mảng
     */
    @NonNull
    public static <T> T[] swap(@NonNull @Size(min=1) T[] array, int i, int j) {
        if (array.length < 1) throw new NotEnoughItemsException(i, 1);
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        return array;
    }

    /**
     * Đổi ngẫu nhiên các phần tử trong mảng
     */
    @NonNull
    public static int[] shuffle(@NonNull int[] array) {
        for (int i = 0; i < array.length - 1; i++)
            swap(array, i, i + sRandom.nextInt(array.length - i));
        return array;
    }

    /**
     * Đổi ngẫu nhiên các phần tử trong mảng
     */
    @NonNull
    public static char[] shuffle(@NonNull char[] array) {
        // Fisher-Yates shuffle
        for (int i = 0; i < array.length - 1; i++)
            swap(array, i, i + sRandom.nextInt(array.length - i));
        return array;
    }

    /**
     * Đổi ngẫu nhiên các phần tử trong mảng
     */
    @NonNull
    public static <T> T[] shuffle(@NonNull T[] array) {
        // Fisher-Yates shuffle
        for (int i = 0; i < array.length - 1; i++)
            swap(array, i, i + sRandom.nextInt(array.length - i));
        return array;
    }

    // --- Chọn ngẫu nhiên ---
    /**
     * Chọn một phần ngẫu nhiên từ một mảng
     */
    public static int pickRandom(@NonNull @Size(min=1) int[] array) {
        if (array.length == 0) throw new NotEnoughItemsException(array.length, 1);
        return array[sRandom.nextInt(array.length)];
    }

    /**
     * Chọn một phần ngẫu nhiên từ một mảng
     */
    public static char pickRandom(@NonNull @Size(min=1) char[] array) {
        if (array.length == 0) throw new NotEnoughItemsException(array.length, 1);
        return array[sRandom.nextInt(array.length)];
    }

    /**
     * Chọn một phần ngẫu nhiên từ một mảng
     */
    @Nullable
    public static <T> T pickRandom(@NonNull @Size(min=1) T[] array) {
        if (array.length == 0) throw new NotEnoughItemsException(array.length, 1);
        return array[sRandom.nextInt(array.length)];
    }

    /**
     * Chọn một phần ngẫu nhiên từ một mảng
     */
    @Nullable
    public static <T> T pickRandom(@NonNull @Size(min=1) List<T> list) {
        if (list.size() == 0) throw new NotEnoughItemsException(list.size(), 1);
        return list.get(sRandom.nextInt(list.size()));
    }

    /**
     * Chọn <code>n</code> phần ngẫu nhiên từ một mảng
     */
    @NonNull
    public static int[] pickRandom(@NonNull int[] array, int n) {
        if (array.length < n) throw new NotEnoughItemsException(array.length, n);
        int[] array2 = shuffle(java.util.Arrays.copyOf(array, array.length));
        return java.util.Arrays.copyOfRange(array2, 0, n);
    }

    /**
     * Chọn <code>n</code> phần ngẫu nhiên từ một mảng
     */
    @NonNull
    public static char[] pickRandom(@NonNull char[] array, int n) {
        if (array.length < n) throw new NotEnoughItemsException(array.length, n);
        char[] array2 = shuffle(java.util.Arrays.copyOf(array, array.length));
        return java.util.Arrays.copyOfRange(array2, 0, n);
    }

    /**
     * Chọn <code>n</code> phần ngẫu nhiên từ một mảng
     */
    @NonNull
    public static <T> T[] pickRandom(@NonNull T[] array, int count) {
        if (array.length < count) throw new NotEnoughItemsException(array.length, count);
        T[] array2 = shuffle(java.util.Arrays.copyOf(array, array.length));
        return java.util.Arrays.copyOfRange(array2, 0, count);
    }

    /**
     * Chọn <code>n</code> phần ngẫu nhiên từ một mảng
     */
    @NonNull
    public static <T> List<T> pickRandom(@NonNull List<T> list, int count) {
        if (list.size() < count) throw new NotEnoughItemsException(list.size(), count);
        List<T> list2 = new ArrayList<>(list);
        Collections.shuffle(list2);
        return list2.subList(0, count);
    }

    // --- Chuyển kiểu mảng ---
    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static int[] toIntArray(@NonNull List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static <T> int[] toIntArray(@NonNull List<T> list, Function<T, Integer> mapFn) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = mapFn.apply(list.get(i));
        return array;
    }

    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static <T> int[] toIntArray(@NonNull T[] array, Function<T, Integer> mapFn) {
        int[] array2 = new int[array.length];
        for (int i = 0; i < array2.length; i++) array2[i] = mapFn.apply(array[i]);
        return array2;
    }

    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static char[] toCharArray(@NonNull List<Character> list) {
        char[] array = new char[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static <T> char[] toCharArray(@NonNull List<T> list, Function<T, Character> mapFn) {
        char[] array = new char[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = mapFn.apply(list.get(i));
        return array;
    }

    /**
     * Chuyển mảng động sang mảng tĩnh
     */
    @NonNull
    public static <T> char[] toCharArray(@NonNull T[] array, Function<T, Character> mapFn) {
        char[] array2 = new char[array.length];
        for (int i = 0; i < array2.length; i++) array2[i] = mapFn.apply(array[i]);
        return array2;
    }

    /**
     * Chuyển mảng tĩnh (kiểu <code>T[]</code>) sang mảng tĩnh kiểu chuỗi
     */
    @NonNull
    public static <T> String[] toStringArray(@NonNull T[] array, Function<T, String> mapFn) {
        String[] newArray = new String[array.length];
        for (int i = 0; i < array.length; i++) newArray[i] = mapFn.apply(array[i]);
        return newArray;
    }

    // --- Tìm vị trí ---
    /**
     * Tìm vị trí đầu của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static int indexOf(@NonNull int[] array, int value) {
        for (int i = 0; i < array.length; i++)
            if (array[i] == value) return i;
        return -1;
    }

    /**
     * Tìm vị trí đầu của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static int indexOf(@NonNull char[] array, char value) {
        for (int i = 0; i < array.length; i++)
            if (array[i] == value) return i;
        return -1;
    }

    /**
     * Tìm vị trí đầu của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static <T> int indexOf(@NonNull T[] array, T value) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(value)) return i;
        return -1;
    }

    /**
     * Tìm vị trí cuối của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static int lastIndexOf(@NonNull int[] array, int value) {
        for (int i = array.length - 1; i >= 0; i--)
            if (array[i] == value) return i;
        return -1;
    }

    /**
     * Tìm vị trí cuối của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static int lastIndexOf(@NonNull char[] array, char value) {
        for (int i = array.length - 1; i >= 0; i--)
            if (array[i] == value) return i;
        return -1;
    }

    /**
     * Tìm vị trí cuối của của phần tử có giá trị <code>value</code> trong mảng
     */
    public static <T> int lastIndexOf(@NonNull T[] array, T value) {
        for (int i = array.length - 1; i >= 0; i--)
            if (array[i].equals(value)) return i;
        return -1;
    }

    // --- Chứa ---
    /**
     * Mảng có chứa phần tử <code>value</code> không?
     */
    public static boolean contains(@NonNull int[] array, int value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Mảng có chứa phần tử <code>value</code> không?
     */
    public static boolean contains(@NonNull char[] array, char value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Mảng có chứa phần tử <code>value</code> không?
     */
    public static <T> boolean contains(@NonNull T[] array, T value) {
        return indexOf(array, value) != -1;
    }
}