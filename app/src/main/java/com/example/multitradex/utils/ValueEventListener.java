package com.example.multitradex.utils;

public interface ValueEventListener<T> {
    void onSuccess(T value);
    void onError(String error);
}
