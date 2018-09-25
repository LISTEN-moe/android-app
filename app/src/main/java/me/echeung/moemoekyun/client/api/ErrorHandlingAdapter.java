package me.echeung.moemoekyun.client.api;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import lombok.RequiredArgsConstructor;
import me.echeung.moemoekyun.client.api.callback.BaseCallback;
import me.echeung.moemoekyun.client.api.response.BaseResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

// Based on https://github.com/square/retrofit/blob/e6a7cd01657670807bed24f6f4ed56eb59c9c9ab/samples/src/main/java/com/example/retrofit/ErrorHandlingAdapter.java
public class ErrorHandlingAdapter {

    private static final String TAG = ErrorHandlingAdapter.class.getSimpleName();

    public interface WrappedCall<T extends BaseResponse> {
        void enqueue(WrappedCallback<T> callback);

        void cancel();

        WrappedCall<T> clone();
    }

    @RequiredArgsConstructor
    public static abstract class WrappedCallback<T extends BaseResponse> {
        private final BaseCallback callback;

        public abstract void success(T response);

        public void error(String message) {
            callback.onFailure(message);
        }
    }

    public static class ErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
        @Override
        public CallAdapter<?, ?> get(@NonNull Type returnType,
                                     @NonNull Annotation[] annotations,
                                     @NonNull Retrofit retrofit) {
            if (getRawType(returnType) != WrappedCall.class) {
                return null;
            }
            if (!(returnType instanceof ParameterizedType)) {
                throw new IllegalStateException(
                        "WrappedCall must have generic type (e.g., WrappedCall<ResponseBody>)");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
            return new ErrorHandlingCallAdapter<>(responseType);
        }

        private static final class ErrorHandlingCallAdapter<R extends BaseResponse> implements CallAdapter<R, WrappedCall<R>> {
            private final Type responseType;

            ErrorHandlingCallAdapter(Type responseType) {
                this.responseType = responseType;
            }

            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public WrappedCall<R> adapt(@NonNull Call<R> call) {
                return new WrappedCallAdapter<>(call);
            }
        }
    }

    /**
     * Adapts a {@link Call} to {@link WrappedCall}.
     */
    static class WrappedCallAdapter<T extends BaseResponse> implements WrappedCall<T> {
        private final Call<T> call;

        WrappedCallAdapter(Call<T> call) {
            this.call = call;
        }

        @Override
        public void enqueue(WrappedCallback<T> callback) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                    if (response.isSuccessful()) {
                        callback.success(response.body());
                    } else {
                        if (response.errorBody() == null) {
                            error(callback, "Unsuccessful response: " + response);
                            return;
                        }

                        // Parse response body for errors
                        final Converter<ResponseBody, BaseResponse> errorConverter =
                                APIClient.getRetrofit().responseBodyConverter(BaseResponse.class, new Annotation[0]);
                        try {
                            final BaseResponse error = errorConverter.convert(response.errorBody());
                            error(callback, error.getMessage());
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        error(callback, "Error: " + response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                    error(callback, t.getMessage());
                }
            });
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public WrappedCall<T> clone() {
            return new WrappedCallAdapter<>(call.clone());
        }

        private void error(WrappedCallback<T> callback, final String message) {
            Log.e(TAG, "API error: " + message);
            callback.error(message);
        }
    }

}
