package org.geometerplus.android.fbreader.network.bookshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.bookshare.net.BookshareWebServiceClient;
import org.geometerplus.android.fbreader.benetech.AsyncResponse;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by animal@martus.org on 5/25/16.
 */

public class BookshareApiV1UserHistoryRetriever {

    private String API_HISTORY_URI = Bookshare_Webservice_Login.BOOKSHARE_API_PROTOCOL + Bookshare_Webservice_Login.BOOKSHARE_API_HOST + "/user/history/for/";
    private String username;
    private String password;
    private boolean isFree= false;
    private String historyApiUri;
    private BookshareWebServiceClient webServiceClient;
    private AsyncResponse responseHandler;
    private Vector<Bookshare_Result_Bean> vectorResults;

    public BookshareApiV1UserHistoryRetriever(Context context, AsyncResponse responseHandlerToUse) {
        responseHandler = responseHandlerToUse;
        vectorResults = new Vector();

        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(context);
        username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");
        historyApiUri = getApiUrlForHistory();

        webServiceClient = new BookshareWebServiceClient(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
        getListing(historyApiUri);
    }

    private String getApiUrlForHistory() {

        return API_HISTORY_URI + getSubscriptionBasedUsername() + "?api_key=" + BookshareDeveloperKey.DEVELOPER_KEY;
    }

    private String getSubscriptionBasedUsername() {
        if (isFree)
            return "";

        return username;
    }

    public Vector<Bookshare_Result_Bean> getResultBeans() {
        return vectorResults;
    }

    private void parseResponse(String response){
        InputSource is = new InputSource(new StringReader(response));
        try{
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            BookshareApiResultSaxHandler saxHandler = new BookshareApiResultSaxHandler();
            parser.setContentHandler(saxHandler);
            parser.parse(is);

            vectorResults = saxHandler.getVectorResults();
        }
        catch(Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void getListing(final String uri){
        final AsyncTask<Object, Void, Void> bookResultsFetcher = new BookListingTask(uri);
        bookResultsFetcher.execute();
    }

    private String getUserPassword() {
        return password;
    }

    private class BookListingTask extends AsyncTask<Object, Void, Void> {
        private String apiUri;

        public BookListingTask(String apiUriToUse) {
            apiUri = apiUriToUse;
        }

        @Override
        protected Void doInBackground(Object... params) {
            try {
                InputStream inputStream = webServiceClient.getResponseStream(getUserPassword(), apiUri);
                String responseAsHtml = webServiceClient.convertStreamToString(inputStream);
                String response = responseAsHtml.replace("&apos;", "\'").replace("&quot;", "\"").replace("&amp;", "and").replace("&#xd;","").replace("&#x97;", "-");
                parseResponse(response);
            }
            catch(Exception e){
                Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

           responseHandler.processFinish(param);
        }
    }
}
