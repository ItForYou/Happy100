package kr.co.itforone.happy100;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private int notiId = 0;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        sendNotification(remoteMessage);

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]



    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private void sendNotification(RemoteMessage remote) {
        String messageBody=remote.getData().get("message");
        String subject=remote.getData().get("subject");
        String goUrl=remote.getData().get("goUrl");
        String gubun=remote.getData().get("gubun");
        String viewUrl=remote.getData().get("viewUrl");
        String od_step=remote.getData().get("od_step");
        //String channelId = (od_step.equals("1"))? "od_channel_01" : "od_channel";
        String channelId = (od_step.equals("0"))? "od_channel_01_210427" : "od_channel"; //210427변경
        int notiId = createID();

        Log.d("로그:step", od_step +"//"+ channelId + "//"+ od_step.equals("0"));
        //Log.d("로그:remote","Message:"+remote.getFrom());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("goUrl",goUrl);

        //Log.d("로그:remote",remote.toString());
        Log.d("로그:goUrl", goUrl);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notiId /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap BigPictureStyle= BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        long vibrate[]={500,0,500,0};

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(subject)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setVibrate(vibrate)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 오레오 이상 (안드로이드8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);

            if (od_step.equals("0")) {
                // 푸시알림음
                //Uri uri = Uri.parse("android.resource://kr.co.itforone.happy100/" + R.raw.step01_sound);
                Uri uri = Uri.parse("android.resource://kr.co.itforone.happy100/" + R.raw.step01_sound2);
                channel.setDescription("order_ready_210427");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setSound(uri, att);

//                Log.d("로그:푸시알림음세팅", "1");
            }

            notificationManager.createNotificationChannel(channel);

        }
        //notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        notificationManager.notify(notiId, notificationBuilder.build());
    }

    private int createID() {
        int id = 0;
        Random rd = new Random();
        id = rd.nextInt(99999);

        //Log.d("createID()", String.valueOf(id));
        return id;
    }
}