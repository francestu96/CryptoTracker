package com.example.cryptotracker.utils;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;

public class LockableNestedScrollView extends NestedScrollView {

    public LockableNestedScrollView(Context context) {
        this(context, null);
    }

    public LockableNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockableNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean isScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        isScrollable = enabled;
    }

    public boolean isScrollable() {
        return isScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (isScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return isScrollable; // isScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!isScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

}
