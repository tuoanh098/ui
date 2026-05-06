package com.trohub.ui.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NetworkClient {
    // Android emulator maps host machine localhost to 10.0.2.2.
    private static final String BASE_URL = "http://10.0.2.2:8081/";
    // private static final String BASE_URL = "https://backend-ufb3.onrender.com/";
    private static Retrofit retrofit;
    private static String authToken = null;

    public static synchronized void setAuthToken(String token) {
        authToken = token;
        // Rebuild client when token changes to avoid stale interceptor/auth state.
        retrofit = null;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static synchronized Retrofit getRetrofitClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder();
                            if (authToken != null) {
                                requestBuilder.header("Authorization", "Bearer " + authToken);
                            }
                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

