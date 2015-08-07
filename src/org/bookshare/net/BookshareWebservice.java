package org.bookshare.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;

/**
 * This class provides the services needed for getting data from Bookshare's Webservice API.
 */
public final class BookshareWebservice {

    // endpoint for bookshare API calls
    private static String URL = "api.bookshare.org";

    /**
     * Default constructor.
     */
    public BookshareWebservice() {
        // empty
    }

    /**
     * Constructor that allows setting of api host.
     * @param apiHost String api host
     */
    public BookshareWebservice(final String apiHost) {
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
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public InputStream getResponseStream(final String wsPassword, final String requestUri)
            throws URISyntaxException, IOException
    {
        final HttpResponse response = getHttpResponse(wsPassword, requestUri);
        return response.getEntity().getContent();
    }

    /**
     * Retrieves an HttpResponse for a requested URI.
     * @param wsPassword password of the Bookshare web service account.
     * @param requestUri The request URI.
     * @return HttpResponse A HttpResponse object.
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpResponse getHttpResponse(final String wsPassword, final String requestUri)
            throws URISyntaxException, IOException
    {
         
        // BROWSER_COMPATIBLE_HOSTNAME_VERIFIER lets the *.bookshare.org certificate match the 
        // *.qa.bookshare.org and *.staging.bookshare.org sites
        
        final HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

        final DefaultHttpClient client = new DefaultHttpClient();

        final SchemeRegistry registry = new SchemeRegistry();
        final SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        
        // We are only registering https scheme, which means http requests will throw an error
        registry.register(new Scheme("https", socketFactory, 443));
        final SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        final DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        final URI uri = new URI(requestUri);

        // Prepare a HTTP GET Request
        final HttpGet httpget = new HttpGet(uri);

        if (wsPassword != null) {
            final Header header = new BasicHeader("X-password", md5sum(wsPassword));
            httpget.setHeader(header);
        }

        // Execute the request
        return httpClient.execute(httpget);


    }
}
