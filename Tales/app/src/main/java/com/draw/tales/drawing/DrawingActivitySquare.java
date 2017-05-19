package com.draw.tales.drawing;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.draw.tales.R;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Me;
import com.draw.tales.groups.GroupCoverActivity;
import com.draw.tales.main.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;

public class DrawingActivitySquare extends AppCompatActivity {
    private String iGroupId, iUserId, mType, mGroupName;
    private DrawViewSquare mDrawView;
    private CardView mCard;
    private Button mUndoButton, mSaveButton;
    private ImageView mCurrentColor;
    private TextView mLoadingBg, mOpacity, mBrushSize, mTitle;
    private ProgressBar mLoadingCircle;
    private Bitmap mBitmapToSave;
    private boolean drawingGroupCover;
    private float mDrawViewWidth;

    private FirebaseDatabase db;
    private StorageReference dStorageRef;
    private DatabaseReference dDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_square);

        simpleSetup();
        changeSizeSeekbar();
        changeOpacitySeekbar();

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
                createSaveDialog();
            }
        });
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
                mDrawView.setClickable(false);
                mCard.setAlpha(0);
                mSaveButton.setClickable(false);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String path = saveImageToDisk(mBitmapToSave);

                Uri image = Uri.fromFile(new File(path + "/" + Constants.FILE_NAME));

                UploadTask imageUploadTask = uploadDrawing(image);

                imageUploadTask.addOnSuccessListener(
                        DrawingActivitySquare.this,
                        createDrawingUploadSuccessListener());

                return null;
            }
        }.execute();

    }

    private void simpleSetup() {
        drawingGroupCover = getIntent().getBooleanExtra(Constants.FROM_GROUP_COVER_INTENT,false);

        mDrawView = (DrawViewSquare) findViewById(R.id.dsquare_drawview);
        mCard = (CardView) findViewById(R.id.dsquare_card);
        mUndoButton = (Button) findViewById(R.id.dsquare_undo_button);
        mSaveButton = (Button) findViewById(R.id.dsquare_save_button);
        mBrushSize = (TextView) findViewById(R.id.dsquare_size_textview);
        mOpacity = (TextView) findViewById(R.id.dsquare_opacity_textview);
        mCurrentColor = (ImageView) findViewById(R.id.dsquare_current_color);
        mTitle = (TextView) findViewById(R.id.dsquare_title);

        Typeface typeface = Typeface.createFromAsset(getAssets(),Constants.FONT);
        mTitle.setTypeface(typeface);

        mLoadingBg = (TextView) findViewById(R.id.dsquare_loading_drawing_bg);
        mLoadingCircle = (ProgressBar) findViewById(R.id.dsquare_loading_drawing_circle);

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }

        iUserId = Me.getInstance().getUserId();
        db = FirebaseDatabase.getInstance();

        if(drawingGroupCover) {
            setupGroupCoverStuff();
        } else {
            setupUserIconStuff();
        }
    }

    private void setupGroupCoverStuff() {
        mType = getIntent().getStringExtra(Constants.TYPE_INTENT);
        iGroupId = getIntent().getStringExtra(Constants.STORY_INTENT);
        mGroupName = getIntent().getStringExtra(Constants.GROUP_NAME);
        mTitle.setText("Draw the group's cover!");
        dDbRef = db.getReference(Constants.GROUPS).child(iGroupId).child(Constants.GROUP_COVER);
        dStorageRef = FirebaseStorage.getInstance().getReference(mType).child(iGroupId).child(Constants.GROUP_COVER).child(Constants.FILE_NAME);


    }

    private void setupUserIconStuff() {
        mCard.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mDrawViewWidth = mCard.getWidth();
                mCard.setRadius(mDrawViewWidth/2);
            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCard.getLayoutParams();
        params.setMargins(0,0,0,32);
        mCard.setLayoutParams(params);

        dDbRef = db.getReference(Constants.USERS_REF).child(iUserId).child(Constants.USER_IMAGE);
        dStorageRef = FirebaseStorage.getInstance().getReference(Constants.USERS_REF).child(iUserId).child(Constants.FILE_NAME);
    }

    //=================================
    //      Opacity Seekbar
    //=================================
    private void changeOpacitySeekbar() {
        SeekBar opacityPicker = (SeekBar)findViewById(R.id.dsquare_seekbar_opacity_picker);
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
        SeekBar sizePicker = (SeekBar)findViewById(R.id.dsquare_seekbar_size_picker);
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

    //=======================================
    //  Save Image to Disk before uploading
    //=======================================
    private String saveImageToDisk(Bitmap bitmap){
        Bitmap bitmapToSave;
        if(drawingGroupCover){
            bitmapToSave = Bitmap.createScaledBitmap(bitmap,1000,1000,false);
        } else {
            Bitmap tempMap = Bitmap.createScaledBitmap(bitmap,1000,1000,false);
            bitmapToSave = BitmapManipulation.cropToCircle(bitmap);
        }

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
                        protected Void doInBackground(Void... voids) {
                            String imagePath = taskSnapshot.getDownloadUrl().toString();
                            dDbRef.setValue(imagePath);
                            SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
                            sp.edit().putString(Constants.MY_USER_IMAGE,imagePath);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            goBack(true);
                        }
                    }.execute();
                }
            }
        };
    }

    private void goBack(boolean successful) {
        if(successful){
            if (drawingGroupCover) {
                Intent intent = new Intent(DrawingActivitySquare.this, GroupCoverActivity.class);
                intent.putExtra(Constants.GROUP_INTENT, iGroupId);
                intent.putExtra(Constants.GROUP_NAME, mGroupName);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(DrawingActivitySquare.this, MainActivity.class);
                intent.putExtra(Constants.USER, true);
                startActivity(intent);
                finish();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_go_back, null));
            builder.setPositiveButton("Yes, exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (drawingGroupCover) {
                        Intent intent = new Intent(DrawingActivitySquare.this, GroupCoverActivity.class);
                        intent.putExtra(Constants.GROUP_INTENT, iGroupId);
                        intent.putExtra(Constants.GROUP_NAME, mGroupName);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(DrawingActivitySquare.this, MainActivity.class);
                        intent.putExtra(Constants.USER, true);
                        startActivity(intent);
                        finish();
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
    }

    public void paintClicked(View view){
        String color = view.getTag().toString();
        mDrawView.setColor(color);
        mCurrentColor.setBackgroundColor(Color.parseColor(color));
    }

    @Override
    public void onBackPressed() {
        goBack(false);
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
