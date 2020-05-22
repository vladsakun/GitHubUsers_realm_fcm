package com.example.githubusers_realm_fcm.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import com.example.githubusers_realm_fcm.R;
import com.example.githubusers_realm_fcm.adapter.UsersAdapter;
import com.example.githubusers_realm_fcm.db.DBService;
import com.example.githubusers_realm_fcm.db.models.User;
import com.example.githubusers_realm_fcm.db.models.UserInfo;
import com.example.githubusers_realm_fcm.db.models.UserRepository;
import com.example.githubusers_realm_fcm.retrofit.GitHubApi;
import com.example.githubusers_realm_fcm.retrofit.ServiceGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.example.githubusers_realm_fcm.common.Common.ACTION_NAME;
import static com.example.githubusers_realm_fcm.common.Common.CHANGES_COUNT_MESSAGE;
import static com.example.githubusers_realm_fcm.common.Common.USER_ID_MESSAGE;

public class MainActivity extends AppCompatActivity {

    //Views
    RecyclerView usersRecyclerView;
    ProgressBar mProgressBar;

    UsersAdapter adapter;

    GitHubApi api = ServiceGenerator.getRequestApi();
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    DBService dbService = new DBService();
    Realm realm;
    MyBroadcastReceiver receiver;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersRecyclerView = findViewById(R.id.users_recyclerview);
        mProgressBar = findViewById(R.id.progress_circular);

        changeSupportActionBarTitle();

        //Init Broadcast Receiver
        receiver = new MyBroadcastReceiver();

        realm = Realm.getDefaultInstance();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        //Init FCM and print token
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

    private void changeSupportActionBarTitle() {
        getSupportActionBar().setTitle(getString(R.string.git_hub_users));
    }

    private void fetchData() {
        List<UserInfo> fetchedUsers = new ArrayList<>();

        getUsersObservable()
                .subscribeOn(Schedulers.io())

                //For each user get repositories
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
                        //On error (connectivity)
                        //Show local data
                        Log.e(TAG, "onError: ", e);
                        displayLocalData();
                    }

                    @Override
                    public void onComplete() {

                        //Add data to local db
                        persistFetchedData(userResponseToUser(fetchedUsers));
                        displayLocalData();
                    }
                });
    }

    //Get repositories of user from GitHub Api
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

    //Get users from GitHub api
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

    //Convert list of user info from response to list of user entity for local db
    private ArrayList<User> userResponseToUser(List<UserInfo> userInfos) {
        ArrayList<User> users = new ArrayList<>();
        for (UserInfo user : userInfos) {

            users.add(new User(user.getId(), user));

        }
        return users;
    }

    //Add data from api to local db
    private void persistFetchedData(List<User> users) {
        realm.beginTransaction();

        //Local db has data, so changesCount has been initialized
        if (!realm.isEmpty()) {

            for (User user : users) {

                User currentUser = realm.where(User.class).equalTo("id", user.getId()).findFirst();

                assert currentUser != null;
                user.setChangesCount(currentUser.getChangesCount());

            }

        }
        //First opening of the application
        //Init changes count to default (0)
        else {

            for (User user : users) {

                user.setChangesCount(0);

            }

        }

        realm.copyToRealmOrUpdate(users);

        realm.commitTransaction();
        realm.close();
    }

    private void initRecyclerView() {

        adapter = new UsersAdapter(this, new ArrayList<>());
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);

    }

    private void displayLocalData() {

        //Convert all statements from db to list of user
        List<User> users = Realm.getDefaultInstance().copyFromRealm(dbService.getAll(User.class));

        if (!users.isEmpty()) {
            adapter.setUsers(users);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Init filters for receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NAME);

        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Clear all disposables from rxjava calls
        mCompositeDisposable.clear();

        unregisterReceiver(receiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            assert extras != null;

            //Get extras from onMessageReceived in MyFirebaseMessagingService
            Integer userId = extras.getInt(USER_ID_MESSAGE);
            Integer changesCount = extras.getInt(CHANGES_COUNT_MESSAGE);

            //Update user in local db
            updateUser(userId, changesCount);

            //Update user list on the screen
            displayLocalData();
        }
    }

    public void updateUser(Integer userId, Integer changesCount) {
        Realm realm = Realm.getDefaultInstance();

        User currentUser;
        try {

            //Get user by id
            RealmResults<User> users = realm.where(User.class).equalTo("id", userId).findAll();
            currentUser = users.get(0);

            realm.beginTransaction();

            //Set new changes count
            currentUser.setChangesCount(changesCount);

            realm.commitTransaction();
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "User does not exist");
        }
    }

}

