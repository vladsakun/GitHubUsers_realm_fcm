package com.example.githubusers_realm_fcm.db.models;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements Serializable {

    @PrimaryKey
    private Integer id;

    private UserInfo mUserInfo;
    private Integer changesCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public Integer getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(Integer changesCount) {
        this.changesCount = changesCount;
    }

    public User(Integer id, UserInfo userInfo) {
        this.id = id;
        mUserInfo = userInfo;
    }

    public User() {
    }
}
