package org.geometerplus.android.fbreader.benetech;

import android.support.annotation.NonNull;

import org.geometerplus.fbreader.library.Author;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.util.List;


/**
 * Created by animal@martus.org on 4/26/16.
 */
public class DownloadedTitleListRowItem extends AbstractTitleListRowItem {
    private Book book;

    public DownloadedTitleListRowItem(Book bookToUse) {
        book = bookToUse;
    }

    public String getBookTitle() {
        return getBook().getTitle();
    }

    public String getAuthors() {
        return concatinateAuthors();
    }

    public String getBookFilePath() {
        return getBook().File.getPath();
    }

    public ZLFile getBookZlFile() {
        return getBook().File;
    }

    public Book getBook() {
        return book;
    }

    private String concatinateAuthors() {
        List<Author> authors = getBook().authors();
        return concatenateAuthorNames(authors);
    }

    @NonNull
    public static String concatenateAuthorNames(List<Author> authors) {
        final StringBuilder buffer = new StringBuilder();
        for (Author author: authors) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(author.DisplayName);
        }

        return buffer.toString();
    }

    @Override
    public boolean isDownloadedBook() {
        return true;
    }

    @Override
    public int getBookId() {
        return (int) getBook().getId();
    }
}
