package org.accessibility;

import android.app.Activity;
import android.view.GestureDetector;
import com.googlecode.eyesfree.inputmethod.MultitouchGestureDetector;
import com.googlecode.eyesfree.inputmethod.SimpleMultitouchGestureListener;

/**
 * @author roms
 *         Date: 8/15/12
 */
public class SimpleMultitouchGestureFilter extends SimpleMultitouchGestureListener {

    private Activity context;
    private SimpleGestureListener listener;


    public SimpleMultitouchGestureFilter(Activity context, SimpleGestureListener sgl) {

        this.context = context;
        this.listener = sgl;
    }


    @Override
    public boolean onSimpleDown(int pointerCount, float centroidX, float centroidY) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onSimpleTap(int pointerCount, float centroidX, float centroidY) {
        this.listener.onSimpleTap(pointerCount, centroidX, centroidY);
        return true;
    }

    @Override
    public boolean onSimpleDoubleTap(int pointerCount, float centroidX, float centroidY) {
        this.listener.onMultiDoubleTap(pointerCount);
        return true;
    }

    @Override
    public boolean onSimpleLongPress(int pointerCount, float centroidX, float centroidY) {
        this.listener.onLongPress(pointerCount);
        return true;
    }

    @Override
    public boolean onSimpleMove(int pointerCount, float centroidX, float centroidY) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onSimpleFlick(int pointerCount, int direction) {
        this.listener.onSwipe(pointerCount, direction);
        return true;
    }

    public static interface SimpleGestureListener {
        void onSwipe(int pointerCount, int direction);

        void onMultiDoubleTap(int pointerCount);

        void onLongPress(int pointerCount);

        void onSimpleTap(int pointerCount, float centroidX, float centroidY);
    }
}
