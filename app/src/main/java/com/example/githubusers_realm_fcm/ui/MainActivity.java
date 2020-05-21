package com.example.githubusers_realm_fcm.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.githubusers_realm_fcm.R;
import com.example.githubusers_realm_fcm.adapter.UsersAdapter;
import com.example.githubusers_realm_fcm.common.SharedPrefManager;
import com.example.githubusers_realm_fcm.db.DBService;
import com.example.githubusers_realm_fcm.db.models.User;
import com.example.githubusers_realm_fcm.db.models.UserInfo;
import com.example.githubusers_realm_fcm.db.models.UserRepository;
import com.example.githubusers_realm_fcm.retrofit.GitHubApi;
import com.example.githubusers_realm_fcm.retrofit.ServiceGenerator;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.HttpException;

import static com.example.githubusers_realm_fcm.common.Common.ACTION_NAME;
import static com.example.githubusers_realm_fcm.common.Common.CHANGES_COUNT_MESSAGE;
import static com.example.githubusers_realm_fcm.common.Common.USER_ID_MESSAGE;

public class MainActivity extends AppCompatActivity {

    GitHubApi api = ServiceGenerator.getRequestApi();
    RecyclerView usersRecyclerView;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    DBService dbService = new DBService();
    UsersAdapter adapter;
    ProgressBar mProgressBar;
    MyBroadcastReceiver receiver;
    Realm realm;
    List<User> mUserList = new ArrayList<>();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersRecyclerView = findViewById(R.id.users_recyclerview);
        mProgressBar = findViewById(R.id.progress_circular);

        receiver = new MyBroadcastReceiver();

        realm = Realm.getDefaultInstance();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d(TAG, "onComplete: " + token);
                    }
                });

        initRecyclerView();

        displayLocalData();

        fetchData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayLocalData();
    }

    private void fetchData() {
        List<UserInfo> fetchedUsers = new ArrayList<>();
        getUsersObservable()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<User, ObservableSource<UserInfo>>() {
                    @Override
                    public ObservableSource<UserInfo> apply(User user) throws Exception {
                        return getRepositoriesObservable(user.getUserInfo());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        fetchedUsers.add(userInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        displayLocalData();
                    }

                    @Override
                    public void onComplete() {
                        persistFetchedData(userResponseToUser(fetchedUsers));
                        displayLocalData();
                    }
                });
    }

    private ObservableSource<UserInfo> getRepositoriesObservable(final UserInfo userInfo) {
        return api.getRepositories(userInfo.getLogin())
                .map(new Function<List<UserRepository>, UserInfo>() {
                    @Override
                    public UserInfo apply(List<UserRepository> userRepositories) throws Exception {
                        RealmList<UserRepository> repositories = new RealmList<>();
                        repositories.addAll(userRepositories);
                        userInfo.setRepositories(repositories);
                        return userInfo;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private Observable<User> getUsersObservable() {

        return api.getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<UserInfo>, ObservableSource<User>>() {
                    @Override
                    public ObservableSource<User> apply(List<UserInfo> userInfos) throws Exception {
                        return Observable.fromIterable(userResponseToUser(userInfos))
                                .subscribeOn(Schedulers.io());
                    }
                });

    }

    private ArrayList<User> userResponseToUser(List<UserInfo> userInfos) {
        ArrayList<User> users = new ArrayList<>();
        for (UserInfo user : userInfos) {

            users.add(new User(user.getId(), user));

        }
        return users;
    }

    private void persistFetchedData(List<User> users) {
        realm.beginTransaction();

        if (!realm.isEmpty()) {

            for (User user : users) {

                User currentUser = realm.where(User.class).equalTo("id", user.getId()).findFirst();

                assert currentUser != null;
                user.setChangesCount(currentUser.getChangesCount());

            }

        } else {

            for (User user : users) {

                user.setChangesCount(0);

            }

        }

        realm.copyToRealmOrUpdate(users);

        realm.commitTransaction();
        realm.close();
    }

    private void initRecyclerView() {

        adapter = new UsersAdapter(this, mUserList);
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);

    }

    private void displayLocalData() {
        List<User> users = Realm.getDefaultInstance().copyFromRealm(dbService.getAll(User.class));

        if (!users.isEmpty()) {
            mUserList.clear();
            mUserList.addAll(users);
            adapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NAME);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.clear();
        unregisterReceiver(receiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            assert extras != null;

            Integer userId = extras.getInt(USER_ID_MESSAGE);
            Integer changesCount = extras.getInt(CHANGES_COUNT_MESSAGE);

            displayLocalData();
        }
    }

}

