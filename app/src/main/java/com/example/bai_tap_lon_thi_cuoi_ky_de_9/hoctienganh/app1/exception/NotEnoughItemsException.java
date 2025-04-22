package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception;

public class NotEnoughItemsException extends RuntimeException {
    public NotEnoughItemsException(int current, int min) {
        super("Expected minimum size of " + min + ", got " + current);
    }

    public NotEnoughItemsException(String message) {
        super(message);
    }
}
