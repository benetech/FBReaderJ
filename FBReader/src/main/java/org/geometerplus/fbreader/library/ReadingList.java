package org.geometerplus.fbreader.library;

import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 3/2/16.
 */
public class ReadingList {

    private Long id;
    private String readingListName;
    private ArrayList<Long> bookIds;

    public ReadingList() {
        bookIds = new ArrayList<>();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setReadingListName(String readingListName) {
        this.readingListName = readingListName;
    }

    public ArrayList<Long> getBooksIds() {
        return new ArrayList<>(bookIds);
    }

    public void addBook(Long bookIdToAdd) {
        bookIds.add(bookIdToAdd);
    }

    public void save() throws Exception {
        AbstractSQLiteBooksDatabase database = (AbstractSQLiteBooksDatabase) SQLiteBooksDatabase.Instance();
        database.saveReadingList(this);
    }

    public void addBooks(List<Long> bookIdsToAdd) {
        bookIds.addAll(bookIdsToAdd);
    }
}
