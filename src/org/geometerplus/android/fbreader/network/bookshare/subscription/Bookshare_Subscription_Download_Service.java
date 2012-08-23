package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
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
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp.AutomaticDownloadType;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Main automatic dowload service
 * 
 * @author thushan
 * 
 */
public class Bookshare_Subscription_Download_Service extends Service {

	// SubscriptionSQLiteHelper dbHelper;
	BooksharePeriodicalDataSource dataSource;
	PeriodicalsSQLiteHelper dbHelper;
	private String username;
	private String password;
	private String downloadedBookDir;
	private String omDownloadPassword;
	private boolean isFree = false;
	private boolean isOM;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;

	private Bookshare_Edition_Metadata_Bean metadata_bean;
	private Set<Integer> myOngoingNotifications = new HashSet<Integer>();
	private SQLiteDatabase periodicalDb;
	private PeriodicalEditionListFetcher editionFetcher;
	private PeriodicalEditionMetadataFetcher metadataFetcher;
	private ServiceBinder serviceBinder = new ServiceBinder();

	private String usernameKey = "username";
	private String passwordKey = "password";

	@Override
	public IBinder onBind(Intent arg0) {

		SharedPreferences logingPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		username = logingPrefs.getString(usernameKey, "");
		password = logingPrefs.getString(passwordKey, "");

		// TODO: Get first name, last name, member id
		dataSource = BooksharePeriodicalDataSource
				.getInstance(getApplicationContext());
		dbHelper = new PeriodicalsSQLiteHelper(getApplicationContext());
		periodicalDb = dbHelper.getWritableDatabase();

		editionFetcher = new PeriodicalEditionListFetcher();

		if (username == null || password == null || TextUtils.isEmpty(username)) {
			isFree = true;
		}

		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopSelf();
		periodicalDb.close();
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// dbHelper=new SubscriptionSQLiteHelper(getApplicationContext());
		// dataSrc=new BooksharePeriodicalDataSource(getApplicationContext());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		periodicalDb.close();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(getClass().getName(),
				"**** Service Started by Alarm Manager ****");

		if (serviceBinder == null) {
			serviceBinder = new ServiceBinder();
		}

		SharedPreferences logingPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		username = logingPrefs.getString(usernameKey, "");
		password = logingPrefs.getString(passwordKey, "");

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

		if (username == null || password == null || TextUtils.isEmpty(username)) {
			isFree = true;
		}

		ArrayList<String> ids = (ArrayList<String>) intent
				.getStringArrayListExtra(FBReader.SUBSCRIBED_PERIODICAL_IDS_KEY);

		String downTypeStr = intent
				.getStringExtra(FBReader.AUTOMATIC_DOWNLOAD_TYPE_KEY);
		AutomaticDownloadType downType;
		Log.i(getClass().getName(), "Extras passed: id array size" + ids.size()
				+ " , " + downTypeStr);
		// Determine the download type user has set
		if (AutomaticDownloadType.downloadAll.toString().equals(downTypeStr)) {
			downType = AutomaticDownloadType.downloadAll;
		} else {
			downType = AutomaticDownloadType.downloadMostRecent;
		}

