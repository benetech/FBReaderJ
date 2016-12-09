package org.geometerplus.android.fbreader.benetech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.geometerplus.android.fbreader.library.BookInfoActivity.REQUEST_BOOK_INFO;

/**
 * Created by animal@martus.org on 4/26/16.
 */
public class GoReadTabMainTabContent extends ListFragment implements SortUtil.SortChangesListener{


    private ArrayList<AbstractTitleListRowItem> downloadedBooksList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadedBooksList = new ArrayList<>();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        SortUtil.registerForSortChanges(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SortUtil.unregisterForSortChanges(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Toast.makeText(getActivity(), "qwerqwer", Toast.LENGTH_LONG);
        if(requestCode == BookInfoActivity.REQUEST_BOOK_INFO){
            if(resultCode == BookInfoActivity.RESULT_BOOK_DELETED){
                try {
                    fillListAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fillListAdapter() throws Exception {
        if(getActivity() instanceof MyBooksActivity){
            HashMap<Long, Book> map = ((MyBooksActivity)getActivity()).getDownloadedBooksMap();
            for(Book book :map.values()){
                downloadedBooksList.add(new DownloadedTitleListRowItem(book));
            }
        }
        sortListItems();
        setListAdapter(new BookListAdapter(getActivity(), downloadedBooksList));
    }

    private void sortListItems() {
        Collections.sort(downloadedBooksList, SortUtil.getComparator());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AbstractTitleListRowItem clickedRowItem = downloadedBooksList.get(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), BookInfoActivity.class);
        intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, clickedRowItem.getBookFilePath());
        startActivityForResult(intent, REQUEST_BOOK_INFO);
    }

    @Override
    public void onSortChanged(){
        sortListItems();
        ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onForceRefresh(){
        try {
            downloadedBooksList.clear();
            fillListAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
