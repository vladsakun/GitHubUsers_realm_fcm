package com.example.githubusers_realm_fcm.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.githubusers_realm_fcm.R;
import com.example.githubusers_realm_fcm.common.Common;
import com.example.githubusers_realm_fcm.db.DBService;
import com.example.githubusers_realm_fcm.db.models.User;

public class UserDetailActivity extends AppCompatActivity {

    DBService mDBService = new DBService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Integer userId = (Integer) getIntent().getSerializableExtra(Common.USER_ID_MESSAGE);
        User user = mDBService.getById(userId, User.class);
        Toast.makeText(this, String.valueOf(user.getUserInfo().getRepositories().size()), Toast.LENGTH_SHORT).show();
    }
}
