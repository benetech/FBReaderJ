package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
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
public class AddToReadingListDialogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView mListView;

    private ArrayList<ReadingListsItem> readingListsItems;
    private int selectedReadingListIndex = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_reading_lists);
        mListView = (ListView)findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.negative_button:
                this.finish();
                break;
            case R.id.positive_button:
                addToReadingList();
                break;
        }
    }

    private void addToReadingList() {
        ReadingList readingList = readingListsItems.get(selectedReadingListIndex).readingList;

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectItem(i);
    }

    private void selectItem(int i){
        selectedReadingListIndex = i;
        ((ReadingListsAdapter)mListView.getAdapter()).notifyDataSetChanged();

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
                convertView = inflater.inflate(R.layout.cell_readinglist_dialog, parent, false);

                viewHolder = new AddToReadingListDialogActivity.ViewHolder();
                viewHolder.readingListNameTextView = (TextView) convertView.findViewById(R.id.readingListName);
                viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radiobutton);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (AddToReadingListDialogActivity.ViewHolder) convertView.getTag();
            }

            ReadingListsItem item = getItem(position);
            viewHolder.readingListNameTextView.setText(
                    String.format("%s  (%s)", item.readingListName, item.readingListBooksCount));
            viewHolder.radioButton.setChecked(position == selectedReadingListIndex);
            viewHolder.radioButton.setClickable(false);
            return convertView;
        }
    }

    private static class ViewHolder {
        public RadioButton radioButton;
        public TextView readingListNameTextView;
    }
    private class ReadingListsItem {
        private String readingListName;
        private String readingListBooksCount;
        private ReadingList readingList;

        public ReadingListsItem(ReadingList readingListToUse) {
            readingList = readingListToUse;
            readingListName = readingList.getReadingListName();

            final int bookCount = readingList.getBookCount();
            readingListBooksCount = Integer.toString(bookCount);
        }
    }
}
