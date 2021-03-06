package org.geometerplus.android.fbreader.network.bookshare;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.accessibility.ParentCloserDialog;
import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.bookshare.net.BookshareWebServiceClient;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.benetech.Analytics;
import org.geometerplus.android.fbreader.benetech.FBReaderWithNavigationBar;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.network.bookshare.subscription.BookDetailsFetechedResultsHandler;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Shows the details of a selected book. Will also show a download option if applicable.
 */
public class OnlineBookDetailActivity extends BookDetailActivity implements OnClickListener, BookDetailsFetechedResultsHandler {

    private String LOG_TAG = FBReader.LOG_LABEL;

    private String username;

    private String password;

    protected Bookshare_Metadata_Bean metadata_bean;

    private BookshareWebServiceClient bws = new BookshareWebServiceClient(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);

    private static final int MESSAGE_TIMEOUT_MILLIS = 3000;

    private static final int POPUP_TIMEOUT_MILLIS = 2000;

    private static final int DAISY_DOWNLOAD_TYPE = 1;

    private static final int DAISY_WITH_IMAGES_DOWNLOAD_TYPE = 4;

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

    private boolean isOM; //Organization member
    private boolean isIM; //Independent member
    private boolean isLoggedIn;

    private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;

    private final int START_BOOKSHARE_OM_LIST = 0;
    private final int START_READINGLIST_DIALOG = 1;

    private String memberId = null;

    private String omDownloadPassword;

    private String firstName = null;

    private String lastName = null;

    private boolean downloadSuccess;

    private Resources resources;

    private String downloadedBookDir;

    private Set<Integer> myOngoingNotifications = new HashSet<Integer>();

