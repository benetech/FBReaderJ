package org.geometerplus.android.fbreader.library;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import org.benetech.android.BuildConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by animal@martus.org on 2/29/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TestSQLiteBooksDatabase  {

    private Context context;
    private SQLiteBooksDatabase database;

    public TestSQLiteBooksDatabase() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Application application = RuntimeEnvironment.application;
        context = application.getApplicationContext();
        assertNotNull("context should not be null?", context);
        database = new SQLiteBooksDatabase(context, "");
        sleepToAllowDatabaseToFinishMigration();
    }

    @Test
    public void testDatabaseVersionAfterMigration() throws InterruptedException {
        assertEquals("incorrect database version?", SQLiteBooksDatabase.currentVersion, database.getDatabaseVersion());
    }

    private void sleepToAllowDatabaseToFinishMigration() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
