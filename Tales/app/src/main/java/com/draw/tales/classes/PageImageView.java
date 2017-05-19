package com.draw.tales.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by KorbBookProReturns on 2/20/17.
 */

public class PageImageView extends ImageView {
    public PageImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = (int)(width * 0.8);

        setMeasuredDimension(width,height);
    }
}
