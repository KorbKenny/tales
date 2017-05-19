package com.draw.tales.groups;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.draw.tales.R;

/**
 * Created by KorbBookProReturns on 3/17/17.
 */

public class InvitesViewholder extends RecyclerView.ViewHolder {
    CardView mAccept, mDeny;
    TextView mGroupTitle;

    public InvitesViewholder(View itemView) {
        super(itemView);
        mAccept = (CardView) itemView.findViewById(R.id.vh_invites_accept);
        mDeny = (CardView) itemView.findViewById(R.id.vh_invites_deny);
        mGroupTitle = (TextView) itemView.findViewById(R.id.vh_invites_group_name);
    }

    public void bindAccept(final String groupId, final String groupName, final InvitesRecyclerAdapter.AcceptListener listener){
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnAcceptClicked(groupId,groupName);
            }
        });
    }

    public void bindDeny(final String groupId, final String groupName, final InvitesRecyclerAdapter.DenyListener listener){
        mDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnDenyClicked(groupId,groupName);
            }
        });
    }

    public void bindGroupName(final String groupId, final String groupName, final InvitesRecyclerAdapter.InviteGroupListener listener){
        mGroupTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnGroupNameClicked(groupId, groupName);
            }
        });
    }
}
