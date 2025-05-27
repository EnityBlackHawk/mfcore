package org.utfpr.mf.tools;

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
    private Function<RuntimeException, Void> exceptionCallback;

    private boolean isStarted = false;

    public TemplatedThread(Callable<T> task) {
        this.thread = new Thread(() -> {
            try {
                result = task.call();
                if(callback != null)
                    callback.apply(result);
            } catch (Exception e) {

                if(exceptionCallback != null) {
                    exceptionCallback.apply(new RuntimeException(e));
                } else {
                    throw new RuntimeException(e);
                }
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

    public TemplatedThread<T> then(Function<T, Void> callback) {
        this.callback = callback;
        return this;
    }

    public TemplatedThread<T> catching(Function<RuntimeException, Void> exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
        return this;
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
