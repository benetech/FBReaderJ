package org.geometerplus.android.fbreader.benetech;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivityforActionBar;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Makoy on 11/14/16.
 */

public class OptionsMenuHandler {

    private Activity mActivity;

    public OptionsMenuHandler(Activity activity) {
        mActivity = activity;
    }

    private Activity getmActivity() {
        return mActivity;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary) ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getmActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        changeLoginState(menu);
    }

    public void changeLoginState(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.menu_item_login_bookshare);
        MenuItem logoutMenuItem = menu.findItem(R.id.menu_item_logout_bookshare);

        final boolean isLoggedintoBookshare = isLoggedintoBookshare();
        if(isLoggedintoBookshare) {
            String title = String.format(getmActivity().getString(R.string.signout_button_title_pattern), getCurrentLoggedUsername());
            logoutMenuItem.setTitle(title);
        }
        loginMenuItem.setVisible(!isLoggedintoBookshare);
        logoutMenuItem.setVisible(isLoggedintoBookshare);
    }

    public boolean isLoggedintoBookshare() {
        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getmActivity().getBaseContext());
        String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");
        if (username == null || username.isEmpty())
            return false;

        if (password == null || password.isEmpty())
            return false;

        return true;
    }

    public String getCurrentLoggedUsername() {
        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getmActivity().getBaseContext());
        String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        return username;
    }

    public void onOptionsMenuClosed() {
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getmActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    public void onOptionsItemSelected(MenuItem item) {
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getmActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        String action = findActionForMenuItem(item.getItemId());
        Object[] params = findParamsForMenuItemAction(item.getItemId());
        if(params == null){
            ZLApplication.Instance().doAction(action);
        }
        else {
            ZLApplication.Instance().doAction(action, params);
        }
    }

    @NonNull
    public String findActionForMenuItem(int itemId) {
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

    public Object[] findParamsForMenuItemAction(int itemId){
        if (itemId == R.id.menu_item_sync_with_bookshare)
            return new Object[]{SyncReadingListsWithBookshareAction.SyncType.USER_ACTIVATED};
        return null;
    }

    public void addMenuItem(Menu menu, String actionId, String name) {
        final ZLAndroidApplication application = (ZLAndroidApplication)getmActivity().getApplication();
        application.myMainWindow.addMenuItem(menu, actionId, null, name);
    }

    public boolean onCreateOptionsMenu(Menu menu, List<PluginApi.ActionInfo> myPluginActions) {
        getmActivity().getMenuInflater().inflate(R.menu.toolbar_overflow_menu, menu);

        synchronized (myPluginActions) {
            int index = 0;
            for (PluginApi.ActionInfo info : myPluginActions) {
                if (info instanceof PluginApi.MenuActionInfo) {
                    addMenuItem(menu, ZLAndroidActivityforActionBar.PLUGIN_ACTION_PREFIX + index++,
                            ((PluginApi.MenuActionInfo)info).MenuItemName);
                }
            }
        }

        changeLoginState(menu);

        final ZLAndroidApplication application = (ZLAndroidApplication)getmActivity().getApplication();
        application.myMainWindow.refreshMenu();

        return true;
    }

    public boolean onCreateOptionsMenuWithoutPluginActions(Menu menu){
        return onCreateOptionsMenu(menu,new LinkedList<PluginApi.ActionInfo>());
    }

    public void hideMainScreenMenuOptions(Menu menu) {
        menu.findItem(R.id.menu_item_logout_bookshare).setVisible(false);
        menu.findItem(R.id.menu_item_login_bookshare).setVisible(false);
        menu.findItem(R.id.menu_item_help).setVisible(false);
        menu.findItem(R.id.menu_item_sync_with_bookshare).setVisible(false);
    }
}
