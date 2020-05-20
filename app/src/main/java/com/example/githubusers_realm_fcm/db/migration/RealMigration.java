package com.example.githubusers_realm_fcm.db.migration;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if(oldVersion == 0){
            schema.create("TaskRealmModel")
                    .addField("title", String.class);
            oldVersion++;
        }
    }
}
