package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.AsyncTask;
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
import org.bookshare.net.BookshareHttpOauth2Client;
import org.geometerplus.fbreader.library.ReadingList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class BookshareReadingListsFragment extends ListFragment {

    private ArrayList<ReadingListsItem> readingListsItems;

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

    private void fillListAdapter() throws Exception {
        LoadReadingListsTask readingListsLoaderTask = new LoadReadingListsTask();
        readingListsLoaderTask.execute();
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
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.replace(R.id.container_framelayout, fragment);
        transaction.commit();
        getFragmentManager().executePendingTransactions();
    }

    public class ReadingListsAdapter extends ArrayAdapter<ReadingListsItem> {

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

    private class LoadReadingListsTask extends AsyncTask<Void, Void, ArrayList<ReadingList>> {
        @Override
        protected ArrayList<ReadingList> doInBackground(Void... params) {
            try {
                BookshareHttpOauth2Client client =  new BookshareHttpOauth2Client();
                HttpsURLConnection urlConnection = client.createBookshareApiUrlConnection();

                String response = client.requestData(urlConnection);
                JSONObject jsonResponse = new JSONObject(response);
                String accessToken = jsonResponse.getString(BookshareHttpOauth2Client.ACCESS_TOKEN_CODE);

                return client.getReadingLists(accessToken);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ReadingList> readingLists) {
            super.onPostExecute(readingLists);

            fillListAdapter(readingLists);
        }
    }
}