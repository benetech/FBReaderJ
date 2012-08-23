
/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.eyesfree.inputmethod;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityManager;

import com.googlecode.eyesfree.compat.view.accessibility.AccessibilityManagerCompatUtils;
import com.googlecode.eyesfree.compat.view.InputDeviceCompatUtils;
import com.googlecode.eyesfree.compat.view.MotionEventCompatUtils;

/**
 * Detects various gestures and events using the supplied {@link MotionEvent}s.
 * The {@link MultitouchGestureListener} callback will notify users when a
 * particular motion event has occurred. This class should only be used with
 * {@link MotionEvent}s reported via touch (don't use for trackball events). To
 * use this class:
 * <ul>
 * <li>Create an instance of the {@code GestureDetector} for your {@link View}
 * <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 * {@link #onTouchEvent(MotionEvent)}. The methods defined in your callback will
 * be executed when the events occur.
 * </ul>
 */
public class MultitouchGestureDetector {
    public interface MultitouchGestureListener {
        public boolean onDown(MotionEvent ev);

        public boolean onTap(MotionEvent ev);

        public boolean onDoubleTap(MotionEvent ev);

        public boolean onLongPress(MotionEvent ev);

        public boolean onSlideTap(MotionEvent ev);

        public boolean onMove(MotionEvent ev);

        public boolean onFlick(MotionEvent e1, MotionEvent e2);
    }

    private int mTouchSlopSquare;
    private int mDoubleTapSlopSquare;

    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    /** The maximum duration of a flick gesture in milliseconds. */
    private static final int FLICK_TIMEOUT = 250;

    // constants for Message.what used by GestureHandler below
    private static final int LONG_PRESS = 1;
    private static final int FIRST_TAP = 2;
    private static final int SLIDE_TAP = 3;
    private static final int FLICK = 4;
    private static final int SHOW_PRESS = 5;

    private final Handler mHandler;
    private final AccessibilityManager mAccessibilityManager;

    private MultitouchGestureListener mListener;

