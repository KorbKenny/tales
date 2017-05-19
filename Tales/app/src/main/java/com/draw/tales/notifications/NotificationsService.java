package com.draw.tales.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.draw.tales.OnePathPageActivity;
import com.draw.tales.R;
import com.draw.tales.TwoPathPageActivity;
import com.draw.tales.classes.Constants;
import com.draw.tales.main.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by KorbBookProReturns on 2/21/17.
 */

public class NotificationsService extends IntentService {
    String mPageId, mType, mStory, iMyUserId;
    Integer mOneOrTwo;

    public NotificationsService() {
        super("DebugTag");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(networkIsAvailable()){
            boolean loggedIn = intent.getBooleanExtra(Constants.LOGGED_IN_INTENT,false);
            if(loggedIn){
                iMyUserId = intent.getStringExtra(Constants.MY_USER_ID);
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference notificationsRef = db.getReference(Constants.NOTIFICATIONS).child(iMyUserId);
                notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                if (dataSnapshot != null) {
                                    mPageId = dataSnapshot.child(Constants.N_PAGE_ID).getValue(String.class);
                                    mType = dataSnapshot.child(Constants.N_TYPE).getValue(String.class);
                                    mOneOrTwo = dataSnapshot.child(Constants.N_ONE_OR_TWO).getValue(Integer.class);
                                    mStory = dataSnapshot.child(Constants.N_STORY).getValue(String.class);
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if(mPageId!=null) {
                                    if (!mPageId.equals(Constants.DB_NULL)) {
                                        showNotification(mPageId,mType,mOneOrTwo,mStory);
                                    }
                                }
                            }
                        }.execute();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void showNotification(String pageId, String type, int oneOrTwo, String story){
        Intent notificationIntent;
        if(type.equals(Constants.GLOBAL)){
            notificationIntent = new Intent(this, TwoPathPageActivity.class);
        } else {
            if(oneOrTwo == 1){
                notificationIntent = new Intent(this, OnePathPageActivity.class);
            } else {
                notificationIntent = new Intent(this, TwoPathPageActivity.class);
            }
        }
        notificationIntent.putExtra(Constants.TYPE_INTENT,type);
        notificationIntent.putExtra(Constants.STORY_INTENT,story);
        notificationIntent.putExtra(Constants.PAGE_ID_INTENT,pageId);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);


        DatabaseReference notifyRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATIONS).child(iMyUserId);
        notifyRef.child(Constants.N_PAGE_ID).setValue(null);
        notifyRef.child(Constants.N_TYPE).setValue(null);
        notifyRef.child(Constants.N_STORY).setValue(null);
        notifyRef.child(Constants.N_ONE_OR_TWO).setValue(null);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("A page you worked on has been updated")
                .setContentText("Check it out and continue the story!")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager nMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMan.notify(0,n);
    }

    private boolean networkIsAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
