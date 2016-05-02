package org.geometerplus.android.fbreader.benetech;

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
}