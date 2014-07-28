package org.geometerplus.android.fbreader.network.bookshare;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.accessibility.ParentCloserDialog;
import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.Analytics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * This ListActivity shows options for retrieving data from Bookshare.
 */
public class Bookshare_Menu extends ListActivity {
    
    protected final static String REQUEST_TYPE = "requestType";
    protected final static String REQUEST_URI = "requestUri";

    protected final static int TITLE_SEARCH_REQUEST = 1;
    protected final static int AUTHOR_SEARCH_REQUEST = 2;
    protected final static int ISBN_SEARCH_REQUEST = 3;
    protected final static int LATEST_REQUEST = 4;
    protected final static int POPULAR_REQUEST = 5;
    protected final static int ALL_PERIODICAL_REQUEST = 6; //(thushv)
    protected final static int PERIODICAL_EDITION_REQUEST = 7;

	ArrayList<TreeMap<String,Object>> list = new ArrayList<TreeMap<String, Object>>();
	private Dialog dialog;
	private EditText dialog_search_term;
	private TextView dialog_search_title;
	private TextView dialog_example_text;
	private Button dialog_ok;
    private String search_term = "";
	private String URI_String = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/book/";
	private String URI_Periodical_String = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/periodical/";
	private int query_type;
	private Intent intent;
	private final int START_BOOKSHARE_BOOKS_LISTING_ACTIVITY = 0;
	private final int BOOKSHARE_BOOKS_LISTING_FINISHED = 2;
	private final int BOOKSHARE_MENU_FINISHED = 1;
	
	private final int START_BOOKSHARE_PERIODICAL_LISTING_ACTIVITY = 3; //This is to start listing periodicals (thushv)
	private final int BOOKSHARE_PERIODICAL_LISTING_FINISHED=4;
	
