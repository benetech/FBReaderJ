package org.geometerplus.android.fbreader.library;

import android.app.Application;
import android.content.Context;

import org.benetech.android.BuildConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
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
        database = new SQLLiteBooksDatabaseForTesting(context, "");
    }

    @Test
        public void testDatabaseVersionAfterMigration() throws InterruptedException {
        assertEquals("incorrect database version?", SQLiteBooksDatabase.CURRENT_VERSION, database.getDatabaseVersion());
    }

    @Test
    public void testDatabaseMigrationToVersion19() {
        assertEquals("incorrect database version after migration?", 19, database.getDatabaseVersion());
    }
}
