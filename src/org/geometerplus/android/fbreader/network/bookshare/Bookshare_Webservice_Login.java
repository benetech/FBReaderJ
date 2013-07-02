package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.accessibility.VoiceableDialog;
import org.bookshare.net.BookshareWebservice;
import org.benetech.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.analytics.tracking.android.EasyTracker;


/**
 * Login page for the webservice account of Bookshare.
 * Webservice login is mandatory for accessing generally
 * available services of bookshare API. This includes
 * searching and downloading public domain books.
 */
public class Bookshare_Webservice_Login extends Activity{

    public static final String USER = "username";
    public static final String PASSWORD = "password";

    public static final String BOOKSHARE_API_PROTOCOL = "https://";
    public static final String BOOKSHARE_API_HOST = "api.bookshare.org";
	private String BOOKSHARE_URL = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + BOOKSHARE_API_HOST + "/book/search/title/potter";
	private String FORGOT_PW_URL = "http://www.bookshare.org/forgotPassword";
	private String SIGNUP_URL = "https://www.bookshare.org/signUpType";

	private Button btn_login;
	private Button btn_continue_without_login;
	private TextView text_username;
	private TextView text_password;
	private EditText editText_username;
	private EditText editText_password;
	private Intent intent;
	private final static int LOGIN_SUCCESSFUL = 1;
	private final static int STATUS_NOT_SET = 0;
	private final static int LOGIN_FAILED = -1;
	private final static int NETWORK_ERROR = -2;

	private String username;
	private String password;
	private int status;
	private ProgressDialog pd_spinning;
	private boolean isFree= false;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private boolean isOM = false;
	private String response;
	//private InputAccess inputAccess = new InputAccess(this, true);

	private int requestType;
	private String uri;
	private String periodicalEdition;
	private String periodicalRevision;
	private String periodicalId;
	private String periodicalTitle;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        //todo:
		//inputAccess.onCreate();
		
		// Obtain the SharedPreferences object shared across the application
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		username = login_preference.getString(USER, "");
		password = login_preference.getString(PASSWORD, "");
		
		// If login credentials are already stored, navigate to the next Activity
		if(!username.equals("") && !password.equals("")){
			intent = new Intent(getApplicationContext(), Bookshare_Menu.class);
			intent.putExtra(USER, username);
			intent.putExtra(PASSWORD, password);
			startActivity(intent);
			finish();
		}
		
		
		// Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Set to full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.bookshare_webservice_login);

		btn_login = (Button)findViewById(R.id.btn_bookshare_bookshare_webservice_login);
		btn_continue_without_login = (Button)findViewById(R.id.btn_bookshare_bookshare_continue_without_login);
		
		//Disable 'continue without logging in' if required
		Intent intent=getIntent();
		Boolean disableContinue=intent.getBooleanExtra("disable_no_login",false);
		requestType=intent.getIntExtra(Bookshare_Menu.REQUEST_TYPE, -1);
		//If there is any extra then the continue without login button should be disabled
		if(disableContinue){
			btn_continue_without_login.setEnabled(false);
		}
		// These are the details passed by Periodical_Edition_Listing activity
		// This occurs if the user choose to login when he clicks on a certain periodical
		if(requestType!=-1){
			periodicalId=intent.getStringExtra("periodical_edition_id");
			periodicalEdition=intent.getStringExtra("periodical_edition");
			periodicalRevision=intent.getStringExtra("periodical_revision");
			periodicalTitle=intent.getStringExtra("PERIODICAL_TITLE");
		}
		
		text_username = (TextView)findViewById(R.id.bookshare_login_username_text);
		text_password = (TextView)findViewById(R.id.bookshare_login_password_text);
		editText_username = (EditText)findViewById(R.id.bookshare_login_username_edit_text);
		editText_password = (EditText)findViewById(R.id.bookshare_login_password_edit_text);

