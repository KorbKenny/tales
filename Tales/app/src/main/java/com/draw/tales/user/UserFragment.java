package com.draw.tales.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.R;
import com.draw.tales.SplashActivity;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.InfoDialogs;
import com.draw.tales.classes.Me;
import com.draw.tales.drawing.DrawingActivitySquare;
import com.draw.tales.groups.DBSQLiteHelper;
import com.draw.tales.login.LoginActivity;
import com.draw.tales.main.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by KorbBookProReturns on 2/14/17.
 */

public class UserFragment extends Fragment {
    private String iMyUserId, mDescription, mUserName, mImagePath;
    private TextView mDescriptionView, mUserNameView, mLogOut, mEdit;
    private ImageView mUserImage;
    private FirebaseDatabase db;
    private SharedPreferences sp;
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        simpleSetup(view);
        getImageAndDescription(view);
        buildGoogleApiClient();
        logOut(view);

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DrawingActivitySquare.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InfoDialogs.createUserDescriptionDialog(getActivity(),iMyUserId).show();
            }
        });

    }

    private void simpleSetup(View view) {
        sp = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }

        mDescriptionView = (TextView) view.findViewById(R.id.my_user_info);
        mUserNameView = (TextView) view.findViewById(R.id.my_user_name);
        mUserImage = (ImageView) view.findViewById(R.id.my_user_image);
        mEdit = (TextView) view.findViewById(R.id.my_user_edit);

        iMyUserId = Me.getInstance().getUserId();
        mUserName = Me.getInstance().getUsername();

        mUserNameView.setText(mUserName);

        mImagePath = sp.getString(Constants.MY_USER_IMAGE,null);
        if(mImagePath!=null){
            Picasso.with(getActivity()).load(mImagePath).placeholder(R.drawable.loadingsquareimage).into(mUserImage);
        }

        mDescription = sp.getString(Constants.MY_USER_DESCRIPTION,null);
        if(mDescription!=null){
            mDescriptionView.setText(mDescription);
            mEdit.setClickable(true);
        }


        db = FirebaseDatabase.getInstance();

        Typeface typeface= Typeface.createFromAsset(getActivity().getAssets(), Constants.FONT);
        mUserNameView.setTypeface(typeface);
        mDescriptionView.setTypeface(typeface);
        mEdit.setTypeface(typeface);



    }

    private void buildGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    private void getImageAndDescription(View view) {
        DatabaseReference myRef = db.getReference(Constants.USERS_REF).child(iMyUserId);

        if(mImagePath==null) {
            myRef.child(Constants.USER_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (dataSnapshot != null) {
                                mImagePath = dataSnapshot.getValue(String.class);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (mImagePath != null) {
                                if(getContext()!=null) {
                                    Picasso.with(getActivity()).load(mImagePath).placeholder(R.drawable.loadingsquareimage).into(mUserImage);
                                }
                            }

                            mUserImage.setClickable(true);
                        }
                    }.execute();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(mDescription==null) {
            myRef.child(Constants.USER_DESCRIPTION).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (dataSnapshot != null) {
                                mDescription = dataSnapshot.getValue(String.class);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (mDescription != null) {
                                mDescriptionView.setText(mDescription);
                            } else {
                                mDescriptionView.setText("You have 160 characters to describe yourself.");
                            }
                            mEdit.setClickable(true);
                        }
                    }.execute();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void logOut(View view) {
        mLogOut = (TextView) view.findViewById(R.id.logout);
        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        Toast.makeText(getActivity(), "Logging Out...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        FirebaseAuth.getInstance().signOut();
                        sp.edit().putString(Constants.MY_USER_ID,null).commit();
                        sp.edit().putString(Constants.MY_USER_NAME,null).commit();
                        sp.edit().putString(Constants.MY_USER_DESCRIPTION,null).commit();
                        sp.edit().putString(Constants.MY_USER_IMAGE,null).commit();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        DBSQLiteHelper.getInstance(getContext()).clearAllGroups();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Intent intent = new Intent(getActivity(), SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }.execute();


            }
        });
    }
}