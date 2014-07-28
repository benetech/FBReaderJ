
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.bookshare.net.BookshareWebservice;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.android.fbreader.network.bookshare.PeriodicalEditionSAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;

/**
 * Fetch periodical editions of the given periodical id
 * 
 * @author thushan
 * 
 */
public class PeriodicalEditionListFetcher {

	private static PeriodicalEditionListFetcher singleton;
	private InputStream inputStream;
	private final int DATA_FETCHED = 99;
	ArrayList<Bookshare_Periodical_Edition_Bean> results;
	String password;
	BookshareWebservice bws;
	private PeriodicalEditionListener callback;
	PeriodicalEditionSAXHandler saxHandler;

	/*
	 * public static PeriodicalEditionListFetcher getInstance() { if (singleton
	 * == null) { singleton = new PeriodicalEditionListFetcher(); } return
	 * singleton; }
	 */

	public void getListing(final String uri, final String password,
			PeriodicalEditionListener callback) {

		this.password = password;
		this.callback = callback;

		new Thread() {
			public void run() {
				try {
					bws = new BookshareWebservice(
							Bookshare_Webservice_Login.BOOKSHARE_API_HOST);

					inputStream = bws.getResponseStream(password, uri);

					// Once the response is obtained, send message to the
					// handler
					Message msg = Message.obtain();
					msg.what = DATA_FETCHED;
					msg.setTarget(handler);
					msg.sendToTarget();
				} catch (IOException ioe) {
					System.out.println(ioe);
				} catch (URISyntaxException use) {
					System.out.println(use);
				}
			}
		}.start();

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			// Message received that data has been fetched from the
			// bookshare web services
			if (msg.what == DATA_FETCHED) {

				String response_HTML = bws.convertStreamToString(inputStream);

				// Cleanup the HTML formatted tags
				String response = response_HTML.replace("&apos;", "\'")
						.replace("&quot;", "\"").replace("&amp;", "and")
						.replace("&#xd;", "").replace("&#x97;", "-");

				System.out.println(response);
				// Parse the response of search result
				parseResponse(response);
				results = saxHandler.getResults();

				callback.onPeriodicalEditionListResponse(results);
			}
		}
	};

	private void parseResponse(String response) {

		InputSource is = new InputSource(new StringReader(response));

		try {
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader parser = sp.getXMLReader();
			saxHandler = new PeriodicalEditionSAXHandler();
			parser.setContentHandler(saxHandler);
			parser.parse(is);
		} catch (SAXException e) {
			System.out.println(e);
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

}