    private Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bookshare_blank_page);
        Log.i(LOG_TAG, developerKey);
        resources = getApplicationContext().getResources();
        myActivity = this;
        // Set full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Intent intent = getIntent();
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");


        if (username == null || password == null) {
            isFree = true;
        }
        // Obtain the application wide SharedPreferences object and store the
        // login information
        final SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isOM = login_preference.getBoolean("isOM", false);
        isIM = login_preference.getBoolean("isIM", false);
        isLoggedIn = login_preference.getString("username", null) != null;
        final String uri = intent.getStringExtra("ID_SEARCH_URI");

        final VoiceableDialog finishedDialog = new VoiceableDialog(this);
        final String msg = "Fetching book details. Please wait.";
        finishedDialog.popup(msg, POPUP_TIMEOUT_MILLIS);

        final AsyncTask<Object, Void, Integer> bookResultsFetcher = new BookDetailsDownloaderTask(this, uri, password);
        bookResultsFetcher.execute();

        ViewGroup rootLayout = (ViewGroup)findViewById(R.id.book_detail_view);
        SortUtil.applyCurrentFontToAllInViewGroup(this, rootLayout);
    }

    @Override
    public void onResultsFetched(Bookshare_Metadata_Bean metadata_bean) {
        this.metadata_bean = metadata_bean;
        String temp = "";

        if (this.metadata_bean == null) {
            final TextView txtView_msg = (TextView) findViewById(R.id.bookshare_blank_txtView_msg);
            final String noBookFoundMsg = "Book not found.";
            txtView_msg.setText(noBookFoundMsg);
            // todo : return book not found result code

            final View decorView = getWindow().getDecorView();
            if (null != decorView) {
                decorView.setContentDescription(noBookFoundMsg);
            }

            setResult(InternalReturnCodes.NO_BOOK_FOUND);
            confirmAndClose(noBookFoundMsg, MESSAGE_TIMEOUT_MILLIS);
            return;
        }
        if (this.metadata_bean != null) {
            setIsDownloadable(this.metadata_bean);
            setImagesAvailable(this.metadata_bean);
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

            btnReadingList = (Button) findViewById(isIM?R.id.bookshare_btn_readinglist_bottom:R.id.bookshare_btn_readinglist_top);
            btnReadingList.setOnClickListener(OnlineBookDetailActivity.this);
            btnReadingList.setVisibility(shouldShowAddToReadingListButton()? View.VISIBLE : View.GONE);

            bookshare_book_detail_language.setNextFocusDownId(R.id.bookshare_book_detail_category);
            bookshare_book_detail_category.setNextFocusDownId(R.id.bookshare_book_detail_publish_date);
            bookshare_book_detail_publish_date.setNextFocusUpId(R.id.bookshare_book_detail_category);
            bookshare_book_detail_synopsis_text.setNextFocusUpId(R.id.bookshare_book_detail_copyright);

            book_detail_view.requestFocus();
            // If the book is not downloadable, do not show the download
            // button
            if (!isDownloadable) {
                btnDownload.setVisibility(View.GONE);
                btnDownloadWithImages.setVisibility(View.GONE);
                bookshare_book_detail_authors.setNextFocusDownId(R.id.bookshare_download_not_available_msg);
                bookshare_book_detail_isbn.setNextFocusUpId(R.id.bookshare_download_not_available_msg);
                bookshare_download_not_available_text.setNextFocusUpId(R.id.bookshare_book_detail_authors);
            } else {
                bookshare_download_not_available_text.setVisibility(View.GONE);
                btnReadingList.setNextFocusUpId(R.id.bookshare_book_detail_authors);
                btnReadingList.setNextFocusDownId(R.id.bookshare_btn_download);
                btnDownload.setNextFocusDownId(R.id.bookshare_btn_download_images);
                btnDownload.setNextFocusUpId(btnReadingList.getId());
                btnDownloadWithImages.setNextFocusDownId(R.id.bookshare_book_detail_isbn);
                btnDownloadWithImages.setNextFocusUpId(R.id.bookshare_btn_download);
                bookshare_book_detail_authors.setNextFocusDownId(R.id.bookshare_btn_download);

                btnDownload.setOnClickListener(OnlineBookDetailActivity.this);
                btnDownloadWithImages.setOnClickListener(OnlineBookDetailActivity.this);

            }
            if (!imagesAvailable) {
                Log.d("checking images", String.valueOf(imagesAvailable));
                btnDownloadWithImages.setVisibility(View.GONE);
            }
            if (this.metadata_bean.getTitle() != null) {
                for (int i = 0; i < this.metadata_bean.getTitle().length; i++) {
                    temp = temp + this.metadata_bean.getTitle()[i];
                }
                bookshare_book_detail_title_text.append(temp);
                temp = "";
            }

            if (this.metadata_bean.getAuthors() != null) {
                for (int i = 0; i < this.metadata_bean.getAuthors().length; i++) {
                    if (i == 0) {
                        temp = this.metadata_bean.getAuthors()[i];
                    } else {
                        temp = temp + ", " + this.metadata_bean.getAuthors()[i];
                    }
                }
                if (temp == null) {
                    temp = "";
                }
                temp = temp.trim().equals("") ? getResources().getString(R.string.book_details_not_available) : temp;
                bookshare_book_detail_authors.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_authors.setText(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getIsbn() != null) {
                temp = this.metadata_bean.getIsbn().trim().equals("") ? getResources().getString(
                        R.string.book_details_not_available) : this.metadata_bean.getIsbn();
                bookshare_book_detail_isbn.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_isbn.append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getLanguage() != null) {
                temp = this.metadata_bean.getLanguage().trim().equals("") ? getResources().getString(
                        R.string.book_details_not_available) : this.metadata_bean.getLanguage();
                bookshare_book_detail_language.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_language.append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getCategory() != null) {
                for (int i = 0; i < this.metadata_bean.getCategory().length; i++) {
                    if (i == 0) {
                        temp = this.metadata_bean.getCategory()[i];
                    } else {
                        temp = temp + ", " + this.metadata_bean.getCategory()[i];
                    }
                }

                if (temp == null) {
                    temp = "";
                }
                temp = temp.trim().equals("") ? getResources().getString(R.string.book_details_not_available) : temp;
                bookshare_book_detail_category.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_category.append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getPublishDate() != null) {
                final StringBuilder str_date = new StringBuilder(this.metadata_bean.getPublishDate());
                final String mm = str_date.substring(0, 2);
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

                final String publish_date = str_date.substring(2, 4) + " " + month + " " + str_date.substring(4);
                temp = publish_date.trim().equals("") ? "Not available" : publish_date;
                bookshare_book_detail_publish_date.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_publish_date
                        .append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getPublisher() != null) {
                temp = this.metadata_bean.getPublisher().trim().equals("") ? getResources().getString(
                        R.string.book_details_not_available) : this.metadata_bean.getPublisher();
                bookshare_book_detail_publisher.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_publisher.append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getCopyright() != null) {
                temp = this.metadata_bean.getCopyright().trim().equals("") ? getResources().getString(
                        R.string.book_details_not_available) : this.metadata_bean.getCopyright();
                bookshare_book_detail_copyright.append(temp);
                temp = "";
            } else {
                bookshare_book_detail_copyright.append(getResources().getString(R.string.book_details_not_available));
            }

            if (this.metadata_bean.getBriefSynopsis() != null) {
                for (int i = 0; i < this.metadata_bean.getBriefSynopsis().length; i++) {
                    if (i == 0) {
                        temp = this.metadata_bean.getBriefSynopsis()[i];
                    } else {
                        temp = temp + " " + this.metadata_bean.getBriefSynopsis()[i];
                    }
                }
                if (temp == null) {
                    temp = "";
                }
                temp = temp.trim().equals("") ? getResources().getString(R.string.book_details_not_available) : temp;
                bookshare_book_detail_synopsis_text.append(temp.trim());
            } else if (this.metadata_bean.getCompleteSynopsis() != null) {
                for (int i = 0; i < this.metadata_bean.getCompleteSynopsis().length; i++) {
                    if (i == 0) {
                        temp = this.metadata_bean.getCompleteSynopsis()[i];
                    } else {
                        temp = temp + " " + this.metadata_bean.getCompleteSynopsis()[i];
                    }
                }
                if (temp == null) {
                    temp = "";
                }
                temp = temp.trim().equals("") ? getResources().getString(R.string.book_details_not_available) : temp;

                bookshare_book_detail_synopsis_text.append(temp.trim());
            } else if (this.metadata_bean.getBriefSynopsis() == null && this.metadata_bean.getCompleteSynopsis() == null) {
                bookshare_book_detail_synopsis_text.append("No Synopsis available");
            }

            ViewGroup rootLayout = (ViewGroup)findViewById(R.id.book_detail_view);
            SortUtil.applyCurrentFontToAllInViewGroup(this, rootLayout);

            findViewById(R.id.bookshare_book_detail_title).requestFocus();
        }

    }

    // Start downlading task if the OM download password has been received
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         if (requestCode == START_BOOKSHARE_OM_LIST) {
            if (data != null) {
                memberId = data.getStringExtra(Bookshare_OM_Member_Bean.MEMBER_ID);
                firstName = data.getStringExtra(Bookshare_OM_Member_Bean.FIRST_NAME);
                lastName = data.getStringExtra(Bookshare_OM_Member_Bean.LAST_NAME);
                new DownloadFilesTask().execute();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showAlert(String msg) {
        final VoiceableDialog downloadStartedDialog = new VoiceableDialog(myActivity);
        downloadStartedDialog.popup(msg, POPUP_TIMEOUT_MILLIS);
    }

    private ZLFile getOpfFile() {
        final ZLFile bookDir = ZLFile.createFileByPath(downloadedBookDir);
        final List<ZLFile> bookEntries = bookDir.children();
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
        final Intent intent = new Intent(getApplicationContext(), FBReaderWithNavigationBar.class);
        if (file != null) {
            intent.setAction(Intent.ACTION_VIEW).setData(Uri.fromFile(file));
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private Notification createDownloadFinishNotification(File file, String title, boolean success) {
        final ZLResource resource = BookDownloaderService.getResource();
        final String tickerText = success ? resource.getResource("tickerSuccess").getValue() : resource.getResource(
                "tickerError").getValue();
        final String contentText = success ? resource.getResource("contentSuccess").getValue() : resource.getResource(
                "contentError").getValue();
        final Notification notification = new Notification(android.R.drawable.stat_sys_download_done, tickerText,
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        final Intent intent = success ? getFBReaderIntent(file) : new Intent();
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(getApplicationContext(), title, contentText, contentIntent);
        return notification;
    }

    private Notification createDownloadProgressNotification(final String title) {

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

        final Notification notification = new NotificationCompat.Builder(this).setContentTitle(title)
                .setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true).setProgress(0, 0, true)
                .setAutoCancel(true).setContentIntent(contentIntent).build();

        return notification;
    }

    // A custom AsyncTask class for carrying out the downloading task in a
    // separate background thread
    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

        private Bookshare_Error_Bean error;

        private Bookshare_PackagingStatus_Bean status;

        private String download_uri;

        private final String id = metadata_bean.getContentId();

        // Will be called in the UI thread
        @Override
        protected void onPreExecute() {

            // Disable the download button while the download is in progress

            currentButton.setText(R.string.book_details_download_downloading);
            // Disable the download button while the download is in progress
            currentButton.setEnabled(false);

            downloadedBookDir = null;

            if (isFree) {
                download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
                        + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/content/" + id + "/version/"
                        + downloadType + "?api_key=" + developerKey;
            } else if (isOM) {
                download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
                        + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/member/" + memberId + "content/"
                        + id + "/version/1/for/" + username + "?api_key=" + developerKey;
            } else {
                download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL
                        + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/content/" + id + "/version/"
                        + downloadType + "/for/" + username + "?api_key=" + developerKey;
            }

        }

        // Will be called in a separate thread
        // change the downloadType here
        // for
        // images
        @Override
        protected Void doInBackground(final Void... params) {

            final Notification progressNotification = createDownloadProgressNotification(metadata_bean.getTitle()[0]);

            final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            myOngoingNotifications.add(Integer.valueOf(id));
            notificationManager.notify(Integer.valueOf(id), progressNotification);

            try {
                Log.d(LOG_TAG, "download_uri :" + download_uri);

                HttpsURLConnection httpsUrlConnection = bws.getHttpsUrlConnection(password, download_uri);
                String headerValue = httpsUrlConnection.getContentType();
                Log.i(LOG_TAG, "header value " + headerValue);

                if (downloadType == DAISY_WITH_IMAGES_DOWNLOAD_TYPE) {
                    if (!headerValue.contains("zip")) {
                        status = new Bookshare_PackagingStatus_Bean();
                        status.parseInputStream(httpsUrlConnection.getInputStream());

                        Log.i(LOG_TAG, "packaging status, before while" + status.getPackagingStatus());

                        while (!headerValue.contains("zip")) {

                            publishProgress();
                            Log.d(LOG_TAG, "header of response in while" + headerValue);
                            Log.d(LOG_TAG, "status in while" + status.getPackagingStatus());

                            if (status.getContentId() == "" || status.getPackagingStatus() == "CANCELLED")
                                break;

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated
                                e.printStackTrace();
                                Log.e(LOG_TAG, " problem waiting book with images to download", e);
                            }

                            httpsUrlConnection = bws.getHttpsUrlConnection(password, download_uri);
                            headerValue = httpsUrlConnection.getContentType();

                        }
                    }
                }
                // response.
                // Get hold of the response entity

                if (httpsUrlConnection.getContent() != null && httpsUrlConnection.getContentLength() > 0) {
                    Log.i(LOG_TAG, "get hold of the response entity");
                    String filename = "bookshare_" + Math.random() * 10000 + ".zip";
                    if (metadata_bean.getTitle() != null) {
                        String temp = "";
                        for (int i = 0; i < metadata_bean.getTitle().length; i++) {
                            temp = temp + metadata_bean.getTitle()[i];
                        }
                        filename = temp;
                        filename = filename.replaceAll(" +", "_").replaceAll(":", "__").replaceAll("/", "-");
                        if (isOM) {
                            filename = filename + "_" + firstName + "_" + lastName;
                        }
                    }
                    String zip_file = Paths.BooksDirectoryOption().getValue() + "/" + filename + ".zip";
                    downloadedBookDir = Paths.BooksDirectoryOption().getValue() + "/" + filename;

                    File downloaded_zip_file = new File(zip_file);
                    if (downloaded_zip_file.exists()) {
                        downloaded_zip_file.delete();
                    }

                    // entity.

                    // Log.w("", "******  zip_file *****" + zip_file);

                    if (headerValue.contains("zip") || headerValue.contains("bks2")) {
                        try {
                            Log.d(LOG_TAG, "Contains zip");
                            final java.io.BufferedInputStream in = new java.io.BufferedInputStream(
                                    httpsUrlConnection.getInputStream());
                            final java.io.FileOutputStream fos = new java.io.FileOutputStream(downloaded_zip_file);
                            final java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
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
                                Log.i(LOG_TAG, "******Before creating ZipFile******" + zip_file);
                                // Initiate ZipFile object with the path/name of
                                // the zip file.
                                final ZipFile zipFile = new ZipFile(zip_file);

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
                                        omDownloadPassword = login_preference.getString("downloadPassword", "");
                                        zipFile.setPassword(omDownloadPassword);
                                    }
                                }

                                // Get the list of file headers from the zip
                                // file
                                List fileHeaderList = zipFile.getFileHeaders();

                                Log.e(LOG_TAG, "******Before for******");
                                // Loop through the file headers
                                for (int i = 0; i < fileHeaderList.size(); i++) {
                                    FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                                    Log.i(LOG_TAG, downloadedBookDir);
                                    // Extract the file to the specified
                                    // destination
                                    zipFile.extractFile(fileHeader, downloadedBookDir);
                                }
                            }
                            // Unzip the non-encrypted archive file
                            else {
                                try {
                                    final File file = new File(downloadedBookDir);
                                    file.mkdir();
                                    final String destinationname = downloadedBookDir + "/";
                                    final byte[] buf = new byte[1024];
                                    ZipInputStream zipinputstream = null;
                                    ZipEntry zipentry;
                                    zipinputstream = new ZipInputStream(new FileInputStream(zip_file));

                                    zipentry = zipinputstream.getNextEntry();
                                    while (zipentry != null) {
                                        // for each entry to be extracted
                                        final String entryName = zipentry.getName();
                                        Log.e(LOG_TAG, "entryname " + entryName);
                                        int n;
                                        FileOutputStream fileoutputstream;
                                        final File newFile = new File(entryName);
                                        final String directory = newFile.getParent();

                                        if (directory == null) {
                                            if (newFile.isDirectory())
                                                break;
                                        }

                                        fileoutputstream = new FileOutputStream(destinationname + entryName);

                                        while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                                            fileoutputstream.write(buf, 0, n);
                                        }

                                        fileoutputstream.close();
                                        zipinputstream.closeEntry();
                                        zipentry = zipinputstream.getNextEntry();

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
                        httpsUrlConnection = bws.getHttpsUrlConnection(password, download_uri);
                        headerValue = httpsUrlConnection.getContentType();
                        downloadSuccess = false;
                        error = new Bookshare_Error_Bean();
                        error.parseInputStream(httpsUrlConnection.getErrorStream());
                    }
                }
            } catch (IOException ie) {
                Log.e(LOG_TAG, "IOException: " + ie, ie);
            }
            return null;
        }

        // Will be called in the UI thread
        @Override
        protected void onPostExecute(final Void param) {

            if (downloadSuccess) {
                currentButton.setText(resources.getString(R.string.book_details_download_success));
                currentButton.setEnabled(true);

            } else {
                currentButton.setText(resources.getString(R.string.book_details_download_error));
                currentButton.setEnabled(memberId != null);
                if (memberId != null) {
                    currentButton.setText(resources.getString(R.string.book_details_download_error_other_member));
                }
                downloadedBookDir = null;
            }

            final Handler downloadFinishHandler = new Handler() {
                public void handleMessage(Message message) {
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    final int id = Integer.valueOf(metadata_bean.getContentId());
                    notificationManager.cancel(id);
                    myOngoingNotifications.remove(Integer.valueOf(id));
                    File file = null;
                    if (downloadSuccess) {
                        file = new File(getOpfFile().getPath());

                        try {
                            ZLFile zlFile = ZLFile.createFileByPath(file.getAbsolutePath());
                            final Book book = Book.getByFile(zlFile);
                            book.save();
                            ((SQLiteBooksDatabase) SQLiteBooksDatabase.Instance()).updateBookBookshareId(book, metadata_bean.getBookshareId());
                            ((SQLiteBooksDatabase) SQLiteBooksDatabase.Instance()).updateBookStatus(book);
                        }
                        catch (Exception e){
                            Log.e("DATABASE", "error updating book access date after download", e);
                        }
                    }
                    notificationManager.notify(id,
                            createDownloadFinishNotification(file, metadata_bean.getTitle()[0], message.what != 0));
                }
            };
            currentButton.requestFocus();
            downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
        }
    }

    // For keeping the screen from rotating
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setIsDownloadable(final Bookshare_Metadata_Bean bean) {
        String availableToDownloadValue = bean.getAvailableToDownload();
        if (availableToDownloadValue == null) {
            isDownloadable = false;
            return;
        }

        isDownloadable = (availableToDownloadValue.equals("1"));
    }

    private void setImagesAvailable(final Bookshare_Metadata_Bean bean) {
        imagesAvailable = (bean.getImages() != null && !bean.getImages().contains("0"));
        // Log.i(LOG_TAG, "images" + bean.getImages() +
        // String.valueOf(imagesAvailable));
    }

    /*
     * Display voiceable message and then close
     */
    private void confirmAndClose(final String msg, final int timeout) {
        final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
        dialog.popup(msg, timeout);
    }

    // called after the download button is pressed
    @Override
    public void onClick(View v) {
        if(v.getId() == btnReadingList.getId()) {
            showReadingListsDialog(metadata_bean.getBookshareId());
        }
        else {
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
    }

    // called after the download button is pressed, after onClick method
    private void downloadPressed() {
        // TODO Auto-generated method stub

        final String downloadText = currentButton.getText().toString();
        if (downloadText.equalsIgnoreCase(resources.getString(R.string.book_details_download_button))
                || downloadText.equalsIgnoreCase(resources.getString(R.string.book_details_download_images))
                || downloadText
                        .equalsIgnoreCase(resources.getString(R.string.book_details_download_error_other_member))) {

            // Start a new Activity for getting the OM
            // member list
            // See onActivityResult for further
            // processing
            if (isOM) {
                final Intent intent = new Intent(getApplicationContext(), Bookshare_OM_List.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivityForResult(intent, START_BOOKSHARE_OM_LIST);
            } else {
                if (downloadType == DAISY_DOWNLOAD_TYPE) {
                    ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON, Analytics.EVENT_LABEL_DOWNLOAD_BOOK);
                } else if (downloadType == DAISY_WITH_IMAGES_DOWNLOAD_TYPE) {
                    ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON, Analytics.EVENT_LABEL_DOWNLOAD_BOOK_WITH_IMAGES);
                }
                new DownloadFilesTask().execute();

            }
            showAlert(getResources().getString(R.string.book_details_download_started));
        }

        // View book or display error
        else if (currentButton.getText().toString()
                .equalsIgnoreCase(resources.getString(R.string.book_details_download_success))) {
            setResult(BOOKSHARE_BOOK_DETAILS_FINISHED);
            if (null == downloadedBookDir) {
                final VoiceableDialog finishedDialog = new VoiceableDialog(currentButton.getContext());
                String message = resources.getString(R.string.book_details_open_error);
                finishedDialog.popup(message, POPUP_TIMEOUT_MILLIS);
            } else {
                if (null != downloadedBookDir) {
                    ZLFile opfFile = getOpfFile();
                    if (null != opfFile) {
                        startActivity(new Intent(getApplicationContext(), FBReaderWithNavigationBar.class).setAction(Intent.ACTION_VIEW)
                                .putExtra(FBReader.BOOK_PATH_KEY, opfFile.getPath())
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }

                }
            }
        }
    }

}
