package org.mf.langchain.util;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class TemplatedThread<T> {

    private static class ThreadNotStarted extends RuntimeException {
         ThreadNotStarted() {
            super("This thread was not started, so it cant be awaited");
        }
    }

    private T result = null;
    private final Thread thread;
    private Function<T, Void> callback;

    private boolean isStarted = false;

    public TemplatedThread(Callable<T> task) {
        this.thread = new Thread(() -> {
            try {
                result = task.call();
                if(callback != null)
                    callback.apply(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void runAsync() {
        isStarted = true;
        result = null;
        thread.start();
    }

    public T await() {
        if(!isStarted) throw new ThreadNotStarted();

        if(result != null)  return result;
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public void then(Function<T, Void> callback) {
        this.callback = callback;
    }

    public T runBlocking() {
        isStarted = true;
        result = null;
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
