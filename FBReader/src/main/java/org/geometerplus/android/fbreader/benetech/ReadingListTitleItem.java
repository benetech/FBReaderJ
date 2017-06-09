package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.android.fbreader.ReadingListAllowanceHelper;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.util.Date;
import java.util.Set;

/**
 * Created by animal@martus.org on 5/2/16.
 */
public class ReadingListTitleItem extends AbstractTitleListRowItem {
    private long bookshareId;
    private String readingListBookName;
    private String readingListBookAuthors;
    private Date compareDate;
    private Book downloadedBook = null;
    private Set<String> allows;

    public ReadingListTitleItem(long bookshareIdToUse, String readingListNameToUse, String readingListBookAuthorsToUse, Date compareDate, Book book, Set<String> allows) {
        bookshareId = bookshareIdToUse;
        readingListBookName = readingListNameToUse;
        readingListBookAuthors = readingListBookAuthorsToUse;
        downloadedBook = book;
        this.allows = allows;
        this.compareDate = compareDate;
    }

    @Override
    public long getBookId() {
        return bookshareId;
    }

    @Override
    public String getBookTitle() {
        return readingListBookName;
    }

    @Override
    public String getAuthors() {
        return readingListBookAuthors;
    }

    @Override
    public ZLFile getBookZlFile() {
        if(downloadedBook == null) {
            return null;
        }
        else {
            return downloadedBook.File;
        }
    }

    @Override
    public Book getBook() {
        return downloadedBook;
    }
    public void setBook(Book book) {
        downloadedBook = book;
    }

    public String getBookFilePath() {
        if(downloadedBook == null) {
            return null;
        }
        else {
            return downloadedBook.File.getPath();
        }
    }

    @Override
    public boolean isDownloadedBook() {
        return downloadedBook != null;
    }

    @Override
    public Date getCompareDate(){
        return compareDate;
    }

    @Override
    public boolean canDeleteFromReadinglist() {
        return allows.contains(ReadingListAllowanceHelper.DELETE);
    }
}
