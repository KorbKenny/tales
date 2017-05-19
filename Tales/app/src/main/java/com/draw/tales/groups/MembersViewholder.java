package com.draw.tales.groups;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.draw.tales.R;

/**
 * Created by KorbBookProReturns on 3/16/17.
 */

public class MembersViewholder extends RecyclerView.ViewHolder {
    TextView mMemberName;

    public MembersViewholder(View itemView) {
        super(itemView);
        mMemberName = (TextView) itemView.findViewById(R.id.mvh_member_name);
    }

    public void bind(final String id, final MembersRecyclerAdapter.MemberClickListener listener){
        mMemberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMemberClicked(id);
            }
        });
    }
}
