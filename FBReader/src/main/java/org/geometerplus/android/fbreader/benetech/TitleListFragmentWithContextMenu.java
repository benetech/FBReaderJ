package org.geometerplus.android.fbreader.benetech;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.network.bookshare.BookshareDeveloperKey;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Book_Details;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by animal@martus.org on 5/2/16.
 */
abstract public class TitleListFragmentWithContextMenu extends ListFragment implements SortUtil.SortChangesListener{

    private static final int BOOK_INFO_REQUEST = 1;
    private static final int OPEN_BOOK_ITEM_ID = 0;
    private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
    private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
    private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
    private static final int DELETE_BOOK_ITEM_ID = 4;

    protected ArrayList<AbstractTitleListRowItem> bookRowItems;

    private static final String URI_BOOKSHARE_ID_SEARCH = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/book/id/";
    private static final int START_BOOKSHARE_BOOK_DETAILS_ACTIVITY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookRowItems = new ArrayList<>();
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


    protected void sortListItems() {
        Collections.sort(bookRowItems, SortUtil.getComparator());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        Book bookClicked = bookRowItems.get(position).getBook();
        if (bookClicked == null) {
            return super.onContextItemSelected(item);
        }

        return onContextItemSelected(item.getItemId(), bookClicked);
    }

    private boolean onContextItemSelected(int itemId, Book book) {
        switch (itemId) {
            case OPEN_BOOK_ITEM_ID:
                openBook(book);
                return true;
            case SHOW_BOOK_INFO_ITEM_ID:
                showBookInfo(book);
                return true;
            case ADD_TO_FAVORITES_ITEM_ID:
                Library.Instance().addBookToFavorites(book);
                return true;
            case REMOVE_FROM_FAVORITES_ITEM_ID:
                Library.Instance().removeBookFromFavorites(book);
                getListView().invalidateViews();
                return true;
            case DELETE_BOOK_ITEM_ID:
                tryToDeleteBook(book);
        }
        return false;
    }

    private void openBook(Book book) {
        Intent intent = new Intent(getActivity().getApplicationContext(), FBReaderWithNavigationBar.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void tryToDeleteBook(Book book) {
        final ZLResource dialogResource = ZLResource.resource("dialog");
        final ZLResource buttonResource = dialogResource.getResource("button");
        final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
        new AlertDialog.Builder(getActivity())
                .setTitle(book.getTitle())
                .setMessage(boxResource.getResource("message").getValue())
                .setIcon(0)
                .setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book, Library.REMOVE_FROM_DISK))
                .setNegativeButton(buttonResource.getResource("no").getValue(), null)
                .create().show();
    }

    private class BookDeleter implements DialogInterface.OnClickListener {
        private final Book myBook;
        private final int myMode;

        BookDeleter(Book book, int removeMode) {
            myBook = book;
            myMode = removeMode;
        }

        public void onClick(DialogInterface dialog, int which) {
            deleteBook(myBook, myMode);
        }
    }

    private void deleteBook(Book book, int mode) {
        Library.Instance().removeBook(book, mode);
        AbstractTitleListRowItem rowItemToRemove = findBookRowItem(book);
        ((ArrayAdapter) getListView().getAdapter()).remove(rowItemToRemove);
        getListView().invalidateViews();
        ((BaseAdapter) getListView().getAdapter()).notifyDataSetChanged();

    }

    private AbstractTitleListRowItem findBookRowItem(Book bookToMatch) {
        for (AbstractTitleListRowItem bookListRowItem : bookRowItems) {
            if (bookListRowItem.getBook().getId() == bookToMatch.getId())
                return bookListRowItem;
        }

        return null;
    }

    protected void showBookInfo(Book book) {
        Intent intent = new Intent(getActivity().getApplicationContext(), BookInfoActivity.class);
        intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, book.File.getPath());
        startActivityForResult(intent, BOOK_INFO_REQUEST);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        AbstractTitleListRowItem item = (AbstractTitleListRowItem) getListAdapter().getItem(position);
        final Book book = item.getBook();
        if (book != null) {
            createBookContextMenu(menu, book);
        }
    }

    private void createBookContextMenu(ContextMenu menu, Book book) {
        final ZLResource resource = Library.resource();
        menu.setHeaderTitle(book.getTitle());
        menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
        menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, resource.getResource("showBookInfo").getValue());
        if (Library.Instance().isBookInFavorites(book)) {
            menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, resource.getResource("removeFromFavorites").getValue());
        } else {
            menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, resource.getResource("addToFavorites").getValue());
        }
        if ((Library.Instance().getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
            menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int returnCode, Intent intent) {
        if (requestCode == BOOK_INFO_REQUEST && intent != null) {
            getListView().invalidateViews();
        } else {
            super.onActivityResult(requestCode, returnCode, intent);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AbstractTitleListRowItem clickedRowItem = bookRowItems.get(position);
        if (clickedRowItem.isDownloadedBook()) {
            Intent intent = new Intent(getActivity().getApplicationContext(), BookInfoActivity.class);
            intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, clickedRowItem.getBookFilePath());
            startActivity(intent);
        }
        else {
            showBookDetailsPageWithDownloadButton(clickedRowItem.getBookId());
        }
    }

    protected void showBookDetailsPageWithDownloadButton(long bookshareId) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = defaultSharedPreferences.getString(Bookshare_Webservice_Login.USER, "");
        String password = defaultSharedPreferences.getString(Bookshare_Webservice_Login.PASSWORD, "");
        Intent intent = new Intent(getActivity().getApplicationContext(),Bookshare_Book_Details.class);
        String uri;
//FIXME urgent alyways setting isDownloadable to true.  This needs to change to mimic current book details behavior.
        // Update by Miguel Villalobos: Going with the following logic. If user is logged in keep old behavior of always downloadable.
        // If no user is logged in assume books cant be downloaded (since they are form some user's list)
        if(username != null && username.length() > 0) {
            intent.putExtra("isDownloadable", true);
            uri = createUriForKnownUser(bookshareId, username);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
        }
        else {
            intent.putExtra("isDownloadable", false);
            uri = createUriForNoUser(bookshareId);
        }
        intent.putExtra("ID_SEARCH_URI", uri);

        startActivityForResult(intent, START_BOOKSHARE_BOOK_DETAILS_ACTIVITY);
    }

    @NonNull
    private String createUriForKnownUser(long bookshareId, String username) {
        return URI_BOOKSHARE_ID_SEARCH + bookshareId +"/for/"+username+"?api_key="+ BookshareDeveloperKey.DEVELOPER_KEY;
    }

    @NonNull
    private String createUriForNoUser(long bookshareId) {
        return URI_BOOKSHARE_ID_SEARCH + bookshareId + "?api_key="+ BookshareDeveloperKey.DEVELOPER_KEY;
    }


    @Override
    public void onSortChanged(){
        sortListItems();
        ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onForceRefresh(){
        bookRowItems.clear();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }


    abstract protected void fillListAdapter();
}
