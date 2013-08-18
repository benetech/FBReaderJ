package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

public class ShowTeclaAccessSettingsAction extends FBAndroidAction {
    ShowTeclaAccessSettingsAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        Intent launchTeclaAccessSettings = BaseActivity.getApplicationContext().getPackageManager().getLaunchIntentForPackage("ca.idi.tekla");
        launchTeclaAccessSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        BaseActivity.getApplicationContext().startActivity(launchTeclaAccessSettings);
    }
}
