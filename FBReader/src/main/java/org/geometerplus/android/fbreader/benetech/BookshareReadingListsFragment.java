package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class BookshareReadingListsFragment extends ListFragment {

    private ArrayList<ReadingListsItem> readingListsItems;
    private static final String READINGLIST_TAG = "ReadingListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readingListsItems = new ArrayList<>();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getActivity() instanceof MyBooksActivity){
            ((MyBooksActivity)getActivity()).onBookshareReadingListsFragmentAppeared();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void fillListAdapter() throws Exception {
        SQLiteBooksDatabase database = (SQLiteBooksDatabase) AbstractSQLiteBooksDatabase.Instance();
        ArrayList<ReadingList> readingLists = database.getAllReadingLists();
        fillListAdapter(readingLists);
    }

    private void fillListAdapter(ArrayList<ReadingList> readingLists) {
        for (int index = 0; index < readingLists.size(); ++index) {
            ReadingList readingList = readingLists.get(index);
            readingListsItems.add(new ReadingListsItem(readingList));
        }

        setListAdapter(new ReadingListsAdapter(getActivity(), readingListsItems));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ReadingListsItem item = readingListsItems.get(position);

        ReadingListFragment readingListFragment = new ReadingListFragment();
        readingListFragment.setReadingList(item.getReadingList());
        replaceFragment(readingListFragment, true);
        Toast.makeText(getActivity(), item.readingListName, Toast.LENGTH_SHORT).show();
        if(getActivity() instanceof MyBooksActivity){
            ((MyBooksActivity)getActivity()).onReadingListSelectedWithTitle(item.readingListName);
        }
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(READINGLIST_TAG);
        }

        transaction.replace(R.id.container_framelayout, fragment);
        transaction.commit();
        getFragmentManager().executePendingTransactions();
    }

    private class ReadingListsAdapter extends ArrayAdapter<ReadingListsItem> {
        public ReadingListsAdapter(Context context, List<ReadingListsItem> items) {
            super(context, R.layout.reading_lists_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.reading_lists_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.readingListNameTextView = (TextView) convertView.findViewById(R.id.readingListName);
                viewHolder.readingListBooksCountTextView = (TextView) convertView.findViewById(R.id.readingListBookCount);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ReadingListsItem item = getItem(position);
            viewHolder.readingListNameTextView.setText(item.readingListName);
            viewHolder.readingListBooksCountTextView.setText(item.readingListBooksCount);

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView readingListNameTextView;
        public TextView readingListBooksCountTextView;
    }

    private class ReadingListsItem {
        private String readingListName;
        private String readingListBooksCount;
        private ReadingList readingList;

        public ReadingListsItem(ReadingList readingListToUse) {
            readingList = readingListToUse;
            readingListName = readingList.getReadingListName();

            final int bookCount = readingList.getBookCount();
            readingListBooksCount = Integer.toString(bookCount) + " " + "titles";
        }

        public ReadingList getReadingList() {
            return readingList;
        }
    }
}