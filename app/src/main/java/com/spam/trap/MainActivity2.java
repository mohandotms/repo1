package com.spam.trap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.spam.trap.R;


public class MainActivity2 extends AppCompatActivity {

    Context context;
    private static final String TAG = "MainActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            context = getApplicationContext();
            super.onCreate(savedInstanceState);
            SplashScreen.installSplashScreen(this);
            setContentView(R.layout.activity_main2);

        }
        catch(Exception e){
            Log.v(TAG,e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        context = getApplicationContext();
        super.onStart();

        try {

            boolean isNotificationServiceRunning = isNotificationServiceRunning(); //Asks User to enable Notification Access if not
            if(!isNotificationServiceRunning) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                Toast.makeText(context, "Allow SPAM TRAP to Access Notification to function Properly", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Log.v(TAG,e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Utility method to check Notification Access Enabled or not
    @Param boolean isNotificationServiceRunning
     */
    private boolean isNotificationServiceRunning() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }



}