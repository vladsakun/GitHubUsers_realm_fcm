package com.example.githubusers_realm_fcm.db;

import android.util.Log;

import com.example.githubusers_realm_fcm.db.models.User;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class DBService {

    private static final String TAG = "DBService";

    private Realm realm = Realm.getDefaultInstance();

    public <T extends RealmObject> RealmResults<T> getAll(Class<T> tClass) {
        return realm.where(tClass).findAllAsync();
    }

    public <T extends RealmObject> T getById(Integer id, Class<T> tClass) {
        return realm.where(tClass).equalTo("id", id).findFirst();
    }
}
