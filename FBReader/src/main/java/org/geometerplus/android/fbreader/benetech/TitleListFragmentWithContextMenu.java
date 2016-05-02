package org.geometerplus.android.fbreader.benetech;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 5/2/16.
 */
abstract public class TitleListFragmentWithContextMenu extends ListFragment {

    private static final int BOOK_INFO_REQUEST = 1;
    private static final int OPEN_BOOK_ITEM_ID = 0;
    private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
    private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
    private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
    private static final int DELETE_BOOK_ITEM_ID = 4;

    protected ArrayList<AbstractTitleListRowItem> bookRowItems;

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

    abstract protected void fillListAdapter();
}