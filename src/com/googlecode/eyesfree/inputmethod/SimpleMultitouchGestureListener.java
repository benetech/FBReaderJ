
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

import android.graphics.PointF;
import android.view.MotionEvent;

import com.googlecode.eyesfree.inputmethod.MultitouchGestureDetector.MultitouchGestureListener;

public abstract class SimpleMultitouchGestureListener implements MultitouchGestureListener {
    public static final int FLICK_LEFT = 0;
    public static final int FLICK_DOWN = 1;
    public static final int FLICK_UP = 2;
    public static final int FLICK_RIGHT = 3;

    @Override
    public boolean onDown(MotionEvent ev) {
        PointF centroid = pointerCentroid(ev);
        int pointerCount = getFakePointerCount(ev);

        return onSimpleDown(pointerCount, centroid.x, centroid.y);
    }

    public abstract boolean onSimpleDown(int pointerCount, float centroidX, float centroidY);

    @Override
    public boolean onTap(MotionEvent ev) {
        PointF centroid = pointerCentroid(ev);
        int pointerCount = getFakePointerCount(ev);

        return onSimpleTap(pointerCount, centroid.x, centroid.y);
    }

    @Override
    public boolean onSlideTap(MotionEvent ev) {
        int pointerCount = getFakePointerCount(ev);

        return onSimpleTap(pointerCount, ev.getX(), ev.getX());
    }

    public abstract boolean onSimpleTap(int pointerCount, float centroidX, float centroidY);

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        PointF centroid = pointerCentroid(ev);
        int pointerCount = getFakePointerCount(ev);

        return onSimpleDoubleTap(pointerCount, centroid.x, centroid.y);
    }

    public abstract boolean onSimpleDoubleTap(int pointerCount, float centroidX, float centroidY);

    @Override
    public boolean onLongPress(MotionEvent ev) {
        PointF centroid = pointerCentroid(ev);
        int pointerCount = getFakePointerCount(ev);

        return onSimpleLongPress(pointerCount, centroid.x, centroid.y);
    }

    public abstract boolean onSimpleLongPress(int pointerCount, float centroidX, float centroidY);

    @Override
    public boolean onMove(MotionEvent ev) {
        PointF centroid = pointerCentroid(ev);
        int pointerCount = getFakePointerCount(ev);

        return onSimpleMove(pointerCount, centroid.x, centroid.y);
    }

    public abstract boolean onSimpleMove(int pointerCount, float centroidX, float centroidY);

    @Override
    public boolean onFlick(MotionEvent e1, MotionEvent e2) {
        PointF down = pointerCentroid(e1);
        PointF up = pointerCentroid(e2);

        float deltaX = (up.x - down.x);
        float deltaY = (up.y - down.y);

        boolean a = (deltaY > deltaX);
        boolean b = (deltaY > -deltaX);

        int pointerCount = getFakePointerCount(e1);
        int direction = (a ? 2 : 0) | (b ? 1 : 0);

        return onSimpleFlick(pointerCount, direction);
    }

    public abstract boolean onSimpleFlick(int pointerCount, int direction);

    private PointF pointerCentroid(MotionEvent ev) {
        float x = 0;
        float y = 0;
        int pointerCount = ev.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            y += ev.getX(i);
            x += ev.getY(i);
        }

        x /= ev.getPointerCount();
        y /= ev.getPointerCount();

        return new PointF(x, y);
    }

    public static void setFakePointerCount(MotionEvent e, int pointerCount) {
        e.setEdgeFlags((pointerCount << 4) | e.getEdgeFlags());
    }

    public static int getFakePointerCount(MotionEvent e) {
        final int fakePointerCount = (e.getEdgeFlags() >> 4);

        if (fakePointerCount == 0) {
            return e.getPointerCount();
        }

        return fakePointerCount;
    }
}
