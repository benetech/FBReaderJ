package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

public class ShowInputSettingsAction extends FBAndroidAction {
    ShowInputSettingsAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        Intent launchInputSettings = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
        launchInputSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        BaseActivity.getApplicationContext().startActivity(launchInputSettings);
    }
}
