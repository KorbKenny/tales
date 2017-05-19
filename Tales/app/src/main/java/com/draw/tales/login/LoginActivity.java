package com.draw.tales.login;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.R;
import com.draw.tales.SplashActivity;
import com.draw.tales.classes.Constants;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN = 444;
    private static final String TAG = "ok";
    private SignInButton mGoogleLogIn;

    private CardView mFacebookLoginButton, mGoogleLoginButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase db;
    private DatabaseReference dUserNamesRef;
    private String mUserName, mUserId;
    private ValueEventListener mUsernameListener, mAllUsersListener;
    private ArrayList<String> mUserNameList;
    private ProgressBar mProgressBar;

    private boolean usersLoaded = false;
    private boolean loginClick = false;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseDatabase.getInstance();
        dUserNamesRef = db.getReference(Constants.USERNAMES_REF);

        mGoogleLoginButton = (CardView)findViewById(R.id.google_login_button);
        mProgressBar = (ProgressBar)findViewById(R.id.usernames_loading);
        TextView loginText = (TextView)findViewById(R.id.login_text);
        Typeface typeface = Typeface.createFromAsset(getAssets(),Constants.FONT);
        loginText.setTypeface(typeface);

        mAllUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    mUserNameList = new ArrayList<>();
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                mUserNameList.add(ds.getValue(String.class).toLowerCase());
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            mProgressBar.setVisibility(View.GONE);
                            mGoogleLoginButton.setVisibility(View.VISIBLE);
                        }
                    }.execute();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dUserNamesRef.addListenerForSingleValueEvent(mAllUsersListener);

        signInWithGoogle();

        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    mUserId = user.getUid();
                    Log.d(TAG, "onAuthStateChanged: " + mUserId);
                    mUsernameListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    mUserName = dataSnapshot.getValue(String.class);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    if(mUserName==null){
                                        chooseUsernameDialog();
                                    } else {
                                        Log.d(TAG, "onPostExecute: " + mUserName);
                                        goToMainActivity(mUserId,mUserName);
                                    }
                                }
                            }.execute();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    dUserNamesRef.child(mUserId).addValueEventListener(mUsernameListener);
                }
            }
        };
    }

    private void chooseUsernameDialog() {
        mGoogleLoginButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        LayoutInflater inflater = this.getLayoutInflater();
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_username,null))
                .setPositiveButton("Good!",null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog d = (Dialog) dialogInterface;
                        EditText theEditText = (EditText)d.findViewById(R.id.username_dialog_edit);
                        String username = theEditText.getText().toString();
                        if(username.length() < 5){
                            theEditText.setError("Must be longer than that");
                        } else if(mUserNameList.contains(username.toLowerCase())){
                            theEditText.setError("Username already in use");
                        } else {
                            mUserName = username;
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    DatabaseReference myUserRef = db.getReference(Constants.USERS_REF).child(mUserId);
                                    myUserRef.child(Constants.UID).setValue(mUserId);
                                    myUserRef.child(Constants.USER_NAME).setValue(mUserName);
                                    dUserNamesRef.child(mUserId).setValue(mUserName);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    dialog.dismiss();
                                }
                            }.execute();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void goToMainActivity(String userId,String userName) {
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        sp.edit().putString(Constants.MY_USER_ID,userId).commit();
        sp.edit().putString(Constants.MY_USER_NAME,userName).commit();

//        AlarmManager alarmMan = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        Intent receiverIntent = new Intent(this, AlarmReceiver.class);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(this,Constants.REPEATING_ALARM_ID,receiverIntent,0);
//
//        alarmMan.setRepeating(AlarmManager.RTC,0,60000*30,alarmIntent);

        Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.MY_USER_ID,userId);
        if(mUsernameListener!=null){
            dUserNamesRef.removeEventListener(mUsernameListener);
        }
        startActivity(intent);
    }

    protected void signInWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }



    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            Log.d(TAG, "onActivityResult: ");
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, R.string.firebase_error_login, Toast.LENGTH_SHORT).show();
                        } else {
                            loginClick = true;
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
        if(mUsernameListener!=null) {
            dUserNamesRef.removeEventListener(mUsernameListener);
        }
    }

}
