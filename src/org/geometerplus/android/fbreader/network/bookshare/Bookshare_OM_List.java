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

import org.bookshare.net.BookshareWebservice;
import org.benetech.android.R;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import ca.idi.tecla.lib.InputAccess;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 
 * This Class lists the Organizational Members for a given sponsor account
 *
 */
public class Bookshare_OM_List extends ListActivity{

	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private String username;
	private String password;
	private InputStream inputStream;
	private final int DATA_FETCHED = 99;
	private BookshareWebservice bws = new BookshareWebservice(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	private ProgressDialog pd_spinning;
	private List<TreeMap<String,Object>> list = new ArrayList<TreeMap<String, Object>>();
	private Vector<Bookshare_OM_Member_Bean> vectorResults;
	private String omDownloadPassword;
	private int START_BOOKSHARE_OM_DOWNLOAD_PASSWORD = 1;
	private int BOOKSHARE_OM_SELECTED = 2;
	private int downloadsRemaining = 0;
	private InputAccess inputAccess = new InputAccess(this, true);

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		inputAccess.onCreate();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent  = getIntent();
		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");

		// Show a Progress Dialog before the book opens
		pd_spinning = ProgressDialog.show(this, null, "Fetching member list. Please wait.", Boolean.TRUE);
		vectorResults = new Vector<Bookshare_OM_Member_Bean>();
		
		// Carry out the network operation in a non-UI thread
		new Thread(){
			public void run(){
				try{
					String uri = "https://" + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/user/members/list/for/";
					uri += username+"/?api_key="+developerKey;
					System.out.println(uri);
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

//	@Override
/*	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == START_BOOKSHARE_OM_DOWNLOAD_PASSWORD){
			omDownloadPassword = data.getStringExtra("omDownloadPassword");
		}
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add("Cancel");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getTitle().equals("Cancel")){
			finish();
		}
		return true;
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
				String response = response_HTML.replace("&apos;", "'").replace("&quot;", "\"").replace("&amp;", "&").replace("&#xd;","").replace("&#x97;", "-");

				// Parse the response of search result
				parseResponse(response);

				list.clear();

				// For each bean object stored in the vector, create a row in the list
				for(Bookshare_OM_Member_Bean bean : vectorResults){
					TreeMap<String, Object> row_item = new TreeMap<String, Object>();
					System.out.println("Name = "+bean.getFirstName()+" "+bean.getlastName());
					row_item.put("name", bean.getFirstName()+" "+bean.getlastName());
					row_item.put("icon", R.drawable.authors);
					list.add(row_item);
				}
			}

			// Instantiate the custom SimpleAdapter for populating the ListView
			MySimpleAdapter simpleadapter = new MySimpleAdapter(
					getApplicationContext(),list,
					R.layout.bookshare_menu_item,
					new String[]{"name","icon"},
					new int[]{R.id.text1,R.id.row_icon});


			//Set the adapter for this view
			setListAdapter(simpleadapter);

			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new OnItemClickListener(){

				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					// Obtain the layout for selected row
					LinearLayout row_view  = (LinearLayout)view;

					// Obtain the text of the row containing title
					TextView txt_name = (TextView)row_view.findViewById(R.id.text1);


					// Find the corresponding bean object for this row
					for(final Bookshare_OM_Member_Bean bean : vectorResults){

						// Find the matching bean entry from the vector and get its book ID
						if((bean.getFirstName() +" "+bean.getlastName()).equalsIgnoreCase(txt_name.getText().toString())){
							buildDialog(bean);
						}
					}
				}
			});
		}
	};
	
	// Confirmation dialog to select the OM for download
	private void buildDialog(final Bookshare_OM_Member_Bean bean){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					Intent intent = new Intent();
					intent.putExtra(Bookshare_OM_Member_Bean.MEMBER_ID, bean.getMemberId());
					intent.putExtra(Bookshare_OM_Member_Bean.FIRST_NAME, bean.getFirstName());
					intent.putExtra(Bookshare_OM_Member_Bean.LAST_NAME, bean.getlastName());
					setResult(BOOKSHARE_OM_SELECTED, intent);
					dialog.dismiss();
					finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Download for this member - "+bean.getFirstName() +" "+bean.getlastName()).setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();

	}

	// Used for keeping the screen from rotating
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

	// Class containing the logic for parsing OM member listing for a given OM sponsor
	private class SAXHandler extends DefaultHandler{

		boolean downloadsRemainingFlag = false;
		boolean member = false;
		boolean memberId = false;
		boolean firstName = false;
		boolean lastName = false;

		Bookshare_OM_Member_Bean member_bean;

		public void startElement(String namespaceURI, String localName, String qName, Attributes atts){
			if(qName.equalsIgnoreCase("downloads-remaining")){
				downloadsRemainingFlag = true;
			}
			if(qName.equalsIgnoreCase("member")){
				member = true;
				member_bean = new Bookshare_OM_Member_Bean();
			}
			if(qName.equalsIgnoreCase("member-id")){
				memberId = true;
			}
			if(qName.equalsIgnoreCase("first-name")){
				firstName = true;
			}
			if(qName.equalsIgnoreCase("last-name")){
				lastName = true;
			}
		}

		public void endElement(String uri, String localName, String qName){

			if(qName.equalsIgnoreCase("downloads-remaining")){
				downloadsRemainingFlag = false;
			}

			if(qName.equalsIgnoreCase("member")){
				member = false;
				vectorResults.add(member_bean);
				member_bean = null;
			}

			if(qName.equalsIgnoreCase("member-id")){
				memberId = false;
			}
			if(qName.equalsIgnoreCase("first-name")){
				firstName = false;
			}
			if(qName.equalsIgnoreCase("last-name")){
				lastName = false;
			}
		}

		public void characters(char[] c, int start, int length){

			if(downloadsRemainingFlag){
				downloadsRemaining = Integer.parseInt(new String(c,start,length));
			}
			if(member){
				if(memberId){
					System.out.println("member ID = "+new String(c,start,length));
					member_bean.setMemberId(new String(c,start,length));
				}
				if(firstName){
					System.out.println("firstName = "+new String(c,start,length));
					member_bean.setFirstName(new String(c,start,length));
				}
				if(lastName){
					System.out.println("lastName = "+new String(c,start,length));
					member_bean.setLastName(new String(c,start,length));
				}
			}
		}
	}

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
			.setText((String) data.get("name"));

			((ImageView) convertView.findViewById(R.id.row_icon))
			.setImageResource(((Integer)data.get("icon")).intValue());

			return convertView;
		}
	}
}
