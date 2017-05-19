package com.draw.tales.classes;

import java.util.List;

/**
 * Created by KorbBookProReturns on 3/10/17.
 */

public class Group {
    private String mId, mName;
    private int mType, mUpdated;
    private List<String> mMemberList, mIdList;

    public Group() {
    }

    public Group(String id, String name, int type, int updated, List<String> memberList, List<String> idList) {
        mId = id;
        mName = name;
        mType = type;
        mUpdated = updated;
        mMemberList = memberList;
        mIdList = idList;
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

    public int getUpdated() {
        return mUpdated;
    }

    public void setUpdated(int updated) {
        mUpdated = updated;
    }

    public List<String> getMemberList() {
        return mMemberList;
    }

    public void setMemberList(List<String> memberList) {
        mMemberList = memberList;
    }

    public List<String> getIdList() {
        return mIdList;
    }

    public void setIdList(List<String> idList) {
        mIdList = idList;
    }
}
