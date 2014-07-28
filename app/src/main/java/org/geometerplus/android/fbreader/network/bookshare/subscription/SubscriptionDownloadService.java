
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.benetech.android.R;
import org.bookshare.net.BookshareWebservice;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.network.bookshare.BookshareDeveloperKey;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Error_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Details;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * This is an intent service. This was used to stop simultaneous downloads as it
 * causes system not to respond as well as corrupt zip files. So with this
 * downloads are done sequentially
 * 
 * @author thushan
 * 
 */
public class SubscriptionDownloadService extends IntentService {

	BooksharePeriodicalDataSource dataSource;
	PeriodicalsSQLiteHelper dbHelper;
	private String username;
	private String password;
	private String downloadedBookDir;
	private String omDownloadPassword;
	private boolean isFree = false;
	private boolean isOM;
	private boolean downloadSuccess;

	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;

	private Bookshare_Edition_Metadata_Bean metadata_bean;
	private Set<Integer> myOngoingNotifications = new HashSet<Integer>();
	private SQLiteDatabase periodicalDb;

	private Bookshare_Error_Bean error;
	final BookshareWebservice bws = new BookshareWebservice(
			Bookshare_Webservice_Login.BOOKSHARE_API_HOST);

	public SubscriptionDownloadService() {
		super("SubscriptionDownloadService");

	}

	@Override
	protected void onHandleIntent(Intent intent) {

		dataSource = BooksharePeriodicalDataSource
				.getInstance(getApplicationContext());
		dbHelper = new PeriodicalsSQLiteHelper(getApplicationContext());

		// instantiate periodical db only if its null or is not opened currently
		if (periodicalDb == null) {
			periodicalDb = dbHelper.getWritableDatabase();
		} else {
			if (!periodicalDb.isOpen()) {
				periodicalDb = dbHelper.getWritableDatabase();
			}
		}

		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");

		if (username == null || password == null || TextUtils.isEmpty(username)) {
			isFree = true;
		}

		metadata_bean = (Bookshare_Edition_Metadata_Bean) intent
				.getSerializableExtra("metadata_bean");

		Log.i(FBReader.LOG_LABEL, getClass().getSimpleName() +
				" Downloding one by one... " + "Title: "
						+ metadata_bean.getTitle() + " Edition: "
						+ metadata_bean.getEdition());

		final String id = metadata_bean.getContentId();
		String download_uri;
		if (isFree)
			download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
					+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
					+ "/download/content/" + id + "/version/1?api_key="
					+ developerKey;
		// TODO: Uncomment & Implement
		/*
		 * else if(isOM){
		 * 
		 * download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL +
		 * Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/member/"
		 * +memberId+"content/"+id+"/version/1/for/"+username
		 * +"?api_key="+developerKey; }
		 */

		else {
			download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
					+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
					+ "/download/content/" + id + "/version/1/for/" + username
					+ "?api_key=" + developerKey;
		}

		final Notification progressNotification = createDownloadProgressNotification(metadata_bean
				.getTitle());

		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		myOngoingNotifications.add(Integer.valueOf(id));
        Log.i(FBReader.LOG_LABEL, getClass().getSimpleName() + " added progress notification for download_uri: " + download_uri);
		notificationManager.notify(Integer.valueOf(id), progressNotification);

		try {
            Log.i(FBReader.LOG_LABEL, getClass().getSimpleName() + " about to make http call with download_uri: " + download_uri);
			HttpResponse response = bws.getHttpResponse(password, download_uri);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String filename = "bookshare_" + Math.random() * 10000 + ".zip";
				if (metadata_bean.getTitle() != null
						&& metadata_bean.getEdition() != null) {
					String temp = "";
					// Changed the file name to <title>_<edition>
					temp = metadata_bean.getTitle() + "_"
							+ metadata_bean.getEdition();

					filename = temp;
					filename = filename.replaceAll(" +", "_").replaceAll(":",
							"__");
					if (isOM) {
						// TODO: Uncomment & Implement
						// filename = filename + "_" + firstName + "_" +
						// lastName;
					}
				}
				String zip_file = Paths.BooksDirectoryOption().getValue() + "/"
						+ filename + ".zip";
				downloadedBookDir = Paths.BooksDirectoryOption().getValue()
						+ "/" + filename;
				Log.i(FBReader.LOG_LABEL, "Download Book dir is set: "+downloadedBookDir);

				File downloaded_zip_file = new File(zip_file);
				if (downloaded_zip_file.exists()) {
					downloaded_zip_file.delete();
				}
				Header header = entity.getContentType();
				// Log.w("FBR", "******  zip_file *****" + zip_file);
				final String headerValue = header.getValue();
				if (headerValue.contains("zip") || headerValue.contains("bks2")) {
					try {
						Log.i(FBReader.LOG_LABEL, "Contains zip");
						java.io.BufferedInputStream in = new java.io.BufferedInputStream(entity.getContent());
						java.io.FileOutputStream fos = new java.io.FileOutputStream(downloaded_zip_file);
                        Bookshare_Periodical_Edition_Details.copyStream(in, fos);

                        Log.i(FBReader.LOG_LABEL, getClass().getSimpleName() + " : Downloading complete");

						if (!isFree) {
                            Bookshare_Periodical_Edition_Details.UnzipEncryptedFile(zip_file, isOM, password,
                                omDownloadPassword, downloadedBookDir, getApplicationContext());
						}
						else {
                            Bookshare_Periodical_Edition_Details.UnzipNonEncryptedFile(zip_file, downloadedBookDir);
						}
						// Delete the downloaded zip file as it has been
						// extracted
						downloaded_zip_file = new File(zip_file);
						if (downloaded_zip_file.exists()) {
							downloaded_zip_file.delete();
						}
						downloadSuccess = true;
					} catch (ZipException e) {
						Log.e(FBReader.LOG_LABEL, "Zip Exception", e);
					}
				} else {
					downloadSuccess = false;
					error = new Bookshare_Error_Bean();
					error.parseInputStream(response.getEntity().getContent());
				}
			}
		} catch (URISyntaxException use) {
            Log.e(FBReader.LOG_LABEL, " uri problem downloading subscription", use);
		} catch (IOException ie) {
            Log.e(FBReader.LOG_LABEL, " io problem downloading subscription", ie);
		}

