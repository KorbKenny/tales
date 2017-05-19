package com.draw.tales.groups;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.draw.tales.R;
import com.draw.tales.classes.Constants;
import com.draw.tales.classes.GroupLite;

import java.util.List;

/**
 * Created by KorbBookProReturns on 3/10/17.
 */

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsViewholder> {
    private List<GroupLite> mGroupList;
    private GroupClickListener mListener;
    private GroupLongClickListener mLongListener;

    public GroupsRecyclerAdapter(List<GroupLite> groupList, GroupClickListener listener, GroupLongClickListener longListener) {
        mGroupList = groupList;
        mListener = listener;
        mLongListener = longListener;
    }

    @Override
    public GroupsViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_groups,parent,false));
    }

    @Override
    public void onBindViewHolder(GroupsViewholder holder, int position) {

        if(mGroupList.get(position).getType() == 1) {
            holder.mGreenBackground.setVisibility(View.GONE);
//            if (position % 2 == 0) {
//                holder.mGreenBackground.setVisibility(View.VISIBLE);
//                holder.mBlueBackground.setVisibility(View.GONE);
//            } else {
//                holder.mBlueBackground.setVisibility(View.VISIBLE);
//                holder.mGreenBackground.setVisibility(View.GONE);
//            }
        }  else {
            holder.mGreenBackground.setVisibility(View.VISIBLE);
        }

//        List<String> memberList = mGroupList.get(position).getMemberList();
//        memberList.remove(Me.getInstance().getUsername());
//        Collections.shuffle(memberList);
//
//        String memberText = "w/ ";
//        String lastAdd = "";
//
//        Log.d(TAG, "onBindViewHolder: FIRST" + memberText);
//
//        for (int i = 0; i < memberList.size(); i++) {
//            if(i == 0){
//                lastAdd = memberList.get(i);
//                memberText += lastAdd;
//                Log.d(TAG, "onBindViewHolder: IF" + memberText);
//            } else {
//                lastAdd = ", " + memberList.get(i);
//                memberText += lastAdd;
//                Log.d(TAG, "onBindViewHolder: ELSE" + memberText);
//            }
//            holder.mMembers.setText(memberText);
//        }

        holder.mMembers.setText("w/ " + mGroupList.get(position).getMembers());

        holder.mGroupName.setText(mGroupList.get(position).getName());


        Typeface typeface= Typeface.createFromAsset(holder.mGroupName.getContext().getAssets(), Constants.FONT);
        holder.mGroupName.setTypeface(typeface);
        holder.mMembers.setTypeface(typeface);

        holder.bind(mGroupList.get(position).getId(),mGroupList.get(position).getName(),mListener,mLongListener,mGroupList.get(position));
        if(position == mGroupList.size() - 1) {
            holder.mExtra.setVisibility(View.VISIBLE);
        } else {
            holder.mExtra.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public interface GroupClickListener{
        void onGroupClicked(String groupId, String groupName);
    }

    public interface GroupLongClickListener{
        void onGroupLongClicked(String groupId, String groupName, GroupLite g);
    }
}
