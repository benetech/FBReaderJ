package org.geometerplus.zlibrary.ui.android.library;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;

import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.benetech.AccessibleMainMenuActivity;
import org.geometerplus.android.fbreader.benetech.OptionsMenuHandler;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Makoy on 11/11/16.
 */

public abstract class ZLAndroidActivityforActionBar extends ZLAndroidActivity {

    public static final String BOOK_PATH_KEY = "BookPath";
    public static final String PLUGIN_ACTION_PREFIX = "___";
    protected final List<PluginApi.ActionInfo> myPluginActions =
        new LinkedList<PluginApi.ActionInfo>();
    //Added for the detecting whether the talkback is on
    protected AccessibilityManager accessibilityManager;
    private boolean initialOpen = true;
    private OptionsMenuHandler optionsMenuHandler;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        optionsMenuHandler = new OptionsMenuHandler(this);
    }

    /**
     * This is a workaround solution because the Ice Cream Sandwich and later releases of Android
     * made it so that the options menu will not open on larger sized screens.
     * This solution is gross, but fixes the problem with the menu and
     * maintains backwards compatibility.
     * http://stackoverflow.com/questions/9996333/openoptionsmenu-function-not-working-in-ics/17903128#17903128
     * In the future we should replace this with the options overflow menu.
     */
    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
        Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        optionsMenuHandler.onPrepareOptionsMenu(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    protected boolean isLoggedintoBookshare() {
       return optionsMenuHandler.isLoggedintoBookshare();
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        optionsMenuHandler.onOptionsMenuClosed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        optionsMenuHandler.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return optionsMenuHandler.onCreateOptionsMenu(menu, myPluginActions);
    }

    /*
    * show accessible full screen menu when accessibility is turned on
    *
    */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (accessibilityManager.isEnabled()) {
            if(keyCode == KeyEvent.KEYCODE_MENU){
                Intent i = new Intent(this, AccessibleMainMenuActivity.class);
                startActivity(i);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && accessibilityManager.isEnabled() && initialOpen) {
            initialOpen = false;
        }
    }

    @Override
    protected ZLFile fileFromIntent(Intent intent) {
        String filePath = intent.getStringExtra(BOOK_PATH_KEY);
        if (filePath == null) {
            final Uri data = intent.getData();
            if (data != null) {
                filePath = data.getPath();
            }
        }
        return filePath != null ? ZLFile.createFileByPath(filePath) : null;
    }

    @Override
    protected FBReaderApp createApplication(ZLFile file) {
        if (SQLiteBooksDatabase.Instance() == null) {
            new SQLiteBooksDatabase(this);
        }
        return new FBReaderApp(file != null ? file.getPath() : null);
    }
}
