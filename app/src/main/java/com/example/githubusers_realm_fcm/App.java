package com.example.githubusers_realm_fcm;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    private void initRealm(){
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(configuration);
    }

    @Override
    public void onCreate() {
        initRealm();
        super.onCreate();
    }
}
