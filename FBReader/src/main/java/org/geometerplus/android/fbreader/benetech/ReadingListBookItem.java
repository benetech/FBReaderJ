package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

/**
 * Created by animal@martus.org on 5/2/16.
 */
public class ReadingListBookItem extends AbstractTitleListRowItem {
    private String readingListBookName;
    private String readingListBookAuthors;

    public ReadingListBookItem(String readingListNameToUse, String readingListBookAuthorsToUse) {
        readingListBookName = readingListNameToUse;
        readingListBookAuthors = readingListBookAuthorsToUse;
    }

    public String getBookTitle() {
        return readingListBookName;
    }

    public String getAuthors() {
        return readingListBookAuthors;
    }

    @Override
    public ZLFile getBookZlFile() {
        return null;
    }

    @Override
    public Book getBook() {
        return null;
    }

    public String getBookFilePath() {
        return null;
    }
}