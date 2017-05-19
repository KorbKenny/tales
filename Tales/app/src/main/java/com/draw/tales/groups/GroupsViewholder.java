package com.draw.tales.groups;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.draw.tales.R;
import com.draw.tales.classes.GroupLite;

/**
 * Created by KorbBookProReturns on 3/10/17.
 */

public class GroupsViewholder extends RecyclerView.ViewHolder {
    CardView mCardView;
    TextView mGroupName, mGreenBackground, mBlueBackground, mMembers;
    View mExtra;

    public GroupsViewholder(View itemView) {
        super(itemView);

        mCardView = (CardView) itemView.findViewById(R.id.vh_group_cardview);
        mGroupName = (TextView) itemView.findViewById(R.id.vh_group_title);
        mGreenBackground = (TextView) itemView.findViewById(R.id.vh_group_green);
        mBlueBackground = (TextView) itemView.findViewById(R.id.vh_group_blue);
        mMembers = (TextView) itemView.findViewById(R.id.vh_group_members);
        mExtra = itemView.findViewById(R.id.extra_group_view);
    }

    public void bind(final String id, final String groupName, final GroupsRecyclerAdapter.GroupClickListener listener,
                     final GroupsRecyclerAdapter.GroupLongClickListener longListener, final GroupLite g){
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onGroupClicked(id, groupName);
            }
        });
        mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longListener.onGroupLongClicked(id,groupName,g);
                return true;
            }
        });

    }
}