	private String username;
	private String password;
	private boolean isFree = false; 
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
    private final Activity myActivity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bookshare_menu_main);
		
		// Fetch the login info from the caller intent
		Intent callerIntent  = getIntent();
		username = callerIntent.getStringExtra("username");
		password = callerIntent.getStringExtra("password");
		
		if(username == null || password == null){
			isFree = true;
		}
		final int[] drawables = new int[] {
            R.drawable.titles,
            R.drawable.authors,
            R.drawable.isbn,
            R.drawable.latest,
            R.drawable.isbn,
            R.drawable.periodicals,		//Icon for 'All Periodicals' (thushv)
            R.drawable.titles
		};
        
        String logInMenuItem = isFree ? getResources().getString(R.string.bks_menu_log_in) : getResources().getString(R.string.bks_menu_log_out);
		//Create a TreeMap for use in the SimpleAdapter
        String[] items = {getResources().getString(R.string.bks_menu_title_label),
                getResources().getString(R.string.bks_menu_author_label), getResources().getString(R.string.bks_menu_isbn_label),
                getResources().getString(R.string.bks_menu_latest_label), getResources().getString(R.string.bks_menu_popular_label), getResources().getString(R.string.bks_menu_periodicals_label), logInMenuItem};
		for(int i = 0; i < drawables.length; i++){
			TreeMap<String, Object> row_item = new TreeMap<String, Object>();
			row_item.put("Name", items[i]);
			row_item.put("icon", drawables[i]);
			list.add(row_item);
		}		
		// Construct a SimpleAdapter which will serve as data source for this ListView
		MySimpleAdapter simpleadapter = new MySimpleAdapter(
				this,list,
				R.layout.bookshare_menu_item,
				new String[]{"Name","icon"},
				new int[]{R.id.text1,R.id.row_icon});

		//Set the adapter for this view
		setListAdapter(simpleadapter);
		
		ListView lv = getListView();

		dialog = new Dialog(this);
		dialog.setContentView(R.layout.bookshare_dialog);
		dialog_search_term = (EditText)dialog.findViewById(R.id.bookshare_dialog_search_edit_txt);
		dialog_search_title = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_txt);
		dialog_example_text = (TextView)dialog.findViewById(R.id.bookshare_dialog_search_example);
		dialog_ok = (Button)dialog.findViewById(R.id.bookshare_dialog_btn_ok);
        Button dialog_cancel = (Button) dialog.findViewById(R.id.bookshare_dialog_btn_cancel);

        dialog_search_term.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		            doSearch();
		            return true;
		        }
		        return false;
		    }
		});
		
		dialog_ok.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
                doSearch();
            }
		});
		
		dialog_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
		
		//Listener for the ListView
		lv.setOnItemClickListener(new MenuClickListener(this));
	}

	private class MenuClickListener implements OnItemClickListener {
	    private final Activity activity;

        private MenuClickListener(final Activity activity) {
            this.activity = activity;
        }

        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            // Obtain the layout for selected row
            LinearLayout row_view  = (LinearLayout)view;

            // Obtain the text of the row
            TextView txt_name = (TextView)row_view.findViewById(R.id.text1);
	        String selected = txt_name.getText().toString();


            if(position == MenuControl.title.ordinal()){
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_BKS_SEARCH,
                    Analytics.EVENT_LABEL_SEARCH_TITLE, null);
                showTitleSearch();
            }
            else if(position == MenuControl.author.ordinal()){
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_BKS_SEARCH,
                    Analytics.EVENT_LABEL_SEARCH_AUTHOR, null);
                showAuthorSearch();
            }
            else if(position == MenuControl.isbn.ordinal()){
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_BKS_SEARCH,
                    Analytics.EVENT_LABEL_SEARCH_ISBN, null);
                showISBNSearch();
            }
            else if(position == MenuControl.latest.ordinal()){
                // Changed the behavior to search for the latest book without having to enter the date range
                //dialog.setTitle("Search for latest books");
                //dialog_search_title.setText("Enter \"from\" date in MMDDYYYY format");
                //dialog_example_text.setText("E.g. 01012001 or 10022005");
                if(isFree)
                    search_term = URI_String+"latest?api_key="+developerKey;
                else
                    search_term = URI_String+"latest/for/"+username+"?api_key="+developerKey;
                query_type = LATEST_REQUEST;
                intent = new Intent(getApplicationContext(),Bookshare_Books_Listing.class);
                intent.putExtra(REQUEST_TYPE, LATEST_REQUEST);
                intent.putExtra(REQUEST_URI, search_term);
                if(!isFree){
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                }
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_BKS_SEARCH,
                    Analytics.EVENT_LABEL_SEARCH_LATEST, null);
                startActivityForResult(intent, START_BOOKSHARE_BOOKS_LISTING_ACTIVITY);
            }

            // Option to search for popular books on Bookshare website
            else if(position == MenuControl.popular.ordinal()){
                if(isFree)
                    search_term = URI_String+"popular?api_key="+developerKey;
                else
                    search_term = URI_String+"popular/for/"+username+"?api_key="+developerKey;
                query_type = POPULAR_REQUEST;
                intent = new Intent(getApplicationContext(),Bookshare_Books_Listing.class);
                intent.putExtra(REQUEST_TYPE, POPULAR_REQUEST);
                intent.putExtra(REQUEST_URI, search_term);
                if(!isFree){
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                }
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_BKS_SEARCH,
                    Analytics.EVENT_LABEL_SEARCH_POPULAR, null);
                startActivityForResult(intent, START_BOOKSHARE_BOOKS_LISTING_ACTIVITY);
            }
            //This is when user clickes on 'All Periodicals' (thushv)
            else if(position == MenuControl.periodicals.ordinal()){
                if(!isFree)
                    search_term= URI_Periodical_String+"list/for/"+username+"?api_key="+developerKey;
                else
                    search_term= URI_Periodical_String+"list?api_key="+developerKey;


                query_type= ALL_PERIODICAL_REQUEST;
                intent = new Intent(getApplicationContext(),Bookshare_Periodical_Listing.class);
                //set all the extras accordingly
                intent.putExtra(REQUEST_TYPE, ALL_PERIODICAL_REQUEST);
                intent.putExtra(REQUEST_URI, search_term);
                if(!isFree){
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                }
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_PERIODICALS_BROWSE,
                    Analytics.EVENT_LABEL_SEARCH_PERIODICALS, null);
                startActivityForResult(intent, START_BOOKSHARE_PERIODICAL_LISTING_ACTIVITY);


            }
            else if(position == MenuControl.logout.ordinal()){
	            if (txt_name.getText().toString().equals(getString(R.string.bks_menu_log_in))) {
		            Intent intent = new Intent(getApplicationContext(), Bookshare_Webservice_Login.class);
                    startActivity(intent);
                    EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_LOGIN,
                        Analytics.EVENT_ACTION_LOGIN, null);
                    finish();
	            } else {
		            final Dialog confirmDialog = new Dialog(myActivity);
                    confirmDialog.setTitle(getResources().getString(R.string.accessible_alert_title));
                    confirmDialog.setContentView(R.layout.accessible_alert_dialog);
                    TextView confirmation = (TextView)confirmDialog.findViewById(R.id.bookshare_confirmation_message);
                    confirmation.setText(getResources().getString(R.string.logout_dialog_message));
                    Button yesButton = (Button)confirmDialog.findViewById(R.id.bookshare_dialog_btn_yes);
                    Button noButton = (Button) confirmDialog.findViewById(R.id.bookshare_dialog_btn_no);

                    yesButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v){
                            // Upon logout clear the stored login credentials
                            SharedPreferences login = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = login.edit();
                            editor.putString("username", "");
                            editor.putString("password", "");
                            editor.putBoolean("isOM", false);
                            editor.commit();
                            confirmAndClose(getResources().getString(R.string.bks_menu_log_out_confirmation), 2000);
                        }
                    });

                    noButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmDialog.dismiss();
                        }
                    });
                    EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_SEARCH, Analytics.EVENT_ACTION_LOGOUT,
                        Analytics.EVENT_ACTION_LOGOUT, null);
                    confirmDialog.show();
	            }
            }
        }
	}

	private AlertDialog createLoginDialogBox(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		//builder.setIcon(android.R.drawable.dialog_question);
		builder.setTitle("Login Required");
		builder.setMessage(R.string.warning_no_general_downloads_for_periodicals);
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
			//Take the user to the login page with 'continue without logging in' disabled
		  @Override
		  public void onClick(DialogInterface dialog, int which) {
			  Intent intent = new Intent(getApplicationContext(),Bookshare_Webservice_Login.class);
				intent.putExtra("disable_no_login", true);
		    dialog.dismiss();
		    
		    startActivity(intent);
		  }
		});
		//Just close the dialog box and activity
		builder.setNegativeButton("Continue Anyway", new DialogInterface.OnClickListener() {
		  @Override
		  public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		    startActivityForResult(intent, START_BOOKSHARE_PERIODICAL_LISTING_ACTIVITY);
		  }
		});
		AlertDialog alert = builder.create();
		return alert;
		
	}
	
    private void showAuthorSearch() {
        intent = new Intent(getApplicationContext(),Bookshare_Books_Listing.class);
        intent.putExtra(REQUEST_TYPE, AUTHOR_SEARCH_REQUEST);
        showSearch(R.string.search_dialog_title_author, R.string.search_dialog_label_author, 
            R.string.search_dialog_example_author, R.string.search_dialog_description_author, AUTHOR_SEARCH_REQUEST, 
            InputType.TYPE_CLASS_TEXT);
    }

    private void showTitleSearch() {
        intent = new Intent(getApplicationContext(),Bookshare_Books_Listing.class);
        intent.putExtra(REQUEST_TYPE, TITLE_SEARCH_REQUEST);
        showSearch(R.string.search_dialog_title_title, R.string.search_dialog_label_title, 
            R.string.search_dialog_example_title, R.string.search_dialog_description_title, TITLE_SEARCH_REQUEST, 
            InputType.TYPE_CLASS_TEXT);
    }

    private void showISBNSearch() {
        intent = new Intent(getApplicationContext(),Bookshare_Book_Details.class);
        intent.putExtra(REQUEST_TYPE, ISBN_SEARCH_REQUEST);
        showSearch(R.string.search_dialog_title_isbn, R.string.search_dialog_label_isbn, 
            R.string.search_dialog_example_isbn, R.string.search_dialog_description_isbn, ISBN_SEARCH_REQUEST, 
            InputType.TYPE_CLASS_NUMBER);
    }
    
    private void showSearch(int titleId, int labelId, int exampleId, int contentDescriptionId, int queryType, 
            int inputType) {
        // Clear the EditText box of any previous text
        dialog_search_term.setText("");

        dialog_search_term.setInputType(inputType);
        dialog.setTitle(getResources().getString(titleId));
        dialog_search_title.setText(getResources().getString(labelId));
        dialog_example_text.setText(getResources().getString(exampleId));
        dialog_search_term.setContentDescription(getResources().getString(contentDescriptionId));
        query_type = queryType;
        dialog_search_term.requestFocus();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        if (!isNetworkAvailable()) {
            confirmAndClose(getResources().getString(R.string.bks_menu_no_internet), 3500);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    private void doSearch() {
        // Hide the virtual keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(dialog_search_term.getWindowToken(), 0);

        // Remove the leading and trailing spaces
        String search_term = dialog_search_term.getText().toString().trim();
        try {
            search_term = URLEncoder.encode(search_term, "utf-8");
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }

        if(search_term.equals("")){
            final VoiceableDialog finishedDialog = new VoiceableDialog(dialog_ok.getContext());
            String message = getResources().getString(R.string.empty_search_term_error);
            finishedDialog.popup(message, 5000);
            return;
        }

        boolean isMetadataSearch = false;

        if(query_type == TITLE_SEARCH_REQUEST){
            if(isFree)
                search_term = URI_String+"search/title/"+search_term+"?api_key="+developerKey;
            else
                search_term = URI_String+"search/title/"+search_term+"/for/"+username+"?api_key="+developerKey;
        }
        else if(query_type == AUTHOR_SEARCH_REQUEST){
            if(isFree)
                search_term = URI_String+"search/author/"+search_term+"?api_key="+developerKey;
            else
                search_term = URI_String+"search/author/"+search_term+"/for/"+username+"?api_key="+developerKey;
        }
        else if(query_type == ISBN_SEARCH_REQUEST){
            if(isFree)
                search_term = URI_String+"isbn/"+search_term+"?api_key="+developerKey;
            else
                search_term = URI_String+"isbn/"+search_term+"/for/"+username+"?api_key="+developerKey;

            isMetadataSearch = true;
        }

        if(isMetadataSearch){
            intent.putExtra("ID_SEARCH_URI", search_term);
        }
        else{
            intent.putExtra(REQUEST_URI, search_term);
        }
        dialog.dismiss();
        if(!isFree){
            intent.putExtra("username", username);
            intent.putExtra("password", password);
        }

        startActivityForResult(intent, START_BOOKSHARE_BOOKS_LISTING_ACTIVITY);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == START_BOOKSHARE_BOOKS_LISTING_ACTIVITY){
			if(resultCode == BOOKSHARE_BOOKS_LISTING_FINISHED){
				setResult(BOOKSHARE_MENU_FINISHED);
				finish();              
			} else if (resultCode == InternalReturnCodes.NO_BOOK_FOUND) {
                // can only get here from failed ISBN search (other search return no books found)
                // take back to ISBN search since we assume user mistake
                showISBNSearch();
            } else if (resultCode == InternalReturnCodes.NO_BOOKS_FOUND) {
                // go back to either title or author search
                if (query_type == TITLE_SEARCH_REQUEST) {
                    showTitleSearch();    
                } else if (query_type == AUTHOR_SEARCH_REQUEST) {
                    showAuthorSearch();
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
		
        /**
         * Retrieves view for the item in the adapter, at the
         * specified position and populates it with data.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.bookshare_menu_item, null);
            }
            final TreeMap<String, Object> data = (TreeMap<String, Object>) getItem(position);
            ((TextView) convertView.findViewById(R.id.text1)).setText((String) data.get("Name"));
            ((ImageView) convertView.findViewById(R.id.row_icon)).setImageResource((Integer) data.get("icon"));
            return convertView;
        }
	}

    /*
     * Display logged out confirmation and close the bookshare menu screen
     */
    private void confirmAndClose(String msg, int timeout) {
        final ParentCloserDialog dialog = new ParentCloserDialog(this, this);
        dialog.popup(msg, timeout);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

	private enum MenuControl {
		title, author, isbn, latest, popular, periodicals, logout
	}
}
