package com.draw.tales.groups;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.draw.tales.R;

import java.util.List;

/**
 * Created by KorbBookProReturns on 3/17/17.
 */

public class InvitesRecyclerAdapter extends RecyclerView.Adapter<InvitesViewholder> {
    private List<String> mGroupIdList, mGroupNameList;
    private AcceptListener mAcceptListener;
    private DenyListener mDenyListener;
    private InviteGroupListener mInviteGroupListener;

    public InvitesRecyclerAdapter(List<String> groupIdList, List<String> groupNameList, AcceptListener acceptListener, DenyListener denyListener, InviteGroupListener inviteGroupListener) {
        mGroupIdList = groupIdList;
        mGroupNameList = groupNameList;
        mAcceptListener = acceptListener;
        mDenyListener = denyListener;
        mInviteGroupListener = inviteGroupListener;
    }

    @Override
    public InvitesViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InvitesViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_invites,parent,false));
    }

    @Override
    public void onBindViewHolder(InvitesViewholder holder, int position) {
        holder.mGroupTitle.setText(mGroupNameList.get(position));
        Typeface typeface= Typeface.createFromAsset(holder.mGroupTitle.getContext().getAssets(), "fonts/conform.TTF");
        holder.mGroupTitle.setTypeface(typeface);

        String groupId = mGroupIdList.get(position);
        holder.bindAccept(groupId,mGroupNameList.get(position),mAcceptListener);
        holder.bindDeny(groupId,mGroupNameList.get(position),mDenyListener);
        holder.bindGroupName(groupId,mGroupNameList.get(position),mInviteGroupListener);
    }

    @Override
    public int getItemCount() {
        return mGroupIdList.size();
    }

    public interface AcceptListener{
        void OnAcceptClicked(String groupId, String groupName);
    }

    public interface DenyListener{
        void OnDenyClicked(String groupId, String groupName);
    }

    public interface InviteGroupListener{
        void OnGroupNameClicked(String groupId, String groupName);
    }
}
