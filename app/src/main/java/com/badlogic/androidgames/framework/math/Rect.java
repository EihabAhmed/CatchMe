package com.badlogic.androidgames.framework.math;

/**
 * Created by Eihab on 12/6/2014.
 */
public class Rect {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public static Rect inflateRect(Rect rect, int dx, int dy) {
        rect.left -= dx;
        rect.right += dx;
        rect.top -= dy;
        rect.bottom += dy;

        return rect;
    }

    public static Rect offsetRect(Rect rect, int dx, int dy) {
        rect.left += dx;
        rect.right += dx;
        rect.top += dy;
        rect.bottom += dy;

        return rect;
    }
}