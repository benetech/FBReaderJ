package org.geometerplus.android.fbreader;

import android.content.Intent;

import org.geometerplus.android.fbreader.preferences.AboutGoReadPreference;
import org.geometerplus.android.fbreader.preferences.PreferenceActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * Created by animal@martus.org on 11/3/15.
 */
public class ShowAboutGoReadAction extends FBAndroidAction {

    public ShowAboutGoReadAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object ... params) {
        final Intent intent = new Intent(getBaseActivity().getApplicationContext(), AboutGoReadPreference.class);
        if (params.length == 1 && params[0] instanceof String) {
            intent.putExtra(PreferenceActivity.SCREEN_KEY, (String)params[0]);
        }
        getBaseActivity().startActivityForResult(intent, FBReader.REPAINT_CODE);
    }
}
