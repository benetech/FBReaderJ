package org.geometerplus.android.fbreader.library;

import android.app.Application;
import android.content.Context;

import org.benetech.android.BuildConfig;
import org.geometerplus.fbreader.library.ReadingList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created by animal@martus.org on 2/29/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TestSQLiteBooksDatabase  {

    private Context context;
    private SQLLiteBooksDatabaseForTesting database;

    public TestSQLiteBooksDatabase() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Application application = RuntimeEnvironment.application;
        context = application.getApplicationContext();
        assertNotNull("context should not be null?", context);
        database = new SQLLiteBooksDatabaseForTesting(context);
    }

    @Test
        public void testDatabaseVersionAfterMigration() throws InterruptedException {
        assertEquals("incorrect database version?", SQLiteBooksDatabase.CURRENT_DB_VERSION, database.getDatabaseVersion());
    }

    @Test
    public void testDatabaseMigrationToVersion19() {
        assertEquals("incorrect database version after migration?", 19, database.getDatabaseVersion());
    }

    @Test
    public void testReadlingListsTable() throws Exception {
        assertEquals("Reading lists should be empty?", 0, database.getAllReadingLists().size());
    }

    @Test
    public void testReadingListWithoutBooks() throws Exception {
        database.insertEmptyReadingList("Testing Reading List");
        ArrayList<ReadingList> readingLists = database.getAllReadingLists();
        assertEquals("Incorrect reading list size?", 1, readingLists.size());

        final ReadingList readingList = readingLists.get(0);
        assertTrue("Reading list's book list should be empty?", readingList.getBooksIds().isEmpty());
    }

    @Test
    public void testReadingListWithBooks() throws Exception {
        ReadingList newReadingList = database.insertEmptyReadingList("Testing Reading List with one book");
        final long NON_EXISTING_RANDOM_BOOK_ID = 1001;
        newReadingList.addBook(NON_EXISTING_RANDOM_BOOK_ID);
        newReadingList.save();

        ArrayList<ReadingList> readingLists = database.getAllReadingLists();
        ReadingList readingList = readingLists.get(0);
        final ArrayList<Long> booksIds = readingList.getBooksIds();
        assertEquals("Incorrect book count?", 1, booksIds.size());
        assertEquals("Incorrect book id saved?", NON_EXISTING_RANDOM_BOOK_ID, booksIds.get(0).longValue());
    }
}
