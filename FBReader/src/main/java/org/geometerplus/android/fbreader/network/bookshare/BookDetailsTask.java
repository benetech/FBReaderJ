package org.geometerplus.android.fbreader.network.bookshare;

import android.os.AsyncTask;
import android.util.Log;

import org.bookshare.net.BookshareWebServiceClient;
import org.geometerplus.android.fbreader.FBReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This task gets the book details outside of the main UI thread
 */
public class BookDetailsTask extends AsyncTask<Object, Void, Integer> {

    private String LOG_TAG = FBReader.LOG_LABEL;
    private BookshareWebServiceClient bws = new BookshareWebServiceClient(Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
    private InputStream inputStream;
    private String password;
    private Bookshare_Book_Details bookshare_book_details;
    private String uri;
    private Bookshare_Metadata_Bean metadata_bean;

    public BookDetailsTask(Bookshare_Book_Details bookshare_book_details, final String requestUri, String password) {
        this.bookshare_book_details = bookshare_book_details;
        this.uri = requestUri;
        this.password = password;
    }

    @Override
    protected Integer doInBackground(final Object... params) {
        try {
            inputStream = bws.getResponseStream(password, uri);
            final String response_HTML = bws.convertStreamToString(inputStream);
            final String response = response_HTML.replace("&apos;", "\'").replace("&quot;", "\"")
                    .replace("&amp;", "and").replace("&#xd;\n", "\n").replace("&#x97;", "-");

            // Parse the response String
            parseResponse(response);

            Log.w(FBReader.LOG_LABEL, "done with parseResponse in task");

        } catch (Exception e) {
            Log.e(FBReader.LOG_LABEL, "problem getting results", e);
        }

        return 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(final Integer results) {
        super.onPostExecute(results);
        Log.w(FBReader.LOG_LABEL, "about to call on ResultsFetched");
        bookshare_book_details.onResultsFetched(metadata_bean);
    }

    /**
     * Uses a SAX parser to parse the response
     * @param response String representing the response
     */
    private void parseResponse(String response) {
        Log.i(LOG_TAG, response);
        final InputSource is = new InputSource(new StringReader(response));

        try {
            /* Get a SAXParser from the SAXPArserFactory. */
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp;
            sp = spf.newSAXParser();

            /* Get the XMLReader of the SAXParser we created. */
            final XMLReader parser = sp.getXMLReader();
            metadata_bean = new Bookshare_Metadata_Bean();
            parser.setContentHandler(new BookMetadataSaxHandler(metadata_bean));
            parser.parse(is);
        } catch (SAXException e) {
            Log.e(LOG_TAG, e.toString(), e);
        } catch (ParserConfigurationException e) {
            Log.e(LOG_TAG, e.toString(), e);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.toString(), ioe);
        }
    }


}
