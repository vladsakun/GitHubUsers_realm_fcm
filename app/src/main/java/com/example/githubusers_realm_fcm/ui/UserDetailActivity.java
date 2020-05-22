package com.example.githubusers_realm_fcm.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.githubusers_realm_fcm.R;
import com.example.githubusers_realm_fcm.adapter.RepositoriesAdapter;
import com.example.githubusers_realm_fcm.common.Common;
import com.example.githubusers_realm_fcm.db.DBService;
import com.example.githubusers_realm_fcm.db.models.User;

import java.util.Objects;

public class UserDetailActivity extends AppCompatActivity {

    //Views
    RecyclerView repositoriesRecyclerview;
    TextView noRepositories;

    DBService mDBService = new DBService();
    RepositoriesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        repositoriesRecyclerview = findViewById(R.id.user_repositories_recyclerview);
        noRepositories = findViewById(R.id.no_repositories);

        //Get user id from intent
        Integer userId = (Integer) getIntent().getSerializableExtra(Common.USER_ID_MESSAGE);

        //Get user from local db
        User user = mDBService.getById(userId, User.class);

        changeSupportActionBarTitle(user.getUserInfo().getLogin());

        initRecyclerView(user);

    }

    private void changeSupportActionBarTitle(String userLogin) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.repositories));
        getSupportActionBar().setSubtitle(userLogin);
    }

    private void initRecyclerView(User user) {

        //If user does not have repositories show message
        if (user.getUserInfo().getRepositories().isEmpty()) {
            repositoriesRecyclerview.setVisibility(View.GONE);
            noRepositories.setText(getString(R.string.user_does_not_have_repositories, user.getUserInfo().getLogin()));
            noRepositories.setVisibility(View.VISIBLE);
        }
        //Show all repositories of specific user from local db
        else {
            mAdapter = new RepositoriesAdapter(user.getUserInfo().getRepositories());
            repositoriesRecyclerview.setLayoutManager(new LinearLayoutManager(this));
            repositoriesRecyclerview.setHasFixedSize(true);
            repositoriesRecyclerview.setAdapter(mAdapter);
        }
    }

}
