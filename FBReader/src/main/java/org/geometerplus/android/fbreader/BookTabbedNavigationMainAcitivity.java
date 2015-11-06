package org.geometerplus.android.fbreader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TabHost;
import android.widget.TextView;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 11/4/15.
 */
public class BookTabbedNavigationMainAcitivity extends FragmentActivity implements TabHost.OnTabChangeListener{

    private FragmentTabHost mTabHost;

    private static final String TAB_SECTION_CODE = "section_tab";
    private static final String TAB_PAGE_CODE = "page_tab";
    private static final String TAB_BOOKMARK_CODE = "bookmark_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_tabbed_navigation_layout);
        mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabContent);
        mTabHost.setOnTabChangedListener(this);

        mTabHost.addTab(mTabHost.newTabSpec(TAB_SECTION_CODE).setIndicator(getString(R.string.tab_label_section)), BookNavigationTabSection.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_PAGE_CODE).setIndicator(getString(R.string.tab_label_page)), BookNavigationTabPage.class, null);
//FIXME undo and fix bookmark init, list gets reset
//        mTabHost.addTab(mTabHost.newTabSpec(TAB_BOOKMARK_CODE).setIndicator(getString(R.string.tab_label_bookmark)), BookNavigationTabBookmark.class, null);

        turnOffDefaultAllCapsTabLabels();
    }

    private void turnOffDefaultAllCapsTabLabels() {
        for (int index = 0; index < mTabHost.getTabWidget().getTabCount(); ++index) {
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(index).findViewById(android.R.id.title);
            tv.setAllCaps(false);
        }
    }

    @Override
    public void onTabChanged(String tabId) {
    }
}