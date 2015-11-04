package org.geometerplus.android.fbreader;

import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * Created by animal@martus.org on 11/3/15.
 */
public class ShowAboutGoReadAction extends FBAndroidAction {

    public ShowAboutGoReadAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final PackageManager packageManager = getBaseActivity().getPackageManager();
        if (packageManager != null) {
            try {
                android.content.pm.PackageInfo packageInfo = packageManager.getPackageInfo(getBaseActivity().getPackageName(), 0);
                Toast toast = Toast.makeText(getBaseActivity(), "Version " + packageInfo.versionName, Toast.LENGTH_LONG);
                toast.show();
            } catch (Exception e) {
                Log.e("ShowAboutGoReadAction", e.getMessage(), e);
            }
        }
    }
}
