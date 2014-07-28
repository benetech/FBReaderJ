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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;


public class Bookshare_Periodical_Listing extends ListActivity{

	private final static int LIST_RESPONSE = 1;
	private final static int METADATA_RESPONSE = 2;

	private String URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/periodical/id/";

	//Request Response codes


	private String username;
	private String password;
	private String requestURI;
	private int requestType;
	private int responseType;
	private final int DATA_FETCHED = 99;
	private Vector<Bookshare_Periodical_Bean> vectorResults;
	private ProgressDialog pd_spinning;
	private final int START_BOOKSHARE_PERIODICAL_EDITION_ACTIVITY = 10;
	private final int BOOKSHARE_PERIODICAL_EDITION_FINISHED = 11;
	private final int BOOKSHARE_PERIODICAL_LISTING_FINISHED = 12;
	

	private final int PREVIOUS_PAGE_BOOK_ID = -1;
	private final int NEXT_PAGE_BOOK_ID = -2;
	private int current_result_page = 1;
	private boolean total_pages_count_known = false;
	private ArrayList<TreeMap<String,Object>> list = new ArrayList<TreeMap<String, Object>>();
	InputStream inputStream;
	final BookshareWebservice bws = new BookshareWebservice(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private int total_pages_result;
	private Boolean isFree=false;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private Resources resources;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//ListView lv=getListView();
		//lv.setTextFilterEnabled(true);

		resources = getApplicationContext().getResources();

		Intent intent  = getIntent();
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");

		if(username == null || password == null){
			isFree = true;
		}

		//Request URI is not needed since we're not using search terms (thushv)
		requestURI = intent.getStringExtra(Bookshare_Menu.REQUEST_URI);

		requestType = intent.getIntExtra(Bookshare_Menu.REQUEST_TYPE, Bookshare_Menu.ALL_PERIODICAL_REQUEST);
		responseType= LIST_RESPONSE;

		getListing(requestURI);
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


	/**This method fetch data returned from a certain URI
	 * @param uri - The URI to bring data from
	 */
	private void getListing(final String uri) {
		vectorResults = new Vector<Bookshare_Periodical_Bean>();

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

				//process list
				list.clear();

				// For each bean object stored in the vector, create a row in the list
				for(Bookshare_Periodical_Bean bean : vectorResults){

					TreeMap<String, Object> row_item = new TreeMap<String, Object>();

					//puts item title
					row_item.put("title", bean.getTitle());

					row_item.put("icon", R.drawable.periodicals);
					row_item.put("periodical_id", bean.getId());
                    if(isFree){
                        row_item.put("download_icon", R.drawable.black_icon);
                    }
                    else{
                        row_item.put("download_icon", R.drawable.download_icon);
                    }

					list.add(row_item);
				}

				if(current_result_page > 1 ){
					createPageChanger("Previous Page", PREVIOUS_PAGE_BOOK_ID, R.drawable.arrow_left_blue);
				}

				if(current_result_page < total_pages_result ){
					createPageChanger("Next Page", NEXT_PAGE_BOOK_ID, R.drawable.arrow_right_blue);
				}
			}


			// Instantiate the custom SimpleAdapter for populating the ListView
			// The bookId view in the layout file is used to store the id , but is not shown on screen
            final MySimpleAdapter simpleadapter = new MySimpleAdapter(
                getApplicationContext(),list,
                R.layout.bookshare_menu_item,
                new String[]{"title","authors","icon","download_icon","periodical_id"},
                new int[]{R.id.text1, R.id.text2, R.id.row_icon, R.id.bookshare_download_icon,R.id.bookId});

			//Set the adapter for this view
			setListAdapter(simpleadapter);
			
			ListView lv = getListView();

			View decorView = getWindow().getDecorView();
			if (null != decorView) {
				String resultsMessage;
				if (vectorResults.isEmpty()) {
					resultsMessage = resources.getString(R.string.search_complete_no_books);
					setResult(InternalReturnCodes.NO_BOOKS_FOUND);
					confirmAndClose("no books found", 3000);
				} else {
					resultsMessage = resources.getString(R.string.search_complete_with_books);
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
					if (null != bookId.getText().toString() ) {
						int numericId =  Integer.valueOf(bookId.getText().toString());
						if (numericId < 0) {
							pageChangeSelected(numericId);
							return;
						}
					}

					// Find the corresponding bean object for this row
					for(Bookshare_Periodical_Bean bean : vectorResults){

						// Since book ID is unique, that can serve as comparison parameter
						// Retrieve the book ID from the entry that is clicked
						if(bean.getId().equalsIgnoreCase(bookId.getText().toString())){
							String bookshare_ID = bean.getId();
							Intent intent = new Intent(getApplicationContext(),Bookshare_Periodical_Edition_Listing.class);
							String uri;
							if(isFree)
								uri = URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH + bookshare_ID + "?api_key="+developerKey;
							else
								uri = URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH + bookshare_ID +"/for/"+username+"?api_key="+developerKey;

							/*--------------------------------------------------------------------
							if((isFree && bean.getAvailableToDownload().equals("1") &&
									bean.getFreelyAvailable().equals("1")) ||
									(!isFree && bean.getAvailableToDownload().equals("1"))){
								intent.putExtra("isDownloadable", true);
							}
							else{
								intent.putExtra("isDownloadable", false);
							}
							--------------------------------------------------------------------*/
							
							intent.putExtra("ID_SEARCH_URI", uri);
							if(!isFree){
								intent.putExtra("username", username);
								intent.putExtra("password", password);
							}

							startActivityForResult(intent, START_BOOKSHARE_PERIODICAL_EDITION_ACTIVITY);
							break;
						}
					}
				}
			});
		}


		private void createPageChanger(String title, int id, int iconId) {
			TreeMap<String, Object> row_item = new TreeMap<String, Object>();
			row_item.put("title", title);
			row_item.put("authors", "");
			row_item.put("icon", iconId);
			row_item.put("periodical_id", String.valueOf(id));
			row_item.put("download_icon", iconId);
			list.add(row_item);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == START_BOOKSHARE_PERIODICAL_EDITION_ACTIVITY){
			if(resultCode == BOOKSHARE_PERIODICAL_EDITION_FINISHED){
				setResult(BOOKSHARE_PERIODICAL_LISTING_FINISHED);
				finish();
			} else if (resultCode == InternalReturnCodes.NO_BOOK_FOUND) {
                setResult(resultCode);
                finish();
            }
		}
	}


    /*
     * Display voiceable message and then close
     */
    private void confirmAndClose(String msg, int timeout) {
        final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
        dialog.popup(msg, timeout);
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
			System.out.println(ioe);
		}
	}

	// Class containing the logic for parsing the response of search results
	private class SAXHandler extends DefaultHandler{

		boolean result = false;
		boolean id = false;
		boolean title = false;
		boolean num_pages = false;

		Bookshare_Periodical_Bean result_bean;

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
				result_bean = new Bookshare_Periodical_Bean();
			}
			if(qName.equalsIgnoreCase("id")){
				id = true;
			}
			if(qName.equalsIgnoreCase("title")){
				title = true;
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
				vectorResults.add(result_bean);
				result_bean = null;
			}
			if(qName.equalsIgnoreCase("id")){
				id = false;
			}
			if(qName.equalsIgnoreCase("title")){
				title = false;
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
			}
		}
	}


	// A custom ArrayAdapter class for providing data to the ListView
	// Here we need to filter items by the text entered in the Edittext above
	// Only the Periodical which contains the string entered in the edittext must be shown to the user. 
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

            // would have preferred to set this as setContentDescription, but that didn't voice
            ((TextView) convertView.findViewById(R.id.text2))
            .setText(isFree ? "(not downloadable)" : "(downloadable)");

            ((ImageView) convertView.findViewById(R.id.row_icon))
                   .setImageResource((Integer) data.get("icon"));

            ((ImageView)convertView.findViewById(R.id.bookshare_download_icon))
                    .setImageResource((Integer) data.get("download_icon"));

			((TextView) convertView.findViewById(R.id.bookId))
			.setText((String) data.get("periodical_id"));
			
			return convertView;
		}
	}
}
