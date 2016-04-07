package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.fbreader.library.ReadingListBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 4/6/16.
 */
public class ReadingListFragment extends ListFragment {

    private ReadingList readingList;
    private ArrayList<ReadingListBookItem> readingListBookItems;

    public void setReadingList(ReadingList readingListToUse) {
        readingList = readingListToUse;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readingListBookItems = new ArrayList<>();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void fillListAdapter() {
        ArrayList<ReadingListBook> readingListBooks = readingList.getReadingListBooks();
        for (int index = 0; index < readingListBooks.size(); ++index) {
            ReadingListBook readingListBook = readingListBooks.get(index);
            final String readingListBookTitle = readingListBook.getTitle();
            final String readingListBookAuthors = readingListBook.getAllAuthorsAsString();
            readingListBookItems.add(new ReadingListBookItem(readingListBookTitle, readingListBookAuthors));
        }

        setListAdapter(new ReadingListBooksAdapter(getActivity(), readingListBookItems));
    }

    public class ReadingListBooksAdapter extends ArrayAdapter<ReadingListBookItem> {

        public ReadingListBooksAdapter(Context context, List<ReadingListBookItem> items) {
            super(context, R.layout.reading_list_book_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.reading_list_book_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.readingListBook = (TextView) convertView.findViewById(R.id.readingListBook);
                viewHolder.readingListBookAuthors = (TextView) convertView.findViewById(R.id.readingListBookAuthors);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ReadingListBookItem item = getItem(position);
            viewHolder.readingListBook.setText(item.readingListBookName);
            viewHolder.readingListBookAuthors.setText(item.readingListBookAuthors);

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView readingListBook;
        public TextView readingListBookAuthors;
    }

    private class ReadingListBookItem {
        private String readingListBookName;
        private String readingListBookAuthors;

        public ReadingListBookItem(String readingListNameToUse, String readingListBookAuthorsToUse) {
            readingListBookName = readingListNameToUse;
            readingListBookAuthors = readingListBookAuthorsToUse;
        }
    }
}