    private boolean mInLongPress;
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInDoubleTapRegion;

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    /**
     * The first UP (or POINTER_UP) event to occur after a DOWN (or
     * POINTER_DOWN) event. This gets reset to null on DOWN events.
     */
    private MotionEvent mFirstUpEvent;

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events). Can only be true if there is a double tap listener attached.
     */
    private boolean mIsDoubleTapping;

    /** Whether the user is still holding a finger on the screen. */
    private boolean mIsStillDown;

    /** The event time from the last received event. */
    private long mLastEventTime;

    private boolean mIsLongPressEnabled;
    private boolean mIsDoubleTapEnabled;
    private int mDoubleTapMinFingers;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    private class MultitouchGestureHandler extends Handler {
        MultitouchGestureHandler() {
            super();
        }

        MultitouchGestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PRESS:
                    mListener.onDown(mCurrentDownEvent);
                    break;
                case FIRST_TAP:
                    if (!mIsStillDown) {
                        mListener.onTap(mCurrentDownEvent);
                    }
                    break;
                case LONG_PRESS:
                    mHandler.removeMessages(FIRST_TAP);
                    mInLongPress = true;
                    mListener.onLongPress(mCurrentDownEvent);
                    break;
                case FLICK:
                    break;
            }
        }
    }

    /**
     * Creates a GestureDetector with no listener. You may only use this
     * constructor from a UI thread (this is the usual situation).
     *
     * @see android.os.Handler#Handler()
     * @param context the application's context
     */
    public MultitouchGestureDetector(Context context) {
        this(context, null, null);
    }

    /**
     * Creates a GestureDetector with the supplied listener. You may only use
     * this constructor from a UI thread (this is the usual situation).
     *
     * @see android.os.Handler#Handler()
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks
     */
    public MultitouchGestureDetector(Context context, MultitouchGestureListener listener) {
        this(context, listener, null);
    }

    /**
     * Creates a GestureDetector with the supplied listener. You may only use
     * this constructor from a UI thread (this is the usual situation).
     *
     * @see android.os.Handler#Handler()
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks
     * @param handler the handler to use
     */
    public MultitouchGestureDetector(Context context, MultitouchGestureListener listener,
            Handler handler) {
        if (handler != null) {
            mHandler = new MultitouchGestureHandler(handler);
        } else {
            mHandler = new MultitouchGestureHandler();
        }

        mAccessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        mListener = listener;

        init(context);
    }

    /**
     * Sets the listener.
     *
     * @param listener The listener invoked for all the callbacks.
     */
    public void setListener(MultitouchGestureListener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        mIsLongPressEnabled = true;
        mIsDoubleTapEnabled = true;
        mDoubleTapMinFingers = 1;

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final int touchSlop = (int) (0.5 * configuration.getScaledTouchSlop());
        final int doubleTapSlop = configuration.getScaledDoubleTapSlop();

        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    /**
     * Sets whether double tap is enabled. If this is enabled, when a user
     * quickly taps twice you get a double tap event and nothing further. If
     * it's disabled, you get two tap events. By default, double tap is enabled.
     *
     * @param isDoubleTapEnabled Whether double tap should be enabled.
     */
    public void setIsDoubleTapEnabled(boolean isDoubleTapEnabled) {
        mIsDoubleTapEnabled = isDoubleTapEnabled;
    }

    /**
     * Sets the minimum number of fingers required for a double-tap event. By
     * default, this number is one.
     *
     * @param count The minimum number of fingers required for a double-tap.
     * @see #setIsDoubleTapEnabled(boolean)
     */
    public void setDoubleTapMinFingers(int count) {
        mDoubleTapMinFingers = count;
    }

    /**
     * Sets whether long press is enabled. If this is enabled, when a user
     * presses and holds down you get a long press event and nothing further. If
     * it's disabled, the user can press and hold down and then later move their
     * finger and you will get scroll events. By default, long press is enabled.
     *
     * @param isLongPressEnabled Whether long press should be enabled.
     */
    public void setIsLongPressEnabled(boolean isLongPressEnabled) {
        mIsLongPressEnabled = isLongPressEnabled;
    }

    /**
     * @return true if long press is enabled, else false.
     */
    public boolean isLongPressEnabled() {
        return mIsLongPressEnabled;
    }

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the {@link MultitouchGestureListener} supplied.
     *
     * @param ev The current motion event.
     * @return true if the {@link MultitouchGestureListener} consumed the event,
     *         else false.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (mListener == null) {
            return false;
        }

        // If touch exploration is enabled, use the following workarounds:
        // 1. Detect two-finger scroll and adjust the pointer count.
        // 2. Detect hover to touch transition and drop extra up event.
        if (AccessibilityManagerCompatUtils.isTouchExplorationEnabled(mAccessibilityManager)) {
            handleTouchExploration(ev);
        }

        mLastEventTime = ev.getEventTime();

        final int action = ev.getAction();
        final float y = ev.getY();
        final float x = ev.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        boolean handled = false;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mIsDoubleTapEnabled) {
                    boolean withinFirstTapTimeout = mHandler.hasMessages(FIRST_TAP);
                    mHandler.removeMessages(FIRST_TAP);

                    if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null)
                            && withinFirstTapTimeout
                            && isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev)) {
                        mIsDoubleTapping = true;
                        mHandler.removeMessages(SHOW_PRESS);
                    } else {
                        mHandler.sendEmptyMessageAtTime(FIRST_TAP, ev.getEventTime()
                                + DOUBLE_TAP_TIMEOUT);
                    }
                }

                setCurrentDownEvent(ev);
                setFirstUpEvent(null);

                mAlwaysInTapRegion = true;
                mAlwaysInDoubleTapRegion = true;
                mInLongPress = false;
                mIsStillDown = true;

                if (mIsLongPressEnabled) {
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageAtTime(LONG_PRESS, ev.getDownTime() + TAP_TIMEOUT
                            + LONGPRESS_TIMEOUT);
                }

                mHandler.sendEmptyMessageAtTime(FLICK, ev.getDownTime() + FLICK_TIMEOUT);
                mHandler.sendEmptyMessageAtTime(SHOW_PRESS, ev.getDownTime() + TAP_TIMEOUT);

                handled = true;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                setCurrentDownEvent(ev);
                setFirstUpEvent(null);

                // If this is a second finger, start the SLIDE_PRESS timeout.
                if (!mAlwaysInTapRegion && ev.getPointerCount() == 2) {
                    mHandler.sendEmptyMessageAtTime(SLIDE_TAP, ev.getEventTime() + TAP_TIMEOUT);
                }

                handled = true;

                break;
            case MotionEvent.ACTION_MOVE:
                if (mAlwaysInTapRegion) {
                    // We might still be in the tap region, so check pointer
                    // distances.
                    int distance = maxPointerDistanceSquared(ev, mCurrentDownEvent);
                    if (distance > mTouchSlopSquare) {
                        mAlwaysInTapRegion = false;
                        mHandler.removeMessages(SLIDE_TAP);
                        mHandler.removeMessages(FIRST_TAP);
                        mHandler.removeMessages(LONG_PRESS);
                        mHandler.removeMessages(SHOW_PRESS);
                    }
                    if (distance > mDoubleTapSlopSquare) {
                        mAlwaysInDoubleTapRegion = false;
                    }
                    handled = true;
                } else if (mHandler.hasMessages(FLICK)) {
                    handled = true;
                } else {
                    // We're outside the tap region and there are no other
                    // options.
                    handled |= mListener.onMove(ev);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mFirstUpEvent == null) {
                    setFirstUpEvent(ev);
                }

                // If this is a second finger, check for the SLIDE_PRESS
                // timeout.
                if (!mAlwaysInTapRegion && ev.getPointerCount() == 2
                        && mHandler.hasMessages(SLIDE_TAP)) {
                    setCurrentDownEvent(ev);
                    handled |= mListener.onSlideTap(ev);
                } else {
                    handled = true;
                }

                mHandler.removeMessages(SLIDE_TAP);

                break;
            case MotionEvent.ACTION_UP:
                if (mFirstUpEvent == null) {
                    setFirstUpEvent(ev);
                }

                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                mPreviousUpEvent = MotionEvent.obtain(ev);

                // TODO(alanv): This could be replaced with a check for
                // hasMessage(LONG_PRESS)
                mIsStillDown = false;

                if (mIsDoubleTapping) {
                    handled |= mListener.onDoubleTap(mCurrentDownEvent);
                    mIsDoubleTapping = false;
                } else if (mInLongPress) {
                    mHandler.removeMessages(FIRST_TAP);
                    mInLongPress = false;
                } else if (!mIsDoubleTapEnabled && mAlwaysInTapRegion) {
                    handled |= mListener.onTap(mCurrentDownEvent);
                } else if (!mAlwaysInTapRegion && mHandler.hasMessages(FLICK)) {
                    mHandler.removeMessages(FIRST_TAP);
                    handled |= mListener.onFlick(mCurrentDownEvent, mFirstUpEvent);
                } else if (mHandler.hasMessages(FIRST_TAP)) {
                    // If we don't have enough fingers down for a double-tap
                    // event, just go ahead and send the tap event.
                    if (mCurrentDownEvent.getPointerCount() < mDoubleTapMinFingers) {
                        mHandler.removeMessages(FIRST_TAP);
                        mListener.onTap(mCurrentDownEvent);
                    }
                    handled = true;
                }

                mHandler.removeMessages(FLICK);
                mHandler.removeMessages(LONG_PRESS);
                mHandler.removeMessages(SHOW_PRESS);
                break;
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
            case MotionEvent.ACTION_OUTSIDE:
                // Consume and ignore this action.
                handled = true;
                break;
        }

        return handled;
    }

    private void handleTouchExploration(MotionEvent ev) {
        // Cancel duplicate events, these are touch exploration bugs.
        if (ev.getEventTime() == mLastEventTime) {
            ev.setAction(MotionEvent.ACTION_OUTSIDE);
            return;
        }

        // Adjust the pointer count on single-touch events.
        if ((ev.getPointerCount() == 1)
                && (MotionEventCompatUtils.getSource(ev) == InputDeviceCompatUtils.SOURCE_TOUCHSCREEN)) {
            SimpleMultitouchGestureListener.setFakePointerCount(ev, 2);
        }
    }

    private void setFirstUpEvent(MotionEvent ev) {
        if (mFirstUpEvent != null) {
            mFirstUpEvent.recycle();
        }

        if (ev != null) {
            mFirstUpEvent = MotionEvent.obtain(ev);
        } else {
            mFirstUpEvent = null;
        }
    }

    private void setCurrentDownEvent(MotionEvent ev) {
        if (mCurrentDownEvent != null) {
            mCurrentDownEvent.recycle();
        }

        mCurrentDownEvent = MotionEvent.obtain(ev);
    }

    private int maxPointerDistanceSquared(MotionEvent e1, MotionEvent e2) {
        // Ensure that the events have the same number of pointers.
        if (e1.getPointerCount() != e2.getPointerCount()) {
            return Integer.MAX_VALUE;
        }

        int maxDistance = 0;
        int pointerCount = e1.getPointerCount();

        for (int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++) {
            // Ensure that the events have the same pointer index mapping.
            if (e1.getPointerId(pointerIndex) != e2.getPointerId(pointerIndex)) {
                return Integer.MAX_VALUE;
            }

            int deltaX = (int) (e1.getX(pointerIndex) - e2.getX(pointerIndex));
            int deltaY = (int) (e1.getY(pointerIndex) - e2.getY(pointerIndex));
            int distance = (deltaX * deltaX + deltaY * deltaY);

            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }

        return maxDistance;
    }

    private void cancel() {
        // Remove all pending messages.
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(FIRST_TAP);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(FLICK);
        
        // Clear the velocity tracker.
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        
        // Clear the current state.
        mIsDoubleTapping = false;
        mInLongPress = false;
        mIsStillDown = false;
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
            MotionEvent secondDown) {
        if (!mAlwaysInDoubleTapRegion) {
            return false;
        }

        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }
}
