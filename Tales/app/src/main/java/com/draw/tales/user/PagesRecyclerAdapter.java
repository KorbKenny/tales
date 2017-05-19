package com.draw.tales.user;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.draw.tales.R;
import com.draw.tales.classes.UserPage;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by KorbBookProReturns on 4/27/17.
 */

public class PagesRecyclerAdapter extends RecyclerView.Adapter<PagesViewholder> {
    private List<UserPage> mUserPageList;
    private PageClickedListener mListener;

    public PagesRecyclerAdapter(List<UserPage> userPageList, PageClickedListener listener) {
        mUserPageList = userPageList;
        mListener = listener;
    }


    @Override
    public PagesViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PagesViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_pages,parent,false));
    }

    @Override
    public void onBindViewHolder(PagesViewholder holder, int position) {
        Picasso.with(holder.mPagePageImage.getContext()).load(mUserPageList.get(position).getPageImage()).placeholder(R.drawable.loadingpageimage).into(holder.mPagePageImage);

        holder.bind(mUserPageList.get(position).getPageId(),mListener);

    }

    @Override
    public int getItemCount() {
        return mUserPageList.size();
    }

    public interface PageClickedListener{
        void onPageClicked(String pageId);
    }
}
