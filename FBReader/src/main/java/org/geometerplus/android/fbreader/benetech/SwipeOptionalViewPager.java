package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by GreatArcantos on 12/06/2017.
 */

public class SwipeOptionalViewPager extends ViewPager {
    private boolean pagingEnabled;

    public SwipeOptionalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.pagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.pagingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.pagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.pagingEnabled = enabled;
    }
}
