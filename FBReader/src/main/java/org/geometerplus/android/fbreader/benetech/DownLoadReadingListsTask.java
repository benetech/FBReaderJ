package org.geometerplus.android.fbreader.benetech;

import android.os.AsyncTask;
import android.util.Log;

import org.bookshare.net.BookshareHttpOauth2Client;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by animal@martus.org on 4/19/16.
 */
public class DownLoadReadingListsTask extends AsyncTask<Void, Void, JSONArray> {

    private AsyncResponse asyncResponse;
    private String userName;
    private String password;

    public DownLoadReadingListsTask(AsyncResponse asyncResponseToUse, String userNameToUse, String passwordToUse) {
        asyncResponse = asyncResponseToUse;
        userName = userNameToUse;
        password = passwordToUse;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        try {
            BookshareHttpOauth2Client client =  new BookshareHttpOauth2Client();
            HttpsURLConnection urlConnection = client.createBookshareApiUrlConnection(getUserName(), getPassword());

            String response = client.requestData(urlConnection);
            JSONObject jsonResponse = new JSONObject(response);
            String accessToken = jsonResponse.getString(BookshareHttpOauth2Client.ACCESS_TOKEN_CODE);

            return client.getReadingLists(accessToken);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            return new JSONArray();
        }
    }

    @Override
    protected void onPostExecute(JSONArray readingListJsonArray) {
        super.onPostExecute(readingListJsonArray);

        asyncResponse.processFinish(readingListJsonArray);
    }

    private String getUserName() {
        return userName;
    }

    private String getPassword() {
        return password;
    }
}
