package com.draw.tales;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.classes.Bookmark;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.InfoDialogs;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.PageImageView;
import com.draw.tales.classes.TwoPathPage;
import com.draw.tales.drawing.DrawingActivity;
import com.draw.tales.groups.DBSQLiteHelper;
import com.draw.tales.groups.GroupCoverActivity;
import com.draw.tales.main.MainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TwoPathPageActivity extends AppCompatActivity {

    private static final String TAG = "Hello";
    private RelativeLayout mLoadingLayout, mEmptyLayout, mFullLayout, mCantDrawLayout;
    private TextView mLeftButton, mRightButton, mImageText, mEmptyText;

    private PageImageView mImage;
    private TwoPathPage mThisPage;

    private FirebaseDatabase db;
    private DatabaseReference dThisStoryRef, dBookmarkRef;
    private ValueEventListener mPageListener;
    private ValueEventListener mBeingWorkedOnListener;
    private ChildEventListener mNextLeftImageListener, mNextRightImageListener;
    private List<Bookmark> mBookmarks;

    private boolean leftIsEmpty = false;
    private boolean rightIsEmpty = false;
    private boolean beingWorkedOn = true;
    private boolean bookmarksLoaded = false;

    private String iMyUserId, iThisPageId, mType, mStory, mClickedPrompt;
    private String mBookimage1,mBookimage2,mBookimage3,mBookimage4,mBookmark1,mBookmark2,mBookmark3,mBookmark4;

    private ImageView mHome, mInfo, mComments, mBookmarksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_path_page);

        simpleSetup();

        getThisPage();

        getBookmarks();

        workingOnListener();

//        getPageInfo();


        //=================================
        //   Click Listener for Buttons
        //=================================
        View.OnClickListener leftRightClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmpty;
                String leftOrRight, nextLeftOrRight;

                switch (view.getId()){
                    case R.id.two_path_button_left:
                        isEmpty = leftIsEmpty;
                        leftOrRight = Constants.LEFT;
                        nextLeftOrRight = mThisPage.getLeftNextPageId();
                        mClickedPrompt = mThisPage.getLeftText();
                        break;
                    case R.id.two_path_button_right:
                        isEmpty = rightIsEmpty;
                        leftOrRight = Constants.RIGHT;
                        nextLeftOrRight = mThisPage.getRightNextPageId();
                        mClickedPrompt = mThisPage.getRightText();
                        break;
                    default:
                        return;
                }

                //=================================
                //  Opens the next page
                //=================================
                if(!isEmpty){
                    Intent intent = new Intent(TwoPathPageActivity.this,TwoPathPageActivity.class);
                    intent.putExtra(Constants.PAGE_ID_INTENT,nextLeftOrRight);
                    intent.putExtra(Constants.TYPE_INTENT,mType);
                    intent.putExtra(Constants.STORY_INTENT,mStory);
                    intent.putExtra(Constants.CLICKED_PROMPT_INTENT,mClickedPrompt);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom,R.anim.out_toward_top);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button or
                //  denies you access
                //=================================
                if(!mThisPage.getImageUser().equals(iMyUserId)
                        && !mThisPage.getLeftTextUser().equals(iMyUserId)
                        && !mThisPage.getRightTextUser().equals(iMyUserId)){
                    createLeftOrRightDialog(leftOrRight);
                }
