package org.geometerplus.android.fbreader.subscription;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BooksharePeriodicalDataSource extends PeriodicalsDatabase{

	private static BooksharePeriodicalDataSource dSource;
	//private PeriodicalsSQLiteHelper dbHelper;
	private String[] subscribedAllColumns = new String[]{PeriodicalsSQLiteHelper.COLUMN_ID,PeriodicalsSQLiteHelper.COLUMN_TITLE,PeriodicalsSQLiteHelper.SUBSCRIBED_COLUMN_LATEST_EDITION,PeriodicalsSQLiteHelper.SUBSCRIBED_COLUMN_LATEST_REVISION};
	private String[] allPAllColumns = new String[]{PeriodicalsSQLiteHelper.COLUMN_ID,PeriodicalsSQLiteHelper.COLUMN_TITLE,
			PeriodicalsSQLiteHelper.ALL_P_COLUMN_EDITION,PeriodicalsSQLiteHelper.ALL_P_COLUMN_REVISION,
			PeriodicalsSQLiteHelper.ALL_P_COLUMN_DOWNLOADED_DATE,PeriodicalsSQLiteHelper.ALL_P_COLUMN_DOWNLOADED_TIME};

	private BooksharePeriodicalDataSource(Context context){		
	}

	public static BooksharePeriodicalDataSource getInstance(Context context){
		
		if(dSource == null){
			dSource = new BooksharePeriodicalDataSource(context);
		}
		return dSource;
	}

	@Override
	public void insertEntity(SQLiteDatabase db, String tableName,PeriodicalEntity pEntity) {
		
		String[] allCols = null;
		Cursor cursor=null;
		String periodicalId= pEntity.getId();
		
		if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
			allCols= subscribedAllColumns;
			cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "= ?", new String[]{periodicalId}, null, null, null);
		}
		else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			allCols = allPAllColumns; 
			AllDbPeriodicalEntity allEntity = (AllDbPeriodicalEntity) pEntity;
			cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "= ? "
					+"AND "+PeriodicalsSQLiteHelper.ALL_P_COLUMN_EDITION + "= ? "
					+"AND "+PeriodicalsSQLiteHelper.ALL_P_COLUMN_REVISION + "= ? ", new String[]{periodicalId,allEntity.getEdition(),allEntity.getRevision()+""}, null, null, null);
		}
		
		
		

		ContentValues values = composeCValue(pEntity);
		
		//TODO: Need to see whether edition is greater than the previous or not before updating
		//TODO: for subscribed periodic table
		//insert only if the entry is not there already in the db
		if(cursor.getCount()<=0){
			db.insert(tableName, null, values);
		}else{
			db.update(tableName, values, PeriodicalsSQLiteHelper.COLUMN_ID+"= ?", new String[]{periodicalId});
		}
		
		cursor.close();
	}
	
	private ContentValues composeCValue(PeriodicalEntity pEntity){
		
		ContentValues values=new ContentValues();
		values.put(PeriodicalsSQLiteHelper.COLUMN_ID, pEntity.getId());
		values.put(PeriodicalsSQLiteHelper.COLUMN_TITLE, pEntity.getTitle());
		
		if(pEntity instanceof SubscribedDbPeriodicalEntity){
			SubscribedDbPeriodicalEntity entity = (SubscribedDbPeriodicalEntity) pEntity;
			values.put(PeriodicalsSQLiteHelper.SUBSCRIBED_COLUMN_LATEST_EDITION, entity.getLatestEdition());
			values.put(PeriodicalsSQLiteHelper.SUBSCRIBED_COLUMN_LATEST_REVISION, entity.getLatestRevision());
			
		}else if(pEntity instanceof AllDbPeriodicalEntity){
			AllDbPeriodicalEntity entity = (AllDbPeriodicalEntity)pEntity;
			values.put(PeriodicalsSQLiteHelper.ALL_P_COLUMN_EDITION, entity.getEdition());
			values.put(PeriodicalsSQLiteHelper.ALL_P_COLUMN_REVISION, entity.getRevision());
			values.put(PeriodicalsSQLiteHelper.ALL_P_COLUMN_DOWNLOADED_DATE, entity.getDwnldDate()+"");
			values.put(PeriodicalsSQLiteHelper.ALL_P_COLUMN_DOWNLOADED_TIME, entity.getDwnldTime()+ "");
		}
		return values;
	}
	

	@Override
	public PeriodicalEntity getEntity(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity) {
		String[] allCols = null;
		if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
			allCols= subscribedAllColumns;
		}
		else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			allCols = allPAllColumns; 
		}
		
		Cursor cursor = null;
		if(pEntity instanceof AllDbPeriodicalEntity){
			AllDbPeriodicalEntity allEntity = (AllDbPeriodicalEntity)pEntity;
			cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "= ?"
					+" AND "+PeriodicalsSQLiteHelper.ALL_P_COLUMN_EDITION +"= ?"
					+" AND "+PeriodicalsSQLiteHelper.ALL_P_COLUMN_REVISION+"= ?", 
					new String[]{allEntity.getId(),allEntity.getEdition(),allEntity.getRevision()+""}, null, null, null);
		}else if(pEntity instanceof SubscribedDbPeriodicalEntity){
			cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "="+pEntity.getId(), null, null, null, null);
		}
		if(cursor != null && cursor.getCount()>0){
			cursor.moveToFirst();
			PeriodicalEntity entity = PeriodicalFromCursorFactory.getPeriodicalEntityInstance(tableName,cursor);
			cursor.close();
			return entity;
		}
		return null;
		
	}

	
	public List<PeriodicalEntity> getEntityByIdOnly(SQLiteDatabase db,String tableName,String id) {
		String[] allCols = null;
		ArrayList<PeriodicalEntity> entities = new ArrayList<PeriodicalEntity>();
		if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
			allCols= subscribedAllColumns;
		}
		else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			allCols = allPAllColumns; 
		}
		
		Cursor cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "= ?", new String[]{id}, null, null, null);
		
		if(cursor != null && cursor.getCount()>0){
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				entities.add(PeriodicalFromCursorFactory.getPeriodicalEntityInstance(tableName,cursor));
				cursor.moveToNext();
			}
			cursor.close();
			
		}
		return entities;
		
	}
	
	/** This method checks whether a specified entry exists in the table in the database 
	 * @param db
	 * @param tableName
	 * @param pEntity If entity is AllDbPeriodicalEntity match id,edition,revision. If entity is SubscribedPeriodicalEntity match id only
	 * @return Whether entry exists or not
	 */
	public boolean doesExist(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity){
		if(pEntity instanceof AllDbPeriodicalEntity){
			AllDbPeriodicalEntity allEntity = (AllDbPeriodicalEntity)pEntity;
			if(allEntity.getId() != null || allEntity.getEdition() != null || allEntity.getRevision() > 0){
				if(getEntity(db, tableName, allEntity)!=null){
					return true;
				}
			}
		}else if (pEntity instanceof SubscribedDbPeriodicalEntity){
			SubscribedDbPeriodicalEntity subEntity = (SubscribedDbPeriodicalEntity)pEntity;
			if(subEntity.getId() != null){
				if(getEntity(db, tableName, subEntity)!=null){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public List<PeriodicalEntity> getAllEntities(SQLiteDatabase db, String tableName) {
		ArrayList<PeriodicalEntity> entities = new ArrayList<PeriodicalEntity>();
		
		String[] allCols = null;
		if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
			allCols= subscribedAllColumns;
		}
		else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			allCols = allPAllColumns; 
		}
		
		Cursor cursor = db.query(tableName, allCols, null, null, null, null, null);
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			entities.add(PeriodicalFromCursorFactory.getPeriodicalEntityInstance(tableName, cursor));
			cursor.moveToNext();
		}
		
		return entities;
		
	}
	
	
	/**
	 * @param db
	 * @param tableName
	 * @param pEntity A PeriodicalEntity with the periodical content ID
	 * @return Most recent edition of that particular periodical
	 */
	public AllDbPeriodicalEntity getMostRecentEdition(SQLiteDatabase db,String tableName,String id){
		if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			ArrayList<AllDbPeriodicalEntity> entities = new ArrayList<AllDbPeriodicalEntity>();
			String[] allCols = null;
			if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
				allCols= subscribedAllColumns;
			}
			else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
				allCols = allPAllColumns; 
			}
			Cursor cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "= ?", new String[]{id}, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				entities.add((AllDbPeriodicalEntity)PeriodicalFromCursorFactory.getPeriodicalEntityInstance(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS, cursor));
				cursor.moveToNext();
				//TODO: Implement comparer which gives the entity with highest edition
			}
			cursor.close();
			return PeriodicalDBUtils.getMostRecentEdition(entities);
		}
		return null;
	}

	@Override
	public PeriodicalEntity deleteEntity(SQLiteDatabase db,String tableName,PeriodicalEntity pEntity) {
		String[] allCols = null;
		if(PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS.equals(tableName)){
			allCols= subscribedAllColumns;
		}
		else if(PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS.equals(tableName)){
			allCols = allPAllColumns; 
		}
		Cursor cursor = db.query(tableName, allCols, PeriodicalsSQLiteHelper.COLUMN_ID + "="+pEntity.getId(), null, null, null, null);
		cursor.moveToFirst();
		
		PeriodicalEntity delEntry=PeriodicalFromCursorFactory.getPeriodicalEntityInstance(tableName, cursor);
		db.delete(tableName, PeriodicalsSQLiteHelper.COLUMN_ID+ " = " + pEntity.getId(), null);		
		cursor.close();
		return delEntry;
	}
}
