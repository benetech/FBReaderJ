package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.benetech.AccessibleNavigateActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

/**
 * @author roms
 * Action to bring up full screen accessible menu
 */
public class ShowAccessiblePageNavigateAction extends FBAndroidAction {
    ShowAccessiblePageNavigateAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        Intent intent = new Intent(BaseActivity.getApplicationContext(), AccessibleNavigateActivity.class);
        BaseActivity.startActivity(intent);
    }
}
