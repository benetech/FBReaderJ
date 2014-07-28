package org.geometerplus.android.fbreader.network.bookshare;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geometerplus.android.fbreader.FBReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Parse error responses.
 * 
 * @author meghan larson
 */
public final class Bookshare_Error_Bean {

	private String version;
	private String statusCode;
	private List<String> messages;
	private String LOG_TAG = FBReader.LOG_LABEL;

	/**
	 * Default constructor.
	 */
	public Bookshare_Error_Bean() {
		// empty
	}

	/**
	 * @return statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @return messages
	 */
	public List<String> getMessages() {
		return messages;
	}

	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Get messages formatted for display.
	 * 
	 * @return messages formatted for display.
	 */
	public String getMessagesFormatted() {
		final StringBuilder message = new StringBuilder();
		if (messages != null) {
			for (final String m : messages) {
				message.append(m).append("\n");
			}
		}
		return message.toString();

	}

	/**
	 * Create an error bean from an input stream.
	 * 
	 * @param is
	 *            not null
	 */
	public void parseInputStream(final InputStream is) {
		try {
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader parser = sp.getXMLReader();
			parser.setContentHandler(new ErrorHandler());
			parser.parse(new InputSource(is));
		} catch (SAXException e) {
			Log.e(LOG_TAG, e.toString(), e);
		} catch (ParserConfigurationException e) {
			Log.e(LOG_TAG, e.toString(), e);
		} catch (IOException ioe) {
			Log.e(LOG_TAG, ioe.toString(), ioe);
		}
	}

	// parse error information
	private class ErrorHandler extends DefaultHandler {

		private boolean inVersion;
		private boolean inStatusCode;
		private boolean inMessages;
		private boolean inString;

		@Override
		public void characters(char[] c, int start, int length) {
			final String content = new String(c, start, length);
			if (inVersion) {
				version = content;
			} else if (inStatusCode) {
				statusCode = content;
			} else if (inMessages && inString) {
				if (messages == null) {
					messages = new ArrayList<String>();
				}
				messages.add(content);
			}
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) {
			flipBoolean(qName);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			flipBoolean(qName);
		}

		private void flipBoolean(final String qName) {
			if (qName.equals("version")) {
				inVersion = !inVersion;
			} else if (qName.equals("status-code")) {
				inStatusCode = !inStatusCode;
			} else if (qName.equals("messages")) {
				inMessages = !inMessages;
			} else if (qName.equals("string")) {
				inString = !inString;
			}
		}
	}

}
