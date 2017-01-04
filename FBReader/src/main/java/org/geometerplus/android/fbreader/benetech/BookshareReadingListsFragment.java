package org.geometerplus.android.fbreader.benetech;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareActionObserver;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class BookshareReadingListsFragment extends ListFragment implements SortUtil.SortChangesListener{

    private ArrayList<ReadingListsItem> readingListsItems;
    private static final String READINGLIST_TAG = "ReadingListFragment";
    private ReadingListFragment myReadingListFragment = null;

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
        ViewGroup root = (ViewGroup)super.onCreateView(inflater, container, savedInstanceState);

        FloatingActionButton mButton = new FloatingActionButton(getActivity());
        mButton.setImageResource(R.drawable.ic_add_white_24dp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int dpInPx = Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm));

        params.bottomMargin = dpInPx;
        params.rightMargin = dpInPx;
        root.addView(mButton, params);

        mButton.setOnClickListener(buttonListener);
        return root;
    }

    @Override
    public void onResume(){
        super.onResume();
        SortUtil.registerForSortChanges(this);
        SyncReadingListsWithBookshareActionObserver.getInstance().notifyRelevantBooklistOpened(getActivity());
        if(myReadingListFragment != null){
            try{
                myReadingListFragment.onResume();
            } catch (Exception e){
                Log.e(getClass().getCanonicalName(), "failed trying to refresh inner fragment", e );
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SortUtil.unregisterForSortChanges(this);
    }

    @Override
    public void onSortChanged(){
        ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onForceRefresh(){
        if(getActivity() != null) {
            readingListsItems.clear();
            fillListAdapter();
        }
    }

    private void fillListAdapter()  {
        SQLiteBooksDatabase database = (SQLiteBooksDatabase) AbstractSQLiteBooksDatabase.Instance();
        ArrayList<ReadingList> readingLists;
        try{
            readingLists = database.getAllReadingLists();
        }
        catch (Exception e){
            Log.e("ReadingListsFragment", " BookshareReadingListsFrag fillListAdapter crashed", e);
            readingLists = new ArrayList<>();
        }
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

        myReadingListFragment = new ReadingListFragment();
        if(item.readingListName != null
                && item.readingListName.toLowerCase().contains("favorites")){
            Bundle args = new Bundle();
            args.putBoolean(ReadingListFragment.ARG_SHOULD_ADD_FAVORITES, true);
            myReadingListFragment.setArguments(args);
        }
        myReadingListFragment.setReadingList(item.getReadingList());
        replaceFragment(myReadingListFragment, true);
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

    private void createReadingList(String name){

    }

    private void showCreateListDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.bookshare_dialog);
        final EditText dialog_search_term = (EditText)dialog.findViewById(R.id.bookshare_dialog_search_edit_txt);
        TextView dialog_search_title = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_txt);
        TextView dialog_example_text = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_example);
        Button dialog_ok = (Button)dialog.findViewById(R.id.bookshare_dialog_btn_ok);
        Button dialog_cancel = (Button) dialog.findViewById(R.id.bookshare_dialog_btn_cancel);

        dialog_search_title.setText(R.string.create_new_readinglist_title);

        dialog_search_term.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    createReadingList(dialog_search_term.getText().toString());
                    return true;
                }
                return false;
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                createReadingList(dialog_search_term.getText().toString());
            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showCreateListDialog();
        }
    };

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
