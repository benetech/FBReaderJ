package org.geometerplus.android.fbreader.benetech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.benetech.android.R;
import org.bookshare.net.BookshareHttpOauth2Client;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mvillalobos on dec 2016.
 */
public class RemoveFromReadingListDialogActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";
    public static final String EXTRA_LIST_ID = "EXTRA_LIST_ID";
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_FAIL = 0;

    private String bookId = null;
    private String listBookshareId = null;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_remove_from_reading_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.remove_from_both_button).setOnClickListener(this);
        findViewById(R.id.remove_from_list_button).setOnClickListener(this);
        findViewById(R.id.negative_button).setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        listBookshareId = getIntent().getStringExtra(EXTRA_LIST_ID);
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    protected void handleResult(boolean result){
        Intent intent  = getIntent();
        if(result){
            setResult(RESULT_CODE_SUCCESS, intent);
        }
        else{
            setResult(RESULT_CODE_FAIL, intent);
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.negative_button:
                this.finish();
                break;
            case R.id.remove_from_both_button:
                removeFromReadingList();
                break;
            case R.id.remove_from_list_button:
                removeFromReadingList();
                break;
        }
    }

    private void removeFromReadingList() {
        new RemoveTitleFromReadingListTask().execute();
    }

    class RemoveTitleFromReadingListTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(String... urls) {
            try {
                SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(RemoveFromReadingListDialogActivity.this);
                String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
                String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");

                BookshareHttpOauth2Client client =  new BookshareHttpOauth2Client();
                HttpsURLConnection urlConnection = client.createBookshareApiUrlConnection(username, password);

                String response = client.requestData(urlConnection);
                JSONObject jsonResponse = new JSONObject(response);
                String accessToken = jsonResponse.getString(BookshareHttpOauth2Client.ACCESS_TOKEN_CODE);

                return client.deleteTitleFromReadingList(accessToken, listBookshareId, bookId);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            RemoveFromReadingListDialogActivity.this.handleResult(result);
        }
    }

}
