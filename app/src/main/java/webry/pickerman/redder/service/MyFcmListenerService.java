package webry.pickerman.redder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import webry.pickerman.redder.AppActivity;
import webry.pickerman.redder.ChatFragment;
import webry.pickerman.redder.DialogsActivity;
import webry.pickerman.redder.MainActivity;
import webry.pickerman.redder.NotificationsActivity;
import webry.pickerman.redder.R;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;


public class MyFcmListenerService extends FirebaseMessagingService implements Constants {

    private int flag;

    public MyFcmListenerService () {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            flag = PendingIntent.FLAG_IMMUTABLE;

        } else {

            flag =  PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        String from = message.getFrom();
        Map data = message.getData();

        Log.e("Message", "Could not parse malformed JSON: \"" + data.toString() + "\"");

        generateNotification(getApplicationContext(), data);
    }

    @Override
    public void onNewToken(String token) {

        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        App.getInstance().set_fcm_token(token);
        App.getInstance().update_fcm_token();
    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {

        sendNotification("Upstream message sent. Id=" + msgId);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {

        Log.e("Message", "Could not parse malformed JSON: \"" + msg + "\"");
    }

    /**
     * Create a notification to inform the user that server has sent a message.
     */
    private void generateNotification(Context context, Map data) {

        String CHANNEL_ID = "my_channel_01"; // id for channel.

        CharSequence name = context.getString(R.string.channel_name);     // user visible name of channel.

        NotificationChannel mChannel;

        String msgId = "0";
        String msgFromUserId = "0";
        String msgFromUserState = "0";
        String msgFromUserVerify = "0";
        String msgFromUserUsername = "";
        String msgFromUserFullname = "";
        String msgFromUserPhotoUrl = "";
        String msgMessage = "";
        String msgImgUrl = "";
        String msgCreateAt = "0";
        String msgDate = "";
        String msgTimeAgo = "";
        String msgRemoveAt = "0";

        String message = data.get("msg").toString();
        String type = data.get("type").toString();
        String actionId = data.get("id").toString();
        String accountId = data.get("accountId").toString();

        if (Integer.valueOf(type) == GCM_NOTIFY_MESSAGE) {

            msgId = data.get("msgId").toString();
            msgFromUserId = data.get("msgFromUserId").toString();
            msgFromUserState = data.get("msgFromUserState").toString();
            msgFromUserVerify = data.get("msgFromUserVerify").toString();

            if (data.containsKey("msgFromUserUsername")) {

                msgFromUserUsername = data.get("msgFromUserUsername").toString();
            }

            if (data.containsKey("msgFromUserFullname")) {

                msgFromUserFullname = data.get("msgFromUserFullname").toString();
            }

            if (data.containsKey("msgFromUserPhotoUrl")) {

                msgFromUserPhotoUrl = data.get("msgFromUserPhotoUrl").toString();
            }

            if (data.containsKey("msgMessage")) {

                msgMessage = data.get("msgMessage").toString();
            }

            if (data.containsKey("msgImgUrl")) {

                msgImgUrl = data.get("msgImgUrl").toString();
            }

            msgCreateAt = data.get("msgCreateAt").toString();
            msgDate = data.get("msgDate").toString();
            msgTimeAgo = data.get("msgTimeAgo").toString();
            msgRemoveAt = data.get("msgRemoveAt").toString();
        }

        int icon = R.drawable.ic_action_push_notification;
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);

        switch (Integer.valueOf(type)) {

            case GCM_NOTIFY_SYSTEM: {

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_action_push_notification)
                                .setContentTitle(title)
                                .setContentText(message);

                Intent resultIntent;

                if (App.getInstance().getId() != 0) {

                    resultIntent = new Intent(context, MainActivity.class);

                } else {

                    resultIntent = new Intent(context, AppActivity.class);
                }

                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    int importance = NotificationManager.IMPORTANCE_HIGH;

                    mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                    mNotificationManager.createNotificationChannel(mChannel);
                }

                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mBuilder.setAutoCancel(true);
                mNotificationManager.notify(0, mBuilder.build());

                break;
            }

            case GCM_NOTIFY_CUSTOM: {

                if (App.getInstance().getId() != 0) {

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_PERSONAL: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_COMMENT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);
                    App.getInstance().saveData();

                    message = context.getString(R.string.label_gcm_comment);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(NotificationsActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_COMMENT_REPLY: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);
                    App.getInstance().saveData();

                    message = context.getString(R.string.label_gcm_comment_reply);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(NotificationsActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_ITEM_APPROVED: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);
                    App.getInstance().saveData();

                    message = context.getString(R.string.label_gcm_item_approved);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_ITEM_REJECTED: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);
                    App.getInstance().saveData();

                    message = context.getString(R.string.label_gcm_item_rejected);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_PROFILE_PHOTO_REJECT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    message = context.getString(R.string.label_gcm_profile_photo_reject);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_PROFILE_COVER_REJECT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    message = context.getString(R.string.label_gcm_profile_cover_reject);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", PAGE_NOTIFICATIONS);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_MESSAGE: {

                if (App.getInstance().getId() != 0 && Long.valueOf(accountId) == App.getInstance().getId()) {

                    if (App.getInstance().getCurrentChatId() == Integer.valueOf(actionId)) {

                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);

                        i.putExtra("msgId", Integer.valueOf(msgId));
                        i.putExtra("msgFromUserId", Long.valueOf(msgFromUserId));
                        i.putExtra("msgFromUserState", Integer.valueOf(msgFromUserState));
                        i.putExtra("msgFromUserVerify", Integer.valueOf(msgFromUserVerify));
                        i.putExtra("msgFromUserUsername", String.valueOf(msgFromUserUsername));
                        i.putExtra("msgFromUserFullname", String.valueOf(msgFromUserFullname));
                        i.putExtra("msgFromUserPhotoUrl", String.valueOf(msgFromUserPhotoUrl));
                        i.putExtra("msgMessage", String.valueOf(msgMessage));
                        i.putExtra("msgImgUrl", String.valueOf(msgImgUrl));
                        i.putExtra("msgCreateAt", Integer.valueOf(msgCreateAt));
                        i.putExtra("msgDate", String.valueOf(msgDate));
                        i.putExtra("msgTimeAgo", String.valueOf(msgTimeAgo));

                        context.sendBroadcast(i);

                    } else {

                        if (App.getInstance().getMessagesCount() == 0) App.getInstance().setMessagesCount(App.getInstance().getMessagesCount() + 1);
                        App.getInstance().saveData();

                        if (App.getInstance().getAllowMessagesGCM() == ENABLED) {

                            message = context.getString(R.string.label_gcm_message);

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.ic_action_push_notification)
                                            .setContentTitle(title)
                                            .setContentText(message);

                            Intent resultIntent = new Intent(context, MainActivity.class);
                            resultIntent.putExtra("pageId", PAGE_MESSAGES);
                            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(DialogsActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                            mBuilder.setContentIntent(resultPendingIntent);

                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                int importance = NotificationManager.IMPORTANCE_HIGH;

                                mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                                mNotificationManager.createNotificationChannel(mChannel);
                            }

                            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                            mBuilder.setAutoCancel(true);
                            mNotificationManager.notify(0, mBuilder.build());
                        }
                    }
                }

                break;
            }

            default: {

                break;
            }
        }
    }
}