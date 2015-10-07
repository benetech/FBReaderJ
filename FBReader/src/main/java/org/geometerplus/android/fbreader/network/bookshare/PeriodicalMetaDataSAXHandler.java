package org.geometerplus.android.fbreader.network.bookshare;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author roms
 *         Date: 4/24/13
 */
public class PeriodicalMetaDataSAXHandler extends DefaultHandler {

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
	Bookshare_Edition_Metadata_Bean metadata_bean;

	public Bookshare_Edition_Metadata_Bean getMetadata_bean()
	{
		return metadata_bean;
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

		if (qName.equalsIgnoreCase("metadata")) {
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
				metadata_bean.setDownloadFormats(vector_downloadFormat.toArray(new String[0]));
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
			}

		}
	}
}
