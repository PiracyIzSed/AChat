package com.android.achat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.achat.Activities.ChatActivity;
import com.android.achat.Activities.ProfileActivity;
import com.android.achat.DataModels.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Gaurav on 21-03-2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "AChat Notification Channel" ;

    DatabaseReference dbrefUsers = FirebaseDatabase.getInstance().getReference("Users");
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        final String titleNotif = remoteMessage.getData().get("title_notif");
        final String contentNotif = remoteMessage.getData().get("body_notif");
        final String image = remoteMessage.getData().get("user_image");
        final String type = remoteMessage.getData().get("type");
        final String u_id = remoteMessage.getData().get("from_user_id");

        dbrefUsers.child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.setUid(dataSnapshot.getKey());
                if(type.equals("friend_request")) {
                    Intent resultIntent = new Intent(getApplicationContext(),ProfileActivity.class);
                    resultIntent.putExtra("user_object", u);
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    createNotification(titleNotif,contentNotif,resultPendingIntent,null);
                }
                else if(type.equals("text")){
                    Intent i = new Intent(getApplicationContext(),ChatActivity.class);
                    i.putExtra("user_object",u);

                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                    createNotification(titleNotif,contentNotif,resultPendingIntent,image);
                }
                else if (type==null){
                    Log.d("payload :",""+remoteMessage.getData());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void createNotification(String titleNotif,String contentNotif,PendingIntent resultPendingIntent,String url){

        Notification notification = new Notification();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.defaults |= Notification.DEFAULT_SOUND;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Channel 1";
            String description = "Channel for notifications";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system
            mNotifyMgr.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,CHANNEL_ID);
        mBuilder.setDefaults(notification.defaults);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titleNotif)
                .setContentText(contentNotif)
                .setAutoCancel(true)
                .setVibrate(new long[]{50, 350, 200, 350, 200})
                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(resultPendingIntent);
        if(url!=null && !url.equals("default")){
            mBuilder.setLargeIcon(getCircleBitmap(getImageFromUrl(url)));
        } else{
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.default_avatar_profile));
        }

        int mNotificationId = (int) System.currentTimeMillis();

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private Bitmap getImageFromUrl(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
