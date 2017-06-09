package org.geometerplus.android.fbreader.benetech;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.AlertHelper;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.ReadingListApiManager;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareActionObserver;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.zlibrary.core.application.ZLApplication;
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

    private Dialog createDialog;

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

        if(canCreate()) {
            addCreateButton(root);
        }

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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        SyncReadingListsWithBookshareActionObserver.getInstance().notifyRelevantBooklistOpened(getActivity());
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
        ReadingListApiManager.createReadingList(getActivity(), name, apiListener);
    }

    private final ReadingListApiManager.ReadinglistAPIListener apiListener = new ReadingListApiManager.ReadinglistAPIListener() {
        @Override
        public void onAPICallResult(Bundle results) {
            ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.SILENT_STARTUP);
            SyncReadingListsWithBookshareActionObserver.getInstance().notifyRelevantBooklistOpened(getActivity());
            if(createDialog.isShowing()){
                createDialog.dismiss();
            }
        }

        @Override
        public void onAPICallError(Bundle results) {
            if(createDialog.isShowing()){
                createDialog.dismiss();
                showErrorMessage(getString(R.string.create_new_readinglist_error));
            }
        }
    };

    private void showCreateListDialog(){
        final Dialog dialog = new Dialog(getActivity());
        createDialog = dialog;
        dialog.setContentView(R.layout.bookshare_dialog_with_progress);
        final EditText dialog_search_term = (EditText)dialog.findViewById(R.id.bookshare_dialog_search_edit_txt);
        TextView dialog_search_title = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_txt);
        Button positiveButton = (Button)dialog.findViewById(R.id.bookshare_dialog_btn_ok);
        Button dialog_cancel = (Button) dialog.findViewById(R.id.bookshare_dialog_btn_cancel);
        TextView dialog_example_text = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_example);
        final ProgressBar bar = (ProgressBar)dialog.findViewById(R.id.progressbar);
        dialog.setTitle(R.string.create_new_readinglist_title);
        dialog_search_title.setText(R.string.create_new_readinglist_message);
        dialog_example_text.setVisibility(View.GONE);
        dialog_search_term.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    createReadingList(dialog_search_term.getText().toString());
                    return true;
                }
                return false;
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                createReadingList(dialog_search_term.getText().toString());
                bar.setVisibility(View.VISIBLE);
            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showErrorMessage(final String text) {
        AlertHelper.popupAlert(getActivity(), text);
    }

    private boolean canCreate(){
        boolean ans = false;
        if(getActivity() instanceof MyBooksActivity){
            ans = ((MyBooksActivity) getActivity()).canCreate();
        }
        return ans;
    }

    private void addCreateButton(ViewGroup root){
        FloatingActionButton mButton = new FloatingActionButton(getActivity());
        mButton.setImageResource(R.drawable.ic_add_white_24dp);
        mButton.setContentDescription(getString(R.string.create_new_readinglist_title));
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
