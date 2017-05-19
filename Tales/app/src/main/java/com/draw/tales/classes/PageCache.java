package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 4/5/17.
 */

public class PageCache {
    private static PageCache sInstance;
    private TwoPathPage mLeftPage, mRightPage, mFromTwoPage;
    private OnePathPage mNextPage, mFromOnePage;

    private PageCache (){}

    public static PageCache getCacheInstance(){
        if(sInstance==null){
            sInstance = new PageCache();
        }
        return sInstance;
    }

    public TwoPathPage getLeftPage() {
        return mLeftPage;
    }

    public void setLeftPage(TwoPathPage leftPage) {
        mLeftPage = leftPage;
    }

    public TwoPathPage getRightPage() {
        return mRightPage;
    }

    public void setRightPage(TwoPathPage rightPage) {
        mRightPage = rightPage;
    }

    public TwoPathPage getFromTwoPage() {
        return mFromTwoPage;
    }

    public void setFromTwoPage(TwoPathPage fromTwoPage) {
        mFromTwoPage = fromTwoPage;
    }

    public OnePathPage getNextPage() {
        return mNextPage;
    }

    public void setNextPage(OnePathPage nextPage) {
        mNextPage = nextPage;
    }

    public OnePathPage getFromOnePage() {
        return mFromOnePage;
    }

    public void setFromOnePage(OnePathPage fromOnePage) {
        mFromOnePage = fromOnePage;
    }
}
