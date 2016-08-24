package org.geometerplus.android.fbreader.benetech;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.options.ZLIntegerArrayOption;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

/**
 * Created by animal@martus.org on 10/13/15.
 */
public class FBReaderWithPinchZoom extends FBReader {

    private ScaleGestureDetector scaleDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        this.scaleDetector.onTouchEvent(me);

        return super.dispatchTouchEvent(me);
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        private float beginSpan;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).AllowPinchZoomOption.getValue();
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if(((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).AllowPinchZoomOption.getValue()) {
                beginSpan = detector.getCurrentSpan();
                return true;
            }
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).AllowPinchZoomOption.getValue()) {
                int zoomDelta = -1;
                if (beginSpan < detector.getCurrentSpan()) {
                    zoomDelta = 1;
                }

                ZLIntegerArrayOption option = ZLTextStyleCollection.Instance().getBaseStyle().FontSizeOption;
                option.zoom(zoomDelta);
                ((FBReaderApp) FBReaderApp.Instance()).clearTextCaches();
                FBReaderApp.Instance().getViewWidget().repaint();
            }
        }
    }
}
