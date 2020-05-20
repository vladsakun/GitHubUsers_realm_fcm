package com.example.githubusers_realm_fcm.retrofit;

import com.example.githubusers_realm_fcm.common.Common;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    public static final String BASE_URL = "https://api.github.com/";

    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BasicAuthInterceptor(Common.CLIENT_ID, Common.CLIENT_SECRET))
            .build();

    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    private static GitHubApi api = retrofit.create(GitHubApi.class);

    public static GitHubApi getRequestApi() {
        return api;
    }
}
