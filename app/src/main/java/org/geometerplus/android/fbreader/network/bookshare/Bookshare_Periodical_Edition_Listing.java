package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.accessibility.ParentCloserDialog;
import org.benetech.android.R;
import org.bookshare.net.BookshareWebservice;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

public class Bookshare_Periodical_Edition_Listing extends ListActivity{

	protected final static int EDITION_LIST_RESPONSE = 1;
	protected final static int EDITION_METADATA_RESPONSE = 2;
	
	private String username;
	private String password;
	private String requestURI;
	private int requestType;
	private int responseType;
	
	protected final static String URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/periodical/id/";
	
	private final int DATA_FETCHED = 99;
	private ArrayList<Bookshare_Periodical_Edition_Bean> arrayResults;
	private ProgressDialog pd_spinning;
	
	private final int START_BOOKSHARE_EDITION_DETAILS_ACTIVITY = 13;
	private final int BOOKSHARE_EDITION_DETAILS_FINISHED = 14;
	private final int BOOKSHARE_EDITION_LISTING_FINISHED = 11;

	private final int PREVIOUS_PAGE_BOOK_ID = -1;
	private final int NEXT_PAGE_BOOK_ID = -2;
	private int current_result_page = 1;
	PeriodicalEditionSAXHandler saxHandler;
	
