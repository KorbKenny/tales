package com.draw.tales.groups;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.draw.tales.R;

import java.util.List;

/**
 * Created by KorbBookProReturns on 3/16/17.
 */

public class MembersRecyclerAdapter extends RecyclerView.Adapter<MembersViewholder> {
    private List<String> mMemberList, mIdList;
    private MemberClickListener mListener;

    public MembersRecyclerAdapter(List<String> memberList, List<String> idList, MemberClickListener listener) {
        mMemberList = memberList;
        mIdList = idList;
        mListener = listener;
    }

    @Override
    public MembersViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MembersViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_member,parent,false));
    }

    @Override
    public void onBindViewHolder(MembersViewholder holder, int position) {
        holder.mMemberName.setText(mMemberList.get(position));
        Typeface typeface= Typeface.createFromAsset(holder.mMemberName.getContext().getAssets(), "fonts/conform.TTF");
        holder.mMemberName.setTypeface(typeface);
        holder.bind(mIdList.get(position),mListener);
    }

    @Override
    public int getItemCount() {
        return mMemberList.size();
    }

    public interface MemberClickListener{
        void onMemberClicked(String memberId);
    }
}
