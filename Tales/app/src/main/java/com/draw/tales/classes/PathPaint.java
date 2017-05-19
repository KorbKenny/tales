package com.draw.tales.classes;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by KorbBookProReturns on 2/20/17.
 */

public class PathPaint {
    private Path mPath;
    private Paint mPaint;
    private float mBrushSize;
    private int mPaintColor, mPaintAlpha;

    public PathPaint(Path path, Paint paint, float brushSize, int paintColor, int paintAlpha) {
        mPath = path;
        mPaint = paint;
        mBrushSize = brushSize;
        mPaintColor = paintColor;
        mPaintAlpha = paintAlpha;
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public float getBrushSize() {
        return mBrushSize;
    }

    public void setBrushSize(float brushSize) {
        mBrushSize = brushSize;
    }

    public int getPaintColor() {
        return mPaintColor;
    }

    public void setPaintColor(int paintColor) {
        mPaintColor = paintColor;
    }

    public int getPaintAlpha() {
        return mPaintAlpha;
    }

    public void setPaintAlpha(int paintAlpha) {
        mPaintAlpha = paintAlpha;
    }
}