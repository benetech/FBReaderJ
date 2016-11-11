package org.geometerplus.zlibrary.ui.android.library;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.benetech.AccessibleMainMenuActivity;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Makoy on 11/11/16.
 */

public abstract class ZLAndroidActivityforActionBar extends ZLAndroidActivity {

    public static final String BOOK_PATH_KEY = "BookPath";
    protected static final String PLUGIN_ACTION_PREFIX = "___";
    protected final List<PluginApi.ActionInfo> myPluginActions =
        new LinkedList<PluginApi.ActionInfo>();
    //Added for the detecting whether the talkback is on
    protected AccessibilityManager accessibilityManager;
    private boolean initialOpen = true;

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
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary) ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        changeLoginState(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void changeLoginState(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.menu_item_login_bookshare);
        MenuItem logoutMenuItem = menu.findItem(R.id.menu_item_logout_bookshare);

        final boolean isLoggedintoBookshare = isLoggedintoBookshare();
        if(isLoggedintoBookshare) {
            String title = String.format(getString(R.string.signout_button_title_pattern), getCurrentLoggedUsername());
            logoutMenuItem.setTitle(title);
        }
        loginMenuItem.setVisible(!isLoggedintoBookshare);
        logoutMenuItem.setVisible(isLoggedintoBookshare);
    }

    protected boolean isLoggedintoBookshare() {
        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(this);
        String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");
        if (username == null || username.isEmpty())
            return false;

        if (password == null || password.isEmpty())
            return false;

        return true;
    }

    protected String getCurrentLoggedUsername() {
        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(this);
        String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        return username;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        String action = findActionForMenuItem(item.getItemId());
        Object[] params = findParamsForMenuItemAction(item.getItemId());
        if(params == null){
            ZLApplication.Instance().doAction(action);
        }
        else {
            ZLApplication.Instance().doAction(action, params);
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private String findActionForMenuItem(int itemId) {
        if (itemId == R.id.menu_item_settings)
            return ActionCode.SHOW_PREFERENCES;

        if (itemId == R.id.menu_item_sync_with_bookshare) {
            if (isLoggedintoBookshare())
                return ActionCode.SYNC_WITH_BOOKSHARE;

            return ActionCode.BOOKSHARE;
        }

        if (itemId == R.id.menu_item_help)
            return ActionCode.SHOW_HELP;

        if (itemId == R.id.menu_item_about_goread)
            return ActionCode.ABOUT_GOREAD;

        if (itemId == R.id.menu_item_logout_bookshare)
            return ActionCode.LOGOUT_BOOKSHARE;

        if (itemId == R.id.menu_item_login_bookshare)
            return ActionCode.BOOKSHARE;
        return "";
    }

    private Object[] findParamsForMenuItemAction(int itemId){
        if (itemId == R.id.menu_item_sync_with_bookshare)
            return new Object[]{SyncReadingListsWithBookshareAction.SyncType.USER_ACTIVATED};
        return null;
    }

    private Menu addSubMenu(Menu menu, String id) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        return application.myMainWindow.addSubMenu(menu, id);
    }

    private void addMenuItem(Menu menu, String actionId, String name) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.addMenuItem(menu, actionId, null, name);
    }

    private void addMenuItem(Menu menu, String actionId, int iconId) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.addMenuItem(menu, actionId, iconId, null);
    }

    private void addMenuItem(Menu menu, String actionId) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.addMenuItem(menu, actionId, null, null);
    }

    private void addMenuItem(Menu menu, int itemId, String actionId) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.addMenuItem(menu, itemId, actionId, null, null);
    }

    private void addMenuItem(Menu menu, int itemId, String actionId, String name) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.addMenuItem(menu, itemId, actionId, null, name);
    }

    private void addMenuItem(Menu menu, String actionId, String name, int iconId) {
            final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
            application.myMainWindow.addMenuItem(menu, actionId, iconId, name);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_overflow_menu, menu);

        synchronized (myPluginActions) {
            int index = 0;
            for (PluginApi.ActionInfo info : myPluginActions) {
                if (info instanceof PluginApi.MenuActionInfo) {
                    addMenuItem(menu, PLUGIN_ACTION_PREFIX + index++, ((PluginApi.MenuActionInfo)info).MenuItemName);
                }
            }
        }

        changeLoginState(menu);

        final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
        application.myMainWindow.refreshMenu();

        return true;
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
