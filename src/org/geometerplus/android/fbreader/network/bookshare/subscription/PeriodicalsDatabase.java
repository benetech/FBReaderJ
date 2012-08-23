package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

/**
 * Has all the absctract db helper methods
 * 
 * @author thushan
 * 
 */
public abstract class PeriodicalsDatabase {

	public abstract void insertEntity(SQLiteDatabase db, String tableName,
			PeriodicalEntity pEntity);

	public abstract PeriodicalEntity getEntity(SQLiteDatabase db,
			String tableName, PeriodicalEntity pEntity);

	/**
	 * Get all the entities in the database table specified
	 * 
	 * @param db
	 * @param tableName
	 * @return
	 */
	public abstract List<PeriodicalEntity> getAllEntities(SQLiteDatabase db,
			String tableName);

	/**
	 * Delete an entry in the db
	 * 
	 * @param db
	 * @param tableName
	 * @param pEntity
	 * @return
	 */
	public abstract PeriodicalEntity deleteEntity(SQLiteDatabase db,
			String tableName, PeriodicalEntity pEntity);
}
