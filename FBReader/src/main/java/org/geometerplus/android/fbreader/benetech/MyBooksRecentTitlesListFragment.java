package org.geometerplus.android.fbreader.benetech;

import android.view.View;
import android.widget.ListView;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.library.FileInfoSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by animal@martus.org on 4/28/16.
 */
public class MyBooksRecentTitlesListFragment extends TitleListFragmentWithContextMenu {

    @Override
    protected void fillListAdapter() {
        final BooksDatabase database = BooksDatabase.Instance();
        final Map<Long,Book> savedBooksByFileId = database.loadBooks(new FileInfoSet(), true);
        final Map<Long,Book> savedBooksByBookId = new HashMap<>();
        for (Book book : savedBooksByFileId.values()) {
            savedBooksByBookId.put(book.getId(), book);
        }

        for (long bookId : database.loadRecentBookIds()) {
            Book book = savedBooksByBookId.get(bookId);
            if (book == null) {
                book = Book.getById(bookId);
                if (book != null && !book.File.exists()) {
                    book = null;
                }
            }
            if (book != null) {
                bookRowItems.add(new DownloadedTitleListRowItem(book));
            }
        }

        BookListAdapter adapter = new BookListAdapter(getActivity(), bookRowItems);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Book bookClicked = bookRowItems.get(position).getBook();
        showBookInfo(bookClicked);
    }
}
