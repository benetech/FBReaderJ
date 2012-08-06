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
import org.geometerplus.android.fbreader.subscription.AllDbPeriodicalEntity;
import org.geometerplus.android.fbreader.subscription.BooksharePeriodicalDataSource;
import org.geometerplus.android.fbreader.subscription.IPeriodicalDownloadAPI;
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
import android.util.Log;
import android.widget.RemoteViews;

public class Bookshare_Subscription_Download_Service extends Service{

	//SubscriptionSQLiteHelper dbHelper;
	BooksharePeriodicalDataSource dataSource;
	PeriodicalsSQLiteHelper dbHelper;
	private String username;
	private String password;
    private String downloadedBookDir;
	private String uri;
	private String omDownloadPassword;
	private boolean isFree=false;
	private boolean isOM;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	final BookshareWebservice bws = new BookshareWebservice(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private Bookshare_Edition_Metadata_Bean metadata_bean;
    private Set<Integer> myOngoingNotifications = new HashSet<Integer>();
    private SQLiteDatabase periodicalDb;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new ServiceBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//TODO: get extra for password
		//dbHelper=new SubscriptionSQLiteHelper(getApplicationContext());
		//dataSrc=new BooksharePeriodicalDataSource(getApplicationContext());
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uri = intent.getStringExtra("ID_SEARCH_URI");
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");
		//TODO: Get first name, last name, member id
		dataSource = BooksharePeriodicalDataSource.getInstance(getApplicationContext());
		dbHelper = new PeriodicalsSQLiteHelper(getApplicationContext());
		periodicalDb = dbHelper.getWritableDatabase();
		
		if(username == null || password == null){
			isFree = true;
		}
		return super.onStartCommand(intent, flags, startId);
		
	}


	public class ServiceBinder extends Binder implements IPeriodicalDownloadAPI{

		private InputStream inputStream;
		private final int DATA_FETCHED = 99;
		Vector<Bookshare_Periodical_Edition_Bean> results;
		private boolean total_pages_count_known = false;
		private int total_pages_result;
		
		@Override
		public boolean downloadPeriodical(Bookshare_Edition_Metadata_Bean bean) {
			metadata_bean = bean;
			new DownloadFilesTask().execute();
			return false;
		}

		@Override
		public Vector<Bookshare_Periodical_Edition_Bean> getUpdates(String id) {
			getListing(uri);
			
			return null;
		}

		
		
