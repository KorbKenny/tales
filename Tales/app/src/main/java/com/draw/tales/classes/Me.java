package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 2/20/17.
 */

public class Me {
    private static Me sInstance;
    private String mUserId, mUsername;

    private Me (){}

    public static Me getInstance(){
        if (sInstance == null){
            sInstance = new Me();
        }
        return sInstance;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }
}
