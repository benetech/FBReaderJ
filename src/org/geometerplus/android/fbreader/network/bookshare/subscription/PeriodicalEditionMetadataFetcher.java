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
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;

/**
 * Fetch metadata of the periodical specified
 * 
 * @author thushan
 * 
 */
public class PeriodicalEditionMetadataFetcher {

	private static PeriodicalEditionMetadataFetcher singleton;
	private InputStream inputStream;
	private final int DATA_FETCHED = 99;
	final BookshareWebservice bws = new BookshareWebservice(
			Bookshare_Webservice_Login.BOOKSHARE_API_HOST);
	Bookshare_Edition_Metadata_Bean metadata_bean;
	PeriodicalMetadataListener callback;
	private static String periodicalTitle;
	private static String periodicalId;

	public PeriodicalEditionMetadataFetcher(String id, String title) {
		periodicalTitle = title;
		periodicalId = id;

	}

	public void getListing(final String uri, final String password,
			PeriodicalMetadataListener callback) {

		this.callback = callback;

		new Thread() {
			public void run() {
				try {
					inputStream = bws.getResponseStream(password, uri);
					Message msg = Message.obtain(handler);
					msg.what = DATA_FETCHED;
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
		public void handleMessage(Message msg) {
			if (msg.what == DATA_FETCHED) {

				String response_HTML = bws.convertStreamToString(inputStream);
				String response = response_HTML.replace("&apos;", "\'")
						.replace("&quot;", "\"").replace("&amp;", "and")
						.replace("&#xd;\n", "\n").replace("&#x97;", "-");

				// Parse the response String
				parseResponse(response);
				metadata_bean.setTitle(periodicalTitle);
				metadata_bean.setPeriodicalId(periodicalId);
				callback.onPeriodicalMetadataResponse(metadata_bean);
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
		}
	}

	// Class that applies parsing logic
	private class SAXHandler extends DefaultHandler {

		boolean metadata = false;
		boolean contentId = false;
		boolean daisy = false;
		boolean brf = false;
		boolean downloadFormats = false;
		boolean images = false;
		boolean edition = false;
		boolean revisionTime = false;
		boolean revision = false;
		boolean category = false;

		boolean downloadFormatElementVisited = false;
		boolean categoryElementVisited = false;

		Vector<String> vector_downloadFormat;
		Vector<String> vector_category;

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) {

			if (qName.equalsIgnoreCase("metadata")) {
				System.out.println("******* metadata visited");
				metadata = true;
				metadata_bean = new Bookshare_Edition_Metadata_Bean();

				downloadFormatElementVisited = false;

				categoryElementVisited = false;
				vector_downloadFormat = new Vector<String>();
				vector_category = new Vector<String>();

			}
			if (qName.equalsIgnoreCase("content-id")) {
				contentId = true;
			}
			if (qName.equalsIgnoreCase("daisy")) {
				daisy = true;
			}
			if (qName.equalsIgnoreCase("brf")) {
				brf = true;
			}
			if (qName.equalsIgnoreCase("download-format")) {
				downloadFormats = true;
				if (!downloadFormatElementVisited) {
					downloadFormatElementVisited = true;
				}
			}
			if (qName.equalsIgnoreCase("images")) {
				images = true;
			}
			if (qName.equalsIgnoreCase("edition")) {
				edition = true;
			}

			if (qName.equalsIgnoreCase("revision-time")) {
				revisionTime = true;
			}
			if (qName.equalsIgnoreCase("revision")) {
				revision = true;
			}
			if (qName.equalsIgnoreCase("category")) {
				category = true;
				if (!categoryElementVisited) {
					categoryElementVisited = true;
				}
			}
		}

		public void endElement(String uri, String localName, String qName) {

			// End of one metadata element parsing.
			if (qName.equalsIgnoreCase("metadata")) {
				metadata = false;
			}
			if (qName.equalsIgnoreCase("content-id")) {
				contentId = false;
			}
			if (qName.equalsIgnoreCase("daisy")) {
				daisy = false;
			}
			if (qName.equalsIgnoreCase("brf")) {
				brf = false;
			}
			if (qName.equalsIgnoreCase("download-format")) {
				downloadFormats = false;
			}
			if (qName.equalsIgnoreCase("images")) {
				images = false;
			}
			if (qName.equalsIgnoreCase("edition")) {
				edition = false;
			}
			if (qName.equalsIgnoreCase("revision-time")) {
				revisionTime = false;
			}

			if (qName.equalsIgnoreCase("revision")) {
				revision = false;
			}
			if (qName.equalsIgnoreCase("category")) {
				category = false;
			}

		}

		public void characters(char[] c, int start, int length) {

			if (metadata) {
				if (contentId) {
					metadata_bean.setContentId(new String(c, start, length));
				}
				if (daisy) {
					metadata_bean.setDaisy(new String(c, start, length));
				}
				if (brf) {
					metadata_bean.setBrf(new String(c, start, length));
				}
				if (downloadFormats) {
					vector_downloadFormat.add(new String(c, start, length));
					metadata_bean.setDownloadFormats(vector_downloadFormat
							.toArray(new String[0]));
				}
				if (images) {
					metadata_bean.setImages(new String(c, start, length));
				}
				if (edition) {
					metadata_bean.setEdition(new String(c, start, length));
				}
				if (revisionTime) {
					metadata_bean.setRevisionTime(new String(c, start, length));
				}
				if (revision) {
					metadata_bean.setRevision(new String(c, start, length));
				}
				if (category) {
					vector_category.add(new String(c, start, length));
					metadata_bean.setCategory(new String(c, start, length));
					System.out.println("metadata_bean.getCategory() = "
							+ metadata_bean.getCategory());

				}

			}
		}
	}

}
