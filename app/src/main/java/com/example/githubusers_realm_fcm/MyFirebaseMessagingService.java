package com.example.githubusers_realm_fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

import static com.example.githubusers_realm_fcm.common.Common.ACTION_NAME;
import static com.example.githubusers_realm_fcm.common.Common.CHANGES_COUNT_MESSAGE;
import static com.example.githubusers_realm_fcm.common.Common.USER_ID_MESSAGE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        // Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Integer userId = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("userId")));
            Integer changesCount = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("changesCount")));

            Intent intent = new Intent();
            intent.putExtra(USER_ID_MESSAGE, userId);
            intent.putExtra(CHANGES_COUNT_MESSAGE, changesCount);
            intent.setAction(ACTION_NAME);

            //Send extras to MainActivity
            sendBroadcast(intent);

            //Send Custom notification
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String title, String content) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "GITHUB";

        //Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "GITHUB Notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("GITHUB Channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);

        //Customize notification
        notifBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(content)
                .setContentInfo("Info");

        //Send custom notification
        notificationManager.notify(new Random().nextInt(), notifBuilder.build());

    }
}
