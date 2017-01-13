package org.geometerplus.android.fbreader.benetech;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.network.ReadingListApiManager;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareActionObserver;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.fbreader.library.ReadingListBook;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by animal@martus.org on 4/6/16.
 */
public class ReadingListFragment extends TitleListFragmentWithContextMenu implements AdapterView.OnItemLongClickListener {

    public static final String ARG_SHOULD_ADD_FAVORITES = "shouldAddFavorites";
    public static final String PARAM_READINGLIST_JSON= "PARAM_READINGLIST_JSON";
    private final int START_READINGLIST_DIALOG = 1;
    private final int REQUEST_CODE_DELETE_FROM_LIST = 2;

    private long lastSwipeEventTimestamp = 0;

    private ReadingList readingList;

    private boolean shouldAddFavorites = false;

    public void setReadingList(ReadingList readingListToUse) {
        readingList = readingListToUse;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            shouldAddFavorites = savedInstanceState.getBoolean(ARG_SHOULD_ADD_FAVORITES, false);
            try {
                String jsonString = savedInstanceState.getString(PARAM_READINGLIST_JSON);
                JSONObject json = new JSONObject(jsonString);
                readingList = new ReadingList(json);
            } catch (Exception e){
                readingList = new ReadingList();
            }
        }
        else if(getArguments() != null){
            shouldAddFavorites = getArguments().getBoolean(ARG_SHOULD_ADD_FAVORITES, false);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(getListView(), view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(requestCode == START_READINGLIST_DIALOG){
            if(resultCode == RemoveFromReadingListDialogActivity.RESULT_OK){
                int position = data.getIntExtra(RemoveFromReadingListDialogActivity.EXTRA_BOOK_POSITION, -1);
                bookRowItems.remove(position);
                ((ReadingListBooksAdapter)getListAdapter()).notifyDataSetChanged();
                sync();
            }
            else {
                showErrorMessage(getString(R.string.delete_from_readinglist_error));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        String readingListJson = readingList.toJSONObject().toString();
        savedInstanceState.putString(PARAM_READINGLIST_JSON, readingListJson);
        savedInstanceState.putBoolean(ARG_SHOULD_ADD_FAVORITES, shouldAddFavorites);
    }

    @Override
    protected void fillListAdapter() {
        ArrayList<ReadingListBook> readingListBooks = readingList.getReadingListBooks();
        for (int index = 0; index < readingListBooks.size(); ++index) {
            ReadingListBook readingListBook = readingListBooks.get(index);
            final String readingListBookTitle = readingListBook.getTitle();
            final String readingListBookAuthors = readingListBook.getAllAuthorsAsString();
            final long bookshareId = readingListBook.getBookId();
            Book book = null;
            if(getActivity() instanceof MyBooksActivity){
                try {
                    HashMap<Long, Book> downloadedBooks = ((MyBooksActivity)getActivity()).getDownloadedBooksMap();
                    book = downloadedBooks.get(bookshareId);
                    if(book != null){
                        Log.d("fetch book ", " by bookshare id success");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bookRowItems.add(new ReadingListTitleItem(bookshareId, readingListBookTitle,
                    readingListBookAuthors, readingListBook.getDateAdded(), book));
        }

        if(shouldAddFavorites) {
            ArrayList<Book> favoriteTitelsOnDevice = getFavoritesOnDevice();
            for (Book favoriteBookOnDevice : favoriteTitelsOnDevice) {
                bookRowItems.add(new DownloadedTitleListRowItem(favoriteBookOnDevice));
            }
        }

        sortListItems();
        setListAdapter(new ReadingListBooksAdapter(getActivity(), bookRowItems));
    }

    private void sync(){
        ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.SILENT_STARTUP);
        SyncReadingListsWithBookshareActionObserver.getInstance().notifyRelevantBooklistOpened(getActivity());
    }

    private boolean canDelete(){
        if(getActivity() instanceof  MyBooksActivity) {
            return true;
        }
        return false;
    }

    private boolean isTalkbackOn(){
        AccessibilityManager am = (AccessibilityManager) getActivity().getSystemService(Activity.ACCESSIBILITY_SERVICE);
        return am.isTouchExplorationEnabled();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        long timeSinceUpdate =  System.currentTimeMillis() - lastSwipeEventTimestamp;
        if(timeSinceUpdate >= 100) {
            super.onListItemClick(l, v, position, id);
        }
    }


    private ArrayList<Book> getFavoritesOnDevice() {
        final BooksDatabase db = BooksDatabase.Instance();
        final Map<Long,Book> savedBooksByBookId = new HashMap<>();
        ArrayList<Book> favoriteBooksOnDevice = new ArrayList<>();
        for (long id : db.loadFavoritesIds()) {
            Book book = savedBooksByBookId.get(id);
            if (book == null) {
                book = Book.getById(id);
                if (book != null && !book.File.exists()) {
                    book = null;
                }
            }
            if (book != null) {
                favoriteBooksOnDevice.add(book);
            }
        }

        return favoriteBooksOnDevice;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AbstractTitleListRowItem item = (AbstractTitleListRowItem)getListView().getAdapter().getItem(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), RemoveFromReadingListDialogActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(RemoveFromReadingListDialogActivity.EXTRA_LIST_ID, readingList.getBookshareId());
        intent.putExtra(RemoveFromReadingListDialogActivity.EXTRA_BOOK_ID, Long.toString(item.getBookId()));
        intent.putExtra(RemoveFromReadingListDialogActivity.EXTRA_BOOK_POSITION, Integer.toString(position));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_CODE_DELETE_FROM_LIST);
        return true;
    }

    public class ReadingListBooksAdapter extends ArrayAdapter<AbstractTitleListRowItem> {

        public ReadingListBooksAdapter(Context context, List<AbstractTitleListRowItem> items) {
            super(context, R.layout.reading_list_book_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                viewHolder = new ViewHolder();
                if(canDelete()) {
                    convertView = inflater.inflate(R.layout.reading_list_book_item_with_drag, parent, false);
                    viewHolder.hiddenLayout = (LinearLayout) convertView.findViewById(R.id.hidden_layout);
                    viewHolder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipe_layout);
                }
                else {
                    convertView = inflater.inflate(R.layout.reading_list_book_item, parent, false);
                }
                viewHolder.readingListBook = (TextView) convertView.findViewById(R.id.bookTitle);
                viewHolder.readingListBookAuthors = (TextView) convertView.findViewById(R.id.bookAuthorsLabel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            AbstractTitleListRowItem item = getItem(position);
            viewHolder.readingListBook.setText(item.getBookTitle());
            viewHolder.readingListBookAuthors.setText(item.getAuthors());

            int userValue = ZLTextStyleCollection.Instance().getBaseStyle().FontSizeOption.getValue();
            userValue = Math.max(userValue, 18); //these values come from ZLFontSizeListPreference
            userValue = Math.min(userValue, 30);
            viewHolder.readingListBook.setTextSize(userValue);

            double lowerValue = userValue / 1.5;
            lowerValue = Math.max(lowerValue, 12d);
            viewHolder.readingListBookAuthors.setTextSize(Math.round(lowerValue));

            if(viewHolder.hiddenLayout != null){
                viewHolder.hiddenLayout.setTag(new Integer(position));
                viewHolder.hiddenLayout.setOnClickListener(deleteListener);
                viewHolder.swipeLayout.setOnLongClickListener(openListener);
                viewHolder.swipeLayout.addSwipeListener(swipeListener);
            }
            return convertView;
        }
    }
    private View.OnClickListener selectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Integer position = (Integer) v.getTag();
            onListItemClick(getListView(), v, position.intValue(), v.getId());
        }
    };

    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Integer position = (Integer) v.getTag();
            final AbstractTitleListRowItem item = bookRowItems.get(position);
            final ProgressDialog progress = new ProgressDialog(getActivity());
            progress.setCancelable(false);
            progress.setTitle(R.string.delete_fromreadinglist_progress_title);
            progress.show();
            ReadingListApiManager.removeFromReadingList(getActivity(), readingList.getBookshareId(),
                    Long.toString(item.getBookId()),
                    new ReadingListApiManager.ReadinglistAPIListener() {
                @Override
                public void onAPICallResult(Bundle results) {
                    progress.hide();
                    bookRowItems.remove(item);
                    ((ReadingListBooksAdapter)getListAdapter()).notifyDataSetChanged();
                    showErrorMessage(getString(R.string.delete_fromreadinglist_success));
                    ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.SILENT_STARTUP);
                }

                @Override
                public void onAPICallError(Bundle results) {
                    progress.hide();
                    showErrorMessage(getString(R.string.delete_fromreadinglist_failure));
                }
            });

        }
    };

    private static class ViewHolder {
        public TextView readingListBook;
        public TextView readingListBookAuthors;
        public ViewGroup hiddenLayout;
        public SwipeLayout swipeLayout;
    }
    View.OnLongClickListener openListener = new View.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            SwipeLayout swipeLayout =((SwipeLayout)v);
            if(swipeLayout.getOpenStatus().equals(SwipeLayout.Status.Open)){
                swipeLayout.close();
            }
            else {
                swipeLayout.open();
            }
            return true;
        }
    };

    private SwipeLayout.SwipeListener swipeListener = new SwipeLayout.SwipeListener() {
        @Override
        public void onStartOpen(SwipeLayout layout) {
            lastSwipeEventTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onOpen(SwipeLayout layout) {
            if(isTalkbackOn()){
                LinearLayout view = (LinearLayout)layout.findViewById(R.id.hidden_layout);
                view.requestFocus();
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
            lastSwipeEventTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onStartClose(SwipeLayout layout) {
            lastSwipeEventTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onClose(SwipeLayout layout) {
            lastSwipeEventTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

        }

        @Override
        public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
        }
    };

}
