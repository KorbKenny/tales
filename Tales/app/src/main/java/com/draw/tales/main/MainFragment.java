package com.draw.tales.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.draw.tales.AdminActivity;
import com.draw.tales.R;
import com.draw.tales.tutorial.TutorialActivity;
import com.draw.tales.TwoPathPageActivity;
import com.draw.tales.classes.Bookmark;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.Me;
import com.draw.tales.classes.PageImageView;
import com.draw.tales.groups.DBSQLiteHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by KorbBookProReturns on 2/14/17.
 */

public class MainFragment extends Fragment {
    private String iContinuePageId;
    private CardView mBeginButton, mContinueButton;
    private ImageView mBookmark;
    private String mBookmark1,mBookmark2,mBookmark3,mBookmark4,mBookimage1,mBookimage2,mBookimage3,mBookimage4,mBookmarkChosen;
    private boolean tuturial = false;
    private FirebaseDatabase db;
    private TextView mPageCountView;
    private int mPageCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        simpleSetup(view);
        getMyBookmarks();
        getPageCount();

        mBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBookmarkDialog();
            }
        });

        mBeginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TwoPathPageActivity.class);
                intent.putExtra(Constants.PAGE_ID_INTENT, Constants.FIRST_PAGE_ID);
                intent.putExtra(Constants.TYPE_INTENT, Constants.GLOBAL);
                intent.putExtra(Constants.STORY_INTENT, Constants.FIRST_STORY);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_toward_top);
                getActivity().finish();

