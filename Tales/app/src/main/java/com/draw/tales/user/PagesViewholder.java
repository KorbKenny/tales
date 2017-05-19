package com.draw.tales.user;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.draw.tales.R;
import com.draw.tales.classes.PageImageView;

/**
 * Created by KorbBookProReturns on 4/27/17.
 */

public class PagesViewholder extends RecyclerView.ViewHolder {
    PageImageView mPagePageImage;

    public PagesViewholder(View itemView) {
        super(itemView);
        mPagePageImage = (PageImageView) itemView.findViewById(R.id.ovh_image);
    }

    public void bind(final String pageId, final PagesRecyclerAdapter.PageClickedListener listener){
        mPagePageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPageClicked(pageId);
            }
        });
    }
}
