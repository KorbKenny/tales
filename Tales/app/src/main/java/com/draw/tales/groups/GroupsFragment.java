package com.draw.tales.groups;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.R;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Group;
import com.draw.tales.classes.GroupLite;
import com.draw.tales.classes.InfoDialogs;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.OnePathPage;
import com.draw.tales.classes.TwoPathPage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by KorbBookProReturns on 2/14/17.
 */

public class GroupsFragment extends Fragment{
    private List<Group> mGroupList;
    private List<String> mInviteIdList, mInviteNameList;
    private List<GroupLite> gList, tempList;

    private FirebaseDatabase db;
    private DatabaseReference dGroupsRef;
    private String iMyUserId,iMyUserName;
    private ValueEventListener mGroupsListener;
    private RecyclerView mRecyclerView, mInvitesRecycler;
    private GroupsRecyclerAdapter mAdapter;
    private InvitesRecyclerAdapter mInviteAdapter;
    private RelativeLayout mNoGroupsLayout, mLoadingLayout;
    private FloatingActionButton fab;
    private int mOneOrTwo = 1;
    private String mNewGroupName;
    private RelativeLayout mLinearButton, mMultiverseButton;
    private int mCurrentTime;
    private CardView mInvitesCard;
    private boolean somethingUpdated = false;
    private Typeface typeface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        simpleSetup(view);
//        groupsListener();
        getGroupsFromDB();
        invitesListener(view);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
            }
        });

    }

    //==============================================================================================
    //                  Simple setup
    //==============================================================================================
    private void simpleSetup(View view) {

        // User
        SharedPreferences sp = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }
        iMyUserId = Me.getInstance().getUserId();
        iMyUserName = Me.getInstance().getUsername();

        // Database
        db = FirebaseDatabase.getInstance();
        dGroupsRef = db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.GROUPS);

        // Main
        mRecyclerView = (RecyclerView) view.findViewById(R.id.vh_group_recycler);
        mNoGroupsLayout = (RelativeLayout) view.findViewById(R.id.group_frag_empty);
        mLoadingLayout = (RelativeLayout) view.findViewById(R.id.groups_loading_layout);
        fab = (FloatingActionButton) view.findViewById(R.id.create_new_group);
        TextView noGroupsText = (TextView)view.findViewById(R.id.you_have_no_groups);
        typeface = Typeface.createFromAsset(getActivity().getAssets(),Constants.FONT);
        noGroupsText.setTypeface(typeface);

        // Recycler
        mGroupList = new ArrayList<>();
        tempList = new ArrayList<>();
        gList = new ArrayList<>();


        mInvitesCard = (CardView) view.findViewById(R.id.invites_card);

    }

    //==============================================================================================
    //                  Get your groups
    //==============================================================================================
    private void getGroupsFromDB(){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                if(tempList!=null) {
                    tempList.clear();
                    tempList = null;
                }
                tempList = DBSQLiteHelper.getInstance(getActivity()).getAllGroups();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                orderList();

                mAdapter = new GroupsRecyclerAdapter(gList, new GroupsRecyclerAdapter.GroupClickListener() {
                    @Override
                    public void onGroupClicked(String groupId, String groupName) {
                        goToGroupCoverActivity(groupId, groupName, false, false);
                    }
                }, new GroupsRecyclerAdapter.GroupLongClickListener() {
                    @Override
                    public void onGroupLongClicked(final String groupId, String groupName, final GroupLite g) {
                        DatabaseReference groupRef = db.getReference(Constants.GROUPS).child(groupId).child(Constants.GROUP_CREATOR);
                        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                new AsyncTask<Void,Void,String>(){
                                    @Override
                                    protected String doInBackground(Void... voids) {
                                        return dataSnapshot.getValue(String.class);
                                    }

                                    @Override
                                    protected void onPostExecute(String s) {
                                        boolean creator = false;
                                        if(s.equals(iMyUserId)){
                                            creator = true;
                                        }
                                        InfoDialogs.createLeaveGroupDialog(getActivity(),groupId,iMyUserId,g,creator).show();
                                    }
                                }.execute();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });




                    }
                });
                LinearLayoutManager llm = new LinearLayoutManager(mRecyclerView.getContext(),LinearLayoutManager.VERTICAL,false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setAdapter(mAdapter);

                mLoadingLayout.setVisibility(View.GONE);
                if(mInvitesCard.getVisibility()!=View.VISIBLE) {
                    fab.setVisibility(View.VISIBLE);
                }
                if(gList.size() == 0){
                    mNoGroupsLayout.setVisibility(View.VISIBLE);
                } else if(gList.size() > 0){
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                groupsListener();
            }
        }.execute();
    }

    private void orderList() {
        if(gList!=null) {
            gList.clear();
            gList = null;
        }
        gList = new ArrayList<>();

        for (GroupLite g:tempList) {
               if(gList.size()>1){
                if(gList.get(0).getUpdated() < g.getUpdated()){
                    gList.add(0,g);
                } else {
                    if(gList.get(gList.size()-1).getUpdated() < g.getUpdated()){
                        gList.add(gList.size()-1,g);
                    } else {
                        gList.add(g);
                    }
                }
            } else {
                gList.add(g);
            }
        }
    }

    private void groupsListener() {
        mGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    new AsyncTask<Void,Void,Boolean>(){
                        @Override
                        protected Boolean doInBackground(Void... voids) {

                            //Each child of the DataSnapshot is a group.
                            // So loop through all the groups.
                            for (DataSnapshot ds:dataSnapshot.getChildren()) {

                                boolean groupMatched = false;

                                //Concatonate the members into a String, which is really a list of them.
                                boolean firstMember = true;
                                String memberList = "";

                                for (DataSnapshot child:ds.child(Constants.GROUP_MEMBERS).getChildren()) {
                                    String member = child.getValue(String.class);
                                    if(!member.equals(Me.getInstance().getUsername())) {
                                        if(firstMember){
                                            memberList += member;
                                            firstMember = false;
                                        } else {
                                            memberList += ", ";
                                            memberList += member;
                                        }
                                    }
                                }

                                // Create a GroupLite object which will have everything from the firebase db
                                GroupLite g = new GroupLite();
                                g.setName(ds.child(Constants.GROUP_NAME).getValue(String.class));
                                g.setType(ds.child(Constants.GROUP_TYPE).getValue(Integer.class));
                                g.setUpdated(ds.child(Constants.GROUP_UPDATED).getValue(Integer.class));
                                g.setId(ds.getKey());
                                g.setMembers(memberList);

                                Log.d(TAG, "doInBackground: " + g.getMembers() + g.getName());

                                // Loops through all the GroupLites in the sqlite db
                                // Compare it against the grouplites to see if anything's been updated.
                                if(gList!=null) {
                                    for (GroupLite gLite : gList) {
                                        if (gLite.getId().equals(g.getId())) {
                                            if (!gLite.getName().equals(g.getName())
                                                    || !gLite.getMembers().equals(g.getMembers())
                                                    || gLite.getType() != g.getType()
                                                    || gLite.getUpdated() != g.getUpdated()) {
                                                DBSQLiteHelper.getInstance(getContext()).updateGroup(g);
                                                somethingUpdated = true;
                                            }
                                            groupMatched = true;
                                        }
                                    }
                                } else {
                                    somethingUpdated = true;
                                }

                                //If the group on the remote db doesn't match any on the local db, create a new row
                                if(!groupMatched){
                                    DBSQLiteHelper.getInstance(getContext()).addNewGroup(g);
                                    somethingUpdated = true;
                                }
                            }
                            return somethingUpdated;
                        }

                        @Override
                        protected void onPostExecute(Boolean updated) {
                            if (updated){
                                refreshGroupList();
                            } else {
                                updateEachGroupsUpdateTime();
                            }
                        }
                    }.execute();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dGroupsRef.addListenerForSingleValueEvent(mGroupsListener);
    }

    private void updateEachGroupsUpdateTime() {

    }

    private void refreshGroupList() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                if(tempList!=null) {
                    tempList.clear();
                    tempList = null;
                }
                tempList = DBSQLiteHelper.getInstance(getContext()).getAllGroups();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                orderList();
                updateEachGroupsUpdateTime();
                mLoadingLayout.setVisibility(View.GONE);

                if(mInvitesCard.getVisibility()!=View.VISIBLE) {
                    fab.setVisibility(View.VISIBLE);
                }
                if(gList.size() == 0){
                    mNoGroupsLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    return;
                }

                if(gList.size() > 0){
                    mNoGroupsLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                mAdapter = new GroupsRecyclerAdapter(gList,  new GroupsRecyclerAdapter.GroupClickListener() {
                    @Override
                    public void onGroupClicked(String groupId, String groupName) {
                        goToGroupCoverActivity(groupId,groupName,false,false);
                    }
                }, new GroupsRecyclerAdapter.GroupLongClickListener() {
                    @Override
                    public void onGroupLongClicked(final String groupId, String groupName, final GroupLite g) {
                        DatabaseReference groupRef = db.getReference(Constants.GROUPS).child(groupId).child(Constants.GROUP_CREATOR);
                        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                new AsyncTask<Void, Void, String>() {
                                    @Override
                                    protected String doInBackground(Void... voids) {
                                        return dataSnapshot.getValue(String.class);
                                    }

                                    @Override
                                    protected void onPostExecute(String s) {
                                        boolean creator = false;
                                        if (s.equals(iMyUserId)) {
                                            creator = true;
                                        }
                                        InfoDialogs.createLeaveGroupDialog(getActivity(), groupId, iMyUserId, g, creator).show();
                                    }
                                }.execute();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }
        }.execute();
    }

    //==============================================================================================
    //                  Create a new group
    //==============================================================================================
    private void createGroupDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertView = inflater.inflate(R.layout.dialog_new_group,null);

        mLinearButton = (RelativeLayout) alertView.findViewById(R.id.new_group_linear_button);
        mMultiverseButton = (RelativeLayout) alertView.findViewById(R.id.new_group_multiverse_button);
        TextView linearText = (TextView)alertView.findViewById(R.id.new_group_linear_text);
        TextView multiText = (TextView)alertView.findViewById(R.id.new_group_multi_text);
        TextView title = (TextView)alertView.findViewById(R.id.new_group_dialog_title);
        linearText.setTypeface(typeface);
        multiText.setTypeface(typeface);
        title.setTypeface(typeface);

        View.OnClickListener linearMultiSwitch = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.new_group_linear_button:
                        mOneOrTwo = 1;
                        mLinearButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        mMultiverseButton.setBackgroundColor(Color.parseColor("#00f9f9f9"));
                        Log.d(TAG, "onClick: " + mOneOrTwo);
                        break;
                    case R.id.new_group_multiverse_button:
                        mOneOrTwo = 2;
                        mMultiverseButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        mLinearButton.setBackgroundColor(Color.parseColor("#00f9f9f9"));
                        Log.d(TAG, "onClick: " + mOneOrTwo);
                        break;
                    default:
                        return;
                }
            }
        };

        mLinearButton.setOnClickListener(linearMultiSwitch);
        mMultiverseButton.setOnClickListener(linearMultiSwitch);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(alertView)
                .setPositiveButton("Good to go!",null)
                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

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
                        Dialog d = (Dialog) dialogInterface;
                        EditText theEditText = (EditText)d.findViewById(R.id.new_group_dialog_edit);
                        String temp = theEditText.getText().toString();
                        String groupName = temp.replace("\n","");
                        if(groupName.length() < 1){
                            theEditText.setError("Must be longer than that");
                        } else {
                            mNewGroupName = null;
                            mNewGroupName = groupName;
                            new AsyncTask<Void,Void,String>(){
                                @Override
                                protected void onPreExecute() {
                                    long time = System.currentTimeMillis()/1000;
                                    mCurrentTime = (int)time;
                                }

                                @Override
                                protected String doInBackground(Void... voids) {
                                    DatabaseReference groupsRef = db.getReference(Constants.GROUPS);
                                    String groupId = groupsRef.push().getKey();
                                    DatabaseReference thisGroupRef = db.getReference(Constants.GROUPS).child(groupId);
                                    thisGroupRef.child(Constants.GROUP_TYPE).setValue(mOneOrTwo);
                                    thisGroupRef.child(Constants.GROUP_NAME).setValue(mNewGroupName);
                                    thisGroupRef.child(Constants.GROUP_MEMBERS).child(iMyUserId).setValue(Me.getInstance().getUsername());
                                    thisGroupRef.child(Constants.GROUP_UPDATED).setValue(mCurrentTime);
                                    thisGroupRef.child(Constants.GROUP_CREATOR).setValue(iMyUserId);
                                    thisGroupRef.child(Constants.INVITE_PERMISSIONS).setValue(0);
                                    thisGroupRef.child(Constants.TITLE_PERMISSIONS).setValue(0);
                                    thisGroupRef.child(Constants.COVER_PERMISSIONS).setValue(0);
                                    thisGroupRef.child(Constants.BLACK_OR_WHITE).setValue(0);


                                    String s = Constants.DB_NULL;
                                    if(mOneOrTwo==1) {
                                        OnePathPage p = new OnePathPage(
                                                "-1",
                                                Constants.FIRST_PAGE_ID,
                                                s, s, s, s, s, s, s, s, s, s);
                                        thisGroupRef.child(Constants.FIRST_PAGE_ID).setValue(p);

                                    } else if (mOneOrTwo == 2){
                                        TwoPathPage p = new TwoPathPage(
                                                Constants.FIRST_PAGE_ID,
                                                "-1",
                                                s,s,s,s,s,s,s,s,s,s,s,s,s,s,0);
                                        thisGroupRef.child(Constants.FIRST_PAGE_ID).setValue(p);
                                    }

                                    GroupLite gl = new GroupLite(groupId,mNewGroupName,mOneOrTwo,Me.getInstance().getUsername(),mCurrentTime);
                                    DBSQLiteHelper.getInstance(getContext()).addNewGroup(gl);

                                    DatabaseReference myUserRef = db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.GROUPS).child(groupId);
                                    myUserRef.child(Constants.GROUP_NAME).setValue(mNewGroupName);
                                    myUserRef.child(Constants.GROUP_TYPE).setValue(mOneOrTwo);
                                    myUserRef.child(Constants.GROUP_MEMBERS).child(iMyUserId).setValue(Me.getInstance().getUsername());
                                    myUserRef.child(Constants.GROUP_UPDATED).setValue(mCurrentTime);
                                    return groupId;
                                }

                                @Override
                                protected void onPostExecute(String groupId) {
                                    Intent intent = new Intent(getActivity(),GroupCoverActivity.class);
                                    intent.putExtra(Constants.GROUP_NAME,mNewGroupName);
                                    intent.putExtra(Constants.GROUP_INTENT,groupId);
                                    startActivity(intent);
                                    getActivity().finish();
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

    private void goToGroupCoverActivity(String groupId, String groupName, boolean visiting, boolean accepted) {
        Intent intent = new Intent(getActivity(),GroupCoverActivity.class);
        intent.putExtra(Constants.GROUP_INTENT,groupId);
        intent.putExtra(Constants.GROUP_NAME,groupName);
        intent.putExtra(Constants.VISITING_INTENT,visiting);
        intent.putExtra(Constants.ACCEPTED_INTENT,accepted);
        startActivity(intent);
    }

    //==============================================================================================
    //                  Gets your invites to groups
    //==============================================================================================
    private void invitesListener(final View view) {
        DatabaseReference invitesRef = db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.INVITES_REF);
        invitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mInviteIdList = new ArrayList<>();
                            mInviteNameList = new ArrayList<>();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                mInviteIdList.add(ds.getKey());
                                mInviteNameList.add(ds.getValue(String.class));
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if(mInviteIdList.size()>0){
                                showInvites(view);
                            }
                        }
                    }.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void showInvites(final View view) {
        mInvitesCard.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        TextView inviteTitle = (TextView)view.findViewById(R.id.invites_title);
        Typeface typeface= Typeface.createFromAsset(view.getContext().getAssets(), "fonts/conform.TTF");
        inviteTitle.setTypeface(typeface);

        mInvitesRecycler = (RecyclerView)view.findViewById(R.id.invites_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,true);
        mInviteAdapter = new InvitesRecyclerAdapter(mInviteIdList, mInviteNameList,
                new InvitesRecyclerAdapter.AcceptListener() {
                    @Override
                    public void OnAcceptClicked(final String groupId, final String groupName) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.GROUPS).child(groupId).setValue(groupName);
                                db.getReference(Constants.GROUPS).child(groupId).child(Constants.GROUP_MEMBERS).child(iMyUserId).setValue(iMyUserName);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(view.getContext(), "Welcome to the group!", Toast.LENGTH_SHORT).show();
                                goToGroupCoverActivity(groupId,groupName,false,true);
                            }
                        }.execute();
                    }
                }, new InvitesRecyclerAdapter.DenyListener() {
            @Override
            public void OnDenyClicked(String groupId, String groupName) {
                db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.INVITES_REF).child(groupId).setValue(null);
                mInviteIdList.remove(groupId);
                mInviteNameList.remove(groupName);
                mInviteAdapter.notifyDataSetChanged();
                if(mInviteIdList.size() == 0){
                    fab.setVisibility(View.VISIBLE);
                    mInvitesCard.setVisibility(View.GONE);
                }
                Toast.makeText(view.getContext(), "Denied", Toast.LENGTH_SHORT).show();
            }
        }, new InvitesRecyclerAdapter.InviteGroupListener() {
            @Override
            public void OnGroupNameClicked(String groupId, String groupName) {
                goToGroupCoverActivity(groupId,groupName,true,false);
            }
        });

        mInvitesRecycler.setLayoutManager(llm);
        mInvitesRecycler.setAdapter(mInviteAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
