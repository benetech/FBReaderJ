package org.geometerplus.android.fbreader.network.bookshare;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.analytics.tracking.android.EasyTracker;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.accessibility.ParentCloserDialog;
import org.accessibility.VoiceableDialog;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.benetech.android.R;
import org.bookshare.net.BookshareWebservice;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.benetech.Analytics;
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Shows the details of a selected book. Will also show a download option if
 * applicable.
 * 
 */
public class Bookshare_Book_Details extends Activity implements OnClickListener {

	private String LOG_TAG = FBReader.LOG_LABEL;
	Context mcontext = this;
	private String username;
	private String password;
	private Bookshare_Metadata_Bean metadata_bean;
	private InputStream inputStream;
	private BookshareWebservice bws = new BookshareWebservice(
			Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private final int DATA_FETCHED = 99;
	private View book_detail_view;
	private TextView bookshare_book_detail_title_text;
	private TextView bookshare_book_detail_authors;
	private TextView bookshare_book_detail_isbn;
	private TextView bookshare_book_detail_language;
	private TextView bookshare_book_detail_category;
	private TextView bookshare_book_detail_publish_date;
	private TextView bookshare_book_detail_publisher;
	private TextView bookshare_book_detail_copyright;
	private TextView bookshare_book_detail_synopsis_text;
	private TextView bookshare_download_not_available_text;
	private TextView subscribe_described_text;
	private Button btnDownload;
	private Button btnDownloadWithImages;
	Button currentButton;// points to btnDownload if(downloadType==1), else
							// btnDownloadWithImages
	private int downloadType;
	private CheckBox chkbox_subscribe;

	boolean imagesAvailable;
	boolean isDownloadable;
	private final int BOOKSHARE_BOOK_DETAILS_FINISHED = 1;
	private boolean isFree = false;
	private boolean isOM;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private final int START_BOOKSHARE_OM_LIST = 0;
	private String memberId = null;
	private String omDownloadPassword;
	private String firstName = null;
	private String lastName = null;
	private boolean downloadSuccess;
	private Resources resources;
	private String downloadedBookDir;
	private Set<Integer> myOngoingNotifications = new HashSet<Integer>();
	private Activity myActivity;
	private String verifier;
	private SharedPreferences mTwtrSharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bookshare_blank_page);
		Log.i(LOG_TAG,developerKey);
		resources = getApplicationContext().getResources();
		myActivity = this;
		// Set full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");

		if (username == null || password == null) {
			isFree = true;
		}
		// Obtain the application wide SharedPreferences object and store the
		// login information
		SharedPreferences login_preference = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		isOM = login_preference.getBoolean("isOM", false);

		final String uri = intent.getStringExtra("ID_SEARCH_URI");

		final VoiceableDialog finishedDialog = new VoiceableDialog(this);
		String msg = "Fetching book details. Please wait.";
		finishedDialog.popup(msg, 2000);