//                else {
//                    Toast.makeText(TwoPathPageActivity.this, "You've worked hard enough on this page", Toast.LENGTH_SHORT).show();
//                }
            }
        };

        mLeftButton.setOnClickListener(leftRightClickListener);
        mRightButton.setOnClickListener(leftRightClickListener);
    }

    //==============================================================================================
    //                  Simple Setup
    //==============================================================================================
    private void simpleSetup() {
        //  Intents
        mType = getIntent().getStringExtra(Constants.TYPE_INTENT);
        mStory = getIntent().getStringExtra(Constants.STORY_INTENT);
        iThisPageId = getIntent().getStringExtra(Constants.PAGE_ID_INTENT);
        mClickedPrompt = getIntent().getStringExtra(Constants.CLICKED_PROMPT_INTENT);

        //  Layouts
        mLoadingLayout = (RelativeLayout)findViewById(R.id.two_path_loading_layout);
        mEmptyLayout = (RelativeLayout)findViewById(R.id.two_path_empty);
        mFullLayout = (RelativeLayout)findViewById(R.id.two_path_full);
        mCantDrawLayout = (RelativeLayout)findViewById(R.id.two_path_cant_draw);

        //  Text & Image
        mImage = (PageImageView)findViewById(R.id.two_path_image);
        mImageText = (TextView)findViewById(R.id.two_path_image_text);
        mLeftButton = (TextView)findViewById(R.id.two_path_button_left);
        mRightButton = (TextView)findViewById(R.id.two_path_button_right);

        TextView emptyButton = (TextView)findViewById(R.id.two_path_empty_draw_button_text);
        TextView cantDrawText = (TextView)findViewById(R.id.two_path_cant_draw_text);
        mEmptyText = (TextView)findViewById(R.id.two_path_this_page_is_empty);

        Typeface typeface= Typeface.createFromAsset(this.getAssets(), Constants.FONT);
        mImageText.setTypeface(typeface);
        mLeftButton.setTypeface(typeface);
        mRightButton.setTypeface(typeface);
        emptyButton.setTypeface(typeface);
        mEmptyText.setTypeface(typeface);
        cantDrawText.setTypeface(typeface);

        //  Toolbar
        mHome = (ImageView)findViewById(R.id.two_path_toolbar_home);
        mInfo = (ImageView)findViewById(R.id.two_path_toolbar_info);
//        mComments = (ImageView)findViewById(R.id.two_path_toolbar_comments);
        mBookmarksButton = (ImageView)findViewById(R.id.two_path_toolbar_bookmark);

        toolbarSetup();

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
        if(iMyUserId!=null){
            Log.d(TAG, "simpleSetup: "+iMyUserId);
        }
    }

    private void getPageInfo() {

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
//                DatabaseReference imageUser = db.getReference(Constants.USERS_REF).child()
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mInfo.setClickable(true);
            }
        }.execute();
    }


    //==============================================================================================
    //                  Being Worked On Listener
    //==============================================================================================
    private void workingOnListener() {
        mBeingWorkedOnListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
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
    //                  Dialog Creation
    //==============================================================================================
    private void createLeftOrRightDialog(final String leftOrRight) {
        int layoutToInflate;
        final int editText;
        final TextView textView;
//        final String thisPageOtherUser;

        switch (leftOrRight){
            case Constants.LEFT:
                layoutToInflate = R.layout.dialog_left;
                editText = R.id.left_edit;
                textView = mLeftButton;
//                thisPageOtherUser = mThisPage.getRightTextUser();
                break;
            case Constants.RIGHT:
                layoutToInflate = R.layout.dialog_right;
                editText = R.id.right_edit;
                textView = mRightButton;
//                thisPageOtherUser = mThisPage.getLeftTextUser();
                break;
            default:
                return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(layoutToInflate,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingLayout.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialogInterface;
                        EditText theEditText = (EditText)view.findViewById(editText);
                        String theText = theEditText.getText().toString();
                        theText = theText.replace("\n","");

                        if(leftOrRight.equals(Constants.LEFT)){
                            dThisStoryRef.child(iThisPageId).child(Constants.LEFT_TEXT).setValue(theText);
                            leftIsEmpty = false;
                            mThisPage.setLeftTextUser(iMyUserId);
                        } else {
                            dThisStoryRef.child(iThisPageId).child(Constants.RIGHT_TEXT).setValue(theText);
                            rightIsEmpty = false;
                            mThisPage.setRightTextUser(iMyUserId);
                        }
                        textView.setText(theText);
                        textView.setShadowLayer(3,3,3,Color.BLACK);
                        textView.setTextColor(Color.WHITE);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(!mThisPage.getLeftTextUser().equals(Constants.DB_NULL) && !mThisPage.getLeftTextUser().equals(iMyUserId)){
                            DatabaseReference notifyRef = db.getReference(Constants.NOTIFICATIONS).child(mThisPage.getLeftTextUser());
                            notifyRef.child(Constants.N_PAGE_ID).setValue(iThisPageId);
                            notifyRef.child(Constants.N_TYPE).setValue(mType);
                            notifyRef.child(Constants.N_STORY).setValue(mStory);
                            notifyRef.child(Constants.N_ONE_OR_TWO).setValue(2);
                        }
                        if(!mThisPage.getRightTextUser().equals(Constants.DB_NULL) && !mThisPage.getRightTextUser().equals(iMyUserId)){
                            DatabaseReference notifyRef = db.getReference(Constants.NOTIFICATIONS).child(mThisPage.getRightTextUser());
                            notifyRef.child(Constants.N_PAGE_ID).setValue(iThisPageId);
                            notifyRef.child(Constants.N_TYPE).setValue(mType);
                            notifyRef.child(Constants.N_STORY).setValue(mStory);
                            notifyRef.child(Constants.N_ONE_OR_TWO).setValue(2);
                        }
                        if(leftOrRight.equals(Constants.LEFT)){
                            String nextLeft = dThisStoryRef.push().getKey();
                            dThisStoryRef.child(iThisPageId).child(Constants.LEFT_NEXT_ID).setValue(nextLeft);
                            dThisStoryRef.child(iThisPageId).child(Constants.LEFT_TEXT_USER).setValue(Me.getInstance().getUserId());
                            dThisStoryRef.child(iThisPageId).child(Constants.LEFT_TEXT_USER_NAME).setValue(Me.getInstance().getUsername());
                            mThisPage.setLeftNextPageId(nextLeft);
                            createNextPage(nextLeft);
                        } else {
                            String nextRight = dThisStoryRef.push().getKey();
                            dThisStoryRef.child(iThisPageId).child(Constants.RIGHT_NEXT_ID).setValue(nextRight);
                            dThisStoryRef.child(iThisPageId).child(Constants.RIGHT_TEXT_USER).setValue(Me.getInstance().getUserId());
                            dThisStoryRef.child(iThisPageId).child(Constants.RIGHT_TEXT_USER_NAME).setValue(Me.getInstance().getUsername());
                            mThisPage.setRightNextPageId(nextRight);
                            createNextPage(nextRight);
                        }
                        DatabaseReference notifyRef = db.getReference(Constants.NOTIFICATIONS).child(mThisPage.getImageUser());
                        notifyRef.child(Constants.N_PAGE_ID).setValue(iThisPageId);
                        notifyRef.child(Constants.N_TYPE).setValue(mType);
                        notifyRef.child(Constants.N_STORY).setValue(mStory);
                        notifyRef.child(Constants.N_ONE_OR_TWO).setValue(2);
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
        TwoPathPage p = new TwoPathPage(
                nextPageId,
                "-1",
                iThisPageId,
                iMyUserId,
                s,s,s,s,s,s,s,s,s,s,s,s,0);

        dThisStoryRef.child(nextPageId).setValue(p);

        Log.d(TAG, "createNextPage: " + p.getBeingWorkedOn() + " " + p.getThisPageId());
    }

    //==============================================================================================
    //                  Get This Page
    //==============================================================================================
    private void getThisPage() {
        mPageListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null){
                            mThisPage = dataSnapshot.getValue(TwoPathPage.class);
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

//        dThisStoryRef.child(iThisPageId).addValueEventListener(mPageListener);
    }

    //==============================================================================================
    //                  Set Views After Loading From DB
    //==============================================================================================
    private void setViewsAfterLoading() {
        Log.d(TAG, "setViewsAfterLoading: " + mThisPage.getImagePath());
        if(mThisPage.getImagePath().equals(Constants.DB_NULL)){
            if(mThisPage.getFromUser().equals(iMyUserId)){
                mCantDrawLayout.setVisibility(View.VISIBLE);
                mLoadingLayout.setVisibility(View.GONE);
            } else {
                mEmptyLayout.setVisibility(View.VISIBLE);
                mLoadingLayout.setVisibility(View.GONE);


                if (!mThisPage.getBeingWorkedOn().equals(Constants.NEGATIVE_ONE)) {
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
            }
        } else {
            Picasso.with(TwoPathPageActivity.this).load(mThisPage.getImagePath()).placeholder(R.drawable.loadingpageimage).into(mImage);
            mImageText.setText(mThisPage.getImageText());
            mFullLayout.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);

//            mIllustratedBy.setText("Illustrated by " + mThisPage.getImageUserName() + " ");

            if(!mThisPage.getLeftText().equals(Constants.DB_NULL)){
                mLeftButton.setText(mThisPage.getLeftText());
                mLeftButton.setTypeface(mLeftButton.getTypeface(), Typeface.BOLD);
            } else {
                leftIsEmpty = true;
                if(mThisPage.getImageUser().equals(iMyUserId)||mThisPage.getRightTextUser().equals(iMyUserId)){
                    mLeftButton.setText(R.string.empty_button_if_you_drew);
                } else if (mThisPage.getLeftTextUser().equals(Constants.DB_NULL)){
                    mLeftButton.setText(R.string.empty_buttons);
                }
                mLeftButton.setShadowLayer(0,0,0,0);
                mLeftButton.setTextColor(Color.BLACK);
            }

            if(!mThisPage.getRightText().equals(Constants.DB_NULL)){
                mRightButton.setText(mThisPage.getRightText());
                mRightButton.setTypeface(mRightButton.getTypeface(), Typeface.BOLD);
            } else {
                rightIsEmpty = true;
                if(mThisPage.getImageUser().equals(iMyUserId)||mThisPage.getLeftTextUser().equals(iMyUserId)){
                    mRightButton.setText(R.string.empty_button_if_you_drew);
                } else if (mThisPage.getRightTextUser().equals(Constants.DB_NULL)){
                    mRightButton.setText(R.string.empty_buttons);
                }
                mRightButton.setShadowLayer(0,0,0,0);
                mRightButton.setTextColor(Color.BLACK);
            }
        }

        if(!mThisPage.getImagePath().equals(Constants.DB_NULL)) {
            SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
            sp.edit().putString(Constants.CONTINUE_PAGE_ID, mThisPage.getThisPageId()).apply();
        }
    }

    //==============================================================================================
    //                  Drawing Activity
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

    private void goToDrawingActivity(int currentTime){
        Intent intent = new Intent(TwoPathPageActivity.this, DrawingActivity.class);
        intent.putExtra(Constants.FROM_INTENT, mThisPage.getFromPageId());
        intent.putExtra(Constants.PAGE_ID_INTENT, mThisPage.getThisPageId());
        intent.putExtra(Constants.TYPE_INTENT,mType);
        intent.putExtra(Constants.STORY_INTENT,mStory);
        intent.putExtra(Constants.CURRENT_TIME_INTENT,currentTime);
        intent.putExtra(Constants.ONE_OR_TWO_INTENT,2);
        intent.putExtra(Constants.CLICKED_PROMPT_INTENT,mClickedPrompt);
        startActivity(intent);
        finish();
    }

    private void toolbarSetup() {
        if(mType.equals(Constants.GROUPS)){
            mBookmarksButton.setVisibility(View.GONE);
        }
        mBookmarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mThisPage.getImagePath().equals(Constants.DB_NULL)) {
                    createBookmarkDialog();
                }
            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mType.equals(Constants.GROUPS)){
                    Intent intent = new Intent(TwoPathPageActivity.this,MainActivity.class);
                    intent.putExtra(Constants.GROUP_TO_MAIN_INTENT,true);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(TwoPathPageActivity.this,MainActivity.class);
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
                        InfoDialogs.createPageInfoDialog(TwoPathPageActivity.this, mThisPage.getImageUser(), mThisPage.getImageUserName(), iThisPageId, mType,mStory,
                                2,mThisPage.getLeftTextUser(),mThisPage.getLeftTextUserName(),mThisPage.getRightTextUser(),mThisPage.getRightTextUserName()).show();
                    }
                }
            }
        });
    }

    //==============================================================================================
    //                               On Back Press
    //==============================================================================================
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (mThisPage != null) {
            if (!mThisPage.getFromPageId().equals(Constants.DB_NULL)) {
                Intent intent = new Intent(TwoPathPageActivity.this, TwoPathPageActivity.class);
                intent.putExtra(Constants.TYPE_INTENT, mType);
                intent.putExtra(Constants.STORY_INTENT, mStory);
                intent.putExtra(Constants.PAGE_ID_INTENT, mThisPage.getFromPageId());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_top,R.anim.out_toward_bottom);
                finish();
            } else {
                Log.d(TAG, "onBackPressed: " + mType + Constants.GROUPS);
                if (mType.equals(Constants.GROUPS)) {
                    Intent intent = new Intent(TwoPathPageActivity.this, GroupCoverActivity.class);
                    intent.putExtra(Constants.GROUP_INTENT, mStory);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(TwoPathPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    //==============================================================================================
    //                               Bookmarks
    //==============================================================================================
    private void getBookmarks() {
        dBookmarkRef = db.getReference(Constants.USERS_REF).child(iMyUserId).child(Constants.BOOKMARKS);
        dBookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mBookimage1 = dataSnapshot.child("1").child(Constants.BOOKMARK_IMAGE).getValue(String.class);
                        mBookimage2 = dataSnapshot.child("2").child(Constants.BOOKMARK_IMAGE).getValue(String.class);
                        mBookimage3 = dataSnapshot.child("3").child(Constants.BOOKMARK_IMAGE).getValue(String.class);
                        mBookimage4 = dataSnapshot.child("4").child(Constants.BOOKMARK_IMAGE).getValue(String.class);

                        mBookmark1 = dataSnapshot.child("1").child(Constants.BOOKMARK_ID).getValue(String.class);
                        mBookmark2 = dataSnapshot.child("2").child(Constants.BOOKMARK_ID).getValue(String.class);
                        mBookmark3 = dataSnapshot.child("3").child(Constants.BOOKMARK_ID).getValue(String.class);
                        mBookmark4 = dataSnapshot.child("4").child(Constants.BOOKMARK_ID).getValue(String.class);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        bookmarksLoaded = true;
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void createBookmarkDialog() {
        if(bookmarksLoaded) {
            View alertView = getLayoutInflater().inflate(R.layout.dialog_add_bookmark, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(alertView)
                    .create();

            TextView tv = (TextView) alertView.findViewById(R.id.add_bookmark_text);
            Typeface tf = Typeface.createFromAsset(getAssets(),Constants.FONT);
            tv.setTypeface(tf);

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Dialog d = (Dialog) dialogInterface;
                    PageImageView b1 = (PageImageView) d.findViewById(R.id.bookmark_11);
                    PageImageView b2 = (PageImageView) d.findViewById(R.id.bookmark_22);
                    PageImageView b3 = (PageImageView) d.findViewById(R.id.bookmark_33);
                    PageImageView b4 = (PageImageView) d.findViewById(R.id.bookmark_44);
                    loadPicturesIntoBookmarks(b1,mBookimage1);
                    loadPicturesIntoBookmarks(b2,mBookimage2);
                    loadPicturesIntoBookmarks(b3,mBookimage3);
                    loadPicturesIntoBookmarks(b4,mBookimage4);

                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.bookmark_11:
                                    dBookmarkRef.child("1").child(Constants.BOOKMARK_ID).setValue(iThisPageId);
                                    dBookmarkRef.child("1").child(Constants.BOOKMARK_IMAGE).setValue(mThisPage.getImagePath());
                                    break;
                                case R.id.bookmark_22:
                                    dBookmarkRef.child("2").child(Constants.BOOKMARK_ID).setValue(iThisPageId);
                                    dBookmarkRef.child("2").child(Constants.BOOKMARK_IMAGE).setValue(mThisPage.getImagePath());
                                    break;
                                case R.id.bookmark_33:
                                    dBookmarkRef.child("3").child(Constants.BOOKMARK_ID).setValue(iThisPageId);
                                    dBookmarkRef.child("3").child(Constants.BOOKMARK_IMAGE).setValue(mThisPage.getImagePath());
                                    break;
                                case R.id.bookmark_44:
                                    dBookmarkRef.child("4").child(Constants.BOOKMARK_ID).setValue(iThisPageId);
                                    dBookmarkRef.child("4").child(Constants.BOOKMARK_IMAGE).setValue(mThisPage.getImagePath());
                                    break;
                                default:
                                    return;
                            }

                            Toast.makeText(TwoPathPageActivity.this, "Bookmarked!", Toast.LENGTH_SHORT).show();
                            d.dismiss();
                        }
                    };

                    b1.setOnClickListener(listener);
                    b2.setOnClickListener(listener);
                    b3.setOnClickListener(listener);
                    b4.setOnClickListener(listener);
                }
            });

            dialog.show();
        }
    }

    private void loadPicturesIntoBookmarks(ImageView v, String s){
        if(s != null){
            Picasso.with(TwoPathPageActivity.this).load(s).placeholder(R.drawable.loadingpageimage).into(v);
        }
    }

    public void cacheNextImages(){
        mNextLeftImageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mNextRightImageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

//        if(!mThisPage.getLeftNextPageId().equals(Constants.DB_NULL)) {
//            dThisStoryRef.child(mThisPage.getLeftNextPageId()).addChildEventListener(mNextLeftImageListener);
//        }
//        if(!mThisPage.getRightNextPageId().equals(Constants.DB_NULL)) {
//            dThisStoryRef.child(mThisPage.getRightNextPageId()).addChildEventListener(mNextRightImageListener);
//        }

    }



    @Override
    protected void onPause() {
        super.onPause();
        if(mPageListener!=null) {
            dThisStoryRef.child(iThisPageId).removeEventListener(mPageListener);
        }
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
