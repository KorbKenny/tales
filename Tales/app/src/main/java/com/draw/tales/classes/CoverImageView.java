package com.draw.tales.classes;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by KorbBookProReturns on 3/22/17.
 */

public class CoverImageView extends AppCompatImageView {
    public CoverImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = (int)(width * 1.25);
        setMeasuredDimension(width,height);
    }
}
