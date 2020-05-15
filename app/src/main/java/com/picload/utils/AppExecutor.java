package com.picload.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    }

    public static void submitTask(Runnable runnable) {
        executorService.submit(runnable);
    }
}
