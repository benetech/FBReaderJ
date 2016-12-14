package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class AddToReadingListDialogActivity extends AppCompatActivity {
    private ListView mListView;

    private ArrayList<ReadingListsItem> readingListsItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_reading_lists);
        mListView = (ListView)findViewById(R.id.listview);
        readingListsItems = new ArrayList<>();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            this.finish();
        }
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

        mListView.setAdapter(new ReadingListsAdapter(this, readingListsItems));
    }

    private class ReadingListsAdapter extends ArrayAdapter<AddToReadingListDialogActivity.ReadingListsItem> {
        public ReadingListsAdapter(Context context, List<AddToReadingListDialogActivity.ReadingListsItem> items) {
            super(context, R.layout.reading_lists_item, items);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AddToReadingListDialogActivity.ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.reading_lists_item, parent, false);

                viewHolder = new AddToReadingListDialogActivity.ViewHolder();
                viewHolder.readingListNameTextView = (TextView) convertView.findViewById(R.id.readingListName);
                viewHolder.readingListBooksCountTextView = (TextView) convertView.findViewById(R.id.readingListBookCount);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (AddToReadingListDialogActivity.ViewHolder) convertView.getTag();
            }

            AddToReadingListDialogActivity.ReadingListsItem item = getItem(position);
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