		if (downloadSuccess) {
            Log.i(FBReader.LOG_LABEL, "SubscriptionDownloadService downloadSuccess");
			// Get download time/date
			Calendar currCal = Calendar.getInstance();
			String currentDate = currCal.get(Calendar.MONTH) + "/"
					+ currCal.get(Calendar.DATE) + "/"
					+ currCal.get(Calendar.YEAR) + "";
			String currentTime = currCal.get(Calendar.HOUR_OF_DAY) + ":"
					+ currCal.get(Calendar.MINUTE) + ":"
					+ currCal.get(Calendar.SECOND) + "";

			// create alldb and subscribeddb entities to be inserted to
			// their respective dbs
			AllDbPeriodicalEntity allEntity = new AllDbPeriodicalEntity(
					metadata_bean.getPeriodicalId(), metadata_bean.getTitle(),
					metadata_bean.getEdition(), Integer.parseInt(metadata_bean
							.getRevision()), currentDate, currentTime);
			SubscribedDbPeriodicalEntity subEntity = new SubscribedDbPeriodicalEntity(
					metadata_bean.getPeriodicalId(), metadata_bean.getTitle(),
					metadata_bean.getEdition(), Integer.parseInt(metadata_bean
							.getRevision()));
			dataSource.insertEntity(periodicalDb,
					PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS, allEntity);
			if (dataSource.doesExist(periodicalDb,
					PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,
					subEntity)) {
				SubscribedDbPeriodicalEntity dbSubEntity = (SubscribedDbPeriodicalEntity) dataSource
						.getEntity(
								periodicalDb,
								PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,
								subEntity);
				// Check whether we're actually trying to put a greater version
				// than existing version
				if (subEntity.getLatestEdition().equals(
						PeriodicalDBUtils.getRecentEditionString(
								subEntity.getLatestEdition(),
								dbSubEntity.getLatestEdition()))) {
					dataSource
							.insertEntity(
									periodicalDb,
									PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,
									subEntity);
				}
			}
		} else {
            Log.i("GoRead", "SubscriptionDownloadService not a downloadSuccess");
			downloadedBookDir = null;
		}

		final Handler downloadFinishHandler = new Handler() {
			public void handleMessage(Message message) {
                Log.i(FBReader.LOG_LABEL, " downloadFinishHandler.handleMessage, message=" + message.toString());
				final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				int id = Integer.valueOf(metadata_bean.getContentId());
				notificationManager.cancel(id);
				myOngoingNotifications.remove(Integer.valueOf(id));
				File file = null;
				if (downloadSuccess) {
					file = new File(getOpfFile().getPath());
					if(file != null){
						Log.i(FBReader.LOG_LABEL, "File path for "+metadata_bean.getTitle()+" is "+file.getPath());
					}else{
						Log.e(FBReader.LOG_LABEL,"File is null for getOpfFile in "+downloadedBookDir);
					}
				}
                Log.i(FBReader.LOG_LABEL, " SubscriptionDownloadService handleMessage - should update notification");
				notificationManager.notify(
						id,
						createDownloadFinishNotification(file,
								metadata_bean.getTitle(), message.what != 0));
			}
		};

		downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
        Log.i(FBReader.LOG_LABEL, "Just sent downloadFinishhandler a value of " + downloadSuccess);

	}

	 @Override
	public void onDestroy() {
		 final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		 notificationManager.cancelAll();
		 periodicalDb.close();
		super.onDestroy();
	}

	private ZLFile getOpfFile() {
		ZLFile bookDir = ZLFile.createFileByPath(downloadedBookDir);
		List<ZLFile> bookEntries = bookDir.children();
		ZLFile opfFile = null;
		for (ZLFile entry : bookEntries) {
			if (entry.getExtension().equals("opf")) {
				opfFile = entry;
				break;
			}
		}
		return opfFile;
	}

	private Intent getFBReaderIntent(final File file) {
		final Intent intent = new Intent(getApplicationContext(),
				FBReader.class);
		if (file != null) {
			intent.setAction(Intent.ACTION_VIEW).setData(Uri.fromFile(file));
		}
		return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	private Notification createDownloadFinishNotification(File file,
			String title, boolean success) {
        Log.i(FBReader.LOG_LABEL, " SubscriptionDownloadService createDownloadFinishNotification with success = " + success);
		final ZLResource resource = BookDownloaderService.getResource();
		final String tickerText = success ? resource.getResource(
				"tickerSuccess").getValue() : resource.getResource(
				"tickerError").getValue();
		final String contentText = success ? resource.getResource(
				"contentSuccess").getValue() : resource.getResource(
				"contentError").getValue();
		final Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done, tickerText,
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		final Intent intent = success ? getFBReaderIntent(file) : new Intent();
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title,
				contentText, contentIntent);
		return notification;
	}

	private Notification createDownloadProgressNotification(String title) {
		final RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.download_notification);
		contentView.setTextViewText(R.id.download_notification_title, title);
		contentView.setTextViewText(R.id.download_notification_progress_text,
				"");
		contentView.setProgressBar(R.id.download_notification_progress_bar,
				100, 0, true);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(), 0);

		final Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;

		return notification;
	}

}