package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.benetech.android.R;
import org.bookshare.net.BookshareHttpOauth2Client;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.library.ReadingList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class AddToReadingListDialogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";
    public static final String EXTRA_READINGLIST_NAME = "EXTRA_READINGLIST_NAME";
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAIL = 0;

    private int selectedReadingListIndex = -1;
    private String bookId = null;

    private ListView mListView;
    private ProgressBar mProgressBar;
    private ArrayList<ReadingListsItem> readingListsItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_reading_lists);
        mListView = (ListView)findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.positive_button).setOnClickListener(this);
        findViewById(R.id.negative_button).setOnClickListener(this);

        readingListsItems = new ArrayList<>();
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
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

    protected void handleResult(boolean result, ReadingList chosenReadingList){
        Intent intent  = getIntent();
        intent.putExtra(EXTRA_READINGLIST_NAME, chosenReadingList.getReadingListName());
        if(result){
            setResult(RESULT_CODE_SUCCESS, intent);
        }
        else{
            setResult(RESULT_CODE_FAIL, intent);
        }
        finish();
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
        new AddTitleToReadingListTask().execute();
    }

    class AddTitleToReadingListTask extends AsyncTask<String, Void, Boolean> {
        ReadingList readingList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            readingList = readingListsItems.get(selectedReadingListIndex).readingList;
            mProgressBar.setVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(String... urls) {
            try {
                SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(AddToReadingListDialogActivity.this);
                String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
                String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");

                BookshareHttpOauth2Client client =  new BookshareHttpOauth2Client();
                HttpsURLConnection urlConnection = client.createBookshareApiUrlConnection(username, password);

                String response = client.requestData(urlConnection);
                JSONObject jsonResponse = new JSONObject(response);
                String accessToken = jsonResponse.getString(BookshareHttpOauth2Client.ACCESS_TOKEN_CODE);

                return client.postTitleToReadingList(accessToken, readingList.getBookshareId(), bookId);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            AddToReadingListDialogActivity.this.handleResult(result, readingList);
        }
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
