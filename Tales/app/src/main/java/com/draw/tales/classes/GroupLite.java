package com.draw.tales.classes;

/**
 * Created by KorbBookProReturns on 3/18/17.
 */

public class GroupLite {
    private String mId, mName;
    private int mType;
    private String mMembers;
    private int mUpdated;

    public GroupLite() {
    }

    public GroupLite(String id, String name, int type, String members, int updated) {
        mId = id;
        mName = name;
        mType = type;
        mMembers = members;
        mUpdated = updated;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getMembers() {
        return mMembers;
    }

    public void setMembers(String members) {
        mMembers = members;
    }

    public int getUpdated() {
        return mUpdated;
    }

    public void setUpdated(int updated) {
        mUpdated = updated;
    }
}


