package com.draw.tales;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Me;
import com.draw.tales.groups.DBAssetHelper;
import com.draw.tales.login.LoginActivity;
import com.draw.tales.main.MainActivity;
import com.draw.tales.notifications.AlarmReceiver;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        String userId = sp.getString(Constants.MY_USER_ID,null);
        String userName = sp.getString(Constants.MY_USER_NAME,null);

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                DBAssetHelper dbSetup = new DBAssetHelper(SplashActivity.this);
                dbSetup.getReadableDatabase();
                return null;
            }
        }.execute();

        if(userId != null){
            Me.getInstance().setUserId(userId);
            Me.getInstance().setUsername(userName);

            //Setup alarm manager for notifications
            AlarmManager alarmMan = (AlarmManager) SplashActivity.this.getSystemService(Context.ALARM_SERVICE);
            Intent receiverIntent = new Intent(SplashActivity.this, AlarmReceiver.class);
            receiverIntent.putExtra(Constants.MY_USER_ID,userId);
            PendingIntent pi = PendingIntent.getBroadcast(SplashActivity.this,1234,receiverIntent,0);
            alarmMan.cancel(pi);
            alarmMan.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,4000,AlarmManager.INTERVAL_HOUR,pi);

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
