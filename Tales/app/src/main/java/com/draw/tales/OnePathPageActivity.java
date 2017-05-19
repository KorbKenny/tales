package com.draw.tales;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.classes.Constants;
import com.draw.tales.classes.InfoDialogs;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.OnePathPage;
import com.draw.tales.classes.PageImageView;
import com.draw.tales.drawing.DrawingActivity;
import com.draw.tales.groups.GroupCoverActivity;
import com.draw.tales.main.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class OnePathPageActivity extends AppCompatActivity {

    private static final String TAG = "Hello";
    private RelativeLayout mLoadingLayout, mEmptyLayout, mFullLayout;
    private TextView mNextButton, mImageText;
    private PageImageView mImage;
    private OnePathPage mThisPage;

    private ImageView mHome, mInfo, mBookmarksButton;

    private FirebaseDatabase db;
    private DatabaseReference dThisStoryRef;
    private ValueEventListener mPageListener, mBeingWorkedOnListener;

    private boolean beingWorkedOn = true;
    private boolean nextIsEmpty = false;

    private String iMyUserId, iThisPageId, mType, mStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_path_page);

        simpleSetup();

        getThisPage();

        workingOnListener();

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nextIsEmpty){
                    Intent intent = new Intent(OnePathPageActivity.this,OnePathPageActivity.class);
                    intent.putExtra(Constants.PAGE_ID_INTENT,mThisPage.getNextPageId());
                    intent.putExtra(Constants.TYPE_INTENT,mType);
                    intent.putExtra(Constants.STORY_INTENT,mStory);
                    startActivity(intent);
                    finish();
                    return;
                }

                if(!mThisPage.getImageUser().equals(iMyUserId)){
                    createNextDialog();
                } else {
                    Toast.makeText(OnePathPageActivity.this, "Somebody else should do this", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //==============================================================================================
    //                           Simple Setup
    //==============================================================================================
    private void simpleSetup() {
        //  Layouts
        mLoadingLayout = (RelativeLayout)findViewById(R.id.one_path_loading_layout);
        mEmptyLayout = (RelativeLayout)findViewById(R.id.one_path_empty);
        mFullLayout = (RelativeLayout)findViewById(R.id.one_path_full);

        //  Text & Image
        mImage = (PageImageView)findViewById(R.id.one_path_image);
        mImageText = (TextView)findViewById(R.id.one_path_image_text);
        mNextButton = (TextView)findViewById(R.id.one_path_button_next);

        //  Toolbar
        mHome = (ImageView)findViewById(R.id.one_path_toolbar_home);
        mInfo = (ImageView)findViewById(R.id.one_path_toolbar_info);
        mBookmarksButton = (ImageView)findViewById(R.id.one_path_toolbar_bookmark);
        toolbarSetup();

        //  Intents
        mType = getIntent().getStringExtra(Constants.TYPE_INTENT);
        mStory = getIntent().getStringExtra(Constants.STORY_INTENT);
        iThisPageId = getIntent().getStringExtra(Constants.PAGE_ID_INTENT);

        TextView emptyButton = (TextView)findViewById(R.id.one_path_empty_draw_button_text);
        TextView emptyText = (TextView)findViewById(R.id.one_path_this_page_is_empty);

        Typeface typeface= Typeface.createFromAsset(this.getAssets(), Constants.FONT);
        mImageText.setTypeface(typeface);
        mNextButton.setTypeface(typeface);
        emptyButton.setTypeface(typeface);
        emptyText.setTypeface(typeface);

        //  Database
        db = FirebaseDatabase.getInstance();
        dThisStoryRef = db.getReference(mType).child(mStory);

        //  User
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }
        iMyUserId = Me.getInstance().getUserId();
    }

    private void toolbarSetup() {
        mBookmarksButton.setVisibility(View.GONE);
        mBookmarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBookmarkDialog();
            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mType.equals(Constants.GROUPS)){
                    Intent intent = new Intent(OnePathPageActivity.this,MainActivity.class);
                    intent.putExtra(Constants.GROUP_TO_MAIN_INTENT,true);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OnePathPageActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThisPage!=null) {
                    if (!mThisPage.getImageUser().equals(Constants.DB_NULL)) {
                        InfoDialogs.createPageInfoDialog(OnePathPageActivity.this,
                                mThisPage.getImageUser(),
                                mThisPage.getImageUserName(),
                                iThisPageId,
                                mType,
                                mStory,
                                1,
                                mThisPage.getNextTextUser(),
                                mThisPage.getNextTextUserName(),
                                null,null)
                                .show();
                    }
                }
            }
        });
    }

    private void createBookmarkDialog() {

    }

    //==============================================================================================
    //                              Gets This Page
    //==============================================================================================
    private void getThisPage() {
        mPageListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null){
                            mThisPage = dataSnapshot.getValue(OnePathPage.class);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        setViewsAfterLoading();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    //==============================================================================================
    //                  Set Views After Loading From DB
    //==============================================================================================
    private void setViewsAfterLoading() {
        if(mThisPage.getImagePath().equals(Constants.DB_NULL)){
            mEmptyLayout.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);

            if(!mThisPage.getBeingWorkedOn().equals(Constants.NEGATIVE_ONE)){
                beingWorkedOn = true;
            } else {
                beingWorkedOn = false;
            }

            mEmptyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkToGoToDrawingActivity();
                }
            });
        } else {
            Picasso.with(OnePathPageActivity.this).load(mThisPage.getImagePath()).placeholder(R.drawable.loadingpageimage).into(mImage);
            mImageText.setText(mThisPage.getImageText());
            mFullLayout.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);

            if(!mThisPage.getNextText().equals(Constants.DB_NULL)){
                mNextButton.setText(mThisPage.getNextText());
                mNextButton.setTypeface(mNextButton.getTypeface(), Typeface.BOLD);
            } else {
                nextIsEmpty = true;
                mNextButton.setText(R.string.empty_buttons);
                mNextButton.setShadowLayer(0,0,0,0);
                mNextButton.setTextColor(Color.BLACK);
            }
        }

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        sp.edit().putString(mStory,mThisPage.getThisPageId()).apply();


    }

    //==============================================================================================
    //                  Next Page Dialog
    //==============================================================================================
    private void createNextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_one_next,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingLayout.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialogInterface;
                        EditText theEditText = (EditText)view.findViewById(R.id.next_edit);
                        String nextText = theEditText.getText().toString();

                        if(nextText.length() > 2){
                            dThisStoryRef.child(iThisPageId).child(Constants.NEXT_TEXT).setValue(nextText);
                            nextIsEmpty = false;
                            mThisPage.setNextTextUser(iMyUserId);
                            mNextButton.setText(nextText);
                        } else {
                            Toast.makeText(OnePathPageActivity.this, "Make it longer than that", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        String nextId = dThisStoryRef.push().getKey();
                        dThisStoryRef.child(iThisPageId).child(Constants.NEXT_PAGE_ID).setValue(nextId);
                        dThisStoryRef.child(iThisPageId).child(Constants.NEXT_TEXT_USER).setValue(iMyUserId);
                        dThisStoryRef.child(iThisPageId).child(Constants.NEXT_TEXT_USER_NAME).setValue(Me.getInstance().getUsername());
                        mThisPage.setNextPageId(nextId);

                        DatabaseReference notifyRef = db.getReference(Constants.NOTIFICATIONS).child(mThisPage.getImageUser());
                        notifyRef.child(Constants.N_PAGE_ID).setValue(iThisPageId);
                        notifyRef.child(Constants.N_TYPE).setValue(mType);
                        notifyRef.child(Constants.N_STORY).setValue(mStory);
                        notifyRef.child(Constants.N_ONE_OR_TWO).setValue(1);

                        createNextPage(nextId);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mLoadingLayout.setVisibility(View.GONE);
                    }
                }.execute();
            }
        }).create().show();
    }

    //==============================================================================================
    //                  Create Next Page
    //==============================================================================================
    private void createNextPage(String nextPageId) {
        String s = Constants.DB_NULL;
        OnePathPage p = new OnePathPage(
                "-1",
                nextPageId,
                iThisPageId,
                iMyUserId,
                s,s,s,s,s,s,s,s);

        dThisStoryRef.child(nextPageId).setValue(p);
    }


    //==============================================================================================
    //                  Listener to see if this page is currently being worked on
    //==============================================================================================
    private void workingOnListener() {
        mBeingWorkedOnListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            return dataSnapshot.getValue(String.class);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (!s.equals(Constants.NEGATIVE_ONE)) {
                                beingWorkedOn = true;
                            } else {
                                beingWorkedOn = false;
                            }
                        }
                    }.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    //==============================================================================================
    //                          Drawing Activity Stuff
    //==============================================================================================
    private void checkToGoToDrawingActivity() {
        int currentTime = (int)(System.currentTimeMillis()/1000);
        Log.d(TAG, "checkToGoToDrawingActivity: " + currentTime + " - " + mThisPage.getBeingWorkedOn() + " = "
                + (currentTime - Integer.parseInt(mThisPage.getBeingWorkedOn())));
        if(mThisPage.getFromUser().equals(iMyUserId)){
            Toast.makeText(this, "Somebody else should draw this page!", Toast.LENGTH_SHORT).show();
        } else {
            if(!beingWorkedOn) {
                dThisStoryRef.child(iThisPageId).child(Constants.BEING_WORKED_ON).setValue(String.valueOf(currentTime));
                goToDrawingActivity(currentTime);
            } else {
                if(currentTime - Integer.parseInt(mThisPage.getBeingWorkedOn()) > 600){
                    dThisStoryRef.child(iThisPageId).child(Constants.BEING_WORKED_ON).setValue(String.valueOf(currentTime));
                    goToDrawingActivity(currentTime);
                } else {
                    Toast.makeText(this, "Somebody is working on this at the moment!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void goToDrawingActivity(int currentTime) {
        Intent intent = new Intent(OnePathPageActivity.this, DrawingActivity.class);
        intent.putExtra(Constants.FROM_INTENT, mThisPage.getFromPageId());
        intent.putExtra(Constants.PAGE_ID_INTENT, mThisPage.getThisPageId());
        intent.putExtra(Constants.TYPE_INTENT,mType);
        intent.putExtra(Constants.STORY_INTENT,mStory);
        intent.putExtra(Constants.CURRENT_TIME_INTENT,currentTime);
        intent.putExtra(Constants.ONE_OR_TWO_INTENT,1);
        startActivity(intent);
        finish();
    }

    //==============================================================================================
    //                               On Back Press
    //==============================================================================================
    @Override
    public void onBackPressed() {
        if(mThisPage!=null) {
            if(!mThisPage.getFromPageId().equals(Constants.DB_NULL)) {
                if(mType.equals(Constants.GROUPS)){
                    Intent intent = new Intent(OnePathPageActivity.this, OnePathPageActivity.class);
                    intent.putExtra(Constants.TYPE_INTENT, mType);
                    intent.putExtra(Constants.STORY_INTENT, mStory);
                    intent.putExtra(Constants.PAGE_ID_INTENT, mThisPage.getFromPageId());
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OnePathPageActivity.this, OnePathPageActivity.class);
                    intent.putExtra(Constants.TYPE_INTENT, mType);
                    intent.putExtra(Constants.STORY_INTENT, mStory);
                    intent.putExtra(Constants.PAGE_ID_INTENT, mThisPage.getFromPageId());
                    startActivity(intent);
                    finish();
                }
            } else {
                if(mType.equals(Constants.GROUPS)){
                    Intent intent = new Intent(OnePathPageActivity.this, GroupCoverActivity.class);
                    intent.putExtra(Constants.GROUP_INTENT,mStory);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OnePathPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }


    //==============================================================================================
    //                           Activity Lifecycle Overrides
    //==============================================================================================
    @Override
    protected void onPause() {
        super.onPause();
        dThisStoryRef.child(iThisPageId).removeEventListener(mPageListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dThisStoryRef.child(iThisPageId).addValueEventListener(mPageListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dThisStoryRef.child(iThisPageId).child(Constants.BEING_WORKED_ON).addValueEventListener(mBeingWorkedOnListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBeingWorkedOnListener!=null){
            dThisStoryRef.child(iThisPageId).child(Constants.BEING_WORKED_ON).removeEventListener(mBeingWorkedOnListener);
        }
    }
}
