package org.geometerplus.android.fbreader.benetech;

import android.view.View;
import android.widget.ListView;

import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.bookshare.BookshareApiV1UserHistoryRetriever;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Result_Bean;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.FileInfoSet;
import org.geometerplus.fbreader.library.ReadingListBook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by animal@martus.org on 4/28/16.
 */
public class MyBooksRecentTitlesListFragment extends TitleListFragmentWithContextMenu implements AsyncResponse<String> {

    private BookshareApiV1UserHistoryRetriever bookshareHistoryRetriever;

    @Override
    protected void fillListAdapter() {
        bookshareHistoryRetriever = new BookshareApiV1UserHistoryRetriever(getActivity(), this);

        final SQLiteBooksDatabase database = (SQLiteBooksDatabase)SQLiteBooksDatabase.Instance();
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
                Date date = database.findLastAccessedDateForBook(book);
                book.setLastAccessedDate(date);

                bookRowItems.add(new DownloadedTitleListRowItem(book));
            }
        }

        recreateAdapterWithUpdatedRows();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AbstractTitleListRowItem abstractTitleListRowItem = bookRowItems.get(position);
        if (abstractTitleListRowItem.isDownloadedBook()) {
            Book bookClicked = abstractTitleListRowItem.getBook();
            showBookInfo(bookClicked);
        }
        else {
            showBookDetailsPageWithDownloadButton(abstractTitleListRowItem.getBookId());
        }
    }

    @Override
    public void processFinish(String type) {
        Vector<Bookshare_Result_Bean> resultBeans = bookshareHistoryRetriever.getResultBeans();
        for (Bookshare_Result_Bean bean : resultBeans) {
            List<String> strings = Arrays.asList(bean.getAuthor());
            String concatinatedAuthors = ReadingListBook.getAllAuthorsAsString(strings);
            String beanId = bean.getId();
            final int bookId = Integer.parseInt(beanId);
            Date downloadDate = dateFromString(bean.getDownloadDateString());
            Book book = null;
            if(getActivity() instanceof MyBooksActivity){
                try {
                    HashMap<Long, Book> downloadedBooks = ((MyBooksActivity)getActivity()).getDownloadedBooksMap();
                    book = downloadedBooks.get(bookId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ReadingListTitleItem newItem = new ReadingListTitleItem(bookId, bean.getTitle(), concatinatedAuthors, downloadDate, book);
            if(!bookRowItems.contains(newItem)){ //this will prevent us from adding duplicates unless they are actually different books for bookshare
                bookRowItems.add(newItem);
            }
        }

        recreateAdapterWithUpdatedRows();
    }

    private Date dateFromString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
        Date date = null;
        if(dateString != null) {
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }



    private void recreateAdapterWithUpdatedRows() {
        sortListItems();
        BookListAdapter adapter = new BookListAdapter(getActivity(), bookRowItems);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }
}