	private ArrayList<TreeMap<String,Object>> list = new ArrayList<TreeMap<String, Object>>();
	InputStream inputStream;
	final BookshareWebservice bws = new BookshareWebservice(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private int total_pages_result;
	private Boolean isFree=false;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private Resources resources;

	private String uri;	//string which contains the uri to fetch periodical details
	private String bookshare_ID;
	private String bookshare_edition;
	private String bookshare_revision;
	private String bookshare_title;
	private boolean isOM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		resources = getApplicationContext().getResources();

		Intent intent  = getIntent();
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");

		if(username == null || password == null){
			isFree = true;
		}

		// Obtain the application wide SharedPreferences object and store the login information
		//Get information about user. (i.e. Whether he's an organizational member)
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		isOM = login_preference.getBoolean("isOM", false);
		
		//Request URI is not needed since we're not using search terms (thushv)
		requestURI = intent.getStringExtra("ID_SEARCH_URI");

		requestType = intent.getIntExtra(Bookshare_Menu.REQUEST_TYPE, Bookshare_Menu.PERIODICAL_EDITION_REQUEST);
		responseType= EDITION_LIST_RESPONSE;

		getListing(requestURI);
		
		getListView().setOnCreateContextMenuListener(this);

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

	/*
     * Display voiceable message and then close
    */
    private void confirmAndClose(String msg, int timeout) {
        final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
        dialog.popup(msg, timeout);
    }
    
    
	private void getListing(final String uri) {

		//Show progress bar
		pd_spinning = ProgressDialog.show(this, null, resources.getString(R.string.fetching_periodicals), Boolean.TRUE);

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
	
	/**When user go to next page, this method clears the list and create the url
	 *for the next page of results 
	 * @param selectorId 
	 */
	public void pageChangeSelected(int selectorId){
		if(selectorId == NEXT_PAGE_BOOK_ID){
			current_result_page++;
		}
		else if(selectorId == PREVIOUS_PAGE_BOOK_ID){
			current_result_page--;
		}
		list.clear();

		StringBuilder strBuilder = new StringBuilder(requestURI);
		int index;

		if((index = strBuilder.indexOf("?api_key=")) != -1){
			strBuilder.delete(index, strBuilder.length());
			strBuilder.append("/page/"+current_result_page+"?api_key="+developerKey);
		}
		getListing(strBuilder.toString());
		
	}

	// Handler for dealing with the stream obtained as a result of search 
		Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg){

				// Message received that data has been fetched from the bookshare web services 
				if(msg.what == DATA_FETCHED){

					setContentView(R.layout.bookshare_menu_main);

					// Dismiss the progress dialog
					pd_spinning.cancel();

					String response_HTML = bws.convertStreamToString(inputStream);

					// Cleanup the HTML formatted tags
					String response = response_HTML.replace("&apos;", "\'").replace("&quot;", "\"").replace("&amp;", "and").replace("&#xd;","").replace("&#x97;", "-");

					System.out.println(response);
					// Parse the response of search result
					parseResponse(response);
					arrayResults = saxHandler.getResults();
					total_pages_result = saxHandler.getTotal_pages_result();

					if(responseType==EDITION_METADATA_RESPONSE){
						//do nothing
					}
					if(responseType == EDITION_LIST_RESPONSE){
					//process list
					list.clear();

					// For each bean object stored in the vector, create a row in the list
					for(Bookshare_Periodical_Edition_Bean bean : arrayResults){

						TreeMap<String, Object> row_item = new TreeMap<String, Object>();

						//puts item title
						row_item.put("title", bean.getTitle());
						row_item.put("edition", bean.getEdition());
						row_item.put("revision", bean.getRevision());
						row_item.put("icon", R.drawable.periodicals);
						row_item.put("book_id", bean.getId());

						list.add(row_item);
					}

					if(current_result_page > 1 ){
						createPageChanger("Previous Page", PREVIOUS_PAGE_BOOK_ID, R.drawable.arrow_left_blue);
					}

					if(current_result_page < total_pages_result ){
						createPageChanger("Next Page", NEXT_PAGE_BOOK_ID, R.drawable.arrow_right_blue);
					}
					}
				}


				// Instantiate the custom SimpleAdapter for populating the ListView
				// The bookId view in the layout file is used to store the id , but is not shown on screen
				MySimpleAdapter simpleadapter = new MySimpleAdapter(
						getApplicationContext(),list,
						R.layout.bookshare_menu_item,
						new String[]{"title","edition","download_icon","book_id"},
						new int[]{R.id.text1,R.id.text2, R.id.bookshare_download_icon,R.id.bookId});

				
				
				//Set the adapter for this view
				setListAdapter(simpleadapter);
				
				ListView lv = getListView();
				lv.setLongClickable(true);
				registerForContextMenu(lv);
				final AlertDialog alert=createSignupDialogBox();
				
				View decorView = getWindow().getDecorView();
				if (null != decorView) {
					String resultsMessage;
					if (arrayResults.isEmpty()) {
						resultsMessage = resources.getString(R.string.search_complete_no_books);
						setResult(InternalReturnCodes.NO_BOOKS_FOUND);
						confirmAndClose("no books found", 3000);
					} else {
						resultsMessage = resources.getString(R.string.search_complete_with_periodicals);
					}
					decorView.setContentDescription(resultsMessage);
				}

				//When list item is clicked item should do a lookup and return editions/revisions
				//they have on the particular magazine (thush)
				lv.setOnItemClickListener(new OnItemClickListener(){

					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

						// Obtain the layout for selected row
						LinearLayout row_view  = (LinearLayout)view;

						//Obtain the book ID
						TextView bookId = (TextView)row_view.findViewById(R.id.bookId);
						TextView periodicalEdition = (TextView)row_view.findViewById(R.id.text2);
						
						if (null != bookId.getText().toString() ) {
							int numericId =  Integer.valueOf(bookId.getText().toString());
							if (numericId < 0) {
								pageChangeSelected(numericId);
								return;
							}
						}

						// Find the corresponding bean object for this row
						for(Bookshare_Periodical_Edition_Bean bean : arrayResults){

							//TODO: Fix bug
							// Since book ID is unique, that can serve as comparison parameter
							// Retrieve the book ID from the entry that is clicked
							
							StringBuilder editionBuilder = new StringBuilder(bean.getEdition());
							editionBuilder.insert(2, "/");
							editionBuilder.insert(5, "/");
							editionBuilder.insert(0, "Edition: ");

							if(periodicalEdition.getText().toString().trim().startsWith(editionBuilder.toString())){
								bookshare_ID = bean.getId();
								bookshare_edition=bean.getEdition();
								bookshare_revision=bean.getRevision();
								bookshare_title=bean.getTitle();
								
								
								if(isFree)
									uri = URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH + bookshare_ID + "/edition/" + bookshare_edition + "/revision/" + bookshare_revision + "?api_key="+developerKey;
								else
									uri = URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH + bookshare_ID + "/edition/" + bookshare_edition + "/revision/" + bookshare_revision + "/for/"+username+"?api_key="+developerKey;

								if(isOM){
									uri = URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH + bookshare_ID + "/edition/" + bookshare_edition + "/revision/" + bookshare_revision +"?api_key="+developerKey;
								}
								
						
								//intent.putExtra("ID_SEARCH_URI", uri);
									Intent intent = new Intent(getApplicationContext(),Bookshare_Periodical_Edition_Details.class);
									intent.putExtra("username", username);
									intent.putExtra("password", password);
									intent.putExtra("ID_SEARCH_URI", uri);
									intent.putExtra("PERIODICAL_TITLE", bookshare_title);
									intent.putExtra("PERIODICAL_ID", bookshare_ID);
									startActivityForResult(intent, START_BOOKSHARE_EDITION_DETAILS_ACTIVITY);
									
									//It might be not to give user suprises by having a different view for periodicals
									//So thought of making it the same
									//getListView().showContextMenuForChild(view);

								
								//startActivityForResult(intent, START_BOOKSHARE_EDITION_DETAILS_ACTIVITY);
								break;
							}
						}
						
						//Show a context menu for a single click on list item
						
					}
					
					
				});
				
				lv.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						return false;
					}
				});
			}


			

			
			private void createPageChanger(String title, int id, int iconId) {
				TreeMap<String, Object> row_item = new TreeMap<String, Object>();
				row_item.put("title", title);
				row_item.put("edition", "");
			
				row_item.put("book_id", String.valueOf(id));
				row_item.put("download_icon", iconId);
				list.add(row_item);
			}
		};
		
		//This box will say that user is have not yet signed in. In order to download
		//periodicals user must log in.
		private AlertDialog createSignupDialogBox(){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			//builder.setIcon(android.R.drawable.dialog_question);
			builder.setTitle("Login Required");
			builder.setMessage(R.string.login_to_continue);
			builder.setInverseBackgroundForced(true);
			builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
				//Take the user to the login page with 'continue without logging in' disabled
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
				  Intent intent = new Intent(getApplicationContext(),Bookshare_Webservice_Login.class);
					intent.putExtra("disable_no_login", true);
					intent.putExtra("periodical_edition_id", bookshare_ID);
					intent.putExtra("periodical_edition", bookshare_edition);
					intent.putExtra("periodical_revision", bookshare_revision);
					intent.putExtra("PERIODICAL_TITLE", bookshare_title);
					//we need to send this to login page. So when it sees the request type it can directly send
					//user to the edition details page
					intent.putExtra(Bookshare_Menu.REQUEST_TYPE, EDITION_METADATA_RESPONSE);
			    dialog.dismiss();
			    
			    startActivityForResult(intent, START_BOOKSHARE_EDITION_DETAILS_ACTIVITY);
			  }
			});
			//Just close the dialog box and activity
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			    //finish();
			  }
			});
			AlertDialog alert = builder.create();
			return alert;
			
		}
		
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data){
			if(requestCode == START_BOOKSHARE_EDITION_DETAILS_ACTIVITY){
				if(resultCode == BOOKSHARE_EDITION_DETAILS_FINISHED){
					setResult(BOOKSHARE_EDITION_LISTING_FINISHED);
					finish();
				} else if (resultCode == InternalReturnCodes.NO_BOOK_FOUND) {
	                setResult(resultCode);
	                finish();
	            }
			}
		}

		// Used for keeping the the screen from rotating
		@Override
		public void onConfigurationChanged(Configuration newConfig){
			super.onConfigurationChanged(newConfig);
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
				saxHandler = new PeriodicalEditionSAXHandler();
				parser.setContentHandler(saxHandler);
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


		//Here we change the textview2 view to have both edition and revision strings concatenated
		// A custom SimpleAdapter class for providing data to the ListView
		private class MySimpleAdapter extends SimpleAdapter{
			public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data,
					int resource, String[] from, int[] to) {
				super(context, data, resource, from, to);
			}

			/*
			 * Retrieves view for the item in the adapter, at the
			 * specified position and populates it with data.
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = getLayoutInflater().inflate(R.layout.bookshare_menu_item, null);

				}

				TreeMap<String, Object> data = (TreeMap<String, Object>) getItem(position);

				((TextView) convertView.findViewById(R.id.text1))
				.setText((String) data.get("title"));

				//Edition and Revision data must be combined and shown in one textview
				//Therefore we need to process 2 strings (edition and revision) and
				//combine 2 strings correctly
	            StringBuilder editionRevisionBuilder = new StringBuilder("");
	            if ( (data.get("edition") != null) &&   ((String)data.get("edition")).length() > 0) {
	                editionRevisionBuilder = new StringBuilder("Edition: ");
	                String edition=(String)data.get("edition");
	                
	                StringBuilder editionBuilder=new StringBuilder(edition);
	                if(edition!=null){	                	
	                	editionBuilder.insert(2, "/");
	                	editionBuilder.insert(5, "/");
	                }
	                
	                editionRevisionBuilder.append(editionBuilder.toString());
	                editionRevisionBuilder.append(" ");
	                editionRevisionBuilder.append(isFree ? "(not downloadable)" : "(downloadable)");

	            }

	            //set the TextViews with appropriate texts
	            String editionR=editionRevisionBuilder.toString();
	            // would have preferred to set this as setContentDescription, but that didn't voice
				((TextView) convertView.findViewById(R.id.text2))
				.setText(editionRevisionBuilder.toString());

				((TextView) convertView.findViewById(R.id.bookId))
				.setText((String) data.get("book_id"));
				
				return convertView;
			}
		}
}