//                if(tuturial){
//                    Intent intent = new Intent(getActivity(), TutorialActivity.class);
//                    intent.putExtra(Constants.PAGE_ID_INTENT, Constants.FIRST_PAGE_ID);
//                    intent.putExtra(Constants.TYPE_INTENT, Constants.GLOBAL);
//                    intent.putExtra(Constants.STORY_INTENT, Constants.FIRST_STORY);
//                    startActivity(intent);
//                    getActivity().overridePendingTransition(R.anim.in_from_bottom,R.anim.out_toward_top);
//                    getActivity().finish();
//                } else {
//                    Intent intent = new Intent(getActivity(), TwoPathPageActivity.class);
//                    intent.putExtra(Constants.PAGE_ID_INTENT, Constants.FIRST_PAGE_ID);
//                    intent.putExtra(Constants.TYPE_INTENT, Constants.GLOBAL);
//                    intent.putExtra(Constants.STORY_INTENT, Constants.FIRST_STORY);
//                    startActivity(intent);
//                    getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_toward_top);
//                    getActivity().finish();
//                }
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TwoPathPageActivity.class);
                intent.putExtra(Constants.PAGE_ID_INTENT,iContinuePageId);
                intent.putExtra(Constants.TYPE_INTENT,Constants.GLOBAL);
                intent.putExtra(Constants.STORY_INTENT,Constants.FIRST_STORY);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom,R.anim.out_toward_top);
                getActivity().finish();
            }
        });

        mContinueButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(Me.getInstance().getUserId().equals("bAYwEEAQuzRVvuvxinVN15CcRVO2")) {
                    Intent intent = new Intent(getActivity(), AdminActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void getPageCount() {
        DatabaseReference pageCountRef = db.getReference(Constants.GLOBAL).child(Constants.FIRST_STORY).child(Constants.PAGE_COUNT);
        pageCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mPageCount = dataSnapshot.getValue(Integer.class);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            showPageCount();
                        }
                    }.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showPageCount() {
        if(getContext()!=null) {
            if(mPageCount == 1){
                mPageCountView.setText("" + mPageCount + " page so far");
            } else {
                mPageCountView.setText("" + mPageCount + " pages so far");
            }
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.page_count_in);
            mPageCountView.setAlpha(1.0f);
            mPageCountView.startAnimation(animation);
        }
    }

    private void getMyBookmarks() {
        DatabaseReference bookmarkRef = db.getReference(Constants.USERS_REF).child(Me.getInstance().getUserId()).child(Constants.BOOKMARKS);

        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
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
                            if(mBookmark1!=null||mBookmark2!=null||mBookmark3!=null||mBookmark4!=null){
                                showBookmark();
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

    private void createBookmarkDialog() {
        View alertView = getActivity().getLayoutInflater().inflate(R.layout.dialog_bookmarks,null);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(alertView)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Dialog d = (Dialog)dialogInterface;
                TextView title = (TextView) d.findViewById(R.id.bm_text);
                Typeface typeface= Typeface.createFromAsset(d.getContext().getAssets(), Constants.FONT);
                title.setTypeface(typeface);
                PageImageView b1 = (PageImageView)d.findViewById(R.id.bookmark_1);
                PageImageView b2 = (PageImageView)d.findViewById(R.id.bookmark_2);
                PageImageView b3 = (PageImageView)d.findViewById(R.id.bookmark_3);
                PageImageView b4 = (PageImageView)d.findViewById(R.id.bookmark_4);

                if(mBookimage1!=null){
                    Picasso.with(getContext()).load(mBookimage1).placeholder(R.drawable.loadingpageimage).into(b1);
                }
                if(mBookimage2!=null){
                    Picasso.with(getContext()).load(mBookimage2).placeholder(R.drawable.loadingpageimage).into(b2);
                }
                if(mBookimage3!=null){
                    Picasso.with(getContext()).load(mBookimage3).placeholder(R.drawable.loadingpageimage).into(b3);
                }
                if(mBookimage4!=null){
                    Picasso.with(getContext()).load(mBookimage4).placeholder(R.drawable.loadingpageimage).into(b4);
                }
                mBookmarkChosen = null;

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()){
                            case R.id.bookmark_1:
                                mBookmarkChosen = mBookmark1;
                                break;
                            case R.id.bookmark_2:
                                mBookmarkChosen = mBookmark2;
                                break;
                            case R.id.bookmark_3:
                                mBookmarkChosen = mBookmark3;
                                break;
                            case R.id.bookmark_4:
                                mBookmarkChosen = mBookmark4;
                                break;
                            default:
                                return;
                        }

                        d.dismiss();

                        if(mBookmarkChosen!=null) {
                            Intent intent = new Intent(getActivity(), TwoPathPageActivity.class);
                            intent.putExtra(Constants.PAGE_ID_INTENT, mBookmarkChosen);
                            intent.putExtra(Constants.TYPE_INTENT, Constants.GLOBAL);
                            intent.putExtra(Constants.STORY_INTENT, Constants.FIRST_STORY);
                            startActivity(intent);
                            getActivity().finish();
                        }
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

    private void showBookmark() {
        if(getContext()!=null) {
            mBookmark.setVisibility(View.VISIBLE);
            mBookmark.setClickable(true);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bookmark_in);
            mBookmark.setAlpha(1.0f);
            mBookmark.startAnimation(animation);
        }
    }

    private void simpleSetup(View view) {
        db = FirebaseDatabase.getInstance();
        mBeginButton = (CardView) view.findViewById(R.id.button_beginning);
        mContinueButton = (CardView) view.findViewById(R.id.button_continue);
        Typeface typeface= Typeface.createFromAsset(getActivity().getAssets(), Constants.FONT);
        mBookmark = (ImageView)view.findViewById(R.id.main_bookmark);
        mPageCountView = (TextView)view.findViewById(R.id.main_page_count);
        TextView beginText = (TextView)view.findViewById(R.id.button_start_beginning_text);
        TextView continueText = (TextView)view.findViewById(R.id.button_continue_text);
        beginText.setTypeface(typeface);
        continueText.setTypeface(typeface);
        mPageCountView.setTypeface(typeface);

        SharedPreferences sp = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        iContinuePageId = sp.getString(Constants.CONTINUE_PAGE_ID,null);

        if(Me.getInstance().getUserId()==null){
            Me.getInstance().setUserId(sp.getString(Constants.MY_USER_ID,null));
            Me.getInstance().setUsername(sp.getString(Constants.MY_USER_NAME,null));
        }

        if(iContinuePageId!=null){
            mContinueButton.setVisibility(View.VISIBLE);
        } else {
            tuturial = true;
        }
    }
}
