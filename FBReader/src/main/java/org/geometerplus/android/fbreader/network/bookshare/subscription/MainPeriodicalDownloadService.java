
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.util.ArrayList;
import java.util.Vector;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.network.bookshare.BookshareDeveloperKey;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.FBReaderApp.AutomaticDownloadType;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Main automatic dowload service
 * 
 * @author thushan
 * 
 */
public class MainPeriodicalDownloadService extends IntentService implements
PeriodicalEditionListener,PeriodicalMetadataListener{

	// SubscriptionSQLiteHelper dbHelper;
	BooksharePeriodicalDataSource dataSource;
	PeriodicalsSQLiteHelper dbHelper;
	private String username;
	private String password;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;

	private SQLiteDatabase periodicalDb;
	private PeriodicalEditionListFetcher editionFetcher;
	private int currIdsIndex = 0;	//this variable is to keep track of for how many periodicals download started
	
	private String usernameKey = "username";
	private String passwordKey = "password";
	private AutomaticDownloadType downType;
	private ArrayList<String> ids;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		periodicalDb.close();
	}

	public MainPeriodicalDownloadService() {
		super("MainPeriodicalDownloadService");

	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(FBReader.LOG_LABEL, "start onHandleIntent of MainPeriodicalDownloadService");

		if(developerKey==null || developerKey.length()==0){
			Log.e(FBReader.LOG_LABEL,
					"Bookshare Developer Key is not defined. Terminating the service");
			return;
		}

		SharedPreferences logingPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		username = logingPrefs.getString(usernameKey, "");
		password = logingPrefs.getString(passwordKey, "");

		Log.i(FBReader.LOG_LABEL, "about to set dataSource in onStart");

				dataSource = BooksharePeriodicalDataSource
						.getInstance(getApplicationContext());
				dbHelper = new PeriodicalsSQLiteHelper(getApplicationContext());

				Log.i(FBReader.LOG_LABEL, "about to instantiate periodicalDB in onStart");
				// instantiate periodical db only if its null or is not opened currently
		        if (periodicalDb == null) {
		            periodicalDb = dbHelper.getWritableDatabase();
		        } else {
		            if (!periodicalDb.isOpen()) {
		                periodicalDb = dbHelper.getWritableDatabase();
		            }
		        }

				editionFetcher = new PeriodicalEditionListFetcher();
				Log.i(FBReader.LOG_LABEL, "about to get intent extras");
				//setting variables from the other extras from parent
				ids = intent.getStringArrayListExtra(FBReader.SUBSCRIBED_PERIODICAL_IDS_KEY);

				Log.i(FBReader.LOG_LABEL, "about to check intent extras");

		        if (null != ids) {
		            Log.i("GoRead", getClass().getSimpleName() + " Extras passed: id array size" + ids.size());
		        } else {
		            Log.i("GoRead", getClass().getSimpleName() + " Extras passed: ids are null");
		        }

		        if (ids != null && ids.size() > 0) {
		                Log.i("GoRead", getClass().getSimpleName() +
		                        " Periodical search started by alarm: " + ids.get(currIdsIndex));
		                runGetUpdatesForCurrentIndex();
		        } else {
		            Log.e("GoRead", getClass().getSimpleName() +
		                    " Couldn't find any subscribed Periodicals");
		        }

	}


	public boolean downloadPeriodical(Bookshare_Edition_Metadata_Bean bean) {
        Log.i("GoRead", getClass().getSimpleName() +
        						" About to start SubscriptionDownloadService for periodical: " + bean.getPeriodicalId() + " and edition: " + bean.getEdition());
		Bookshare_Edition_Metadata_Bean metadata_bean = bean;

		Intent downloadService = new Intent(
				MainPeriodicalDownloadService.this,
				SubscriptionDownloadService.class);
		downloadService.putExtra(usernameKey, username);
		downloadService.putExtra(passwordKey, password);
		downloadService.putExtra("metadata_bean", metadata_bean);
		startService(downloadService);
		
		currIdsIndex++;		
		runGetUpdatesForCurrentIndex();
		return false;
	}

	//run get updates method for the item currently pointed in ids arraylist
	private void runGetUpdatesForCurrentIndex(){
		if(currIdsIndex<ids.size()){
			getUpdates(downType, ids.get(currIdsIndex));
		}
	}
	
	public void getUpdates(AutomaticDownloadType downType, String id) {

		this.downType = downType;
		String serviceURI = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
				+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
				+ "/periodical/id/"
				+ id
				+ "/for/"
				+ username
				+ "?api_key="
				+ developerKey;
		Log.i("GoRead", getClass().getSimpleName() +
				" Fetching Periodical List for periodical with id: " + id
						+ " for: " + username);

		editionFetcher = new PeriodicalEditionListFetcher();
		editionFetcher.getListing(serviceURI, password, this);

	}

	public void onPeriodicalEditionListResponse(
			ArrayList<Bookshare_Periodical_Edition_Bean> results) {
		if (results == null) {
			Log.e("GoRead", getClass().getSimpleName() +
					" Couldn't fetch any periodical Editions");
		} else {
			Log.i("GoRead", getClass().getSimpleName() +
					" Found and Fetched " + results.size() + " periodicals");
			ArrayList<AllDbPeriodicalEntity> entities = new ArrayList<AllDbPeriodicalEntity>();
			for (Bookshare_Periodical_Edition_Bean bean : results) {
				Log.i("GoRead", getClass().getSimpleName() +
						" Found and fetched periodical: Title "
								+ bean.getTitle() + " Edition: "
								+ bean.getEdition());
				if (bean.getId() != null && bean.getEdition() != null
						&& TextUtils.isDigitsOnly(bean.getRevision())) {
					entities.add(new AllDbPeriodicalEntity(bean.getId(),
							bean.getTitle(), bean.getEdition(), Integer
									.parseInt(bean.getRevision()), null,
							null));
				}
			}

			// URL to request metadata of particular edition of a periodical
			String serviceURI;
			AllDbPeriodicalEntity maxEntity = PeriodicalDBUtils.getMostRecentEdition(entities);

			// download the periodical only if it's not been downloaded
			// before
			if (!dataSource.doesExist(periodicalDb,
					PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS,
					maxEntity)) {
				serviceURI = getEditionRequestURL(maxEntity);

				PeriodicalEditionMetadataFetcher metadataFetcher = new PeriodicalEditionMetadataFetcher(
						maxEntity.getId(), maxEntity.getTitle());
				metadataFetcher.getListing(serviceURI, password, this);
			}
			//if the periodical already exists in the db,
			//the we need to go to the next periodical to be downloaded
			//else clause does just that
			else{
				currIdsIndex++;
				runGetUpdatesForCurrentIndex();
			}
		}
	}

	private String getEditionRequestURL(AllDbPeriodicalEntity entity) {
		String serviceURI = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
				+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
				+ "/periodical/id/"
				+ entity.getId()
				+ "/edition/"
				+ entity.getEdition()
				+ "/revision/"
				+ entity.getRevision()
				+ "/for/" + username + "?api_key=" + developerKey;
        Log.i("GoRead", getClass().getSimpleName() +
        						" request url is " + serviceURI);
		return serviceURI;
	}

	public Bookshare_Edition_Metadata_Bean getDetails(
			Bookshare_Periodical_Edition_Bean bean) {
		if (bean == null) {
			Log.e("GoRead", getClass().getSimpleName() +  " Couldn't obtain edition details");
		} else {
			Log.i("GoRead", getClass().getSimpleName() +
					" Fetched Periodical: " + bean.getId() + " "
							+ bean.getTitle() + " " + bean.getEdition());
		}
		return null;
	}

	@Override
	public void onPeriodicalMetadataResponse(
			Bookshare_Edition_Metadata_Bean result) {
		if (result == null) {
			Log.e("GoRead", getClass().getSimpleName() + " Couldn't obtain edition details");
		} else {
			downloadPeriodical(result);
			Log.i("GoRead", getClass().getSimpleName() +
					" Fetched Periodical: " + result.getPeriodicalId() + " "
							+ result.getTitle() + " " + result.getEdition());
		}

	}
	



}