package org.geometerplus.android.fbreader.benetech;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.benetech.android.R;
import org.geometerplus.fbreader.library.ReadingList;

/**
 * Created by animal@martus.org on 4/6/16.
 */
public class ReadingListTabContainer extends AbstractBaseTabContainer {
    private boolean mIsViewInited;
    private ReadingList readingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reading_list_tab_container, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!mIsViewInited) {
            mIsViewInited = true;
            initView();
        }
    }

    private void initView() {
        ReadingListFragment readingListFragment = new ReadingListFragment();
        readingListFragment.setReadingList(readingList);

        replaceFragment(readingListFragment, false);
    }

    public void setReadingList(ReadingList readingListToUse) {
        readingList = readingListToUse;
    }
}
