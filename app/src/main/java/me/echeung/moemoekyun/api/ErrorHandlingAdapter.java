package me.echeung.moemoekyun.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import me.echeung.moemoekyun.api.responses.BaseResponse;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// Based on https://github.com/square/retrofit/blob/e6a7cd01657670807bed24f6f4ed56eb59c9c9ab/samples/src/main/java/com/example/retrofit/ErrorHandlingAdapter.java
public class ErrorHandlingAdapter {

    public interface WrappedCall<T extends BaseResponse> {
        void cancel();
        void enqueue(WrappedCallback<T> callback);
        WrappedCall<T> clone();
    }

    interface WrappedCallback<T extends BaseResponse> {
        void success(Response<T> response);
        void error(String message);
    }

    public static class ErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
        @Override
        public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
                                               Retrofit retrofit) {
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
            public WrappedCall<R> adapt(Call<R> call) {
                return new WrappedCallAdapter<>(call);
            }
        }
    }

    /** Adapts a {@link Call} to {@link WrappedCall}. */
    static class WrappedCallAdapter<T extends BaseResponse> implements WrappedCall<T> {
        private final Call<T> call;

        WrappedCallAdapter(Call<T> call) {
            this.call = call;
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public void enqueue(final WrappedCallback<T> callback) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.error("Unexpected response: " + response);
                    }

                    if (!response.body().isSuccess()) {
                        callback.error(response.body().getMessage());
                    }

                    int code = response.code();
                    if (code >= 200 && code < 300) {
                        callback.success(response);
                    } else {
                        callback.error("Response returned with non-200 code: " + response);
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    callback.error(t.getMessage());
                }
            });
        }

        @Override
        public WrappedCall<T> clone() {
            return new WrappedCallAdapter<>(call.clone());
        }
    }
}
