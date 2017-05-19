package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 3/20/17.
 */

public class Bookmark {
    private int mNumber;
    private String mPageId, mImage;

    public Bookmark(int number, String pageId, String image) {
        mNumber = number;
        mPageId = pageId;
        mImage = image;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getPageId() {
        return mPageId;
    }

    public void setPageId(String pageId) {
        mPageId = pageId;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }
}
