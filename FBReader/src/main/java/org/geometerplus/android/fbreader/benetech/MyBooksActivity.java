package org.geometerplus.android.fbreader.benetech;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ContentFrameLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;
import org.geometerplus.zlibrary.ui.android.util.SortUtil.SORT_ORDER;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class MyBooksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private PopupWindow sortByPopupWindow;
    private TabLayout tabLayout;
    private ViewPager viewPager;
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
        initSortByPopup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_books_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sorty_by:
                showSortByPopup();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initSortByPopup(){
        SORT_ORDER order = SortUtil.initSortOrderFromPreference(getApplicationContext());
        ContentFrameLayout base = (ContentFrameLayout)findViewById(android.R.id.content);
        View contentView = LayoutInflater.from(this).inflate(R.layout.sort_popup_layout, base, false);
        ((RadioButton)contentView.findViewById(order.getId())).setChecked(true);
        ((RadioGroup)contentView).setOnCheckedChangeListener(sortByRadioGroupListener);
        sortByPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        sortByPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        sortByPopupWindow.setOutsideTouchable(true);
        sortByPopupWindow.setContentView(contentView);
    }

    private void showSortByPopup(){
        if(sortByPopupWindow != null){
            if(sortByPopupWindow.isShowing()) {
                sortByPopupWindow.dismiss();
            }
            else {
                sortByPopupWindow.showAtLocation(findViewById(android.R.id.content),
                        Gravity.TOP|Gravity.RIGHT, 40, 140);
            }
        }
    }

    private RadioGroup.OnCheckedChangeListener sortByRadioGroupListener = new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {
            SortUtil.saveSortPreference(getApplicationContext(), SORT_ORDER.fromId(id));
            sortByPopupWindow.dismiss();
            MyBooksPagerAdapter viewPagerAdapter = (MyBooksPagerAdapter) viewPager.getAdapter();
            int currentTabContainerIndex = viewPager.getCurrentItem();
            for(int i = 0; i < viewPagerAdapter.getCount(); i++) {
                AbstractBaseTabContainer currentContainerFragment = (AbstractBaseTabContainer) viewPagerAdapter.getItem(i);
                currentContainerFragment.updateChildFragment();
            }

        }
    };

    private void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
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