		final AsyncTask<Object, Void, Integer> bookResultsFetcher = new BookDetailsTask(uri);
		bookResultsFetcher.execute();
	}

	private class BookDetailsTask extends AsyncTask<Object, Void, Integer> {

			String uri;

			public BookDetailsTask(String requestUri) {
				uri = requestUri;
			}

			@Override
		    protected Integer doInBackground(Object... params) {

				try{
					inputStream = bws.getResponseStream(password, uri);
					String response_HTML = bws.convertStreamToString(inputStream);

					String response = response_HTML.replace("&apos;", "\'")
							.replace("&quot;", "\"").replace("&amp;", "and")
							.replace("&#xd;\n", "\n").replace("&#x97;", "-");

					// Parse the response String
					parseResponse(response);

					Log.w(FBReader.LOG_LABEL, "done with parseResponse in task");

				}
				catch(Exception e){
					Log.e(FBReader.LOG_LABEL, "problem getting results", e);
				}

			    return 0;
			}

			@Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		    }

		    @Override
		    protected void onPostExecute(Integer results) {
		        super.onPostExecute(results);
			    Log.w(FBReader.LOG_LABEL, "about to call on ResultsFetched");
			    onResultsFetched();
		    }

		}

	private void onResultsFetched() {
			String temp = "";

			if (metadata_bean == null) {
				TextView txtView_msg = (TextView) findViewById(R.id.bookshare_blank_txtView_msg);
				String noBookFoundMsg = "Book not found.";
				txtView_msg.setText(noBookFoundMsg);
				// todo : return book not found result code

				View decorView = getWindow().getDecorView();
				if (null != decorView) {
					decorView.setContentDescription(noBookFoundMsg);
				}

				setResult(InternalReturnCodes.NO_BOOK_FOUND);
				confirmAndClose(noBookFoundMsg, 3000);
				return;
			}
			if (metadata_bean != null) {
				setIsDownloadable(metadata_bean);
				setImagesAvailable(metadata_bean);
				setContentView(R.layout.bookshare_book_detail);
				book_detail_view = (View) findViewById(R.id.book_detail_view);
				bookshare_book_detail_title_text = (TextView) findViewById(R.id.bookshare_book_detail_title);
				bookshare_book_detail_authors = (TextView) findViewById(R.id.bookshare_book_detail_authors);
				bookshare_book_detail_isbn = (TextView) findViewById(R.id.bookshare_book_detail_isbn);
				bookshare_book_detail_language = (TextView) findViewById(R.id.bookshare_book_detail_language);
				bookshare_book_detail_category = (TextView) findViewById(R.id.bookshare_book_detail_category);
				bookshare_book_detail_publish_date = (TextView) findViewById(R.id.bookshare_book_detail_publish_date);
				bookshare_book_detail_publisher = (TextView) findViewById(R.id.bookshare_book_detail_publisher);
				bookshare_book_detail_copyright = (TextView) findViewById(R.id.bookshare_book_detail_copyright);
				bookshare_book_detail_synopsis_text = (TextView) findViewById(R.id.bookshare_book_detail_synopsis_text);

				// We don't need subscription for books, needed only for
				// periodicals
				// So we hide it in the book details activity
				chkbox_subscribe = (CheckBox) findViewById(R.id.bookshare_chkbx_subscribe_periodical);
				chkbox_subscribe.setVisibility(View.GONE);
				subscribe_described_text = (TextView) findViewById(R.id.bookshare_subscribe_explained);
				subscribe_described_text.setVisibility(View.GONE);

				btnDownload = (Button) findViewById(R.id.bookshare_btn_download);
				btnDownloadWithImages = (Button) findViewById(R.id.bookshare_btn_download_images);
				bookshare_download_not_available_text = (TextView) findViewById(R.id.bookshare_download_not_available_msg);

				bookshare_book_detail_language
						.setNextFocusDownId(R.id.bookshare_book_detail_category);
				bookshare_book_detail_category
						.setNextFocusDownId(R.id.bookshare_book_detail_publish_date);
				bookshare_book_detail_publish_date
						.setNextFocusUpId(R.id.bookshare_book_detail_category);
				bookshare_book_detail_synopsis_text
						.setNextFocusUpId(R.id.bookshare_book_detail_copyright);

				book_detail_view.requestFocus();
				// If the book is not downloadable, do not show the download
				// button
				if (!isDownloadable) {
					btnDownload.setVisibility(View.GONE);
					btnDownloadWithImages.setVisibility(View.GONE);
					bookshare_book_detail_authors
							.setNextFocusDownId(R.id.bookshare_download_not_available_msg);
					bookshare_book_detail_isbn
							.setNextFocusUpId(R.id.bookshare_download_not_available_msg);
					bookshare_download_not_available_text
							.setNextFocusUpId(R.id.bookshare_book_detail_authors);
				} else {
					bookshare_download_not_available_text
							.setVisibility(View.GONE);
					btnDownload
							.setNextFocusDownId(R.id.bookshare_btn_download_images);
					btnDownload
							.setNextFocusUpId(R.id.bookshare_book_detail_authors);
					btnDownloadWithImages
							.setNextFocusDownId(R.id.bookshare_book_detail_isbn);
					btnDownloadWithImages
							.setNextFocusUpId(R.id.bookshare_btn_download);
					bookshare_book_detail_authors
							.setNextFocusDownId(R.id.bookshare_btn_download);

					btnDownload
							.setOnClickListener(Bookshare_Book_Details.this);
					btnDownloadWithImages
							.setOnClickListener(Bookshare_Book_Details.this);

				}
				if (!imagesAvailable) {
					Log.d("checking images",
							String.valueOf(imagesAvailable));
					btnDownloadWithImages.setVisibility(View.GONE);
				}
				// Set the fields of the layout with book details
				if (metadata_bean.getTitle() != null) {
					for (int i = 0; i < metadata_bean.getTitle().length; i++) {
						temp = temp + metadata_bean.getTitle()[i];
					}
					if (temp == null) {
						temp = "";
					}
					bookshare_book_detail_title_text.append(temp);
					temp = "";
				}

				if (metadata_bean.getAuthors() != null) {
					for (int i = 0; i < metadata_bean.getAuthors().length; i++) {
						if (i == 0) {
							temp = metadata_bean.getAuthors()[i];
						} else {
							temp = temp + ", "
									+ metadata_bean.getAuthors()[i];
						}
					}
					if (temp == null) {
						temp = "";
					}
					temp = temp.trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: temp;
					bookshare_book_detail_authors.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_authors
							.setText(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getIsbn() != null) {
					temp = metadata_bean.getIsbn().trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: metadata_bean.getIsbn();
					bookshare_book_detail_isbn.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_isbn
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getLanguage() != null) {
					temp = metadata_bean.getLanguage().trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: metadata_bean.getLanguage();
					bookshare_book_detail_language.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_language
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getCategory() != null) {
					for (int i = 0; i < metadata_bean.getCategory().length; i++) {
						if (i == 0) {
							temp = metadata_bean.getCategory()[i];
						} else {
							temp = temp + ", "
									+ metadata_bean.getCategory()[i];
						}
					}

					if (temp == null) {
						temp = "";
					}
					temp = temp.trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: temp;
					bookshare_book_detail_category.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_category
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getPublishDate() != null) {
					StringBuilder str_date = new StringBuilder(
							metadata_bean.getPublishDate());
					String mm = str_date.substring(0, 2);
					String month = "";
					if (mm.equalsIgnoreCase("01")) {
						month = "January";
					} else if (mm.equals("02")) {
						month = "February";
					} else if (mm.equals("03")) {
						month = "March";
					} else if (mm.equals("04")) {
						month = "April";
					} else if (mm.equals("05")) {
						month = "May";
					} else if (mm.equals("06")) {
						month = "June";
					} else if (mm.equals("07")) {
						month = "July";
					} else if (mm.equals("08")) {
						month = "August";
					} else if (mm.equals("09")) {
						month = "September";
					} else if (mm.equals("10")) {
						month = "October";
					} else if (mm.equals("11")) {
						month = "November";
					} else if (mm.equals("12")) {
						month = "December";
					}

					String publish_date = str_date.substring(2, 4) + " "
							+ month + " " + str_date.substring(4);
					temp = publish_date.trim().equals("") ? "Not available"
							: publish_date;
					bookshare_book_detail_publish_date.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_publish_date
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getPublisher() != null) {
					temp = metadata_bean.getPublisher().trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: metadata_bean.getPublisher();
					bookshare_book_detail_publisher.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_publisher
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getCopyright() != null) {
					temp = metadata_bean.getCopyright().trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: metadata_bean.getCopyright();
					bookshare_book_detail_copyright.append(temp);
					temp = "";
				} else {
					bookshare_book_detail_copyright
							.append(getResources().getString(
									R.string.book_details_not_available));
				}

				if (metadata_bean.getBriefSynopsis() != null) {
					for (int i = 0; i < metadata_bean.getBriefSynopsis().length; i++) {
						if (i == 0) {
							temp = metadata_bean.getBriefSynopsis()[i];
						} else {
							temp = temp + " "
									+ metadata_bean.getBriefSynopsis()[i];
						}
					}
					if (temp == null) {
						temp = "";
					}
					temp = temp.trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: temp;
					bookshare_book_detail_synopsis_text.append(temp.trim());
				} else if (metadata_bean.getCompleteSynopsis() != null) {
					for (int i = 0; i < metadata_bean.getCompleteSynopsis().length; i++) {
						if (i == 0) {
							temp = metadata_bean.getCompleteSynopsis()[i];
						} else {
							temp = temp
									+ " "
									+ metadata_bean.getCompleteSynopsis()[i];
						}
					}
					if (temp == null) {
						temp = "";
					}
					temp = temp.trim().equals("") ? getResources()
							.getString(R.string.book_details_not_available)
							: temp;

					bookshare_book_detail_synopsis_text.append(temp.trim());
				} else if (metadata_bean.getBriefSynopsis() == null
						&& metadata_bean.getCompleteSynopsis() == null) {
					bookshare_book_detail_synopsis_text
							.append("No Synopsis available");
				}

				findViewById(R.id.bookshare_book_detail_title)
						.requestFocus();
			}

		}

	// Start downlading task if the OM download password has been received
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == START_BOOKSHARE_OM_LIST) {
			if (data != null) {
				memberId = data
						.getStringExtra(Bookshare_OM_Member_Bean.MEMBER_ID);
				firstName = data
						.getStringExtra(Bookshare_OM_Member_Bean.FIRST_NAME);
				lastName = data
						.getStringExtra(Bookshare_OM_Member_Bean.LAST_NAME);
				new DownloadFilesTask().execute();
			}
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private void showAlert(String msg) {
		final VoiceableDialog downloadStartedDialog = new VoiceableDialog(
				myActivity);
		downloadStartedDialog.popup(msg, 2000);
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


		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(), 0);


		final Notification notification = new NotificationCompat.Builder(this)
			.setContentTitle(title)
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.setOngoing(true)
			.setProgress(0, 0, true)
			.setAutoCancel(true)
			.setContentIntent(contentIntent)
			.build();

		return notification;
	}

	// A custom AsyncTask class for carrying out the downloading task in a
	// separate background thread
	private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

		private Bookshare_Error_Bean error;
		private Bookshare_PackagingStatus_Bean status;

		public String download_uri;
		final String id = metadata_bean.getContentId();

		// Will be called in the UI thread
		@Override
		protected void onPreExecute() {

			// Disable the download button while the download is in progress

			currentButton.setText("Downloading Book...");
			// Disable the download button while the download is in progress
			currentButton.setEnabled(false);

			downloadedBookDir = null;

			if (isFree)
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
						+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
						+ "/download/content/"
						+ id
						+ "/version/"
						+ downloadType + "?api_key=" + developerKey;
			else if (isOM) {
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
						+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
						+ "/download/member/"
						+ memberId
						+ "content/"
						+ id
						+ "/version/1/for/"
						+ username
						+ "?api_key="
						+ developerKey;
			} else {
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
						+ Bookshare_Webservice_Login.BOOKSHARE_API_HOST
						+ "/download/content/"
						+ id
						+ "/version/"
						+ downloadType
						+ "/for/"
						+ username
						+ "?api_key="
						+ developerKey;
			}

		}

		// Will be called in a separate thread // change the downloadType here
		// for
		// images
		@Override
		protected Void doInBackground(Void... params) {

			final Notification progressNotification = createDownloadProgressNotification(metadata_bean
					.getTitle()[0]);

			final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			myOngoingNotifications.add(Integer.valueOf(id));
			notificationManager.notify(Integer.valueOf(id),
					progressNotification);

			try {
				Log.d(LOG_TAG, "download_uri :" + download_uri);

				HttpResponse response = bws.getHttpResponse(password,
						download_uri);
				HttpEntity entity = response.getEntity();
				Header header = entity.getContentType();
				String headerValue = header.getValue();
				Log.i(LOG_TAG, "header value " + headerValue);

				if (downloadType == 4) {
					if (!headerValue.contains("zip")) {
						status = new Bookshare_PackagingStatus_Bean();
						status.parseInputStream(response.getEntity()
								.getContent());

						Log.i(LOG_TAG, "packaging status, before while"
								+ status.getPackagingStatus());

						while (!headerValue.contains("zip")) {

							publishProgress();
							Log.d(LOG_TAG, "header of response in while"
									+ headerValue);
							Log.d(LOG_TAG,
									"status in while"
											+ status.getPackagingStatus());

							if (status.getContentId() == ""
									|| status.getPackagingStatus() == "CANCELLED")
								break;

							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO Auto-generated
								e.printStackTrace();
								Log.e(LOG_TAG,
										" problem waiting book with images to download",e);
							}

							response = bws.getHttpResponse(password,
									download_uri);
							entity = response.getEntity();
							headerValue = entity.getContentType().getValue();

						}
					}
				}
				// response.
				// Get hold of the response entity

				if (entity != null) {
					Log.i(LOG_TAG, "get hold of the response entity");
					String filename = "bookshare_" + Math.random() * 10000
							+ ".zip";
					if (metadata_bean.getTitle() != null) {
						String temp = "";
						for (int i = 0; i < metadata_bean.getTitle().length; i++) {
							temp = temp + metadata_bean.getTitle()[i];
						}
						filename = temp;
						filename = filename.replaceAll(" +", "_")
								.replaceAll(":", "__").replaceAll("/", "-");
						if (isOM) {
							filename = filename + "_" + firstName + "_"
									+ lastName;
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

					// entity.

					// Log.w("", "******  zip_file *****" + zip_file);

					if (headerValue.contains("zip")
							|| headerValue.contains("bks2")) {
						try {
							Log.d(LOG_TAG, "Contains zip");
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

							Log.d(LOG_TAG, "******** Downloading complete");

							// Unzip the encrypted archive file
							if (!isFree) {
								Log.i(LOG_TAG,
										"******Before creating ZipFile******"
												+ zip_file);
								// Initiate ZipFile object with the path/name of
								// the zip file.
								ZipFile zipFile = new ZipFile(zip_file);

								// Check to see if the zip file is password
								// protected
								if (zipFile.isEncrypted()) {
									Log.e(LOG_TAG, "******isEncrypted******");

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

								Log.e(LOG_TAG, "******Before for******");
								// Loop through the file headers
								for (int i = 0; i < fileHeaderList.size(); i++) {
									FileHeader fileHeader = (FileHeader) fileHeaderList
											.get(i);
									Log.i(LOG_TAG, downloadedBookDir);
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
										Log.e(LOG_TAG, "entryname " + entryName);
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
							Log.e(LOG_TAG, "FBR " + "Zip Exception", e);
						}
					} else {
						Log.w(LOG_TAG, "zip not found !");
						response = bws.getHttpResponse(password, download_uri);
						downloadSuccess = false;
						error = new Bookshare_Error_Bean();
						error.parseInputStream(response.getEntity()
								.getContent());
					}
				}
			} catch (URISyntaxException use) {
				Log.e(LOG_TAG, "URISyntaxException: " + use, use);
			} catch (IOException ie) {
				Log.e(LOG_TAG, "IOException: " + ie, ie);
			}
			return null;
		}

		// Will be called in the UI thread
		@Override
		protected void onPostExecute(Void param) {

			if (downloadSuccess) {
				currentButton.setText(resources
						.getString(R.string.book_details_download_success));
				currentButton.setEnabled(true);

			} else {
				currentButton.setText(resources
						.getString(R.string.book_details_download_error));
				currentButton.setEnabled(memberId != null);
				if (memberId != null) {
					currentButton
							.setText(resources
									.getString(R.string.book_details_download_error_other_member));
				}
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
					notificationManager.notify(
							id,
							createDownloadFinishNotification(file,
									metadata_bean.getTitle()[0],
									message.what != 0));
				}
			};
			currentButton.requestFocus();
			downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
		}
	}

	/**
	 * Uses a SAX parser to parse the response
	 * 
	 * @param response
	 *            String representing the response
	 */
	private void parseResponse(String response) {

		Log.i(LOG_TAG, response);

		InputSource is = new InputSource(new StringReader(response));

		try {
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader parser = sp.getXMLReader();
			parser.setContentHandler(new SAXHandler());
			parser.parse(is);
		} catch (SAXException e) {
			Log.e(LOG_TAG, e.toString(), e);
		} catch (ParserConfigurationException e) {
			Log.e(LOG_TAG, e.toString(), e);
		} catch (IOException ioe) {
			Log.e(LOG_TAG, ioe.toString(), ioe);
		}
	}

	// Class that applies parsing logic
	private class SAXHandler extends DefaultHandler {

		boolean metadata = false;
		boolean contentId = false;
		boolean daisy = false;
		boolean brf = false;
		boolean downloadFormats = false;
		boolean images = false;
		boolean isbn = false;
		boolean authors = false;
		boolean title = false;
		boolean publishDate = false;
		boolean publisher = false;
		boolean copyright = false;
		boolean language = false;
		boolean briefSynopsis = false;
		boolean completeSynopsis = false;
		boolean quality = false;
		boolean category = false;
		boolean bookshareId = false;
		boolean freelyAvailable = false;

		boolean authorElementVisited = false;
		boolean downloadFormatElementVisited = false;
		boolean titleElementVisited = false;
		boolean categoryElementVisited = false;
		boolean briefSynopsisElementVisited = false;
		boolean completeSynopsisElementVisited = false;
		Vector<String> vector_author;
		Vector<String> vector_downloadFormat;
		Vector<String> vector_category;
		Vector<String> vector_briefSynopsis;
		Vector<String> vector_completeSynopsis;
		Vector<String> vector_title;

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) {

			if (qName.equalsIgnoreCase("metadata")) {
				Log.i(LOG_TAG, "******* metadata visited");
				metadata = true;
				metadata_bean = new Bookshare_Metadata_Bean();
				authorElementVisited = false;
				downloadFormatElementVisited = false;
				titleElementVisited = false;
				categoryElementVisited = false;
				briefSynopsisElementVisited = false;
				completeSynopsisElementVisited = false;
				vector_author = new Vector<String>();
				vector_downloadFormat = new Vector<String>();
				vector_category = new Vector<String>();
				vector_briefSynopsis = new Vector<String>();
				vector_completeSynopsis = new Vector<String>();
				vector_title = new Vector<String>();
			}
			if (qName.equalsIgnoreCase("content-id")) {
				contentId = true;
			}
			if (qName.equalsIgnoreCase("daisy")) {
				daisy = true;
			}
			if (qName.equalsIgnoreCase("brf")) {
				brf = true;
			}
			if (qName.equalsIgnoreCase("download-format")) {
				downloadFormats = true;
				if (!downloadFormatElementVisited) {
					downloadFormatElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("images")) {
				images = true;
			}
			if (qName.equalsIgnoreCase("isbn10")
					|| qName.equalsIgnoreCase("isbn13")) {
				isbn = true;
			}
			if (qName.equalsIgnoreCase("author")) {
				authors = true;
				if (!authorElementVisited) {
					authorElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("title")) {
				title = true;
				if (!titleElementVisited) {
					titleElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("publish-date")) {
				publishDate = true;
			}
			if (qName.equalsIgnoreCase("publisher")) {
				publisher = true;
			}
			if (qName.equalsIgnoreCase("copyright")) {
				copyright = true;
			}
			if (qName.equalsIgnoreCase("language")) {
				language = true;
			}
			if (qName.equalsIgnoreCase("brief-synopsis")) {
				briefSynopsis = true;
				if (!briefSynopsisElementVisited) {
					briefSynopsisElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("complete-synopsis")) {
				completeSynopsis = true;
				if (!completeSynopsisElementVisited) {
					completeSynopsisElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("freely-available")) {
				freelyAvailable = true;
			}
			if (qName.equalsIgnoreCase("quality")) {
				quality = true;
			}
			if (qName.equalsIgnoreCase("bookshare-id")) {
				bookshareId = true;
			}
			if (qName.equalsIgnoreCase("category")) {
				category = true;
				if (!categoryElementVisited) {
					categoryElementVisited = true;
				}
			}
		}

		public void endElement(String uri, String localName, String qName) {

			// End of one metadata element parsing.
			if (qName.equalsIgnoreCase("metadata")) {
				metadata = false;
			}
			if (qName.equalsIgnoreCase("content-id")) {
				contentId = false;
			}
			if (qName.equalsIgnoreCase("daisy")) {
				daisy = false;
			}
			if (qName.equalsIgnoreCase("brf")) {
				brf = false;
			}
			if (qName.equalsIgnoreCase("download-format")) {
				downloadFormats = false;
			}
			if (qName.equalsIgnoreCase("images")) {
				images = false;
			}
			if (qName.equalsIgnoreCase("isbn10")
					|| qName.equalsIgnoreCase("isbn13")) {
				isbn = false;
			}
			if (qName.equalsIgnoreCase("author")) {
				authors = false;
			}
			if (qName.equalsIgnoreCase("title")) {
				title = false;
			}
			if (qName.equalsIgnoreCase("publish-date")) {
				publishDate = false;
			}
			if (qName.equalsIgnoreCase("publisher")) {
				publisher = false;
			}
			if (qName.equalsIgnoreCase("copyright")) {
				copyright = false;
			}
			if (qName.equalsIgnoreCase("language")) {
				language = false;
			}
			if (qName.equalsIgnoreCase("brief-synopsis")) {
				briefSynopsis = false;
			}
			if (qName.equalsIgnoreCase("complete-synopsis")) {
				completeSynopsis = false;
			}
			if (qName.equalsIgnoreCase("freely-available")) {
				freelyAvailable = false;
			}
			if (qName.equalsIgnoreCase("quality")) {
				quality = false;
			}
			if (qName.equalsIgnoreCase("category")) {
				category = false;
			}
			if (qName.equalsIgnoreCase("bookshare-id")) {
				bookshareId = false;
			}
		}

		public void characters(char[] c, int start, int length) {

			if (metadata) {
				if (contentId) {
					metadata_bean.setContentId(new String(c, start, length));
				}
				if (daisy) {
					metadata_bean.setDaisy(new String(c, start, length));
				}
				if (brf) {
					metadata_bean.setBrf(new String(c, start, length));
				}
				if (downloadFormats) {
					vector_downloadFormat.add(new String(c, start, length));
					metadata_bean.setDownloadFormats(vector_downloadFormat
							.toArray(new String[0]));
					// for(int i=0;i<vector_downloadFormat.size();i++)
					String temp = new String(c, start, length);
					Log.i(LOG_TAG, "formats" + temp);
				}
				if (images) {
					metadata_bean.setImages(new String(c, start, length));
				}
				if (isbn) {
					metadata_bean.setIsbn(new String(c, start, length));
				}

				if (authors) {
					vector_author.add(new String(c, start, length));
					metadata_bean.setAuthors(vector_author
							.toArray(new String[0]));
				}
				if (title) {
					vector_title.add(new String(c, start, length));
					metadata_bean.setTitle(vector_title.toArray(new String[0]));
				}
				if (publishDate) {
					metadata_bean.setPublishDate(new String(c, start, length));
				}
				if (publisher) {
					metadata_bean.setPublisher(new String(c, start, length));
				}
				if (copyright) {
					metadata_bean.setCopyright(new String(c, start, length));
				}
				if (language) {
					metadata_bean.setLanguage(new String(c, start, length));
				}
				if (briefSynopsis) {
					vector_briefSynopsis.add(new String(c, start, length));
					metadata_bean.setBriefSynopsis(vector_briefSynopsis
							.toArray(new String[0]));
				}
				if (completeSynopsis) {
					vector_completeSynopsis.add(new String(c, start, length));
					metadata_bean.setCompleteSynopsis(vector_completeSynopsis
							.toArray(new String[0]));
				}
				if (quality) {
					metadata_bean.setQuality(new String(c, start, length));
				}
				if (category) {
					vector_category.add(new String(c, start, length));
					metadata_bean.setCategory(vector_category
							.toArray(new String[0]));
					Log.i(LOG_TAG, "metadata_bean.getCategory() = "
							+ metadata_bean.getCategory());

				}
				if (bookshareId) {
					metadata_bean.setBookshareId(new String(c, start, length));
				}
				if (freelyAvailable) {
					metadata_bean.setFreelyAvailable(new String(c, start,
							length));
				}
			}
		}
	}

	// For keeping the screen from rotating
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// Determine whether the book is downloadable.
	private void setIsDownloadable(final Bookshare_Metadata_Bean bean) {
		isDownloadable = (bean.getDownloadFormats() != null && bean
				.getDownloadFormats().length > 0);
	}

	private void setImagesAvailable(final Bookshare_Metadata_Bean bean) {
		imagesAvailable = (bean.getImages() != null && !bean.getImages()
				.contains("0"));
		// Log.i(LOG_TAG, "images" + bean.getImages() +
		// String.valueOf(imagesAvailable));
	}

	/*
	 * Display voiceable message and then close
	 */
	private void confirmAndClose(String msg, int timeout) {
		final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
		dialog.popup(msg, timeout);
	}

	// called after the download button is pressed
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bookshare_btn_download_images:
			downloadType = 4;
			currentButton = (Button) findViewById(R.id.bookshare_btn_download_images);
			Log.i(LOG_TAG, "books with images" + "on click method");
			break;
		case R.id.bookshare_btn_download:
			downloadType = 1;
			currentButton = (Button) findViewById(R.id.bookshare_btn_download);
			break;
		}
		downloadPressed();

	}

	// called after the download button is pressed, after onClick method
	private void downloadPressed() {
		// TODO Auto-generated method stub

		final String downloadText = currentButton.getText().toString();
		if (downloadText.equalsIgnoreCase(resources
				.getString(R.string.book_details_download_button))
				|| downloadText.equalsIgnoreCase(resources
						.getString(R.string.book_details_download_images))
				|| downloadText
						.equalsIgnoreCase(resources
								.getString(R.string.book_details_download_error_other_member))) {

			// Start a new Activity for getting the OM
			// member list
			// See onActivityResult for further
			// processing
			if (isOM) {
				Intent intent = new Intent(getApplicationContext(),
						Bookshare_OM_List.class);
				intent.putExtra("username", username);
				intent.putExtra("password", password);
				startActivityForResult(intent, START_BOOKSHARE_OM_LIST);
			} else {
				if (downloadType == 1)
					EasyTracker.getTracker().trackEvent(
							Analytics.EVENT_CATEGORY_UI,
							Analytics.EVENT_ACTION_BUTTON,
							Analytics.EVENT_LABEL_DOWNLOAD_BOOK, null);

				else if (downloadType == 4)
					EasyTracker.getTracker().trackEvent(
							Analytics.EVENT_CATEGORY_UI,
							Analytics.EVENT_ACTION_BUTTON,
							Analytics.EVENT_LABEL_DOWNLOAD_BOOK_WITH_IMAGES,
							null);

				new DownloadFilesTask().execute();

			}
			showAlert(getResources().getString(
					R.string.book_details_download_started));
		}

		// View book or display error
		else if (currentButton
				.getText()
				.toString()
				.equalsIgnoreCase(
						resources
								.getString(R.string.book_details_download_success))) {
			setResult(BOOKSHARE_BOOK_DETAILS_FINISHED);
			if (null == downloadedBookDir) {
				final VoiceableDialog finishedDialog = new VoiceableDialog(
						currentButton.getContext());
				String message = resources
						.getString(R.string.book_details_open_error);
				finishedDialog.popup(message, 2000);
			} else {
				if (null != downloadedBookDir) {
					ZLFile opfFile = getOpfFile();
					if (null != opfFile) {
						startActivity(new Intent(getApplicationContext(),
								FBReader.class)
								.setAction(Intent.ACTION_VIEW)
								.putExtra(FBReader.BOOK_PATH_KEY,
										opfFile.getPath())
								.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					}

				}
			}
		}
	}

}
