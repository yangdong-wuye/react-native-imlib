package cn.rongcloud.imlib.react;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

public class RCPushReceiver extends PushMessageReceiver {
    static private final String TAG = "RCPushReceiver";
    static RCTDeviceEventEmitter eventEmitter;

    @Override
    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage message) {
        if (eventEmitter != null) {
            eventEmitter.emit("rcimlib-push-arrived", Convert.toJSON(message, pushType));
        }
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage message) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri.Builder builder = Uri.parse("rong://" + context.getPackageName()).buildUpon();
        builder.appendPath("notification")
                .appendPath("onNotificationMessageClicked")
                .appendQueryParameter("notificationId", message.getNotificationId())
                .appendQueryParameter("pushType", pushType.getName())
                .appendQueryParameter("pushId", message.getPushId())
                .appendQueryParameter("pushTitle", message.getPushTitle())
                .appendQueryParameter("pushFlag", message.getPushFlag())
                .appendQueryParameter("pushContent", message.getPushContent())
                .appendQueryParameter("pushData", message.getPushData())
                .appendQueryParameter("objectName", message.getObjectName())
                .appendQueryParameter("senderId", message.getSenderId())
                .appendQueryParameter("senderName", message.getSenderName())
                .appendQueryParameter("senderPortraitUrl", message.getSenderPortrait().toString())
                .appendQueryParameter("targetId", message.getTargetId())
                .appendQueryParameter("targetUserName", message.getTargetUserName())
                .appendQueryParameter("conversationType", String.valueOf(message.getConversationType().getValue()))
                .appendQueryParameter("extra", message.getExtra());
        intent.setData(builder.build());
        intent.setPackage(context.getPackageName());
        context.startActivity(intent);
        Log.i(TAG, "onNotificationMessageClicked");
        RongPushClient.clearNotificationById(context, Integer.parseInt(message.getNotificationId()));
        return true;
    }

    public static void onNewIntent(ReactContext context, Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String host = uri.getHost();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            Log.i(TAG, intent.getDataString());
            Log.i(TAG, host);
            Log.i(TAG, scheme);
            Log.i(TAG, path);
            Log.i(TAG, String.valueOf(context == null));
            Log.i(TAG, String.valueOf(path.contains("onNotificationMessageClicked")));

            if (context != null && path.contains("onNotificationMessageClicked")) {
                WritableMap map = Convert.toJSON(uri);
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("rcimlib-push-clicked", Convert.toJSON(uri));
                Log.i(TAG, "rcimlib-push-clicked");
            }
        }

    }
}
