package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.bookshare.net.BookshareWebservice;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
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
	Vector<Bookshare_Periodical_Edition_Bean> results;
	private boolean total_pages_count_known = false;
	private int total_pages_result;
	String password;
	BookshareWebservice bws;
	private PeriodicalEditionListener callback;

	/*
	 * public static PeriodicalEditionListFetcher getInstance() { if (singleton
	 * == null) { singleton = new PeriodicalEditionListFetcher(); } return
	 * singleton; }
	 */

	public void getListing(final String uri, final String password,
			PeriodicalEditionListener callback) {

		results = new Vector<Bookshare_Periodical_Edition_Bean>();
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
			parser.setContentHandler(new SAXHandler());
			parser.parse(is);
		} catch (SAXException e) {
			System.out.println(e);
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	private class SAXHandler extends DefaultHandler {

		boolean result = false;
		boolean id = false;
		boolean title = false;
		boolean edition = false;
		boolean revision = false;
		boolean num_pages = false;

		boolean editionElementVisited = false;
		boolean revisionElementVisited = false;

		String editionStr;

		Bookshare_Periodical_Edition_Bean result_bean;

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) {

			if (!total_pages_count_known) {
				if (qName.equalsIgnoreCase("num-pages")) {
					num_pages = true;
					total_pages_count_known = false;
				}
			}

			if (qName.equalsIgnoreCase("result")) {
				result = true;
				result_bean = new Bookshare_Periodical_Edition_Bean();
				editionElementVisited = false;
				revisionElementVisited = false;

			}
			if (qName.equalsIgnoreCase("id")) {
				id = true;
			}
			if (qName.equalsIgnoreCase("title")) {
				title = true;
			}
			if (qName.equalsIgnoreCase("edition")) {
				edition = true;
				if (!editionElementVisited) {
					editionElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("revision")) {
				revision = true;
				if (!revisionElementVisited) {
					revisionElementVisited = true;
				}
			}
		}

		public void endElement(String uri, String localName, String qName) {

			if (num_pages) {
				if (qName.equalsIgnoreCase("num-pages")) {
					num_pages = false;
				}
			}
			if (qName.equalsIgnoreCase("result")) {
				result = false;
				results.add(result_bean);
				result_bean = null;
			}
			if (qName.equalsIgnoreCase("id")) {
				id = false;
			}
			if (qName.equalsIgnoreCase("title")) {
				title = false;
			}
			if (qName.equalsIgnoreCase("edition")) {
				edition = false;
			}
			if (qName.equalsIgnoreCase("revision")) {
				revision = false;
			}

		}

		public void characters(char[] c, int start, int length) {

			if (num_pages) {
				total_pages_result = Integer.parseInt(new String(c, start,
						length));
			}
			if (result) {
				if (id) {
					result_bean.setId(new String(c, start, length));
				}
				if (title) {
					result_bean.setTitle(new String(c, start, length));
				}
				if (edition) {
					result_bean.setEdition(new String(c, start, length));
				}
				if (revision) {
					result_bean.setRevision(new String(c, start, length));
				}

			}
		}
	}

}
