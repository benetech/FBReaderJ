package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.util.Date;

/**
 * Created by animal@martus.org on 5/2/16.
 */
public class ReadingListTitleItem extends AbstractTitleListRowItem {
    private int bookshareId;
    private String readingListBookName;
    private String readingListBookAuthors;
    private Date compareDate;
    private Book downloadedBook = null;

    public ReadingListTitleItem(int bookshareIdToUse, String readingListNameToUse, String readingListBookAuthorsToUse, Date compareDate, Book book) {
        bookshareId = bookshareIdToUse;
        readingListBookName = readingListNameToUse;
        readingListBookAuthors = readingListBookAuthorsToUse;
        downloadedBook = book;
        this.compareDate = compareDate;
    }

    @Override
    public int getBookId() {
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
}