		editText_username.setOnEditorActionListener(new OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
		            editText_password.requestFocus();
		            return true;
		        }
		        return false;
		    }
		});

		editText_password.setOnEditorActionListener(new OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEND) {
		            loginAction();
		            return true;
		        }
		        return false;
		    }
		});
		
		
		// Listener for edit text box to handle the enter key
		editText_password.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
		          // Perform action on key press
		        	loginAction();
		          return true;
		        }
		        return false;
		    }
		});
		// Listener for login button
		btn_login.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				loginAction();
			}
		});
		
		btn_continue_without_login.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				getFreeContent();
			}
		});

        findViewById(R.id.btn_login_forgot_pwd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FORGOT_PW_URL));
                startActivity(myIntent);
            }
        });

        findViewById(R.id.btn_login_sign_up).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SIGNUP_URL));
                startActivity(myIntent);
            }
        });
	}
	private void loginAction(){

		// Hide the virtual keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText_username.getWindowToken(), 0);
        final VoiceableDialog finishedDialog = new VoiceableDialog(btn_login.getContext());

		username = editText_username.getText().toString().trim();
		password = editText_password.getText().toString().trim();
		
		// Test for conditions where the input might be blank
		if(username.equals("") && password.equals("")) {
            String message = getResources().getString(R.string.login_error_non_blank_password_and_username);
            finishedDialog.popup(message, 5000);
            editText_username.requestFocus();
		}
		else if(username.equals("") && !password.equals("")) {
            String message = getResources().getString(R.string.login_error_non_blank_username);
            finishedDialog.popup(message, 5000);
            editText_username.requestFocus();
		}
		else if(!username.equals("") && password.equals("")) {
            String message = getResources().getString(R.string.login_error_non_blank_password) ;
            finishedDialog.popup(message, 5000);
            editText_password.requestFocus();
		}
		else{
			startProgressDialog();
			// Start a new AsyncTask for background processing
			new AuthenticationTask().execute();
		}
	}
    
	private void getFreeContent(){
		isFree = true;
		isOM = false;
		username = null;
		password = null;

        pd_spinning = ProgressDialog.show(this, null, "Connecting to Bookshare", Boolean.TRUE);

        // Start a new AsyncTask for background processing
		new AuthenticationTask().execute();
	}
	
	@Override
	/*
	 * @Non Javadoc
	 * Menu items that will shown when the Menu button on the phone 
	 * is clicked.
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE,1,Menu.NONE,"Forgot Password");
		menu.add(Menu.NONE,2,Menu.NONE,"Signup");
		return true;
	}
	
	@Override
	/*
	 * @Non Javadoc
	 * Callback method that will be called when a menu item is clicked.
	 */
	public boolean onOptionsItemSelected(MenuItem menuitem){
		
		if(menuitem.getTitle().equals("Forgot Password")){
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FORGOT_PW_URL));
			startActivity(myIntent);
		}
		else if(menuitem.getTitle().equals("Signup")){
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SIGNUP_URL));
			startActivity(myIntent);
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * An AsyncTask class which carries out the authentication 
	 * in the background.
	 */
	private class AuthenticationTask extends AsyncTask<Void, Void, Void>{

		/*
		 * (non-Javadoc)
		 * This method is called in the UI thread just before the
		 * doInBackground is called. Disable the UI elements while
		 * the authentication is being done.
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute(){
			
			btn_login.setEnabled(false);

			if(isFree){
				editText_username.setText("");
				editText_password.setText("");
				text_username.setText("");
				text_password.setText("");
			}
			editText_username.setEnabled(false);
			editText_password.setEnabled(false);
		}


		/*
		 * (non-Javadoc)
		 * The entire body of this method is executed in a 
		 * newly spawned thread. Carry out the actual authentication task here
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {

			String result_HTML = "";
			boolean inTry = false;
			status = STATUS_NOT_SET;

			try{
				inTry = true;	
				// Get a BookshareWebservice instance for accessing the utility methods

				final BookshareWebservice bws = new BookshareWebservice(BOOKSHARE_API_HOST);
				if(isFree){
					BOOKSHARE_URL = BOOKSHARE_URL + "?api_key="+developerKey;
				}
				else{
					BOOKSHARE_URL = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + BOOKSHARE_API_HOST + "/user/preferences/list/for/"+username+"/?api_key="+developerKey;
				}

				InputStream inputStream = bws.getResponseStream(password, BOOKSHARE_URL);
				result_HTML = bws.convertStreamToString(inputStream);

				// Cleanup the HTML formatted tags
				response = result_HTML.replace("&apos;", "'").replace("&quot;", "\"").replace("&amp;", "&").replace("&#xd;","").replace("&#x97;", "-");

			}
			catch(URISyntaxException use){
				status = NETWORK_ERROR;
			}
			catch(IOException ioe){
				status = NETWORK_ERROR;
			}
			finally {
				if (!inTry) {                      // uncaught exception
					status = NETWORK_ERROR;
				}
            		}
			// Authentication failed
			if(result_HTML.contains("<status-code>401</status-code>") || result_HTML.contains("<status-code>500</status-code>")
					|| result_HTML.contains("<status-code>403</status-code>") || result_HTML.contains("<status-code>404</status-code>") 
					|| result_HTML.contains("<status-code>400</status-code>")) {

				status = LOGIN_FAILED;
			}
			else if (status != NETWORK_ERROR) {
				if(!isFree){
					Bookshare_UserType userTypeObj = new Bookshare_UserType();
					isOM = userTypeObj.isOM(response);
					if(isOM){
						String downloadPassword = userTypeObj.getDownloadPassword();
						if(downloadPassword == null){
							status = LOGIN_FAILED;
							return null;
						}
						
						SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = login_preference.edit();
						editor.putString("downloadPassword", downloadPassword);
						editor.commit();
					}
				}
				status = LOGIN_SUCCESSFUL;
			}
			return null;
		}
        
		/*
		 * (non-Javadoc)
		 * Called in the UI thread immediately after the
		 * doInBackground ends. Re-enable the UI elements.
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		public void onPostExecute(Void result){
			super.onPostExecute(result);

            final VoiceableDialog finishedDialog = new VoiceableDialog(btn_login.getContext());
			btn_login.setEnabled(true);
			editText_username.setEnabled(true);
			editText_password.setEnabled(true);
			if(pd_spinning != null)
				pd_spinning.cancel();

			switch(status){
			
			// Navigate to the next Activity
			case LOGIN_SUCCESSFUL:
				intent = new Intent(getApplicationContext(), Bookshare_Menu.class);
				
				//this is if user log in after prompted by Bookshare_Periodical_Edition_Listing class
				if(requestType==Bookshare_Periodical_Edition_Listing.EDITION_METADATA_RESPONSE){
					intent = new Intent(getApplicationContext(), Bookshare_Periodical_Edition_Details.class);
					uri= Bookshare_Periodical_Edition_Listing.URI_BOOKSHARE_PERIODICAL_EDITION_SEARCH+ periodicalId + "/edition/" + periodicalEdition + "/revision/" + periodicalRevision+"/for/"+username+"?api_key="+BookshareDeveloperKey.DEVELOPER_KEY;
					intent.putExtra("ID_SEARCH_URI", uri);
					intent.putExtra("PERIODICAL_TITLE", periodicalTitle);;
				}
				if(!isFree){
					intent.putExtra(USER, username);
					intent.putExtra(PASSWORD, password);
				}
				
				// Obtain the application wide SharedPreferences object and store the login information
				SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = login_preference.edit();
				editor.putString(USER, username);
				editor.putString(PASSWORD, password);
				editor.putBoolean("isOM", isOM);
				editor.commit();
				startActivity(intent);
				finish();
				break;
				
			// Give the failure notification and show the login screen
			case LOGIN_FAILED:
                String message =  getResources().getString(R.string.login_failed);
                confirmAndClose(message);
				editText_username.setText("");
				editText_password.setText("");
				break;
			case NETWORK_ERROR:
                String nMessage =  getResources().getString(R.string.login_network_error);
                confirmAndClose(nMessage);
				editText_username.setText("");
				editText_password.setText("");
				break;
			default:
				break;
			}
		}
	}
	private void startProgressDialog(){
        pd_spinning = ProgressDialog.show(this, null, "Authenticating. Please wait.", Boolean.TRUE);
	}

    private void confirmAndClose(String msg) {
        final VoiceableDialog dialog = new VoiceableDialog(this);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                editText_username.requestFocus();
            }
        });
        dialog.popup(msg, 4500);
    }

}

