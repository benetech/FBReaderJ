package org.geometerplus.android.fbreader.preferences;

import android.content.Intent;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

/**
 * Created by animal@martus.org on 11/25/15.
 */
public class AboutGoReadPreference extends ZLPreferenceActivity {

    public AboutGoReadPreference() {
        super("Preferences");
    }

    @Override
    protected void init(Intent intent) {
        final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
        final Screen aboutScreen = createPreferenceScreen("about");
        addPreference(new InfoPreference(this, aboutScreen.Resource.getResource("version").getValue(), androidLibrary.getFullVersionName()));
        addPreference(new UrlPreference(this, aboutScreen.Resource, "site"));
        addPreference(new UrlPreference(this, aboutScreen.Resource, "email"));
        addPreference(new UrlPreference(this, aboutScreen.Resource, "twitter"));
        addPreference(new UrlPreference(this, aboutScreen.Resource, "forum"));
    }
}
