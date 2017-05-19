package com.draw.tales.groups;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.OnePathPageActivity;
import com.draw.tales.R;
import com.draw.tales.TwoPathPageActivity;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Group;
import com.draw.tales.classes.GroupLite;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.SquareImageView;
import com.draw.tales.drawing.DrawingActivitySquare;
import com.draw.tales.main.MainActivity;
import com.draw.tales.user.OtherUserActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupCoverActivity extends AppCompatActivity {
    private static final String TAG = "ok";
    private TextView mTitle, mMembers, mInfo;
    private CardView mStartButton, mContinueButton;
    private SquareImageView mCoverImage;
    private FirebaseDatabase db;
    private DatabaseReference dThisGroupRef;
    private String iGroupId, mGroupTitle, iMyUserId, iContinueId;
    private ValueEventListener mGroupListener;
    private Group mThisGroup;
    private CardView mMembersLayout;
    private LinearLayout mButtonsLayout;
    private FloatingActionButton mInviteFab;
    private ImageView mSettingsButton;
    private long mTime;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<String> mMemberList, mIdList;
    private int mButtonLayoutHeight = 0;
    private boolean mMemberOpen = false;
    private boolean mMemberOpening = false;
    private boolean hasOpenedOnce = false;
    private boolean usersLoaded = false;
    private String mGroupCreator;
    private int mInvitePermissions, mTitlePermissions, mCoverPermissions, mI,mT,mC, mBlackOrWhite;

    private boolean accepted, visiting;
    private String mInviteMemberName, mCoverPath;
    private List<String> mAllUserNames;
    private List<String> mAllUserIds;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_cover);

        simpleSetup();

        getThisGroup();

        inviteSomeone();

        //=============================================================
        //                  Title
        //=============================================================
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!visiting) {
                    if(mTitlePermissions==1 || mGroupCreator.equals(iMyUserId)) {
                        createTitleDialog();
                    }
                }
            }
        });

        mTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mCoverPermissions==1 || mGroupCreator.equals(iMyUserId)) {
                    if (mBlackOrWhite==0) {
                        mBlackOrWhite = 1;
                        mTitle.setTextColor(Color.WHITE);
                        mTitle.setShadowLayer(3, 3, 3, Color.BLACK);
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                dThisGroupRef.child(Constants.BLACK_OR_WHITE).setValue(mBlackOrWhite);
                                return null;
                            }
                        }.execute();
                    } else {
                        mBlackOrWhite = 0;
                        mTitle.setTextColor(Color.BLACK);
                        mTitle.setShadowLayer(3, 3, 3, Color.WHITE);
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                dThisGroupRef.child(Constants.BLACK_OR_WHITE).setValue(mBlackOrWhite);
                                return null;
                            }
                        }.execute();
                    }
                }
                return true;
            }
        });

        //=============================================================
        //                 Cover Image
        //=============================================================
        mCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!visiting) {
                    if(mCoverPermissions==1 || mGroupCreator.equals(iMyUserId)) {
                        createCoverDialog();
                    }
                }
            }
        });

        //=============================================================
        //                Members
        //=============================================================
        mMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasOpenedOnce){
                    getUsernames();
                }
                if(!mMemberOpening){
                    if(mMemberOpen){
                        hideMembersLayout();
                    } else {
                        hasOpenedOnce = true;
                        showMembersLayout();
                    }
                }
            }
        });


        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSettingsDialog();
            }
        });



        //=============================================================
        //             Start From Beginning
        //=============================================================
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThisGroup.getType()==1) {
                    Intent intent = new Intent(GroupCoverActivity.this, OnePathPageActivity.class);
                    intent.putExtra(Constants.TYPE_INTENT,Constants.GROUPS);
                    intent.putExtra(Constants.STORY_INTENT,iGroupId);
                    intent.putExtra(Constants.PAGE_ID_INTENT,Constants.FIRST_PAGE_ID);
                    if(visiting) {
                        intent.putExtra(Constants.VISITING_INTENT, true);
                    }
                    startActivity(intent);
                    finish();
                } else if(mThisGroup.getType()==2){
                    Intent intent = new Intent(GroupCoverActivity.this, TwoPathPageActivity.class);
                    intent.putExtra(Constants.TYPE_INTENT,Constants.GROUPS);
                    intent.putExtra(Constants.STORY_INTENT,iGroupId);
                    intent.putExtra(Constants.PAGE_ID_INTENT,Constants.FIRST_PAGE_ID);
                    if(visiting) {
                        intent.putExtra(Constants.VISITING_INTENT, true);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    //==============================================================================================
    //                  Settings Dialog
    //==============================================================================================
    private void createSettingsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.dialog_settings_group,null);

        TextView permTitle = (TextView)alertView.findViewById(R.id.group_permissions_title);

        final TextView settingsInviteText = (TextView) alertView.findViewById(R.id.settings_invite_text);
        final TextView settingsTitleText = (TextView) alertView.findViewById(R.id.settings_title_text);
        final TextView settingsCoverText = (TextView) alertView.findViewById(R.id.settings_cover_text);

        settingsInviteText.setTypeface(typeface);
        settingsTitleText.setTypeface(typeface);
        settingsCoverText.setTypeface(typeface);
        permTitle.setTypeface(typeface);

        CardView settingsInviteCard = (CardView) alertView.findViewById(R.id.settings_invite_card);
        CardView settingsTitleCard = (CardView) alertView.findViewById(R.id.settings_title_card);
        CardView settingsCoverCard = (CardView) alertView.findViewById(R.id.settings_cover_card);

        final View settingsInviteBlue = alertView.findViewById(R.id.settings_invite_blue);
        final View settingsTitleBlue = alertView.findViewById(R.id.settings_title_blue);
        final View settingsCoverBlue = alertView.findViewById(R.id.settings_cover_blue);

        mI = mInvitePermissions;
        mT = mTitlePermissions;
        mC = mCoverPermissions;

        if(mI==1){
            settingsInviteBlue.setVisibility(View.VISIBLE);
            settingsInviteText.setText("Anyone can invite anyone");
        } else{
            settingsInviteBlue.setVisibility(View.GONE);
            settingsInviteText.setText("Only you can invite others");
        }
        if(mT==1){
            settingsTitleBlue.setVisibility(View.VISIBLE);
            settingsTitleText.setText("Anyone can edit the title");
        } else {
            settingsTitleBlue.setVisibility(View.GONE);
            settingsTitleText.setText("Only you can edit the title");
        }
        if(mC==1){
            settingsCoverBlue.setVisibility(View.VISIBLE);
            settingsCoverText.setText("Anyone can edit the cover");
        } else {
            settingsCoverBlue.setVisibility(View.GONE);
            settingsCoverText.setText("Only you can edit the cover");
        }


        settingsInviteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mI==1){
                    mI = 0;
                    settingsInviteBlue.setVisibility(View.GONE);
                    settingsInviteText.setText("Only you can invite others");
                } else {
                    mI = 1;
                    settingsInviteBlue.setVisibility(View.VISIBLE);
                    settingsInviteText.setText("Anyone can invite anyone");
                }
            }
        });

        settingsTitleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mT==1){
                    mT = 0;
                    settingsTitleBlue.setVisibility(View.GONE);
                    settingsTitleText.setText("Only you can edit the title");
                } else {
                    mT = 1;
                    settingsTitleBlue.setVisibility(View.VISIBLE);
                    settingsTitleText.setText("Anyone can edit the title");
                }
            }
        });

        settingsCoverCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mC==1){
                    mC = 0;
                    settingsCoverBlue.setVisibility(View.GONE);
                    settingsCoverText.setText("Only you can edit the cover");
                } else {
                    mC = 1;
                    settingsCoverBlue.setVisibility(View.VISIBLE);
                    settingsCoverText.setText("Anyone can edit the cover");
                }
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(GroupCoverActivity.this)
                .setView(alertView)
                .setPositiveButton("Save",null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button button = ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected void onPreExecute() {
                                button.setClickable(false);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                dThisGroupRef.child(Constants.INVITE_PERMISSIONS).setValue(mI);
                                dThisGroupRef.child(Constants.TITLE_PERMISSIONS).setValue(mT);
                                dThisGroupRef.child(Constants.COVER_PERMISSIONS).setValue(mC);
                                mInvitePermissions = mI;
                                mTitlePermissions = mT;
                                mCoverPermissions = mC;
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialog.dismiss();
                            }
                        }.execute();
                    }
                });
            }
        });

        dialog.show();

    }


    //==============================================================================================
    //                  Get all Usernames
    //==============================================================================================
    private void getUsernames() {
        mAllUserNames = new ArrayList<>();
        mAllUserIds = new ArrayList<>();
        DatabaseReference userNameRef = db.getReference(Constants.USERNAMES_REF);
        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                mAllUserNames.add(ds.getValue(String.class));
                                mAllUserIds.add(ds.getKey());
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        usersLoaded = true;
                    }
                }.execute();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //==============================================================================================
    //                  Invite Someone to This Group
    //==============================================================================================
    private void inviteSomeone() {
        mInviteFab = (FloatingActionButton)findViewById(R.id.gc_add_member_fab);
        mInviteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInviteDialog();
            }
        });
    }

    private void createInviteDialog() {
        View alertView = getLayoutInflater().inflate(R.layout.dialog_invite,null);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(alertView)
                .setPositiveButton("Invite",null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Dialog d = (Dialog)dialogInterface;
//                        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog d = (Dialog)dialogInterface;
                        EditText edit = (EditText)d.findViewById(R.id.invite_edit);
                        mInviteMemberName = edit.getText().toString();
                        if(mInviteMemberName.length()>4){
                            if(mThisGroup.getMemberList().contains(mInviteMemberName)){
                                Toast.makeText(GroupCoverActivity.this, "Already in this group!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(mAllUserNames.contains(mInviteMemberName)){
                                int index = mAllUserNames.indexOf(mInviteMemberName);
                                String userId = mAllUserIds.get(index);
                                Log.d(TAG, "onClick: " + index + " " + userId + " " + mInviteMemberName);
                                DatabaseReference userRef = db.getReference(Constants.USERS_REF).child(userId).child(Constants.INVITES_REF).child(iGroupId);
                                userRef.setValue(mThisGroup.getName());
                                Toast.makeText(GroupCoverActivity.this, mInviteMemberName + " has been invited!", Toast.LENGTH_SHORT).show();
//                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(GroupCoverActivity.this, "No user by that name", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GroupCoverActivity.this, "No user by that name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    //==============================================================================================
    //                  Edit the Title of the Group
    //==============================================================================================
    private void createTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_group_title_edit,null));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Dialog d = (Dialog)dialogInterface;
                EditText et = (EditText) d.findViewById(R.id.gte_dialog_edit);
                String temp = et.getText().toString();
                String newTitle = temp.replace("\n","");
                if(newTitle.trim().length() > 1){
                    dThisGroupRef.child(Constants.GROUP_NAME).setValue(newTitle);
                    db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.GROUPS).child(iGroupId).child(Constants.GROUP_NAME).setValue(newTitle);
                    mTitle.setText(newTitle);
                    DBSQLiteHelper.getInstance(GroupCoverActivity.this).updateGroupName(iGroupId,newTitle);
                } else {
                    Toast.makeText(GroupCoverActivity.this, "Must be longer than that...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
    }

    //==============================================================================================
    //                  Edits the Group's Cover Image
    //==============================================================================================
    private void createCoverDialog() {
        Intent intent = new Intent(GroupCoverActivity.this, DrawingActivitySquare.class);
        intent.putExtra(Constants.FROM_GROUP_COVER_INTENT,true);
        intent.putExtra(Constants.TYPE_INTENT,Constants.GROUPS);
        intent.putExtra(Constants.STORY_INTENT,iGroupId);
        intent.putExtra(Constants.PAGE_ID_INTENT,Constants.GROUP_COVER);
        intent.putExtra(Constants.GROUP_NAME,mGroupTitle);
        startActivity(intent);
        finish();
    }


    //==============================================================================================
    //                  Show/Hide Members Drawer
    //==============================================================================================
    private void showMembersLayout() {
            if(mButtonLayoutHeight==0){
                mButtonLayoutHeight = mButtonsLayout.getHeight();
            }
            mMembersLayout.setVisibility(View.VISIBLE);
            mMemberOpening = true;
            mButtonsLayout.setClickable(false);
            mMembers.setClickable(false);
            ValueAnimator ani = ValueAnimator.ofFloat(0, 1.0f);
            ani.setDuration(250);
            ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    float translateVale = (1.0f - value) * mButtonLayoutHeight;
                    mMembersLayout.setAlpha(1.0f);
                    mMembersLayout.setTranslationY(translateVale);
                    if(value == 1.0f){
                        mMembersLayout.setClickable(true);
                        mMembers.setClickable(true);
                        mMemberOpening = false;
                        mMemberOpen = true;
                    }

                }
            });
        ani.start();
    }

    private void hideMembersLayout() {
            mMemberOpening = true;
            mButtonsLayout.setClickable(false);
            mMembers.setClickable(false);
            ValueAnimator ani = ValueAnimator.ofFloat(1.0f, 0);
            ani.setDuration(250);
            ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    float translateVale = (1.0f - value) * mButtonLayoutHeight;
                    mMembersLayout.setAlpha(1.0f);
                    mMembersLayout.setTranslationY(translateVale);
                    if (value == 0) {
                        mMembersLayout.setClickable(false);
                        mMembers.setClickable(true);
                        mMemberOpening = false;
                        mMemberOpen = false;
                        mMembersLayout.setAlpha(0);
                        mButtonsLayout.setClickable(true);
                        mMembersLayout.setVisibility(View.GONE);
                    }
                }
            });
        ani.start();
    }

    //==============================================================================================
    //                  Gets the Group You're Currently In
    //==============================================================================================
    private void getThisGroup() {
        mGroupListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mThisGroup.setName(dataSnapshot.child(Constants.GROUP_NAME).getValue(String.class));
                        mThisGroup.setType(dataSnapshot.child(Constants.GROUP_TYPE).getValue(Integer.class));
                        mThisGroup.setUpdated(dataSnapshot.child(Constants.GROUP_UPDATED).getValue(Integer.class));
                        mCoverPath = dataSnapshot.child(Constants.GROUP_COVER).getValue(String.class);
                        for (DataSnapshot ds:dataSnapshot.child(Constants.GROUP_MEMBERS).getChildren()) {
                            mMemberList.add(ds.getValue(String.class));
                            mIdList.add(ds.getKey());
                        }
                        mThisGroup.setMemberList(mMemberList);
                        mThisGroup.setIdList(mIdList);
                        mGroupCreator = dataSnapshot.child(Constants.GROUP_CREATOR).getValue(String.class);
                        mInvitePermissions = dataSnapshot.child(Constants.INVITE_PERMISSIONS).getValue(Integer.class);
                        mTitlePermissions = dataSnapshot.child(Constants.TITLE_PERMISSIONS).getValue(Integer.class);
                        mCoverPermissions = dataSnapshot.child(Constants.COVER_PERMISSIONS).getValue(Integer.class);
                        mBlackOrWhite = dataSnapshot.child(Constants.BLACK_OR_WHITE).getValue(Integer.class);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mAdapter.notifyDataSetChanged();
                        mButtonLayoutHeight = mButtonsLayout.getHeight();
                        setViewsAfterLoading();
                        updateYourLocalAndRemoteGroups();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dThisGroupRef.addListenerForSingleValueEvent(mGroupListener);
    }

    private void updateYourLocalAndRemoteGroups() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                if(accepted){
                    db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.INVITES_REF).child(iGroupId).setValue(null);
                }
                DatabaseReference addGroupRef = db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.GROUPS).child(iGroupId);
                addGroupRef.child(Constants.GROUP_NAME).setValue(mThisGroup.getName());
                addGroupRef.child(Constants.GROUP_TYPE).setValue(mThisGroup.getType());
                addGroupRef.child(Constants.GROUP_UPDATED).setValue(mThisGroup.getUpdated());
                String memberList = "";
                boolean firstMember = true;
                for (int i = 0; i < mMemberList.size() - 1; i++) {
                    addGroupRef.child(Constants.GROUP_MEMBERS).child(mIdList.get(i)).setValue(mMemberList.get(i));
                    if(!mMemberList.get(i).equals(Me.getInstance().getUsername())) {
                        if(firstMember){
                            memberList += mMemberList.get(i);
                            firstMember = false;
                        } else {
                            memberList += (", " + mMemberList.get(i));
                        }
                    }
                }
                GroupLite gl = new GroupLite(mThisGroup.getId(),mThisGroup.getName(),mThisGroup.getType(),memberList,mThisGroup.getUpdated());
                DBSQLiteHelper.getInstance(GroupCoverActivity.this).updateGroup(gl);
                return null;
            }
        }.execute();
    }

    private void setViewsAfterLoading() {
        mTitle.setText(mThisGroup.getName());
        mTitle.setClickable(true);
        mTitle.setLongClickable(true);
        if(mBlackOrWhite==1){
            mTitle.setTextColor(Color.WHITE);
            mTitle.setShadowLayer(3,3,3,Color.BLACK);
        } else {
            mTitle.setTextColor(Color.BLACK);
            mTitle.setShadowLayer(3,3,3,Color.WHITE);
        }

        if(mGroupCreator.equals(iMyUserId)){
            mInviteFab.setVisibility(View.VISIBLE);
            mSettingsButton.setVisibility(View.VISIBLE);
        }
        if(mInvitePermissions == 1){
            mInviteFab.setVisibility(View.VISIBLE);
        }
        long time = (long)(mThisGroup.getUpdated() * 1000);
        String date = DateFormat.getDateTimeInstance().format(new Date(time));
        if(mCoverPath!=null) {
            Picasso.with(GroupCoverActivity.this).load(mCoverPath).placeholder(R.drawable.loadingsquareimage).into(mCoverImage);
        }
//        mUpdatedAt.setText("Updated " + date);

    }

    //==============================================================================================
    //                  Simple Setup
    //==============================================================================================
    private void simpleSetup() {
        mTime = System.currentTimeMillis();
        mTitle = (TextView)findViewById(R.id.gc_title);
        mStartButton = (CardView) findViewById(R.id.gc_start_beginning);
        mContinueButton = (CardView) findViewById(R.id.gc_continue);
        mMembers = (TextView)findViewById(R.id.gc_members);
        mInfo = (TextView)findViewById(R.id.gc_info);
        mCoverImage = (SquareImageView) findViewById(R.id.gc_cover);

        mSettingsButton = (ImageView)findViewById(R.id.gc_settings_button);

        mButtonsLayout = (LinearLayout)findViewById(R.id.gc_buttons_layout);
        mMembersLayout = (CardView) findViewById(R.id.gc_members_card);

        typeface= Typeface.createFromAsset(getAssets(), Constants.FONT);
        TextView start = (TextView)findViewById(R.id.gc_start_beginning_text);
        TextView continueText = (TextView)findViewById(R.id.gc_continue_text);
        mTitle.setTypeface(typeface);
        mMembers.setTypeface(typeface);
        start.setTypeface(typeface);
        continueText.setTypeface(typeface);

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }

        iMyUserId = Me.getInstance().getUserId();

        iGroupId = getIntent().getStringExtra(Constants.GROUP_INTENT);
        mGroupTitle = getIntent().getStringExtra(Constants.GROUP_NAME);
        accepted = getIntent().getBooleanExtra(Constants.ACCEPTED_INTENT,false);
        visiting = getIntent().getBooleanExtra(Constants.VISITING_INTENT,false);

        mTitle.setText(mGroupTitle);

        iContinueId = sp.getString(iGroupId,null);

        if(iContinueId == null){
            mContinueButton.setVisibility(View.GONE);
        }

        db = FirebaseDatabase.getInstance();
        dThisGroupRef = db.getReference(Constants.GROUPS).child(iGroupId);

        mThisGroup = new Group();

        mMemberList = new ArrayList<>();
        mIdList = new ArrayList<>();
        mRecyclerView = (RecyclerView)findViewById(R.id.gc_members_recycler);
        GridLayoutManager glm = new GridLayoutManager(this,4);
        mAdapter = new MembersRecyclerAdapter(mMemberList, mIdList, new MembersRecyclerAdapter.MemberClickListener() {
            @Override
            public void onMemberClicked(String memberId) {
                goToMemberPage(memberId);
            }
        });
        mRecyclerView.setLayoutManager(glm);
        mRecyclerView.setAdapter(mAdapter);
    }

    //==============================================================================================
    //                  Links to the Member You Click
    //==============================================================================================
    private void goToMemberPage(String memberId) {
        Intent intent = new Intent(GroupCoverActivity.this, OtherUserActivity.class);
        intent.putExtra(Constants.USER_INTENT,memberId);
        startActivity(intent);
    }

    //==============================================================================================
    //                  Go Back
    //==============================================================================================
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GroupCoverActivity.this, MainActivity.class);
        intent.putExtra(Constants.GROUP_TO_MAIN_INTENT,true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        long newTime = System.currentTimeMillis();
//        if(newTime-mTime > 60000){
//            Intent intent = new Intent(GroupCoverActivity.this,MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
