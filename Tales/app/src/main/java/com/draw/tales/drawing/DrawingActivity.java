package com.draw.tales.drawing;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.draw.tales.OnePathPageActivity;
import com.draw.tales.R;
import com.draw.tales.TwoPathPageActivity;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.OnePathPage;
import com.draw.tales.classes.TwoPathPage;
import com.draw.tales.groups.GroupCoverActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

public class DrawingActivity extends AppCompatActivity {
    private static final String TAG = "Cool";
    private DrawView mDrawView;
    private DrawViewSquare mDrawViewSquare;
    private CardView mDrawCard;
    private Button mUndoButton, mSaveButton;
    private FirebaseDatabase db;
    private DatabaseReference dPageRef, dPrevPageRef, dCountRef;
    private StorageReference dStorageRef;
    private String iPageId, iUserId, iGroupId, mYellowText, mFromUser, iFromPageId, mStory, mType, mClickedPrompt;
    private TextView mBrushSize, mOpacity, mLoadingBg, mPrevText, mPromptText;
    private ProgressBar mLoadingCircle;
    private ImageView mCurrentColor, mPrevImage;
    private EditText mEditText;
    private Bitmap mBitmapToSave;
    private ValueEventListener mStealCheckListener, mPreviousPageListener, mCountListener;
    private int mOneOrTwo, mPageCount;
    private TwoPathPage mPrevTwoPage;
    private OnePathPage mPrevOnePage;

    private TranslateAnimation mAppearAnimation;

    private RelativeLayout mViewPrevPageLayout, mHidePrevPageLayout;

    private boolean skipFirstTick;
    private boolean fromGroupCover, drawingUserImage;
    private int mStartTime;
    private int mStealCheckTime;
    private int mPrevHeight = 0;

    private CountDownTimer mCountdownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        //=================================
        //  Setup Methods
        //=================================
        simpleSetup();
        changeSizeSeekbar();
        changeOpacitySeekbar();
        setupCountdownTimer();
        getPreviousPage();
//        getPageCount();

        //=================================
        //  Undo Button
        //=================================
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawView.undo();
            }
        });

        //=================================
        //  Save Button
        //=================================
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditText.getText().length()<15){
                    mEditText.setError("Make it longer!");
                } else {
                    createSaveDialog();
                }
            }
        });

        if(!iFromPageId.equals(Constants.DB_NULL)) {

            mViewPrevPageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPrevHeight > 0) {
                        mViewPrevPageLayout.setClickable(false);
                        ValueAnimator ani = ValueAnimator.ofFloat(0, 1.0f);
                        ani.setDuration(400);
                        ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float value = (float) animation.getAnimatedValue();
                                float translateValue = (1.0f - value) * mPrevHeight;
                                mHidePrevPageLayout.setAlpha(1.0f);
//                            mHidePrevPageLayout.setAlpha(value);
                                mHidePrevPageLayout.setTranslationY(-translateValue);
                                if (value == 1.0f) {
                                    mHidePrevPageLayout.setClickable(true);
                                }
                            }
                        });
                        ani.start();

                    }
                }
            });

            mHidePrevPageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewPrevPageLayout.setClickable(true);
                    mHidePrevPageLayout.setClickable(false);
                    ValueAnimator ani = ValueAnimator.ofFloat(1.0f, 0);
                    ani.setDuration(400);
                    ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            float translateValue = (1.0f - value) * mPrevHeight;
