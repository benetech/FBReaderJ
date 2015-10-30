package org.geometerplus.zlibrary.ui.android.library;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

/**
 * Created by animal@martus.org on 10/27/15.
 */
public class ZLAndroidActivityWithActionBar extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationMenu;

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

//FIXME find the right action
//          if (menuItem.getItemId() == R.id.drawer_item_screen_orientation)
//              FBReaderApp.Instance().showPopup();

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