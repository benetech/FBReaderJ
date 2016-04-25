package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by animal@martus.org on 4/20/16.
 */
public class CheckInternetConnectionTask extends AsyncTask<Void, Void, Boolean> {
    private static final String LOG_TAG = "CheckInternect";
    private AsyncResponse responseDelegator;
    private Context context;

    public CheckInternetConnectionTask(Context contextToUse, AsyncResponse responseDelegatorToUse) {
        context = contextToUse;
        responseDelegator = responseDelegatorToUse;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        return hasActiveInternetConnection();
    }

    @Override
    protected void onPostExecute(Boolean hasInternetConnection) {
        super.onPostExecute(hasInternetConnection);

        responseDelegator.processFinish(hasInternetConnection);
    }

    public boolean hasActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                final String URL_TO_TEST_AGAINST = "http://www.google.com";
                HttpURLConnection urlc = (HttpURLConnection) (new URL(URL_TO_TEST_AGAINST).openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();

                return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error checking internet connection", e);
                return false;
            }
        } else {
            Log.i(LOG_TAG, "No network available!");
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null;
    }

    private Context getContext() {
        return context;
    }
}
