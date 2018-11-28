package me.echeung.moemoekyun.client.api.v4

import android.util.Log
import me.echeung.moemoekyun.client.api.callback.BaseCallback
import me.echeung.moemoekyun.client.api.v4.response.BaseResponse
import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// Based on:
// https://github.com/square/retrofit/blob/e6a7cd01657670807bed24f6f4ed56eb59c9c9ab/samples/src/main/java/com/example/retrofit/ErrorHandlingAdapter.java
object ErrorHandlingAdapter {

    private val TAG = ErrorHandlingAdapter::class.java.simpleName

    interface WrappedCall<T : BaseResponse> {
        fun enqueue(callback: WrappedCallback<T>)

        fun cancel()

        fun clone(): WrappedCall<T>
    }

    abstract class WrappedCallback<T : BaseResponse>(private val callback: BaseCallback) {
        abstract fun success(response: T?)

        fun error(message: String?) {
            callback.onFailure(message)
        }
    }

    class ErrorHandlingCallAdapterFactory : CallAdapter.Factory() {
        override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
            if (CallAdapter.Factory.getRawType(returnType) != WrappedCall::class.java ) {
                return null
            }
            if (returnType !is ParameterizedType) {
                throw IllegalStateException(
                        "WrappedCall must have generic type (e.g., WrappedCall<ResponseBody>)")
            }
            val responseType = CallAdapter.Factory.getParameterUpperBound(0, returnType)
            return ErrorHandlingCallAdapter<BaseResponse>(responseType)
        }

        private class ErrorHandlingCallAdapter<R : BaseResponse> internal constructor(private val responseType: Type) : CallAdapter<R, WrappedCall<R>> {
            override fun responseType(): Type {
                return responseType
            }

            override fun adapt(call: Call<R>): WrappedCall<R> {
                return WrappedCallAdapter(call)
            }
        }
    }

    /**
     * Adapts a [Call] to [WrappedCall].
     */
    internal class WrappedCallAdapter<T : BaseResponse>(private val call: Call<T>) : WrappedCall<T> {
        override fun enqueue(callback: WrappedCallback<T>) {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        callback.success(response.body())
                    } else {
                        if (response.errorBody() == null) {
                            error(callback, "Unsuccessful response: $response")
                            return
                        }

                        // Parse response body for errors
                        val errorConverter = APIClient.retrofit.responseBodyConverter<BaseResponse>(BaseResponse::class.java, arrayOfNulls(0))
                        try {
                            val error = errorConverter.convert(response.errorBody()!!)
                            error(callback, error?.message)
                            return
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        error(callback, "Error: $response")
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    error(callback, t.message!!)
                }
            })
        }

        override fun cancel() {
            call.cancel()
        }

        override fun clone(): WrappedCall<T> {
            return WrappedCallAdapter(call.clone())
        }

        private fun error(callback: WrappedCallback<T>, message: String?) {
            Log.e(TAG, "API error: $message")
            callback.error(message)
        }
    }

}
