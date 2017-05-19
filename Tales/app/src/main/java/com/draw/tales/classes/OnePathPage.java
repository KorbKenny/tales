package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 3/10/17.
 */

public class OnePathPage {
    private String mBeingWorkedOn, mThisPageId, mFromPageId, mFromUser, mImagePath, mImageText,
            mImageUser, mImageUserName, mNextText, mNextTextUser, mNextTextUserName, mNextPageId;

    public OnePathPage(){}

    public OnePathPage(String beingWorkedOn, String thisPageId, String fromPageId, String fromUser, String imagePath, String imageText, String imageUser, String imageUserName, String nextText, String nextTextUser, String nextTextUserName, String nextPageId) {
        mBeingWorkedOn = beingWorkedOn;
        mThisPageId = thisPageId;
        mFromPageId = fromPageId;
        mFromUser = fromUser;
        mImagePath = imagePath;
        mImageText = imageText;
        mImageUser = imageUser;
        mImageUserName = imageUserName;
        mNextText = nextText;
        mNextTextUser = nextTextUser;
        mNextTextUserName = nextTextUserName;
        mNextPageId = nextPageId;
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

    public String getNextText() {
        return mNextText;
    }

    public void setNextText(String nextText) {
        mNextText = nextText;
    }

    public String getNextTextUser() {
        return mNextTextUser;
    }

    public void setNextTextUser(String nextTextUser) {
        mNextTextUser = nextTextUser;
    }

    public String getNextPageId() {
        return mNextPageId;
    }

    public void setNextPageId(String nextPageId) {
        mNextPageId = nextPageId;
    }

    public String getNextTextUserName() {
        return mNextTextUserName;
    }

    public void setNextTextUserName(String nextTextUserName) {
        mNextTextUserName = nextTextUserName;
    }
}
