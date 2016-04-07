package org.geometerplus.android.fbreader.benetech;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TabHost;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class MyBooksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FragmentTabHost mTabHost;

    private static final String TAB_READING_LISTS_TAG = "TabReadingListsTag";

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

        TabHost.TabSpec readingListsTabSpec = mTabHost.newTabSpec(TAB_READING_LISTS_TAG);
        Drawable readingListsDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_reading_lists, null);
        readingListsTabSpec.setIndicator("", readingListsDrawable);
        mTabHost.addTab(readingListsTabSpec, ReadingListsTabContainer.class, null);
    }
}