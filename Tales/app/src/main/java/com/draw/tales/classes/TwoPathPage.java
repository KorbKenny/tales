package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 2/20/17.
 */

public class TwoPathPage {
    private String mBeingWorkedOn, mThisPageId, mFromPageId, mFromUser, mImagePath, mImageText,
                    mImageUser, mImageUserName, mLeftText, mLeftTextUser, mLeftTextUserName, mLeftNextPageId, mRightText, mRightTextUser, mRightTextUserName, mRightNextPageId;
    private int mCommentCount;

    public TwoPathPage() {}

    public TwoPathPage(String thisPageId, String beingWorkedOn, String fromPageId, String fromUser, String imagePath, String imageText, String imageUser, String imageUserName, String leftText, String leftTextUser, String leftTextUserName, String leftNextPageId, String rightText, String rightTextUser, String rightTextUserName, String rightNextPageId, int commentCount) {
        mThisPageId = thisPageId;
        mBeingWorkedOn = beingWorkedOn;
        mFromPageId = fromPageId;
        mFromUser = fromUser;
        mImagePath = imagePath;
        mImageText = imageText;
        mImageUser = imageUser;
        mImageUserName = imageUserName;
        mLeftText = leftText;
        mLeftTextUser = leftTextUser;
        mLeftTextUserName = leftTextUserName;
        mLeftNextPageId = leftNextPageId;
        mRightText = rightText;
        mRightTextUser = rightTextUser;
        mRightTextUserName = rightTextUserName;
        mRightNextPageId = rightNextPageId;
        mCommentCount = commentCount;
    }

    public String getBeingWorkedOn() {
        return mBeingWorkedOn;
    }

    public void setBeingWorkedOn(String beingWorkedOn) {
        mBeingWorkedOn = beingWorkedOn;
    }

    public String getThisPageId() {
        return mThisPageId;
    }

    public void setThisPageId(String thisPageId) {
        mThisPageId = thisPageId;
    }

    public String getFromPageId() {
        return mFromPageId;
    }

    public void setFromPageId(String fromPageId) {
        mFromPageId = fromPageId;
    }

    public String getFromUser() {
        return mFromUser;
    }

    public void setFromUser(String fromUser) {
        mFromUser = fromUser;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public String getImageText() {
        return mImageText;
    }

    public void setImageText(String imageText) {
        mImageText = imageText;
    }

    public String getImageUser() {
        return mImageUser;
    }

    public void setImageUser(String imageUser) {
        mImageUser = imageUser;
    }

    public String getImageUserName() {
        return mImageUserName;
    }

    public void setImageUserName(String imageUserName) {
        mImageUserName = imageUserName;
    }

    public String getLeftText() {
        return mLeftText;
    }

    public void setLeftText(String leftText) {
        mLeftText = leftText;
    }

    public String getLeftTextUser() {
        return mLeftTextUser;
    }

    public void setLeftTextUser(String leftTextUser) {
        mLeftTextUser = leftTextUser;
    }

    public String getLeftNextPageId() {
        return mLeftNextPageId;
    }

    public void setLeftNextPageId(String leftNextPageId) {
        mLeftNextPageId = leftNextPageId;
    }

    public String getRightText() {
        return mRightText;
    }

    public void setRightText(String rightText) {
        mRightText = rightText;
    }

    public String getRightTextUser() {
        return mRightTextUser;
    }

    public void setRightTextUser(String rightTextUser) {
        mRightTextUser = rightTextUser;
    }

    public String getRightNextPageId() {
        return mRightNextPageId;
    }

    public void setRightNextPageId(String rightNextPageId) {
        mRightNextPageId = rightNextPageId;
    }

    public int getCommentCount() {
        return mCommentCount;
    }

    public void setCommentCount(int commentCount) {
        mCommentCount = commentCount;
    }

    public String getLeftTextUserName() {
        return mLeftTextUserName;
    }

    public void setLeftTextUserName(String leftTextUserName) {
        mLeftTextUserName = leftTextUserName;
    }

    public String getRightTextUserName() {
        return mRightTextUserName;
    }

    public void setRightTextUserName(String rightTextUserName) {
        mRightTextUserName = rightTextUserName;
    }
}
