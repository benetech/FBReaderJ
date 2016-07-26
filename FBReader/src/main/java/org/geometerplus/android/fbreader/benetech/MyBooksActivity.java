package org.geometerplus.android.fbreader.benetech;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.benetech.android.R;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class MyBooksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private SharedPreferences sharedPreferences;
    private static final String SHARE_PREFERENCE_CURRENT_PAGE_INDEX_TAG = "my_books_current_page_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bookshare_my_books);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initTabs();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new PageChangeHandler(sharedPreferences, SHARE_PREFERENCE_CURRENT_PAGE_INDEX_TAG));
        MyBooksPagerAdapter pagerAdapter = new MyBooksPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int index = 0; index < tabLayout.getTabCount(); ++index) {
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            tab.setTag(pagerAdapter.getTabId(index));
            tab.setCustomView(pagerAdapter.getTabView(index));
        }

        setCurrentPageToLastSelectedPage(viewPager);
    }

    private void setCurrentPageToLastSelectedPage(ViewPager viewPager) {
        int previousSelectedPageIndex = sharedPreferences.getInt(SHARE_PREFERENCE_CURRENT_PAGE_INDEX_TAG, 0);
        viewPager.setCurrentItem(previousSelectedPageIndex, true);
    }

    @Override
    public void onBackPressed() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        int currentTabContainerIndex = viewPager.getCurrentItem();
        MyBooksPagerAdapter viewPagerAdapter = (MyBooksPagerAdapter) viewPager.getAdapter();
        AbstractBaseTabContainer currentContainerFragment = (AbstractBaseTabContainer) viewPagerAdapter.getItem(currentTabContainerIndex);
        boolean isPopFragment = currentContainerFragment.popFragment();
        if (!isPopFragment) {
            finish();
        }
    }

    /**
     * Called from child fragment BookshareReadingListsFragment when a reading list is selected
     */
    public void onReadingListSelectedWithTitle(String title) {
        tabLayout.setVisibility(View.GONE);
        getSupportActionBar().setTitle(title);
    }

    public void onBookshareReadingListsFragmentAppeared() {
        tabLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(getTitle());
    }

    private class MyBooksPagerAdapter extends FragmentStatePagerAdapter {

        ArrayList<AbstractBaseTabContainer> tabFragmentContainers;
        public MyBooksPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            tabFragmentContainers = new ArrayList<>();
            tabFragmentContainers.add(new GoReadTabContainer());
            tabFragmentContainers.add(new RecentTabContainer());
            tabFragmentContainers.add(new FavoritesTabContainer());
            tabFragmentContainers.add(new ReadingListsTabContainer());
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragmentContainers.get(position);
        }

        @Override
        public int getCount() {
            return tabFragmentContainers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(tabFragmentContainers.get(position).getTitleId());
        }

        public int getTabId(int position) {
            return tabFragmentContainers.get(position).getId();
        }

        public View getTabView(int position) {
            View tabView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_books_tab_layout, null);
            TextView textView = (TextView) tabView.findViewById(R.id.tabLabel);
            String tabTitle = getString(tabFragmentContainers.get(position).getTitleId());
            textView.setText(tabTitle);

            ImageView imageView = (ImageView) tabView.findViewById(R.id.tabIcon);
            int tabDrawableId = tabFragmentContainers.get(position).getTabDrawableId();
            imageView.setImageResource(tabDrawableId);

            return tabView;
        }
    }
}