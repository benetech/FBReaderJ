package org.geometerplus.android.fbreader.benetech;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 4/27/16.
 */
abstract public class AbstractReadingListTabContainer extends AbstractBaseTabContainer {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reading_list_tab_container, null);
    }

    @Override
    protected void initView() {
        ReadingList readingList = findRecentReadingList();
        if (readingList == null)
            return;

        ReadingListFragment readingListFragment = new ReadingListFragment();
        readingListFragment.setReadingList(readingList);
        if(readingList.getReadingListName() != null
                && readingList.getReadingListName().toLowerCase().contains("favorites")){
            Bundle args = new Bundle();
            args.putBoolean(ReadingListFragment.ARG_SHOULD_ADD_FAVORITES, true);
            readingListFragment.setArguments(args);
        }

        replaceFragment(readingListFragment, false);
    }

    private ReadingList findRecentReadingList() {
        try {
            AbstractSQLiteBooksDatabase database = (AbstractSQLiteBooksDatabase) AbstractSQLiteBooksDatabase.Instance();
            ArrayList<ReadingList> allReadingLists = database.getAllReadingLists();
            for (ReadingList readingList : allReadingLists) {
                if (readingList.getReadingListName().equalsIgnoreCase(getReadingListName()))
                    return readingList;
            }

            return null;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    abstract protected String getReadingListName();
}
