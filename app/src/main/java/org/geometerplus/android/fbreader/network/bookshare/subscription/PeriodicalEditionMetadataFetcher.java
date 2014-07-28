
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.bookshare.net.BookshareWebservice;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.android.fbreader.network.bookshare.PeriodicalMetaDataSAXHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
	PeriodicalMetaDataSAXHandler saxHandler;
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
					Log.e("GoRead", getClass().getSimpleName(), ioe);
				} catch (URISyntaxException use) {
					Log.e("GoRead", getClass().getSimpleName(), use);
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
				metadata_bean = saxHandler.getMetadata_bean();
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
			saxHandler = new PeriodicalMetaDataSAXHandler();
			parser.setContentHandler(saxHandler);
			parser.parse(is);
		} catch (SAXException e) {
			System.out.println(e);
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (IOException ioe) {
		}
	}



}