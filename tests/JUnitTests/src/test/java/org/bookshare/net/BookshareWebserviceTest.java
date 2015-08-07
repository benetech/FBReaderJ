package org.bookshare.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookshareWebserviceTest {
    private BookshareWebservice service;

    private final static String TEST_PROPERTIES = "test.properties";

    private String env;

    private String apiHost;

    protected Logger logger;

    private String username = "username";

    private String password = "password";

    private String apiKey;

    @Before
    public void setup() {
        service = new BookshareWebservice();
        env = System.getenv("GOLDEN_KEY");
        if (env.equals("dev")) {
            logger.error("Mashery tests won't work against dev environment.");
        }
        apiHost = "api." + env + ".bookshare.org";

        logger = LoggerFactory.getLogger(this.getClass());

        setupProperties();

    }

    public void setupProperties() {

        Properties prop = new Properties();

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            // load a properties file
            InputStream input = loader.getResourceAsStream(TEST_PROPERTIES);
            prop.load(input);

            // get the property value and print it out
            apiKey = prop.getProperty("apikey");
            username = prop.getProperty("username");
            password = prop.getProperty("password");

        } catch (IOException e) {
            logger.error("Problem opening the properties file.", e);
            logger.error("Did you create a " + TEST_PROPERTIES + " file with the apikey in src/test/resources?");
            logger.error("Please see the README file for details.");
        }
    }

    @Test
    public void testMd5sum()
            throws Exception
    {
        logger.info("Testing MD5Sum generator.");
        // setup
        final String input = "test-string-for-md5";

        // run the test
        final String result = service.md5sum(input);

        // verify
        final String expectedOutput = "DC5627ED84AB50D93A30DE90B88DE94E";
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testBadCredentialsLogin()
            throws Exception
    {
        String resultHtml = "";
        logger.info("Logging into API with bad credentials.");
        String url = "https://" + apiHost + "/user/preferences/list/for/" + username + "/?api_key=" + apiKey;
        logger.info(url);
        resultHtml = login(url, "bogus");
        logger.debug(resultHtml);
        assertTrue("Contains the 400 HTTP error code",resultHtml.contains("<status-code>400</status-code>"));
        assertTrue("Contains the incorrect password message", resultHtml.contains("The username and/or password is incorrect."));
    }

    @Test
    public void testGoodCredentialsLogin()
            throws Exception
    {
        String resultHtml = "";

        logger.info("Logging into API with valid credentials.");
        String url = "https://" + apiHost + "/user/preferences/list/for/" + username + "/?api_key=" + apiKey;
        logger.info(url);
        resultHtml = login(url, password);
        logger.debug(resultHtml);

        assertFalse(containsHTTPError(resultHtml));
        assertTrue("Contains user account information", resultHtml.contains("User account information for " + username));
    }

    @Test
    public void testAnonymousLogin()
            throws Exception
    {
        String resultHtml = "";
        logger.info("Logging anonymously into API.");

        String url  = "https://" + apiHost + "/book/search/title/potter?api_key=" + apiKey;

        logger.info(url);
        resultHtml = login(url, null);
        logger.debug(resultHtml);
        
        assertFalse(containsHTTPError(resultHtml));
        assertTrue("Contains at least one book element", resultHtml.contains("<book>"));
    }

    private String login(String url, String password)
            throws URISyntaxException, IOException
    {
        final BookshareWebservice bws = new BookshareWebservice(apiHost);
        InputStream inputStream = bws.getResponseStream(password, url);
        String result_HTML = bws.convertStreamToString(inputStream);

        // Cleanup the HTML formatted tags
        return result_HTML.replace("&apos;", "'").replace("&quot;", "\"").replace("&amp;", "&").replace("&#xd;", "")
                .replace("&#x97;", "-");
    }

    private boolean containsHTTPError(String html) {
        return (html.contains("<status-code>401</status-code>") || html.contains("<status-code>500</status-code>")
                || html.contains("<status-code>403</status-code>") || html.contains("<status-code>404</status-code>") || html
                    .contains("<status-code>400</status-code>"));
    }
}
