package com.example.githubusers_realm_fcm.db;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class DBService {

    private static final String TAG = "DBService";

    private Realm realm = Realm.getDefaultInstance();

    //Get all statements from local db
    public <T extends RealmObject> RealmResults<T> getAll(Class<T> tClass) {
        return realm.where(tClass).findAllAsync();
    }

    //Get statement by id from local db
    public <T extends RealmObject> T getById(Integer id, Class<T> tClass) {
        return realm.where(tClass).equalTo("id", id).findFirst();
    }
}
