package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * @author yash 
 * Parse xml response containing packaging status
 */
public class Bookshare_PackagingStatus_Bean {

	final private String LOG_TAG = "status bean";
	private String version = "";
	private String contentId = "";
	private String packagingStatus = "";
	private List<String> messages;

	/**
	 * Default constructor.
	 */
	public Bookshare_PackagingStatus_Bean() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return contentId
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * @return messages
	 * 
	 */
	public List<String> getMessages() {
		return messages;
	}

	/**
	 * @return version
	 */
	public String getPackagingStatus() {
		return packagingStatus;
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
	 * Create an packaging status bean from an input stream.
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
			parser.setContentHandler(new PackagingStatusHander());
			parser.parse(new InputSource(is));
		} catch (SAXException e) {
			System.out.println(e);
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (IOException ioe) {
		}
	}

	// parse packaging status information
	private class PackagingStatusHander extends DefaultHandler {

		private boolean inVersion;
		private boolean inStatusCode;
		private boolean inMessages;
		private boolean inString;
		private boolean inContentId;
		private boolean inStatus;
		private boolean inBook;

		@Override
		public void characters(char[] c, int start, int length) {
			final String content = new String(c, start, length);
			if (inVersion) {
				version = content;
				Log.d(LOG_TAG, "version = " + content);
			} else if (inMessages && inString) {
				if (messages == null) {
					messages = new ArrayList<String>();
				}
				messages.add(content);
				Log.d(LOG_TAG, "message = " + content);
			} else if (inBook && inContentId) {
				Log.d(LOG_TAG, "contentId = " + content);
				contentId = content;
			} else if (inBook && inStatus) {
				Log.d(LOG_TAG, "packaging status =" + content);
				packagingStatus = content;
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
			} else if (qName.equals("messages")) {
				inMessages = !inMessages;
			} else if (qName.equals("string")) {
				inString = !inString;
			} else if (qName.equals("book")) {
				inBook = !inBook;
			} else if (qName.equals("content-id")) {
				inContentId = !inContentId;
			} else if (qName.equals("packaging-status")) {
				inStatus = !inStatus;
			}
		}
	}

}