		if (ids != null && ids.size() > 0) {
			for (String id : ids) {
				Log.i(getClass().getName(),
						"Periodical search started by alarm: " + id);
				serviceBinder.getUpdates(downType, id);

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Log.e(getClass().getName(),
							e.getCause() + " ," + e.getMessage());
				}
			}
		} else {
			Log.e(getClass().getName(),
					"Couldn't find any subscribed Periodicals");
		}
		return super.onStartCommand(intent, flags, startId);

	}

	public class ServiceBinder extends Binder implements
			IPeriodicalDownloadAPI, PeriodicalEditionListener,
			PeriodicalMetadataListener {
		AutomaticDownloadType downType;

		@Override
		public boolean downloadPeriodical(Bookshare_Edition_Metadata_Bean bean) {
			metadata_bean = bean;

			Intent downloadService = new Intent(
					Bookshare_Subscription_Download_Service.this,
					SubscriptionDownloadService.class);
			downloadService.putExtra(usernameKey, username);
			downloadService.putExtra(passwordKey, password);
			downloadService.putExtra("metadata_bean", metadata_bean);
			startService(downloadService);
			// new DownloadFilesTask().execute();
			return false;
		}

		@Override
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
			Log.i(getClass().getName(),
					"Fetching Periodical List for periodical with id: " + id
							+ " for: " + username);

			editionFetcher = new PeriodicalEditionListFetcher();
			editionFetcher.getListing(serviceURI, password, this);

		}

		@Override
		public void onPeriodicalEditionListResponse(
				Vector<Bookshare_Periodical_Edition_Bean> results) {
			if (results == null) {
				Log.e(getClass().getName(),
						"Couldn't fetch any periodical Editions");
			} else {
				Log.i(getClass().getName(),
						"Found and Fetched " + results.size() + " periodicals");
				ArrayList<AllDbPeriodicalEntity> entities = new ArrayList<AllDbPeriodicalEntity>();
				for (Bookshare_Periodical_Edition_Bean bean : results) {
					Log.i(getClass().getName(),
							"Found and fetched periodical: Title "
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
				// if user has set settings to download only the most recent
				if (downType == AutomaticDownloadType.downloadMostRecent) {
					AllDbPeriodicalEntity maxEntity = PeriodicalDBUtils
							.getMostRecentEdition(entities);

					// download the periodical only if it's not been downloaded
					// before
					if (!dataSource.doesExist(periodicalDb,
							PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS,
							maxEntity)) {
						serviceURI = getEditionRequestURL(maxEntity);

						metadataFetcher = new PeriodicalEditionMetadataFetcher(
								maxEntity.getId(), maxEntity.getTitle());
						metadataFetcher.getListing(serviceURI, password, this);
					}
				}
				// If user has set settings to download all periodicals
				else if (downType == AutomaticDownloadType.downloadAll) {
					for (AllDbPeriodicalEntity entity : entities) {
						// download the periodical only if it's not been
						// downloaded before
						// TODO: Download periodicals which is higer than the
						// highest in alldbperiodicals
						if (!dataSource.doesExist(periodicalDb,
								PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS,
								entity)) {
							serviceURI = getEditionRequestURL(entity);

							metadataFetcher = new PeriodicalEditionMetadataFetcher(
									entity.getId(), entity.getTitle());
							metadataFetcher.getListing(serviceURI, password,
									this);

							// This is to reduce number of queries per second
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ex) {
								Log.e(getClass().getName(), ex.getCause()
										+ " ," + ex.getMessage());
							}
						}
					}
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
			return serviceURI;
		}

		@Override
		public Bookshare_Edition_Metadata_Bean getDetails(
				Bookshare_Periodical_Edition_Bean bean) {
			if (bean == null) {
				Log.e(getClass().getName(), "Couldn't obtain edition details");
			} else {
				Log.i(getClass().getName(),
						"Fetched Periodical: " + bean.getId() + " "
								+ bean.getTitle() + " " + bean.getEdition());
			}
			return null;
		}

		@Override
		public void onPeriodicalMetadataResponse(
				Bookshare_Edition_Metadata_Bean result) {
			if (result == null) {
				Log.e(getClass().getName(), "Couldn't obtain edition details");
			} else {
				downloadPeriodical(result);
				Log.i(getClass().getName(),
						"Fetched Periodical: " + result.getPeriodicalId() + " "
								+ result.getTitle() + " " + result.getEdition());
			}

		}

	}

	private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

		private Bookshare_Error_Bean error;
		final BookshareWebservice bws = new BookshareWebservice(
				Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
		private boolean downloadSuccess;

		// Will be called in the UI thread
		@Override
		protected void onPreExecute() {
			downloadedBookDir = null;

		}

		// Will be called in a separate thread
		@Override
		protected Void doInBackground(Void... params) {
			final String id = metadata_bean.getContentId();
			String download_uri;
			if (isFree)
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
						+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
						+ "/download/content/"
						+ id
						+ "/version/1?api_key="
						+ developerKey;
			// TODO: Uncomment & Implement
			/*
			 * else if(isOM){
			 * 
			 * download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
			 * + Bookshare_Webservice_Login.BOOKSHARE_API_HOST +
			 * "/download/member/"
			 * +memberId+"content/"+id+"/version/1/for/"+username
			 * +"?api_key="+developerKey; }
			 */
			else {
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
						+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
						+ "/download/content/"
						+ id
						+ "/version/1/for/"
						+ username + "?api_key=" + developerKey;
			}

			final Notification progressNotification = createDownloadProgressNotification(metadata_bean
					.getTitle());

			final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			myOngoingNotifications.add(Integer.valueOf(id));
			notificationManager.notify(Integer.valueOf(id),
					progressNotification);

			try {
				System.out.println("download_uri :" + download_uri);
				HttpResponse response = bws.getHttpResponse(password,
						download_uri);
				// Get hold of the response entity
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					String filename = "bookshare_" + Math.random() * 10000
							+ ".zip";
					if (metadata_bean.getTitle() != null
							&& metadata_bean.getEdition() != null) {
						String temp = "";
						// Changed the file name to <title>_<edition>
						temp = metadata_bean.getTitle() + "_"
								+ metadata_bean.getEdition();

						filename = temp;
						filename = filename.replaceAll(" +", "_").replaceAll(
								":", "__");
						if (isOM) {
							// TODO: Uncomment & Implement
							// filename = filename + "_" + firstName + "_" +
							// lastName;
						}
					}
					String zip_file = Paths.BooksDirectoryOption().getValue()
							+ "/" + filename + ".zip";
					downloadedBookDir = Paths.BooksDirectoryOption().getValue()
							+ "/" + filename;

					File downloaded_zip_file = new File(zip_file);
					if (downloaded_zip_file.exists()) {
						downloaded_zip_file.delete();
					}
					Header header = entity.getContentType();
					// Log.w("FBR", "******  zip_file *****" + zip_file);
					final String headerValue = header.getValue();
					if (headerValue.contains("zip")
							|| headerValue.contains("bks2")) {
						try {
							System.out.println("Contains zip");
							java.io.BufferedInputStream in = new java.io.BufferedInputStream(
									entity.getContent());
							java.io.FileOutputStream fos = new java.io.FileOutputStream(
									downloaded_zip_file);
							java.io.BufferedOutputStream bout = new BufferedOutputStream(
									fos, 1024);
							byte[] data = new byte[1024];
							int x = 0;
							while ((x = in.read(data, 0, 1024)) >= 0) {
								bout.write(data, 0, x);
							}
							fos.flush();
							bout.flush();
							fos.close();
							bout.close();
							in.close();

							System.out.println("******** Downloading complete");

							// Unzip the encrypted archive file
							if (!isFree) {
								System.out
										.println("******Before creating ZipFile******"
												+ zip_file);
								// Initiate ZipFile object with the path/name of
								// the zip file.
								ZipFile zipFile = new ZipFile(zip_file);

								// Check to see if the zip file is password
								// protected
								if (zipFile.isEncrypted()) {
									System.out
											.println("******isEncrypted******");

									// if yes, then set the password for the zip
									// file
									if (!isOM) {
										zipFile.setPassword(password);
									}
									// Set the OM password sent by the Intent
									else {
										// Obtain the SharedPreferences object
										// shared across the application. It is
										// stored in login activity
										SharedPreferences login_preference = PreferenceManager
												.getDefaultSharedPreferences(getApplicationContext());
										omDownloadPassword = login_preference
												.getString("downloadPassword",
														"");
										zipFile.setPassword(omDownloadPassword);
									}
								}

								// Get the list of file headers from the zip
								// file
								List fileHeaderList = zipFile.getFileHeaders();

								System.out.println("******Before for******");
								// Loop through the file headers
								for (int i = 0; i < fileHeaderList.size(); i++) {
									FileHeader fileHeader = (FileHeader) fileHeaderList
											.get(i);
									System.out.println(downloadedBookDir);
									// Extract the file to the specified
									// destination
									zipFile.extractFile(fileHeader,
											downloadedBookDir);
								}
							}
							// Unzip the non-encrypted archive file
							else {
								try {
									File file = new File(downloadedBookDir);
									file.mkdir();
									String destinationname = downloadedBookDir
											+ "/";
									byte[] buf = new byte[1024];
									ZipInputStream zipinputstream = null;
									ZipEntry zipentry;
									zipinputstream = new ZipInputStream(
											new FileInputStream(zip_file));

									zipentry = zipinputstream.getNextEntry();
									while (zipentry != null) {
										// for each entry to be extracted
										String entryName = zipentry.getName();
										System.out.println("entryname "
												+ entryName);
										int n;
										FileOutputStream fileoutputstream;
										File newFile = new File(entryName);
										String directory = newFile.getParent();

										if (directory == null) {
											if (newFile.isDirectory())
												break;
										}

										fileoutputstream = new FileOutputStream(
												destinationname + entryName);

										while ((n = zipinputstream.read(buf, 0,
												1024)) > -1)
											fileoutputstream.write(buf, 0, n);

										fileoutputstream.close();
										zipinputstream.closeEntry();
										zipentry = zipinputstream
												.getNextEntry();

									}// while

									zipinputstream.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							// Delete the downloaded zip file as it has been
							// extracted
							downloaded_zip_file = new File(zip_file);
							if (downloaded_zip_file.exists()) {
								downloaded_zip_file.delete();
							}
							downloadSuccess = true;
						} catch (ZipException e) {
							Log.e("FBR", "Zip Exception", e);
						}
					} else {
						downloadSuccess = false;
						error = new Bookshare_Error_Bean();
						error.parseInputStream(response.getEntity()
								.getContent());
					}
				}
			} catch (URISyntaxException use) {
				System.out.println("URISyntaxException: " + use);
			} catch (IOException ie) {
				System.out.println("IOException: " + ie);
			}
			return null;
		}

		// Will be called in the UI thread
		@Override
		protected void onPostExecute(Void param) {

			if (downloadSuccess) {
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
						metadata_bean.getPeriodicalId(),
						metadata_bean.getTitle(), metadata_bean.getEdition(),
						Integer.parseInt(metadata_bean.getRevision()),
						currentDate, currentTime);
				SubscribedDbPeriodicalEntity subEntity = new SubscribedDbPeriodicalEntity(
						metadata_bean.getPeriodicalId(),
						metadata_bean.getTitle(), metadata_bean.getEdition(),
						Integer.parseInt(metadata_bean.getRevision()));
				dataSource.insertEntity(periodicalDb,
						PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS,
						allEntity);
				if (dataSource.doesExist(periodicalDb,
						PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,
						subEntity)) {
					dataSource
							.insertEntity(
									periodicalDb,
									PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,
									subEntity);
				}
			} else {

				downloadedBookDir = null;
			}

			final Handler downloadFinishHandler = new Handler() {
				public void handleMessage(Message message) {
					final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					int id = Integer.valueOf(metadata_bean.getContentId());
					notificationManager.cancel(id);
					myOngoingNotifications.remove(Integer.valueOf(id));
					File file = null;
					if (downloadSuccess) {
						file = new File(getOpfFile().getPath());
					}
					notificationManager
							.notify(id,
									createDownloadFinishNotification(file,
											metadata_bean.getTitle(),
											message.what != 0));
				}
			};

			downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
		}
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
