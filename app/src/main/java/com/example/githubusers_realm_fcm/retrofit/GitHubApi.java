package com.example.githubusers_realm_fcm.retrofit;

import com.example.githubusers_realm_fcm.db.models.UserInfo;
import com.example.githubusers_realm_fcm.db.models.UserRepository;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GitHubApi {

    @GET("users")
    Observable<List<UserInfo>> getAllUsers();

    @GET("users/{login}/repos")
    Observable<List<UserRepository>> getRepositories(@Path("login") String login);
}
