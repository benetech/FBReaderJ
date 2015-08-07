package org.bookshare.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class provides the services needed for getting data from Bookshare's Webservice API.
 */
public final class BookshareWebServiceClient {

    // endpoint for bookshare API calls
    private static String URL = "api.bookshare.org";

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
        URL = apiHost;
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
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine() method. We iterate until the
         * BufferedReader return null which means there's no more data to read. Each line will appended to a
         * StringBuilder and returned as String.
         */
        // Construct a BufferedReader for the inputStream
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            // Read each line and append a newline character at the end
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the stream irrespective of whether the read was successful or not
                inputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        // Return the trimmed response String
        return sb.toString().trim();
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
    
    public HttpsURLConnection getHttpsUrlConnection(final String wsPassword, final String requestUri) throws IOException {
        URL url = new URL(requestUri);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (wsPassword != null && wsPassword.length() > 0) {
            urlConnection.setRequestProperty("X-password", md5sum(wsPassword));
        }
        return urlConnection;
    }  
    
}