		private void getListing(final String uri) {
			results = new Vector<Bookshare_Periodical_Edition_Bean>();
			
			new Thread(){
				public void run(){
					try{
						inputStream = bws.getResponseStream(password, uri);

						// Once the response is obtained, send message to the handler
						Message msg = Message.obtain();
						msg.what = DATA_FETCHED;
						msg.setTarget(handler);
						msg.sendToTarget();
					}catch(IOException ioe){
						System.out.println(ioe);
					}
					catch(URISyntaxException use){
						System.out.println(use);
					}
				}
			}.start();		
			
		}
		
		Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg){

				// Message received that data has been fetched from the bookshare web services 
				if(msg.what == DATA_FETCHED){

					String response_HTML = bws.convertStreamToString(inputStream);

					// Cleanup the HTML formatted tags
					String response = response_HTML.replace("&apos;", "\'").replace("&quot;", "\"").replace("&amp;", "and").replace("&#xd;","").replace("&#x97;", "-");

					System.out.println(response);
					// Parse the response of search result
					parseResponse(response);
				}
			}
		};
		
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
				System.out.println(ioe);
			}
		}
		
		private class SAXHandler extends DefaultHandler{

			boolean result = false;
			boolean id = false;
			boolean title = false;
			boolean edition = false;
			boolean revision= false;
			boolean num_pages = false;

			boolean editionElementVisited = false;
			boolean revisionElementVisited = false;
			
			String editionStr;

			Bookshare_Periodical_Edition_Bean result_bean;

			public void startElement(String namespaceURI, String localName, String qName, Attributes atts){
				
				if(!total_pages_count_known)
				{
					if(qName.equalsIgnoreCase("num-pages")){
						num_pages = true;
						total_pages_count_known = false;
					}
				}

				if(qName.equalsIgnoreCase("result")){
					result = true;
					result_bean = new Bookshare_Periodical_Edition_Bean();
					editionElementVisited = false;
					revisionElementVisited = false;
			
				}
				if(qName.equalsIgnoreCase("id")){
					id = true;
				}
				if(qName.equalsIgnoreCase("title")){
					title = true;
				}
				if(qName.equalsIgnoreCase("edition")){
					edition = true;
					if(!editionElementVisited){
						editionElementVisited = true;
					}
				}
				if(qName.equalsIgnoreCase("revision")){
					revision = true;
					if(!revisionElementVisited){
						revisionElementVisited = true;
					}
				}
			}

			public void endElement(String uri, String localName, String qName){

				if(num_pages){
					if(qName.equalsIgnoreCase("num-pages")){
						num_pages = false;
					}
				}
				if(qName.equalsIgnoreCase("result")){
					result = false;
					results.add(result_bean);
					result_bean = null;
				}
				if(qName.equalsIgnoreCase("id")){
					id = false;
				}
				if(qName.equalsIgnoreCase("title")){
					title = false;
				}
				if(qName.equalsIgnoreCase("edition")){
					edition = false;
				}
				if(qName.equalsIgnoreCase("revision")){
					revision = false;
				}
				
			}

			public void characters(char[] c, int start, int length){
				
				if(num_pages){
					total_pages_result = Integer.parseInt(new String(c,start,length));
				}
				if(result){
					if(id){
						result_bean.setId(new String(c,start,length));
					}
					if(title){
						result_bean.setTitle(new String(c,start,length));
					}
					if(edition){
						result_bean.setEdition(new String(c,start,length));
					}
					if(revision){
						result_bean.setRevision(new String(c,start,length));
					}
					
				}
			}
		}
		
	}
	
	
	private class DownloadFilesTask extends AsyncTask<Void, Void, Void>{
		
		private Bookshare_Error_Bean error;

	    private boolean downloadSuccess;
		// Will be called in the UI thread
		@Override
		protected void onPreExecute(){
	        downloadedBookDir = null;
	        

		}
		
		// Will be called in a separate thread
		@Override
		protected Void doInBackground(Void... params) {			 
			final String id = metadata_bean.getContentId();
			String download_uri;
			if(isFree)
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/content/"+id+"/version/1?api_key="+developerKey;
			//TODO: Uncomment & Implement
			/*else if(isOM){
				
				download_uri = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/download/member/"+memberId+"content/"+id+"/version/1/for/"+username+"?api_key="+developerKey;
			}*/
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
	                    	//TODO: Uncomment & Implement
	                        //filename = filename + "_" + firstName + "_" + lastName;
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
				//TODO: Need to include download time/date
	            AllDbPeriodicalEntity allEntity = new AllDbPeriodicalEntity(metadata_bean.getContentId(), metadata_bean.getTitle(), metadata_bean.getEdition(), Integer.parseInt(metadata_bean.getRevision()), null, null);
	            SubscribedDbPeriodicalEntity subEntity = new SubscribedDbPeriodicalEntity(metadata_bean.getContentId(), metadata_bean.getTitle(), metadata_bean.getEdition(), Integer.parseInt(metadata_bean.getRevision()));
	            dataSource.insertEntity(periodicalDb, PeriodicalsSQLiteHelper.TABLE_ALL_PERIODICALS, allEntity);
	            if(dataSource.doesExist(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS, subEntity)){
	            	dataSource.insertEntity(periodicalDb, PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS,subEntity);
	            }
			}
			else{
				
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
}
