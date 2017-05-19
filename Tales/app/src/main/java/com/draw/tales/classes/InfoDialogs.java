package com.draw.tales.classes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.R;
import com.draw.tales.groups.DBSQLiteHelper;
import com.draw.tales.groups.GroupsRecyclerAdapter;
import com.draw.tales.main.MainActivity;
import com.draw.tales.user.OtherUserActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by KorbBookProReturns on 4/25/17.
 */

public class InfoDialogs {

    //==============================================================================================
    //
    //                                   PAGE INFO DIALOG
    //
    //==============================================================================================

    public static Dialog createPageInfoDialog(final Activity context,
                                              final String imageUserId,
                                              String userName,
                                              final String pageId,
                                              final String type,
                                              final String story,
                                              final int oneOrTwo,
                                              final String firstPromptId,
                                              final String firstPromptName,
                                              final String secondPromptId,
                                              final String secondPromptName){
        LayoutInflater inflater = context.getLayoutInflater();
        View alertView = inflater.inflate(R.layout.dialog_two_info,null);

        //Main
        final RelativeLayout mainLayout = (RelativeLayout) alertView.findViewById(R.id.dti_main_layout);
        TextView username = (TextView) alertView.findViewById(R.id.dti_username);
        TextView report = (TextView) alertView.findViewById(R.id.dti_report);
        TextView illustrated = (TextView) alertView.findViewById(R.id.dti_illustrated);

        TextView leftBy = (TextView) alertView.findViewById(R.id.dti_left_by);
        TextView rightBy = (TextView) alertView.findViewById(R.id.dti_right_by);
        TextView leftName = (TextView) alertView.findViewById(R.id.dti_left_name);
        TextView rightName = (TextView) alertView.findViewById(R.id.dti_right_name);
        TextView nextBy = (TextView) alertView.findViewById(R.id.dti_next_by);
        TextView nextName = (TextView) alertView.findViewById(R.id.dti_next_name);

        if(oneOrTwo==1){
            leftBy.setVisibility(View.GONE);
            leftName.setVisibility(View.GONE);
            rightBy.setVisibility(View.GONE);
            rightName.setVisibility(View.GONE);
            nextBy.setVisibility(View.VISIBLE);
            nextName.setVisibility(View.VISIBLE);

            if(!firstPromptId.equals(Constants.DB_NULL)) {
                nextName.setText(firstPromptName);
            } else {
                nextName.setVisibility(View.GONE);
            }

        } else {
            if(!firstPromptId.equals(Constants.DB_NULL)) {
                leftName.setText(firstPromptName);
            } else {
                leftName.setVisibility(View.GONE);
            }
            if(!secondPromptId.equals(Constants.DB_NULL)){
                rightName.setText(secondPromptName);
            } else {
                rightName.setVisibility(View.GONE);
            }        }

        //Report
        final LinearLayout reportLayout = (LinearLayout) alertView.findViewById(R.id.dti_report_layout);
        TextView why = (TextView) alertView.findViewById(R.id.dti_why);
        final EditText edit = (EditText) alertView.findViewById(R.id.dti_edit);
        final Button submit = (Button) alertView.findViewById(R.id.dti_button);

        Typeface tf = Typeface.createFromAsset(context.getAssets(),Constants.FONT);
        username.setTypeface(tf);
        report.setTypeface(tf);
        illustrated.setTypeface(tf);
        why.setTypeface(tf);
        leftBy.setTypeface(tf);
        leftName.setTypeface(tf);
        rightBy.setTypeface(tf);
        rightName.setTypeface(tf);
        nextBy.setTypeface(tf);
        nextName.setTypeface(tf);

        username.setText(userName);

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OtherUserActivity.class);
                intent.putExtra(Constants.USER_INTENT,imageUserId);
                context.startActivity(intent);
            }
        });

        leftName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OtherUserActivity.class);
                intent.putExtra(Constants.USER_INTENT,firstPromptId);
                context.startActivity(intent);
            }
        });

        rightName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OtherUserActivity.class);
                intent.putExtra(Constants.USER_INTENT,secondPromptId);
                context.startActivity(intent);
            }
        });

        nextName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OtherUserActivity.class);
                intent.putExtra(Constants.USER_INTENT,firstPromptId);
                context.startActivity(intent);
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.setVisibility(View.GONE);
                reportLayout.setVisibility(View.VISIBLE);
                edit.requestFocus();
            }
        });

        final AlertDialog infoDialog = new AlertDialog.Builder(context)
                .setView(alertView)
                .create();

        infoDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String theReport = edit.getText().toString();
                        if(theReport.length() < 5){
                            edit.setError("Give us a reason!");
                        } else {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected void onPreExecute() {
                                    submit.setClickable(false);
                                }

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.REPORTS);
                                    String key = ref.push().getKey();
                                    ref.child(key).child(Constants.R_PAGE_ID).setValue(pageId);
                                    ref.child(key).child(Constants.R_MESSAGE).setValue(theReport);
                                    ref.child(key).child(Constants.R_TYPE).setValue(type);
                                    ref.child(key).child(Constants.R_STORY).setValue(story);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    Toast.makeText(context, "Report submitted. Thanks for looking out!", Toast.LENGTH_SHORT).show();
                                    infoDialog.dismiss();
                                }
                            }.execute();
                        }
                    }
                });
            }
        });
        return infoDialog;
    }


    //==============================================================================================
    //
    //                          INVITE USER TO WHICH GROUP DIALOG
    //
    //==============================================================================================

    public static Dialog createWhichGroupDialog(final Activity context, final List<GroupLite> groupList, final String userId){
        LayoutInflater inflater = context.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_which_group,null);

        final RecyclerView rv = (RecyclerView) alertView.findViewById(R.id.dwg_recycler);
        final TextView ex = (TextView) alertView.findViewById(R.id.dwg_ex_out);
        TextView title = (TextView) alertView.findViewById(R.id.dwg_which_group);

        Typeface tf = Typeface.createFromAsset(context.getAssets(),Constants.FONT);
        title.setTypeface(tf);
        ex.setTypeface(tf);

        final AlertDialog whichDialog = new AlertDialog.Builder(context)
                .setView(alertView)
                .create();

        whichDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                LinearLayoutManager llm = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
                rv.setLayoutManager(llm);
                GroupsRecyclerAdapter adapter = new GroupsRecyclerAdapter(groupList, new GroupsRecyclerAdapter.GroupClickListener() {
                    @Override
                    public void onGroupClicked(final String groupId, final String groupName) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF).child(userId).child(Constants.INVITES_REF).child(groupId);
                                userRef.setValue(groupName);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(context, "Invited!", Toast.LENGTH_SHORT).show();
                                whichDialog.dismiss();
                            }
                        }.execute();
                    }
                }, new GroupsRecyclerAdapter.GroupLongClickListener() {
                    @Override
                    public void onGroupLongClicked(String groupId, String groupName, GroupLite g) {

                    }
                });
                rv.setAdapter(adapter);

                ex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        whichDialog.dismiss();
                    }
                });
            }
        });

        return whichDialog;
    }


    //==============================================================================================
    //
    //                                  LEAVE GROUP DIALOG
    //
    //==============================================================================================
    public static Dialog createLeaveGroupDialog(final Activity context, final String groupId, final String myUserId, final GroupLite g, final boolean creator){
        LayoutInflater inflater = context.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_go_back,null);

        final AlertDialog leaveDialog = new AlertDialog.Builder(context)
                .setView(alertView)
                .setPositiveButton("Yes, leave", null)
                .setNegativeButton("No, stay!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        leaveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                TextView tv = (TextView) alertView.findViewById(R.id.go_back_dialog_description);
                TextView title = (TextView) alertView.findViewById(R.id.go_back_dialog_title);

                title.setText("Leave this group?");
                String leavingText = "Are you sure you want to leave ~~~" + g.getName() + "~~~?";
                tv.setText(leavingText);

                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected void onPreExecute() {
                                TextView tv = (TextView) alertView.findViewById(R.id.go_back_dialog_description);
                                tv.setText("Leaving group... hang tight!");
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();

                                DatabaseReference thisGroupRef = db
                                        .getReference(Constants.GROUPS).child(groupId);

                                thisGroupRef.child(Constants.GROUP_MEMBERS).child(myUserId).setValue(null);

                                if(creator) {
                                    thisGroupRef.child(Constants.TITLE_PERMISSIONS).setValue(1);
                                    thisGroupRef.child(Constants.COVER_PERMISSIONS).setValue(1);
                                    thisGroupRef.child(Constants.INVITE_PERMISSIONS).setValue(1);
                                }

                                DatabaseReference usersRef = db.getReference(Constants.USERS_REF).child(myUserId);
                                usersRef.child(Constants.GROUPS).child(groupId).setValue(null);

                                DBSQLiteHelper.getInstance(context).deleteGroup(g);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constants.GROUP_TO_MAIN_INTENT,true);
                                context.startActivity(intent);
                                leaveDialog.dismiss();
                            }
                        }.execute();
                    }
                });
            }
        });

        return leaveDialog;
    }


    //==============================================================================================
    //
    //                              EDIT USER DESCRIPTION DIALOG
    //
    //==============================================================================================
    public static Dialog createUserDescriptionDialog(final Activity context, final String myUserId){
        LayoutInflater inflater = context.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_edit_description,null);

        TextView title = (TextView) alertView.findViewById(R.id.description_edit_text);
        Typeface tf = Typeface.createFromAsset(context.getAssets(),Constants.FONT);
        title.setTypeface(tf);

        final AlertDialog descriptionDialog = new AlertDialog.Builder(context)
                .setView(alertView)
                .setPositiveButton("Save",null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        descriptionDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText et = (EditText) alertView.findViewById(R.id.description_edit_edit);
                        String temp = et.getText().toString();
                        final String description = temp.replace("\n","");
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                DatabaseReference userDescriptionRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF).child(myUserId).child(Constants.USER_DESCRIPTION);
                                userDescriptionRef.setValue(description);
                                SharedPreferences sp = context.getSharedPreferences(Constants.SHARED_PREF,Context.MODE_PRIVATE);
                                sp.edit().putString(Constants.MY_USER_DESCRIPTION,description).commit();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constants.USER,true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                descriptionDialog.dismiss();
                            }
                        }.execute();
                    }
                });
            }
        });

        return descriptionDialog;
    }

}
