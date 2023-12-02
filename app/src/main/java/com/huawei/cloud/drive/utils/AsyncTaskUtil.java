package com.huawei.cloud.drive.utils;

import android.os.AsyncTask;

public class AsyncTaskUtil {

    public interface AsyncFunctionCallback<T> {
        void onFunctionCompleted(T result);
    }

    public static class AsyncFunctions<T> extends AsyncTask<Void, Void, T> {
        private final AsyncFunctionCallback<T> callback;
        private final Function<T> function;

        public AsyncFunctions(Function<T> function, AsyncFunctionCallback<T> callback) {
            this.function = function;
            this.callback = callback;
        }

        @Override
        protected T doInBackground(Void... voids) {
            return function.execute();
        }

        @Override
        protected void onPostExecute(T result) {
            if (callback != null) {
                callback.onFunctionCompleted(result);
            }
        }
    }

    public interface Function<T> {
        T execute();
    }

    public static <T> void executeAsync(Function<T> function, AsyncFunctionCallback<T> callback) {
        AsyncFunctions<T> asyncFunctions = new AsyncFunctions<>(function, callback);
        asyncFunctions.execute();
    }
}