//                        mHidePrevPageLayout.setAlpha(value);
                            mHidePrevPageLayout.setTranslationY(-translateValue);
                            if (value == 0) {
                                mHidePrevPageLayout.setAlpha(0);
                            }
                        }
                    });
                    ani.start();
                }
            });
        }
    }

    private void getPageCount() {
        dCountRef = db.getReference(mType).child(mStory).child(Constants.PAGE_COUNT);
        mCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null) {
                            mPageCount = dataSnapshot.getValue(Integer.class);
                        } else {
                            mPageCount = 0;
                        }
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void getPreviousPage() {
        if(!iFromPageId.equals(Constants.DB_NULL)) {

            mPreviousPageListener = new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            if (mOneOrTwo == 2) {
                                mPrevTwoPage = dataSnapshot.getValue(TwoPathPage.class);
                            } else {
                                mPrevOnePage = dataSnapshot.getValue(OnePathPage.class);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if(mOneOrTwo == 2) {
                                Picasso.with(DrawingActivity.this).load(mPrevTwoPage.getImagePath()).into(mPrevImage);
                                mPrevText.setText(mPrevTwoPage.getImageText());
                                mPromptText.setText(mClickedPrompt);
                            } else {
                                Picasso.with(DrawingActivity.this).load(mPrevOnePage.getImagePath()).into(mPrevImage);
                                mPrevText.setText(mPrevOnePage.getImageText());
                            }
                            mPrevHeight = mPrevImage.getHeight() + mPrevText.getHeight() + mHidePrevPageLayout.getHeight();

                        }
                    }.execute();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
    }

    private void setupCountdownTimer() {
        mCountdownTimer = new CountDownTimer(4800000,180000) {
            @Override
            public void onTick(long l) {
                if(skipFirstTick){
                    skipFirstTick = false;
                } else {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            int theTime = (int) (System.currentTimeMillis() / 1000);
                            mStealCheckTime = theTime;
                            mStartTime = theTime;
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            dPageRef.child(Constants.BEING_WORKED_ON).setValue(String.valueOf(mStartTime));
                            return null;
                        }
                    }.execute();
                }
            }

            @Override
            public void onFinish() {

            }
        };
    }

    //=================================
    //      Save Dialog
    //=================================
    private void createSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_submit_drawing,null));
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                submitDrawing();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }


    //=======================================
    //    Submit Drawing
    //=======================================
    private void submitDrawing() {
        mBitmapToSave = null;
        mDrawView.setDrawingCacheEnabled(true);
        mBitmapToSave = mDrawView.getDrawingCache();

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                mLoadingBg.setVisibility(View.VISIBLE);
                mLoadingCircle.setVisibility(View.VISIBLE);
                mSaveButton.setClickable(false);
                String temp = mEditText.getText().toString();
                mYellowText = temp.replace("\n","");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String path = saveImageToDisk(mBitmapToSave);

                Uri image = Uri.fromFile(new File(path + "/" + Constants.FILE_NAME));

                UploadTask imageUploadTask = uploadDrawing(image);

                imageUploadTask.addOnSuccessListener(
                        DrawingActivity.this,
                        createDrawingUploadSuccessListener());

                return null;
            }
        }.execute();
    }

    //=================================
    //      Opacity Seekbar
    //=================================
    private void changeOpacitySeekbar() {
        SeekBar opacityPicker = (SeekBar)findViewById(R.id.seekbar_opacity_picker);
        opacityPicker.setMax(100);
        opacityPicker.setProgress(100);
        opacityPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String opacityText = "Opacity: " + String.valueOf(i) + "%";
                mOpacity.setText(opacityText);
                mDrawView.setOpacity(i);
                mCurrentColor.setAlpha((float)i/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    //=================================
    //      Size Seekbar
    //=================================
    private void changeSizeSeekbar() {
        SeekBar sizePicker = (SeekBar)findViewById(R.id.seekbar_size_picker);
        sizePicker.setMax(48);
        sizePicker.setProgress(6);
        sizePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String sizeText = "Size: " + String.valueOf(i+2) + "px";
                mBrushSize.setText(sizeText);
                mDrawView.setBrushSize(i+2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    //=================================
    //      Simple Setup
    //=================================
    private void simpleSetup() {
        iPageId = getIntent().getStringExtra(Constants.PAGE_ID_INTENT);
        mType = getIntent().getStringExtra(Constants.TYPE_INTENT);
        mStory = getIntent().getStringExtra(Constants.STORY_INTENT);
        iFromPageId = getIntent().getStringExtra(Constants.FROM_INTENT);
        mOneOrTwo = getIntent().getIntExtra(Constants.ONE_OR_TWO_INTENT,-1);
        mFromUser = getIntent().getStringExtra(Constants.FROM_USER);
        mClickedPrompt = getIntent().getStringExtra(Constants.CLICKED_PROMPT_INTENT);
        Log.d(TAG, "simpleSetup: " + mClickedPrompt);

        db = FirebaseDatabase.getInstance();
        dPageRef = db.getReference(mType).child(mStory).child(iPageId);
        dStorageRef = FirebaseStorage.getInstance().getReference(mType).child(mStory).child(iPageId).child(Constants.FILE_NAME);

        mDrawView = (DrawView) findViewById(R.id.draw_view);
        mUndoButton = (Button) findViewById(R.id.undo_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mBrushSize = (TextView) findViewById(R.id.size_textview);
        mOpacity = (TextView) findViewById(R.id.opacity_textview);
        mCurrentColor = (ImageView) findViewById(R.id.current_color);
        mEditText = (EditText) findViewById(R.id.yellow_edit_text);

        // Prev Page
        mViewPrevPageLayout = (RelativeLayout) findViewById(R.id.view_prev_page);
        mHidePrevPageLayout = (RelativeLayout) findViewById(R.id.hide_prev_page_layout);
        mPrevImage = (ImageView) findViewById(R.id.prev_image);
        mPrevText = (TextView) findViewById(R.id.prev_text);
        mPromptText = (TextView) findViewById(R.id.prev_prompt_text);
        TextView pv = (TextView) findViewById(R.id.view_prev_page_text);
        TextView tv = (TextView) findViewById(R.id.hide_prev_page);

        Typeface tf = Typeface.createFromAsset(getAssets(),Constants.FONT);

        mPromptText.setTypeface(tf);
        pv.setTypeface(tf);
        tv.setTypeface(tf);

        mPromptText.setText(mClickedPrompt);

        if(!iFromPageId.equals(Constants.DB_NULL)){
            dPrevPageRef = db.getReference(mType).child(mStory).child(iFromPageId);
        } else {
            mViewPrevPageLayout.setVisibility(View.GONE);
        }

        mLoadingBg = (TextView) findViewById(R.id.loading_drawing_bg);
        mLoadingCircle = (ProgressBar) findViewById(R.id.loading_drawing_circle);

        mStartTime = getIntent().getIntExtra(Constants.CURRENT_TIME_INTENT,-1);
        mStealCheckTime = mStartTime;

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }
        iUserId = Me.getInstance().getUserId();

        setupStealCheckListener();
    }



    //=======================================
    //    From xml, when a paint is clicked
    //=======================================
    public void paintClicked(View view){
        String color = view.getTag().toString();
        mDrawView.setColor(color);
        mCurrentColor.setBackgroundColor(Color.parseColor(color));
    }


    //=======================================
    //  Save Image to Disk before uploading
    //=======================================
    private String saveImageToDisk(Bitmap bitmap){
        Bitmap bitmapToSave = Bitmap.createScaledBitmap(bitmap,1000,800,false);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir",MODE_PRIVATE);
        File imagePath = new File(directory,Constants.FILE_NAME);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(imagePath);
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private UploadTask uploadDrawing(Uri image){
        return dStorageRef.putFile(image);
    }

    private OnSuccessListener<UploadTask.TaskSnapshot> createDrawingUploadSuccessListener(){
        return new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getDownloadUrl() != null) {
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected void onPreExecute() {
                            mLoadingBg.setVisibility(View.VISIBLE);
                            mLoadingCircle.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            String imagePath = taskSnapshot.getDownloadUrl().toString();

                            dPageRef.child(Constants.IMAGE_TEXT).setValue(mYellowText);
                            dPageRef.child(Constants.IMAGE_PATH).setValue(imagePath);
                            dPageRef.child(Constants.IMAGE_USER).setValue(iUserId);
                            dPageRef.child(Constants.IMAGE_USER_NAME).setValue(Me.getInstance().getUsername());

                            if(mFromUser!=null) {
                                DatabaseReference notifyRef = db.getReference(Constants.NOTIFICATIONS).child(mFromUser);
                                notifyRef.child(Constants.N_PAGE_ID).setValue(iPageId);
                                notifyRef.child(Constants.N_TYPE).setValue(mType);
                                notifyRef.child(Constants.N_STORY).setValue(mStory);
                                notifyRef.child(Constants.N_ONE_OR_TWO).setValue(mOneOrTwo);
                            }

                            DatabaseReference userRef = db.getReference(Constants.USERS_REF).child(iUserId).child(Constants.PAGES_REF).child(mType).child(mStory).child(iPageId);
                            userRef.setValue(imagePath);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            goBackToPageActivity();

                        }
                    }.execute();
                }
            }
        };
    }

    public void goBackToPageActivity(){
        if(fromGroupCover){
            Intent intent = new Intent(DrawingActivity.this, GroupCoverActivity.class);
            intent.putExtra(Constants.GROUP_INTENT,mStory);
            startActivity(intent);
            finish();
        } else {
            if (mOneOrTwo == 1) {
                Intent intent = new Intent(DrawingActivity.this, OnePathPageActivity.class);
                intent.putExtra(Constants.PAGE_ID_INTENT, iPageId);
                intent.putExtra(Constants.STORY_INTENT, mStory);
                intent.putExtra(Constants.TYPE_INTENT, mType);
                startActivity(intent);
                finish();
            } else if (mOneOrTwo == 2) {
                Intent intent = new Intent(DrawingActivity.this, TwoPathPageActivity.class);
                intent.putExtra(Constants.PAGE_ID_INTENT, iPageId);
                intent.putExtra(Constants.STORY_INTENT, mStory);
                intent.putExtra(Constants.TYPE_INTENT, mType);
                startActivity(intent);
                finish();
            }
        }
    }

    //=================================
    //  Change if BeingWorkedOn
    //=================================

    @Override
    protected void onResume() {
        super.onResume();
        dPageRef.child(Constants.BEING_WORKED_ON).addValueEventListener(mStealCheckListener);
        if(!iFromPageId.equals(Constants.DB_NULL)){
            dPrevPageRef.addValueEventListener(mPreviousPageListener);
        }
        skipFirstTick = true;
        mCountdownTimer.start();
        if(mCountListener!=null){
            dCountRef.addValueEventListener(mCountListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mStealCheckListener!=null){
            dPageRef.child(Constants.BEING_WORKED_ON).removeEventListener(mStealCheckListener);
        }
        if(mPreviousPageListener!=null && !iFromPageId.equals(Constants.DB_NULL)){
            dPrevPageRef.removeEventListener(mPreviousPageListener);
        }
        if(mCountListener!=null){
            dCountRef.removeEventListener(mCountListener);
        }
        mCountdownTimer.cancel();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_go_back,null));
        builder.setPositiveButton("Yes, exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dPageRef.child(Constants.BEING_WORKED_ON).removeEventListener(mStealCheckListener);
                dPageRef.child(Constants.BEING_WORKED_ON).setValue(String.valueOf(-1));
                goBackToPageActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    private void setupStealCheckListener(){
        mStealCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null) {
                            mStealCheckTime = Integer.parseInt(dataSnapshot.getValue(String.class));
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Log.d(TAG, "onPostExecute: " + mStealCheckTime);
                        if(mStealCheckTime!=mStartTime){
                            Toast.makeText(DrawingActivity.this, "Took too long... someone stole it!", Toast.LENGTH_SHORT).show();
                            goBackToPageActivity();
                        }
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            mDrawView.undo();
            return true;
        }else
            return super.onKeyDown(keyCode, event);
    }
}
