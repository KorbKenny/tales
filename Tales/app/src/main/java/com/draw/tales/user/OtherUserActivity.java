package com.draw.tales.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.draw.tales.R;
import com.draw.tales.TwoPathPageActivity;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.GroupLite;
import com.draw.tales.classes.InfoDialogs;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.SquareImageView;
import com.draw.tales.classes.UserPage;
import com.draw.tales.groups.DBSQLiteHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OtherUserActivity extends AppCompatActivity {
    private String iUserId, mUserName, mUserDescription, mUserImage, iMyUserId;
    private TextView mUserNameView, mUserDescriptionView, mInviteButton;
    private SquareImageView mUserImageView;
    private RecyclerView mRecyclerView;
    private PagesRecyclerAdapter mAdapter;
    private List<UserPage> mUserPageList;
    private List<GroupLite> mGroupsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        simpleSetup();
        getUserInfo();
        getGroups();


    }

    private void simpleSetup() {
        iUserId = getIntent().getStringExtra(Constants.USER_INTENT);
        mUserNameView = (TextView) findViewById(R.id.other_name);
        mUserDescriptionView = (TextView) findViewById(R.id.other_description);
        mInviteButton = (TextView) findViewById(R.id.other_invite);
        mUserImageView = (SquareImageView) findViewById(R.id.other_image);
        mRecyclerView = (RecyclerView) findViewById(R.id.other_recycler);

        Typeface tf = Typeface.createFromAsset(getAssets(),Constants.FONT);
        mUserNameView.setTypeface(tf);
        mInviteButton.setTypeface(tf);
        mUserDescriptionView.setTypeface(tf);

        mUserPageList = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }

        iMyUserId = Me.getInstance().getUserId();

        CardView inviteCard = (CardView) findViewById(R.id.other_invite_card);
        if(iUserId.equals(iMyUserId)){
            inviteCard.setVisibility(View.GONE);
        }
    }

    private void getUserInfo() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference(Constants.USERS_REF).child(iUserId);

        //Gets the name, picture, and description
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mUserName = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                        mUserImage = dataSnapshot.child(Constants.USER_IMAGE).getValue(String.class);
                        mUserDescription = dataSnapshot.child(Constants.USER_DESCRIPTION).getValue(String.class);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        setUserViews();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Gets the pages they've done
        userRef.child(Constants.PAGES).child(Constants.GLOBAL).child(Constants.FIRST_STORY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()) {
                            String id = ds.getKey();
                            String pic = ds.getValue(String.class);
                            UserPage up = new UserPage(pic,id);
                            mUserPageList.add(up);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        setUserPages();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUserViews() {
        mUserNameView.setText(mUserName);

        if(mUserDescription!=null) {
            mUserDescriptionView.setText(mUserDescription);
        }

        if(mUserImage!=null) {
            Picasso.with(OtherUserActivity.this).load(mUserImage).placeholder(R.drawable.loadingsquareimage).into(mUserImageView);
        }
    }

    private void setUserPages() {
        GridLayoutManager glm = new GridLayoutManager(OtherUserActivity.this,3);
        mAdapter = new PagesRecyclerAdapter(mUserPageList, new PagesRecyclerAdapter.PageClickedListener() {
            @Override
            public void onPageClicked(String pageId) {
                Intent intent = new Intent(OtherUserActivity.this, TwoPathPageActivity.class);
                intent.putExtra(Constants.PAGE_ID_INTENT,pageId);
                intent.putExtra(Constants.TYPE_INTENT,Constants.GLOBAL);
                intent.putExtra(Constants.STORY_INTENT,Constants.FIRST_STORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        mRecyclerView.setLayoutManager(glm);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getGroups() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                mGroupsList = DBSQLiteHelper.getInstance(OtherUserActivity.this).getAllGroups();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mInviteButton.setClickable(true);
                mInviteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InfoDialogs.createWhichGroupDialog(OtherUserActivity.this,mGroupsList,iUserId).show();
                    }
                });
            }
        }.execute();
    }
}
