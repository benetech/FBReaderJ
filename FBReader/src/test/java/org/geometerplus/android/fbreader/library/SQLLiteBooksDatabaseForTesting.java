package org.geometerplus.android.fbreader.library;

import android.content.Context;

/**
 * Created by animal@martus.org on 3/3/16.
 */
public class SQLLiteBooksDatabaseForTesting extends AbstractSQLiteBooksDatabase {
    public SQLLiteBooksDatabaseForTesting(Context context, String instanceId) {
        super(context, instanceId);
    }

    @Override
    protected void migrateDatabase(Context context, final int version) {
        migrateDatabase(version);
    }
}
