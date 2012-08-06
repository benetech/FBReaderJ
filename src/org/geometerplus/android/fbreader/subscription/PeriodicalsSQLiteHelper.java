package org.geometerplus.android.fbreader.subscription;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PeriodicalsSQLiteHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "periodicals.db";	//db name
	
	public static final String COLUMN_ID = "id";					//table column id
	public static final String COLUMN_TITLE = "title";				//table columns...
	
	public static final String TABLE_ALL_PERIODICALS = "all_downloaded_periodicals";	//table name
	public static final String ALL_P_COLUMN_EDITION = "edition";
	public static final String ALL_P_COLUMN_REVISION = "revision";
	public static final String ALL_P_COLUMN_DOWNLOADED_TIME = "download_time";
	public static final String ALL_P_COLUMN_DOWNLOADED_DATE = "download_date";

	public static final String TABLE_SUBSCRIBED_PERIODICALS = "subscribed_periodicals";	//table name
	public static final String SUBSCRIBED_COLUMN_LATEST_EDITION = "latest_edition";
	public static final String SUBSCRIBED_COLUMN_LATEST_REVISION = "latest_revision";	

	// Database creation sql statement
	private static final String CREATE_ALL_PERIODICALS_TABLE = "create table if not exists "
			+ TABLE_ALL_PERIODICALS + "(" 
			+ COLUMN_ID + " text not null, " 
			+ COLUMN_TITLE + " text not null, " 
			+ ALL_P_COLUMN_EDITION + " text not null, " 
			+ ALL_P_COLUMN_REVISION + " int not null, "
			+ ALL_P_COLUMN_DOWNLOADED_DATE+ " text, "
			+ ALL_P_COLUMN_DOWNLOADED_TIME+ " text, " 
			+" primary key ("+ COLUMN_ID +", "
			+ ALL_P_COLUMN_EDITION + ", "
			+ ALL_P_COLUMN_REVISION +")"
			+");";

	// Database creation sql statement
	private static final String CREATE_SUBSCRIBED_PERIODICALS_TABLE = "create table if not exists "
			+ TABLE_SUBSCRIBED_PERIODICALS + "(" + COLUMN_ID + " text primary key, " 
			+ COLUMN_TITLE + " text not null," 
			+ SUBSCRIBED_COLUMN_LATEST_EDITION + " text not null," 
			+ SUBSCRIBED_COLUMN_LATEST_REVISION + " int not null"+");";

	
	public PeriodicalsSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//when db is created do the following
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ALL_PERIODICALS_TABLE);
		db.execSQL(CREATE_SUBSCRIBED_PERIODICALS_TABLE);
		
		Log.i(getClass().getName(), "Created table with: " + CREATE_ALL_PERIODICALS_TABLE);
		Log.i(getClass().getName(), "Created table with: " + CREATE_SUBSCRIBED_PERIODICALS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_PERIODICALS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIBED_PERIODICALS);
		onCreate(db);
	}
	
	

}
