package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.library.Author;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

/**
 * Created by animal@martus.org on 4/26/16.
 */
public class BookListRowItem {
    private Book book;

    public BookListRowItem(Book bookToUse) {
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
        final StringBuilder buffer = new StringBuilder();
        for (Author author: getBook().authors()) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(author.DisplayName);
        }

        return buffer.toString();
    }
}
