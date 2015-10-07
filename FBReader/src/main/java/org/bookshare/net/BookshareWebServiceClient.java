package org.bookshare.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import android.util.Log;

/**
 * This class provides the services needed for getting data from Bookshare's Webservice API.
 */
public final class BookshareWebServiceClient {

    // endpoint for bookshare API calls
    private static String bookshareApiUrl = "api.bookshare.org";

    private static final String LOG_TAG = "BookshareWebServiceClient";

    /**
     * Default constructor.
     */
    public BookshareWebServiceClient() {
        // empty
    }

    /**
     * Constructor that allows setting of api host.
     * @param apiHost String api host
     */
    public BookshareWebServiceClient(final String apiHost) {
        bookshareApiUrl = apiHost;
    }

    /**
     * Utility method that returns a MD5 encryption of a String.
     * @param str String to be be encrypted.
     * @return MD5 encrypted String.
     * @throws java.io.UnsupportedEncodingException
     */
    protected String md5sum(final String str)
            throws UnsupportedEncodingException
    {
        byte[] md5sum = null;
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md5sum = md.digest(str.getBytes("UTF-8"));
        } catch (final NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        return toHex(md5sum);
    }

    /**
     * Convert a string to hexadecimal
     * @param bytes
     * @return
     */
    private String toHex(final byte[] bytes) {
        final BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    /**
     * Converts the InputStream to a String.
     * @param inputStream InputStream.
     * @return String representation of the InputStream data.
     */
    public String convertStreamToString(final InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to convert web service call result to string.", e);
        }
        return null;
    }

    /**
     * Retrieves the InputStream in response to a requested URI. Method also takes care of user authentication. This
     * InputStream needs to be used in appropriate way to handle the response depending on the response. E.g. If the
     * requested URI is for downloading a file, read data from this InputStream and write to a local file using
     * OutputStream. E.g. If the requested URI returns a XML or JSON response, make use of corresponding parsers to
     * handle the data.
     * @param wsPassword password of the Bookshare's web service account.
     * @param requestUri The request URI.
     * @return InputStream representing the response.
     * @throws URISyntaxException
     * @throws IOException
     */
    public InputStream getResponseStream(final String wsPassword, final String requestUri)
            throws URISyntaxException, IOException
    {
        return getHttpsUrlConnection(wsPassword, requestUri).getInputStream();
    }

    /**
     * Get a secure HTTPS connection to the Bookshare web service
     * @param wsPassword Bookshare password, null if anonymous
     * @param requestUri URI for Bookshare web service connection through Mashery
     * @return The HTTPS connection
     * @throws IOException
     */
    public HttpsURLConnection getHttpsUrlConnection(final String wsPassword, final String requestUri)
            throws IOException
    {
        final URL url = new URL(requestUri);
        final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (wsPassword != null && wsPassword.length() > 0) {
            urlConnection.setRequestProperty("X-password", md5sum(wsPassword));
        }
        return urlConnection;
    }

}
