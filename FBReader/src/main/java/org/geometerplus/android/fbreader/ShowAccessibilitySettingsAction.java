package org.geometerplus.android.fbreader;

import java.io.File;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

/**
 * @author roms
 * Action to bring up accessibility settings
 */
public class ShowAccessibilitySettingsAction extends FBAndroidAction {
    ShowAccessibilitySettingsAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        Intent launchSettings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        launchSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        BaseActivity.getApplicationContext().startActivity(launchSettings);
    }
}
