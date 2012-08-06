package org.geometerplus.android.fbreader.network.bookshare;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.network.bookshare.socialnetworks.Bookshare_FacebookHandler;
import org.geometerplus.android.fbreader.network.bookshare.socialnetworks.Bookshare_Twitter_Handler;
import org.geometerplus.android.fbreader.network.bookshare.socialnetworks.Bookshare_Twitter_Handler.GetAccessTokenTask;
import org.geometerplus.android.fbreader.network.bookshare.socialnetworks.SocialNetowkPosts;
import org.geometerplus.android.fbreader.network.bookshare.socialnetworks.TwitterAccessTokenListener;
import org.geometerplus.android.fbreader.subscription.AllDbPeriodicalEntity;
import org.geometerplus.android.fbreader.subscription.BooksharePeriodicalDataSource;
import org.geometerplus.android.fbreader.subscription.PeriodicalDBUtils;
import org.geometerplus.android.fbreader.subscription.PeriodicalsSQLiteHelper;
import org.geometerplus.android.fbreader.subscription.SubscribedDbPeriodicalEntity;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class Bookshare_Periodical_Edition_Details extends Activity implements TwitterAccessTokenListener{

	
	private String username;
	private String password;
	private Bookshare_Edition_Metadata_Bean metadata_bean;
	private InputStream inputStream;
	private BookshareWebservice bws = new BookshareWebservice(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private final int DATA_FETCHED = 99;
	private View book_detail_view;
	private TextView bookshare_book_detail_title_text;
	private TextView bookshare_book_detail_authors;
	private TextView bookshare_book_detail_edition;
	private TextView bookshare_book_detail_revision;
	private TextView bookshare_book_detail_category;
	private TextView bookshare_book_detail_publish_date;
	private TextView bookshare_book_detail_publisher;
	private TextView bookshare_book_detail_copyright;
	private TextView bookshare_book_detail_synopsis_text;
	private TextView bookshare_download_not_available_text;
	private CheckBox chkbx_subscribe_periodical;
	private TextView bookshare_subscribe_explained;
	private String selectedPeriodicalTitle;
	private String selectedPeriodicalId;
	private Button btn_download;
	private Button twtr_share;
	private Button fb_share;
	
	boolean isDownloadable;
	private final int BOOKSHARE_PERIODICAL_EDITION_DETAILS_FINISHED = 1;
	
	private boolean isFree = false;
	private boolean isOM;	//true if user is an organizational member
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private final int START_BOOKSHARE_OM_LIST = 0;	//Start Organizational Member list
	private String memberId = null;
	private String omDownloadPassword;
    private String firstName = null;
    private String lastName = null;
	private boolean downloadSuccess;
    private Resources resources;
    private String downloadedBookDir;
    private Set<Integer> myOngoingNotifications = new HashSet<Integer>();
    private Activity myActivity;
    private Bookshare_FacebookHandler fbHandler;
    private Bookshare_Twitter_Handler twtrHandler;
	private BooksharePeriodicalDataSource dataSource;
	private SharedPreferences mTwtrSharedPref;
	private String verifier;
	private TextView bookshare_share_with_friends;
	private PeriodicalsSQLiteHelper periodicalDBHelper;
	private SQLiteDatabase periodicalDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bookshare_blank_page);
		
		resources = getApplicationContext().getResources();
        myActivity = this;

		// Set full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent  = getIntent();
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");
		
		if(username == null || password == null){
			isFree = true;
		}
		
		dataSource = BooksharePeriodicalDataSource.getInstance(this);
		periodicalDBHelper = new PeriodicalsSQLiteHelper(this);
		periodicalDb = periodicalDBHelper.getWritableDatabase();
		
		selectedPeriodicalTitle=intent.getStringExtra("PERIODICAL_TITLE");
		selectedPeriodicalId=intent.getStringExtra("PERIODICAL_ID");
		
		fbHandler= new Bookshare_FacebookHandler(this);
		twtrHandler = new Bookshare_Twitter_Handler(this);
		
		// Obtain the application wide SharedPreferences object and store the login information
		//Get information about user. (i.e. Whether he's an organizational member)
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		isOM = login_preference.getBoolean("isOM", false);
				
		final String uri = intent.getStringExtra("ID_SEARCH_URI");

        final VoiceableDialog finishedDialog = new VoiceableDialog(this);
        String msg = "Fetching periodical details. Please wait.";
        finishedDialog.popup(msg, 2000);
		new Thread(){
			public void run(){
				try{
					inputStream = bws.getResponseStream(password, uri);
					Message msg = Message.obtain(handler);
					msg.what = DATA_FETCHED;
					msg.sendToTarget();
				}
				catch(IOException ioe){
					System.out.println(ioe);
				}
				catch(URISyntaxException use){
					System.out.println(use);
				}
			}
		}.start();
    
	}
	
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		periodicalDBHelper.close();
	}


	// Start downlading task if the OM download password has been received
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == START_BOOKSHARE_OM_LIST){
			if(data != null){
				memberId = data.getStringExtra(Bookshare_OM_Member_Bean.MEMBER_ID);
                firstName = data.getStringExtra(Bookshare_OM_Member_Bean.FIRST_NAME);
                lastName = data.getStringExtra(Bookshare_OM_Member_Bean.LAST_NAME);
				new DownloadFilesTask().execute();
			}
		}
		else if(requestCode == Bookshare_Twitter_Handler.BOOKSHARE_TWITTER_REQUEST){
			if(resultCode==Activity.RESULT_OK){
				verifier=data.getStringExtra("verifier");
				if (verifier != null) {
					GetAccessTokenTask accTokenTask = twtrHandler.new GetAccessTokenTask();
					accTokenTask.setVerifier(verifier);
					accTokenTask.execute(new TwitterAccessTokenListener[]{this});
				}
			}

		}
	}
		
		
	@Override
	public void onAccessTokenFound(String result,String accessToken,
			String accessTokenSecret) {
		if (Bookshare_Twitter_Handler.SUCCESS_STRING.equals(result)) {
			Editor e = mTwtrSharedPref.edit();
			e.putString("twtr_access_token", accessToken);
			e.putString("twtr_access_token_secret",
					accessTokenSecret);
			e.commit();
		}
		postOnTwitter(accessToken, accessTokenSecret);
		
	}
	
	//Actual post of twitter update is extracted to this
	private void postOnTwitter(String accessToken,String accessTokenSecret){
		(twtrHandler.new UpdateStatusTask()).execute(new String[] {accessToken,accessTokenSecret,SocialNetowkPosts.getShortenedPeriodicalPost(metadata_bean)});
	}
	
		// Handler for processing the returned stream from book detail search
		Handler handler = new Handler(){
			public void handleMessage(Message msg){
				if(msg.what==DATA_FETCHED){

					String response_HTML = bws.convertStreamToString(inputStream);
					String response = response_HTML.replace("&apos;", "\'").replace("&quot;", "\"").replace("&amp;", "and").replace("&#xd;\n", "\n").replace("&#x97;", "-");
					
					// Parse the response String
					parseResponse(response);
					String temp = "";

					if(metadata_bean == null){
						TextView txtView_msg = (TextView)findViewById(R.id.bookshare_blank_txtView_msg);
	                    String noBookFoundMsg = "Periodical not found.";
						txtView_msg.setText(noBookFoundMsg);
	                    //todo : return book not found result code

	                    View decorView = getWindow().getDecorView();
	                    if (null != decorView) {
	                        decorView.setContentDescription(noBookFoundMsg);
	                    }
	                    
	                    setResult(InternalReturnCodes.NO_PERIODICAL_EDITION_FOUND);
	                    confirmAndClose(noBookFoundMsg, 3000);
	                    return;
					}
					if(metadata_bean != null){
						setIsDownloadable(metadata_bean);
						setContentView(R.layout.bookshare_book_detail);
						
						
						
						((TextView)findViewById(R.id.bookshare_book_detail_heading)).setText("Periodical Details");
						book_detail_view = (View)findViewById(R.id.book_detail_view);
						bookshare_book_detail_title_text = (TextView)findViewById(R.id.bookshare_book_detail_title);
						if(selectedPeriodicalTitle!=null){
							metadata_bean.setTitle(selectedPeriodicalTitle);
						}else{
							bookshare_book_detail_title_text.append(" Title not found");
						}
						
						if(selectedPeriodicalId != null){
							metadata_bean.setPeriodicalId(selectedPeriodicalId);
						}
						
						bookshare_book_detail_authors = (TextView)findViewById(R.id.bookshare_book_detail_authors);
						bookshare_book_detail_authors.setVisibility(View.GONE);
						
						bookshare_book_detail_edition= (TextView)findViewById(R.id.bookshare_book_detail_isbn);
						bookshare_book_detail_edition.setText("Edition: ");
						bookshare_book_detail_revision = (TextView)findViewById(R.id.bookshare_book_detail_language);
						bookshare_book_detail_revision.setText("Revision: ");
						bookshare_book_detail_category = (TextView)findViewById(R.id.bookshare_book_detail_category);
						
						bookshare_book_detail_publish_date = (TextView)findViewById(R.id.bookshare_book_detail_publish_date);
						bookshare_book_detail_publish_date.setVisibility(View.GONE);
						
						bookshare_book_detail_publisher = (TextView)findViewById(R.id.bookshare_book_detail_publisher);
						bookshare_book_detail_publisher.setVisibility(View.GONE);
						
						bookshare_book_detail_copyright = (TextView)findViewById(R.id.bookshare_book_detail_copyright);
						bookshare_book_detail_copyright.setVisibility(View.GONE);
						
						bookshare_book_detail_synopsis_text = (TextView)findViewById(R.id.bookshare_book_detail_synopsis_text);
						bookshare_book_detail_synopsis_text.setVisibility(View.GONE);
						
						btn_download = (Button)findViewById(R.id.bookshare_btn_download);
						bookshare_download_not_available_text = (TextView) findViewById(R.id.bookshare_download_not_available_msg);
	                    
						fb_share = (Button)findViewById(R.id.fb_share);
						twtr_share = (Button)findViewById(R.id.twtr_share);
						
						bookshare_book_detail_title_text.setNextFocusDownId(R.id.bookshare_btn_download);
						
						//Need to set status of the subscibe checkbox everytime user comes to 'details' page
						chkbx_subscribe_periodical=(CheckBox)findViewById(R.id.bookshare_chkbx_subscribe_periodical);
						
						chkbx_subscribe_periodical.setNextFocusDownId(R.id.bookshare_share_with_friends);
						chkbx_subscribe_periodical.setNextFocusUpId(R.id.bookshare_btn_download);
						
						if(dataSource.doesExist(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS, 
								new SubscribedDbPeriodicalEntity(metadata_bean.getPeriodicalId(),null))){
							chkbx_subscribe_periodical.setChecked(true);
						}
						bookshare_subscribe_explained=(TextView)findViewById(R.id.bookshare_subscribe_explained);
						
						bookshare_share_with_friends = (TextView) findViewById(R.id.bookshare_share_with_friends);
						
						bookshare_share_with_friends.setNextFocusDownId(R.id.fb_share);
						bookshare_share_with_friends.setNextFocusUpId(R.id.bookshare_btn_download);
						
						fb_share.setNextFocusDownId(R.id.twtr_share);
						fb_share.setNextFocusRightId(R.id.twtr_share);
						
						twtr_share.setNextFocusDownId(R.id.bookshare_book_detail_isbn);
						twtr_share.setNextFocusUpId(R.id.fb_share);
						twtr_share.setNextFocusLeftId(R.id.fb_share);
						
	                    bookshare_book_detail_revision.setNextFocusDownId(R.id.bookshare_book_detail_category);
	                    bookshare_book_detail_category.setNextFocusDownId(R.id.bookshare_book_detail_publish_date);
	                    
	                    book_detail_view.requestFocus();
	                    
	                    
	            			            		
	                    twtr_share.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								mTwtrSharedPref = getSharedPreferences("twtr_oauth",
										Activity.MODE_PRIVATE);
								
								String accessToken = mTwtrSharedPref.getString("twtr_access_token",
										null);
								String accessTokenSecret = mTwtrSharedPref.getString(
										"twtr_access_token_secret", null);

								if(accessToken == null || accessTokenSecret == null){
									twtrHandler.setUpTwitterForPosting();
								}else{
									postOnTwitter(accessToken, accessTokenSecret);
								}
								
							}
						});
	                    
	                    fb_share.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								fbHandler.ssoInitialAuth();	//get Single Sign
								fbHandler.getFBPermission();	//get permission to check user's friend details
								fbHandler.getAccessToken();	//get Access token which allows to do so
								fbHandler.postPeriodicalOnWall(metadata_bean);	
							}
						});
	                    
						// If the book is not downloadable, do not show the download button
						if(!isDownloadable){
							btn_download.setVisibility(View.GONE);
	                        bookshare_book_detail_edition.setNextFocusUpId(R.id.bookshare_download_not_available_msg);
	                        bookshare_download_not_available_text.setNextFocusUpId(R.id.bookshare_book_detail_authors);
						} else {
							bookshare_download_not_available_text.setVisibility(View.GONE);
							
							//If user is logged in only, show the subscribe option
							if(isFree){
								chkbx_subscribe_periodical.setVisibility(View.GONE);
								bookshare_subscribe_explained.setVisibility(View.GONE);
							}
							
	                        btn_download.setNextFocusDownId(R.id.bookshare_chkbx_subscribe_periodical);
	                        btn_download.setNextFocusUpId(R.id.bookshare_book_detail_title);
	                        
	                        bookshare_book_detail_edition.setNextFocusUpId(R.id.bookshare_share_with_friends);
	                        
							btn_download.setOnClickListener(new OnClickListener(){
								public void onClick(View v){
									
	                                final String downloadText = btn_download.getText().toString();
									if(downloadText.equalsIgnoreCase(resources.getString(R.string.book_details_download_button)) || 
	                                        downloadText.equalsIgnoreCase(resources.getString(R.string.book_details_download_error_other_member))){
										
										// Start a new Activity for getting the OM member list
										// See onActivityResult for further processing
										if(isOM){
											Intent intent = new Intent(getApplicationContext(), Bookshare_OM_List.class);
											intent.putExtra("username", username);
											intent.putExtra("password", password);
											startActivityForResult(intent, START_BOOKSHARE_OM_LIST);
										}
										else{
											new DownloadFilesTask().execute();
	                                        showAlert(getResources().getString(R.string.book_details_download_started)); //'Download Started'
										}
									}
									
									// View book or display error
									else if(btn_download.getText().toString().equalsIgnoreCase(resources.getString(R.string.edition_details_download_success))){
										setResult(BOOKSHARE_PERIODICAL_EDITION_DETAILS_FINISHED);
										if (null == downloadedBookDir) {
											final VoiceableDialog finishedDialog = new VoiceableDialog(btn_download.getContext());
	                                        String message =  resources.getString(R.string.book_details_open_error);
	                                        finishedDialog.popup(message, 2000);
										}
										else {
	                                        if (null != downloadedBookDir) {
	                                            ZLFile opfFile = getOpfFile();
	                                            if (null != opfFile) {
	                                                startActivity(
	                                                    new Intent(getApplicationContext(), FBReader.class)
	                                                        .setAction(Intent.ACTION_VIEW)
	                                                        .putExtra(FBReader.BOOK_PATH_KEY, opfFile.getPath())
	                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
	                                                );
	                                            }
	                                            
	                                        }
										}
									}
								}
							});
							
							chkbx_subscribe_periodical.setOnCheckedChangeListener(new OnCheckedChangeListener() {
								
								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									SubscribedDbPeriodicalEntity sEntity = new SubscribedDbPeriodicalEntity();
									sEntity.setId(metadata_bean.getPeriodicalId());
									sEntity.setTitle(metadata_bean.getTitle());
									sEntity.setLatestEdition("00000000");
									sEntity.setLatestRevision(0);
									//TODO: Update sEntity revision/edition to latest from the AllDbPeriodical db
									
									//If user enables the subscribed option
									if(isChecked){										
										dataSource.insertEntity(periodicalDb,PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,sEntity);									
										Toast.makeText(getApplicationContext(), "You're subscribed to "+selectedPeriodicalTitle, Toast.LENGTH_SHORT).show();
									}//user unsubscribe 
									else{
										dataSource.deleteEntity(periodicalDb,PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,sEntity);
										//if(delEntity != null){
											Toast.makeText(getApplicationContext(), "You're unsubscribed from "+selectedPeriodicalTitle, Toast.LENGTH_SHORT).show();
										//}
									}
								}
							});
						}
						
						// Set the fields of the layout with book details
						if(metadata_bean.getTitle()!=null){
							
								temp = metadata_bean.getTitle();
							
							if(temp == null){
								temp = "";
							}
	                        bookshare_book_detail_title_text.append(temp);
							temp = "";
						}

												
						if(metadata_bean.getRevision() != null){
							temp = metadata_bean.getRevision().trim().equals("") ? getResources().getString(R.string.book_details_not_available) : metadata_bean.getRevision();
							bookshare_book_detail_revision.append(temp);
							temp = "";
						}
						else{
							bookshare_book_detail_revision.append(getResources().getString(R.string.book_details_not_available));
						}

						if(metadata_bean.getCategory() != null){
							temp = metadata_bean.getCategory();
							
							if(temp == null){
								temp = "";
							}
							temp = temp.trim().equals("") ? getResources().getString(R.string.book_details_not_available) : temp;
							bookshare_book_detail_category.append(temp);
							temp = "";
						}
						else{
							bookshare_book_detail_category.append(getResources().getString(R.string.book_details_not_available));
						}

						if(metadata_bean.getEdition() != null){
							StringBuilder str_date =  new StringBuilder(metadata_bean.getEdition());
							String mm = str_date.substring(0,2);
							String month = "";
							if(mm.equalsIgnoreCase("01")){
								month = "January";
							}
							else if(mm.equals("02")){
								month = "February";
							}
							else if(mm.equals("03")){
								month = "March";
							}
							else if(mm.equals("04")){
								month = "April";
							}
							else if(mm.equals("05")){
								month = "May";
							}
							else if(mm.equals("06")){
								month = "June";
							}
							else if(mm.equals("07")){
								month = "July";
							}
							else if(mm.equals("08")){
								month = "August";
							}
							else if(mm.equals("09")){
								month = "September";
							}
							else if(mm.equals("10")){
								month = "October";
							}
							else if(mm.equals("11")){
								month = "November";
							}
							else if(mm.equals("12")){
								month = "December";
							}

							String publish_date = str_date.substring(2, 4) + " "+month+" " + str_date.substring(4);
							temp = publish_date.trim().equals("") ? "Not available" : publish_date;
							bookshare_book_detail_edition.append(temp);
							temp = "";
						}
						else{
							bookshare_book_detail_edition.append(getResources().getString(R.string.book_details_not_available));
						}

						
	                    findViewById(R.id.bookshare_book_detail_title).requestFocus();
					}
				}	
			}
		};
		
		
		private void showAlert(String msg) {
	        final VoiceableDialog downloadStartedDialog = new VoiceableDialog(myActivity);
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
	        final Intent intent = new Intent(getApplicationContext(), FBReader.class);
	        if (file != null) {
	            intent.setAction(Intent.ACTION_VIEW).setData(Uri.fromFile(file));
	        }
	        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    }
	    
	    
	    private Notification createDownloadFinishNotification(File file, String title, boolean success) {
	        final ZLResource resource = BookDownloaderService.getResource();
	        final String tickerText = success ?
	            resource.getResource("tickerSuccess").getValue() :
	            resource.getResource("tickerError").getValue();
	        final String contentText = success ?
	            resource.getResource("contentSuccess").getValue() :
	            resource.getResource("contentError").getValue();
	        final Notification notification = new Notification(
	            android.R.drawable.stat_sys_download_done,
	            tickerText,
	            System.currentTimeMillis()
	        );
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        final Intent intent = success ? getFBReaderIntent(file) : new Intent();
	        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
	        notification.setLatestEventInfo(getApplicationContext(), title, contentText, contentIntent);
	        return notification;
	    }
	    
	    private Notification createDownloadProgressNotification(String title) {
	        final RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
	        contentView.setTextViewText(R.id.download_notification_title, title);
	        contentView.setTextViewText(R.id.download_notification_progress_text, "");
	        contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);

	        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

	        final Notification notification = new Notification();
	        notification.icon = android.R.drawable.stat_sys_download;
	        notification.flags |= Notification.FLAG_ONGOING_EVENT;
	        notification.contentView = contentView;
	        notification.contentIntent = contentIntent;

	        return notification;
	    }
	    
	    
	    private class DownloadFilesTask extends AsyncTask<Void, Void, Void>{
			
			private Bookshare_Error_Bean error;

			// Will be called in the UI thread
			@Override
			protected void onPreExecute(){
				btn_download.setText("Downloading Periodical...");
	            downloadedBookDir = null;

				// Disable the download button while the download is in progress
				btn_download.setEnabled(false);
			}
			
			// Will be called in a separate thread
			@Override
			protected Void doInBackground(Void... params) {			 
				final String id = metadata_bean.getContentId();
				String download_uri;
				if(isFree)
					download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/content/"+id+"/version/1?api_key="+developerKey;
				else if(isOM){
					download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/member/"+memberId+"content/"+id+"/version/1/for/"+username+"?api_key="+developerKey;
				}
				else{
					download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/content/"+id+"/version/1/for/"+username+"?api_key="+developerKey;
				}
	            
	            final Notification progressNotification = createDownloadProgressNotification(metadata_bean.getTitle());
	    
	            final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	            myOngoingNotifications.add(Integer.valueOf(id));
	            notificationManager.notify(Integer.valueOf(id), progressNotification);
				
				try{
					System.out.println("download_uri :"+download_uri);
					HttpResponse response = bws.getHttpResponse(password, download_uri);
					// Get hold of the response entity
					HttpEntity entity = response.getEntity();
					
					if (entity != null) {
						String filename = "bookshare_"+Math.random()*10000+".zip";
						if(metadata_bean.getTitle() != null){
							String temp = "";
							
								temp = metadata_bean.getTitle();
							
							filename = temp;
							filename = filename.replaceAll(" +", "_").replaceAll(":", "__");
	                        if (isOM) {
	                            filename = filename + "_" + firstName + "_" + lastName;
	                        }
						}
						String zip_file = Paths.BooksDirectoryOption().getValue() +"/"+ filename + ".zip";
	                    downloadedBookDir = Paths.BooksDirectoryOption().getValue() + "/" + filename;
						
						File downloaded_zip_file = new File(zip_file);
						if(downloaded_zip_file.exists()){
							downloaded_zip_file.delete();
						}
						Header header = entity.getContentType();
						//Log.w("FBR", "******  zip_file *****" + zip_file);
	                    final String headerValue = header.getValue();
						if(headerValue.contains("zip") || headerValue.contains("bks2")) {
							try{
								System.out.println("Contains zip");
								java.io.BufferedInputStream in = new java.io.BufferedInputStream(entity.getContent());
								java.io.FileOutputStream fos = new java.io.FileOutputStream(downloaded_zip_file);
								java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
								byte[] data = new byte[1024];
								int x=0;
								while((x=in.read(data,0,1024))>=0){
									bout.write(data,0,x);
								}
								fos.flush();
								bout.flush();
								fos.close();
								bout.close();
								in.close();
								
								System.out.println("******** Downloading complete");
								
								// Unzip the encrypted archive file 
								if(!isFree){
									System.out.println("******Before creating ZipFile******"+zip_file);
									// Initiate ZipFile object with the path/name of the zip file.
									ZipFile zipFile = new ZipFile(zip_file);
									
									// Check to see if the zip file is password protected
									if (zipFile.isEncrypted()) {
										System.out.println("******isEncrypted******");

										// if yes, then set the password for the zip file
										if(!isOM){
											zipFile.setPassword(password);
										}									
										// Set the OM password sent by the Intent
										else{
											// Obtain the SharedPreferences object shared across the application. It is stored in login activity
											SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
											omDownloadPassword = login_preference.getString("downloadPassword", "");
											zipFile.setPassword(omDownloadPassword);
										}
									}
									
									// Get the list of file headers from the zip file
									List fileHeaderList = zipFile.getFileHeaders();

									System.out.println("******Before for******");
									// Loop through the file headers
									for (int i = 0; i < fileHeaderList.size(); i++) {
										FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
										System.out.println(downloadedBookDir);
										// Extract the file to the specified destination
										zipFile.extractFile(fileHeader, downloadedBookDir);
									}
								}
								// Unzip the non-encrypted archive file
								else{
									try
							        {
										File file = new File(downloadedBookDir);
										file.mkdir();
							            String destinationname = downloadedBookDir + "/";
							            byte[] buf = new byte[1024];
							            ZipInputStream zipinputstream = null;
							            ZipEntry zipentry;
							            zipinputstream = new ZipInputStream(new FileInputStream(zip_file));

							            zipentry = zipinputstream.getNextEntry();
							            while (zipentry != null)
							            {
							                // for each entry to be extracted
							                String entryName = zipentry.getName();
							                System.out.println("entryname "+entryName);
							                int n;
							                FileOutputStream fileoutputstream;
							                File newFile = new File(entryName);
							                String directory = newFile.getParent();
							                
							                if(directory == null)
							                {
							                    if(newFile.isDirectory())
							                        break;
							                }
							                
							                fileoutputstream = new FileOutputStream(
							                   destinationname+entryName);             

							                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
							                    fileoutputstream.write(buf, 0, n);

							                fileoutputstream.close(); 
							                zipinputstream.closeEntry();
							                zipentry = zipinputstream.getNextEntry();

							            }//while

							            zipinputstream.close();
							        }
							        catch (Exception e)
							        {
							            e.printStackTrace();
							        }
								}
								// Delete the downloaded zip file as it has been extracted
								downloaded_zip_file = new File(zip_file);
								if(downloaded_zip_file.exists()){
									downloaded_zip_file.delete();
								}
								downloadSuccess  = true;
							}
							catch(ZipException e){
	                            Log.e("FBR", "Zip Exception", e);
							}
						}
						else{
							downloadSuccess = false;
							error = new Bookshare_Error_Bean();
							error.parseInputStream(response.getEntity().getContent());
						}
					}
				}
				catch(URISyntaxException use){
					System.out.println("URISyntaxException: "+use);
				}
				catch(IOException ie){
					System.out.println("IOException: "+ie);
				}
				return null;
			}

			// Will be called in the UI thread
			@Override
			protected void onPostExecute(Void param){

				if(downloadSuccess){
					btn_download.setText(resources.getString(R.string.edition_details_download_success));
	                btn_download.setEnabled(true);

	                Calendar currCal = Calendar.getInstance();
	                String currentDate = currCal.get(Calendar.MONTH)+"/"+currCal.get(Calendar.DATE)+"/"+currCal.get(Calendar.YEAR)+"";
	                String currentTime = currCal.get(Calendar.HOUR_OF_DAY)+":"+currCal.get(Calendar.MINUTE)+":"+currCal.get(Calendar.SECOND)+"";
	                AllDbPeriodicalEntity allEntity = new AllDbPeriodicalEntity(metadata_bean.getPeriodicalId(), metadata_bean.getTitle(), metadata_bean.getEdition(), Integer.parseInt(metadata_bean.getRevision()), currentDate, currentTime);
	                SubscribedDbPeriodicalEntity subEntity = new SubscribedDbPeriodicalEntity(metadata_bean.getPeriodicalId(), metadata_bean.getTitle(), metadata_bean.getEdition(), Integer.parseInt(metadata_bean.getRevision()));
	                dataSource.insertEntity(periodicalDb, PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS, allEntity);
	                if(dataSource.doesExist(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS, subEntity)){
	                	SubscribedDbPeriodicalEntity existingEntity = (SubscribedDbPeriodicalEntity) dataSource.getEntity(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS, subEntity);
	                	if(subEntity.getLatestEdition().equalsIgnoreCase(PeriodicalDBUtils.getRecentEditionString(existingEntity.getLatestEdition(),subEntity.getLatestEdition()))){
	                		dataSource.insertEntity(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,subEntity);
	                	}
	                }
				}
				else{
					btn_download.setText(resources.getString(R.string.book_details_download_error));
	                btn_download.setEnabled(memberId != null);
	                if (memberId != null) {
	                    btn_download.setText(resources.getString(R.string.book_details_download_error_other_member));
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
	                        file =  new File(getOpfFile().getPath());
	                    }
	                    notificationManager.notify(
	                            id,
	                            createDownloadFinishNotification(file, metadata_bean.getTitle(), message.what != 0)
	                    );
	                }
	            };
	            btn_download.requestFocus();
	            downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
			}
	    }


	   

		
		
	    
		/**
		 * Uses a SAX parser to parse the response
		 * @param response String representing the response
		 */
		private void parseResponse(String response){

			InputSource is = new InputSource(new StringReader(response));

			try{
				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp;
				sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				XMLReader parser = sp.getXMLReader();
				parser.setContentHandler(new SAXHandler());
				parser.parse(is);
			}
			catch(SAXException e){
				System.out.println(e);
			}
			catch (ParserConfigurationException e) {
				System.out.println(e);
			}
			catch(IOException ioe){
			}
		}
		

		// Class that applies parsing logic
		private class SAXHandler extends DefaultHandler{

			boolean metadata = false;
			boolean contentId = false;
			boolean daisy = false;
			boolean brf = false;
			boolean downloadFormats = false;
			boolean images = false;
			boolean edition = false;
			boolean revisionTime = false;
			boolean revision = false;
			boolean category = false;

			boolean downloadFormatElementVisited = false;
			boolean categoryElementVisited = false;

			Vector<String> vector_downloadFormat;
			Vector<String> vector_category;


			public void startElement(String namespaceURI, String localName, String qName, Attributes atts){

				if(qName.equalsIgnoreCase("metadata")){
					System.out.println("******* metadata visited");
					metadata = true;
					metadata_bean = new Bookshare_Edition_Metadata_Bean();
					
					downloadFormatElementVisited = false;

					categoryElementVisited = false;
					vector_downloadFormat = new Vector<String>();
					vector_category = new Vector<String>();
					
				}
				if(qName.equalsIgnoreCase("content-id")){
					contentId = true;
				}
				if(qName.equalsIgnoreCase("daisy")){
					daisy = true;
				}
				if(qName.equalsIgnoreCase("brf")){
					brf = true;
				}
				if(qName.equalsIgnoreCase("download-format")){
					downloadFormats = true;
					if(!downloadFormatElementVisited){
						downloadFormatElementVisited = true;
					}
				}
				if(qName.equalsIgnoreCase("images")){
					images = true;
				}
				if(qName.equalsIgnoreCase("edition")){
					edition = true;
				}
				
				
				if(qName.equalsIgnoreCase("revision-time")){
					revisionTime = true;
				}
				if(qName.equalsIgnoreCase("revision")){
					revision = true;
				}
				if(qName.equalsIgnoreCase("category")){
					category = true;
					if(!categoryElementVisited){
						categoryElementVisited = true;
					}
				}
			}

			public void endElement(String uri, String localName, String qName){

				//  End of one metadata element parsing.
				if(qName.equalsIgnoreCase("metadata")){
					metadata = false;
				}
				if(qName.equalsIgnoreCase("content-id")){
					contentId = false;
				}
				if(qName.equalsIgnoreCase("daisy")){
					daisy = false;
				}
				if(qName.equalsIgnoreCase("brf")){
					brf = false;
				}
				if(qName.equalsIgnoreCase("download-format")){
					downloadFormats = false;
				}
				if(qName.equalsIgnoreCase("images")){
					images = false;
				}
				if(qName.equalsIgnoreCase("edition")){
					edition = false;
				}
				if(qName.equalsIgnoreCase("revision-time")){
					revisionTime = false;
				}
				
				if(qName.equalsIgnoreCase("revision")){
					revision = false;
				}
				if(qName.equalsIgnoreCase("category")){
					category = false;
				}
				
			}
			public void characters(char[] c, int start, int length){

				if(metadata){
					if(contentId){
						metadata_bean.setContentId(new String(c,start,length));
					}
					if(daisy){
						metadata_bean.setDaisy(new String(c,start,length));
					}
					if(brf){
						metadata_bean.setBrf(new String(c,start,length));
					}
					if(downloadFormats){
						vector_downloadFormat.add(new String(c,start,length));
						metadata_bean.setDownloadFormats(vector_downloadFormat.toArray(new String[0]));
					}
					if(images){
						metadata_bean.setImages(new String(c,start,length));
					}
					if(edition){
						metadata_bean.setEdition(new String(c,start,length));
					}
					if(revisionTime){
						metadata_bean.setRevisionTime(new String(c,start,length));
					}
					if(revision){
						metadata_bean.setRevision(new String(c,start,length));
					}
					if(category){
						vector_category.add(new String(c,start,length));
						metadata_bean.setCategory(new String(c,start,length));
						System.out.println("metadata_bean.getCategory() = "+metadata_bean.getCategory());

					}
					
				}
			}
		}
		

		// For keeping the screen from rotating
		@Override
		public void onConfigurationChanged(Configuration newConfig){
			super.onConfigurationChanged(newConfig);
		}
		
		//Determine whether the book is downloadable.
		private void setIsDownloadable(final Bookshare_Edition_Metadata_Bean bean) {
			isDownloadable = (bean.getDownloadFormats() != null && bean.getDownloadFormats().length > 0);
		}
	    
	    /*
	     * Display voiceable message and then close
	     */
	    private void confirmAndClose(String msg, int timeout) {
	        final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
	        dialog.popup(msg, timeout);
	    }


		
}
