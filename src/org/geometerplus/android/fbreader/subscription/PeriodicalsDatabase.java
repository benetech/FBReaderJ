package org.geometerplus.android.fbreader.subscription;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

public abstract class PeriodicalsDatabase {

	public abstract void insertEntity(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity);
	public abstract PeriodicalEntity getEntity(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity);
	public abstract List<PeriodicalEntity> getAllEntities(SQLiteDatabase db,String tableName);
	public abstract PeriodicalEntity deleteEntity(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity);
}
