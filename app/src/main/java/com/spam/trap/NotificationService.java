package com.spam.trap;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.spam.trap.spamclassifier.SpamClassifier;

public class NotificationService extends NotificationListenerService {

    private  SpamClassifier classifier=null;
    private Context context;
    private final String SPAM_CHANNEL_ID="SPAM TRAP";
    private final String SPAM_CHANNEL_NAME=" Spam Notifications detected";
    private final String SPAM_GROUP_ID="com.spam.trap";
    private Notification spamSummaryNotification=null;
    private int notificationId=0;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initNotificationService();
//        postNotificationInSpamChannel(spamSummaryNotification);
    }

    public void buildSpamClassifier() {
        try {
        classifier = new SpamClassifier(this);
        classifier.prepare();
        classifier.transform();
        classifier.fit();
//        sc.evaluate();
    } catch (Exception e) {
        Log.v(TAG, e.getMessage());
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(classifier==null) {
            buildSpamClassifier();
        }
        for (StatusBarNotification sbm : NotificationService.this.getActiveNotifications()) {
            String title = sbm.getNotification().extras.getString("android.title");
            String text = sbm.getNotification().extras.getString("android.text");
            String package_name = sbm.getPackageName();
            if(SPAM_GROUP_ID.equals(sbm.getGroupKey()))continue;
            classifyNotification(context,title,text,package_name);
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Toast.makeText(context,"Notification Removed",Toast.LENGTH_SHORT).show();
    }
    public void classifyNotification(Context context,String notificationTitle,String notificationText,String senderAppPackageName){
        try {
//            notificationText="goldviking (29/M) is inviting you to be his friend. Reply YES-762 or NO-762 See him: www.SMS.ac/u/goldviking STOP? Send STOP FRND to 62468";
//            notificationTitle="SnapChat";
//            Toast.makeText(context, "Notification from package "+senderAppPackageName, Toast.LENGTH_SHORT).show();
            Log.v(TAG,"notificationText : "+notificationText);
            Log.v(TAG,"notificationTitle : "+notificationTitle);
//            Log.v(TAG,"Notification from package "+senderAppPackageName);
            if (notificationText!=null&&"spam".equals(classifier.classify(notificationText)) || notificationTitle!=null&&"spam".equals(classifier.classify(notificationTitle))){
//                Toast.makeText(context, "Notification text is:" + notificationText+" And it is SPAM!!!!  Notification", Toast.LENGTH_SHORT).show();
                //build spam notification
                Notification spamNotification = new NotificationCompat.Builder(this, SPAM_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setGroup(SPAM_GROUP_ID)
                        .build();
//                spamSummaryNotification=null;
                postNotificationInSpamChannel(spamNotification);
                //post notification in spam channel

            }
//            else{
//                Toast.makeText(context, "Notification text is:" + notificationText+" And it is HAM :) Notification", Toast.LENGTH_SHORT).show();
//            }

        }
        catch(Exception e){
            Log.v(TAG,e.getMessage());
            Toast.makeText(context, "Exception Occurs", Toast.LENGTH_SHORT).show();
        }
    }
    private void initNotificationService(){
        //create spam notification channel
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel spamChannel=new NotificationChannel(SPAM_CHANNEL_ID,SPAM_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(spamChannel);
        }
        // create notificationManagerCompat
        notificationManagerCompat = NotificationManagerCompat.from(this);
        // create spam notifications summary
         spamSummaryNotification =
                new NotificationCompat.Builder(this, SPAM_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle("SPAM TRAP Service Running in Background")
                        .setGroup(SPAM_GROUP_ID)
                        .setGroupSummary(true)
                        .build();

        postNotificationInSpamChannel(spamSummaryNotification);
    }
    private void postNotificationInSpamChannel(Notification spamNotification){
        if(spamNotification==null){
            return;
        }
        notificationManagerCompat.notify(notificationId++,spamNotification);

    }

}