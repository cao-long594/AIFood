package com.example.food.core.concurrent;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static final ExecutorService IO = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static void runOnIo(Runnable task) {
        IO.execute(task);
    }

    public static void runOnMain(Runnable task) {
        MAIN.post(task);
    }
}