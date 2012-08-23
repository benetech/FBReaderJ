package org.geometerplus.android.fbreader.network.bookshare.subscription;

import android.database.Cursor;

/**
 * Pass table name and cursor and get an object out
 * 
 * @author thushan
 * 
 */
public class PeriodicalFromCursorFactory {

	private PeriodicalFromCursorFactory() {
	}

	public static PeriodicalEntity getPeriodicalEntityInstance(
			String tableName, Cursor cursor) {
		if (PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)) {
			if (cursor.getCount() > 0) {
				AllDbPeriodicalEntity entity = new AllDbPeriodicalEntity();
				entity.setId(cursor.getString(0));
				entity.setTitle(cursor.getString(1));
				entity.setEdition(cursor.getString(2));
				entity.setRevision(cursor.getInt(3));
				entity.setDwnldDate(cursor.getString(4));
				entity.setDwnldTime(cursor.getString(5));

				return entity;
			}
		} else if (PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS
				.equals(tableName)) {

			if (cursor.getCount() > 0) {
				SubscribedDbPeriodicalEntity entity = new SubscribedDbPeriodicalEntity();
				entity.setId(cursor.getString(0));
				entity.setTitle(cursor.getString(1));
				entity.setLatestEdition(cursor.getString(2));
				entity.setLatestRevision(cursor.getInt(3));

				return entity;
			}

		}
		return null;
	}
}
