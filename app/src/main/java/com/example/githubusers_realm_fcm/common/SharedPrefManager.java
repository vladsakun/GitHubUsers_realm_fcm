package com.example.githubusers_realm_fcm.common;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    public static final String SHARED_PREF_NAME = "fcmsharedprefdemo";
    public static final String KEY_ACCESS_TOKEN = "token";

    private static Context sContext;
    private static SharedPrefManager mInstance;

    private SharedPrefManager(Context context){
        sContext = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context){
        if(mInstance == null)
            mInstance = new SharedPrefManager(context);
        return mInstance;
    }

    public boolean storeToken(String token){
        SharedPreferences sharedPreferences = sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken(){
        SharedPreferences sharedPreferences = sContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }
}
