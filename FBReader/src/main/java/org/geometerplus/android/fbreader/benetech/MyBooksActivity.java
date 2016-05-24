package org.geometerplus.android.fbreader.benetech;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class MyBooksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FragmentTabHost mTabHost;

    private static final String TAG_GO_READ_TAB_TAG = "TabGoRead";
    private static final String TAG_RECENT_TAG = "TabRecent";
    private static final String TAG_FAVORITES_TAG = "TabFavorites";
    private static final String TAG_READING_LISTS_TAG = "TabReadingLists";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bookshare_my_books);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTabs();
    }

    private void initTabs() {
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        addTab(GoReadTabContainer.class, TAG_GO_READ_TAB_TAG, getString(R.string.my_books_tab_go_read), R.drawable.ic_my_books);
        addTab(RecentTabContainer.class, TAG_RECENT_TAG, getString(R.string.my_books_tab_recent), R.drawable.ic_book_info);
        addTab(FavoritesTabContainer.class, TAG_FAVORITES_TAG, getString(R.string.my_books_tab_favorites), R.drawable.ic_list_library_favorites);
        addTab(ReadingListsTabContainer.class, TAG_READING_LISTS_TAG, getString(R.string.my_books_tab_reading_lists), R.drawable.ic_reading_lists);
    }

    private void addTab(Class tabContantainerClass, String tabTag, String tabTitle, final int drawableResourceId) {
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tabTag);

        View tabIndicatorView = LayoutInflater.from(this).inflate(R.layout.tab_indicator, mTabHost.getTabWidget(), false);
        ((TextView) tabIndicatorView.findViewById(R.id.tab_title)).setText(tabTitle);
        ((ImageView) tabIndicatorView.findViewById(R.id.tab_icon)).setImageResource(drawableResourceId);
        tabSpec.setIndicator(tabIndicatorView);

        mTabHost.addTab(tabSpec, tabContantainerClass, null);
    }

    @Override
    public void onBackPressed() {
        String currentTabTag = mTabHost.getCurrentTabTag();
        boolean isPopFragment = popFragment(currentTabTag);

        if (!isPopFragment) {
            finish();
        }
    }

    private boolean popFragment(String currentTabTag) {
        if (currentTabTag.equals(TAG_GO_READ_TAB_TAG)) {
            return ((AbstractBaseTabContainer)getSupportFragmentManager().findFragmentByTag(TAG_GO_READ_TAB_TAG)).popFragment();
        } else if (currentTabTag.equals(TAG_RECENT_TAG)) {
            return ((AbstractBaseTabContainer)getSupportFragmentManager().findFragmentByTag(TAG_RECENT_TAG)).popFragment();
        } else if (currentTabTag.equals(TAG_FAVORITES_TAG)) {
            return ((AbstractBaseTabContainer)getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES_TAG)).popFragment();
        } else if (currentTabTag.equals(TAG_READING_LISTS_TAG)) {
            return ((AbstractBaseTabContainer)getSupportFragmentManager().findFragmentByTag(TAG_READING_LISTS_TAG)).popFragment();
        }

        return false;
    }
}