package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 4/27/17.
 */

public class UserPage {
    String mPageImage, mPageId;

    public UserPage(String pageImage, String pageId) {
        mPageImage = pageImage;
        mPageId = pageId;
    }

    public String getPageImage() {
        return mPageImage;
    }

    public void setPageImage(String pageImage) {
        mPageImage = pageImage;
    }

    public String getPageId() {
        return mPageId;
    }

    public void setPageId(String pageId) {
        mPageId = pageId;
    }
}
