package org.geometerplus.zlibrary.ui.android.library;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.library.ZLibrary;

/**
 * Created by animal@martus.org on 10/27/15.
 */
public class ZLAndroidActivityWithActionBar extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationMenu;
    private PopupWindow popup;

    @Override
    public void onCreate(Bundle state) {
        doWorkBeforeCallingSuper();
        super.onCreate(state);

        setContentView(R.layout.main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationMenu = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationMenu.setNavigationItemSelectedListener(new OnNavigationItemSelectedHandler());
        mDrawerToggle = new ActionBarDrawerToggleHandler(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
		mDrawerToggle.syncState();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doWorkBeforeCallingSuper() {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
    }

    private void showOrientationPopup() {
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.screen_orientation_layout, viewGroup);

        popup = new PopupWindow(this);
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setContentView(layout);
        popup.setFocusable(true);

        if (ZLibrary.Instance().supportsAllOrientations()) {
            layout.findViewById(R.id.reverseLandscape).setEnabled(true);
            layout.findViewById(R.id.reversePortrait).setEnabled(true);
        }

        RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.orientationGroup);
        radioGroup.check(findCheckRadioButtonId());

        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    private int findCheckRadioButtonId() {
        String value = ZLibrary.Instance().OrientationOption.getValue();
        if (value.equals(ZLibrary.SCREEN_ORIENTATION_SYSTEM))
            return R.id.systemOrientation;

        if (value.equals(ZLibrary.SCREEN_ORIENTATION_SENSOR))
            return R.id.deviceOrientationSensitive;

        if (value.equals(ZLibrary.SCREEN_ORIENTATION_LANDSCAPE))
            return R.id.landscape;

        if (value.equals(ZLibrary.SCREEN_ORIENTATION_PORTRAIT))
            return R.id.portrait;

        if (value.equals(ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT))
            return R.id.reversePortrait;

        if (value.equals(ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))
            return R.id.reverseLandscape;

        throw new RuntimeException("Could not find orienation id for name");
    }

    public void onSystemClick(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM);
    }

    public void onDeviceOrientationSensetive(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_SENSOR);
    }

    public void onPortrait(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onLandscape(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void onReversePortrait(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }

    public void onReverseLandscape(View view) {
        handleOnClick(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    private void handleOnClick(String actionId) {
        ZLApplication.Instance().doAction(actionId);
    }

    private class ActionBarDrawerToggleHandler extends ActionBarDrawerToggle{

        public ActionBarDrawerToggleHandler(ZLAndroidActivityWithActionBar zlAndroidActivityWithActionBar, DrawerLayout mDrawerLayout, int drawer_open, int drawer_close) {
            super(zlAndroidActivityWithActionBar, mDrawerLayout, drawer_open, drawer_close);
        }

        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    private class OnNavigationItemSelectedHandler implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {

            if (menuItem.getItemId() == R.id.drawer_item_my_books)
                ZLApplication.Instance().doAction(ActionCode.SHOW_LIBRARY);

            if (menuItem.getItemId() == R.id.drawer_item_search_bookshare)
                ZLApplication.Instance().doAction(ActionCode.BOOKSHARE);

            if (menuItem.getItemId() == R.id.drawer_item_other_catalogs)
                ZLApplication.Instance().doAction(ActionCode.SHOW_NETWORK_LIBRARY);

            if (menuItem.getItemId() == R.id.drawer_item_search_text)
                ZLApplication.Instance().doAction(ActionCode.SEARCH);

            if (menuItem.getItemId() == R.id.drawer_item_night)
                handleNightEvent();

            if (menuItem.getItemId() == R.id.drawer_item_day)
                handleDayEvent();

            if (menuItem.getItemId() == R.id.drawer_item_screen_orientation)
                showOrientationPopup();

            if (menuItem.getItemId() == R.id.drawer_item_book_info)
                ZLApplication.Instance().doAction(ActionCode.SHOW_BOOK_INFO);

            if (menuItem.getItemId() == R.id.drawer_item_toc)
                ZLApplication.Instance().doAction(ActionCode.SHOW_TOC);

            if (menuItem.getItemId() == R.id.drawer_item_navigate_to_page)
                ZLApplication.Instance().doAction(ActionCode.ACCESSIBLE_NAVIGATION);

            if (menuItem.getItemId() == R.id.drawer_item_bookmarks)
                ZLApplication.Instance().doAction(ActionCode.SHOW_BOOKMARKS);

            mDrawerLayout.closeDrawers();

            return true;
        }

        private void handleNightEvent() {
            final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
            fbReader.doAction(ActionCode.SWITCH_TO_NIGHT_PROFILE);
        }

        private void handleDayEvent() {
            final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
            fbReader.doAction(ActionCode.SWITCH_TO_DAY_PROFILE);
        }

    }
}