package com.draw.tales.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.draw.tales.classes.Constants;

/**
 * Created by KorbBookProReturns on 2/22/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive: ");
        Intent serviceIntent = new Intent(context,NotificationsService.class);
        String userId = intent.getStringExtra(Constants.MY_USER_ID);
        serviceIntent.putExtra(Constants.LOGGED_IN_INTENT,true);
        serviceIntent.putExtra(Constants.MY_USER_ID,userId);
        context.startService(serviceIntent);
    }
}
