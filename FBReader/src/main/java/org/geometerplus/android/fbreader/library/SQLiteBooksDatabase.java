package org.geometerplus.android.fbreader.library;

import android.content.Context;

import org.geometerplus.android.util.UIUtil;

/**
 * Created by animal@martus.org on 3/3/16.
 */
public final class SQLiteBooksDatabase extends AbstractSQLiteBooksDatabase {

    public SQLiteBooksDatabase(Context context, String instanceId) {
        super(context, instanceId);
    }

    @Override
    protected void migrateDatabase(Context context, final int version) {
        UIUtil.wait((version == 0) ? "creatingBooksDatabase" : "updatingBooksDatabase", new Runnable() {
            public void run() {
                migrateDatabase(version);
            }
        }, context);
    }
}
