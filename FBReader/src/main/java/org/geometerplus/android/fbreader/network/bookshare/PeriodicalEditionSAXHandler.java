package org.geometerplus.android.fbreader.network.bookshare;

import java.util.ArrayList;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author roms
 *         Date: 4/24/13
 */
public class PeriodicalEditionSAXHandler extends DefaultHandler {

	boolean result = false;
	boolean id = false;
	boolean title = false;
	boolean edition = false;
	boolean revision = false;
	boolean num_pages = false;

	boolean editionElementVisited = false;
	boolean revisionElementVisited = false;

	private boolean total_pages_count_known = false;
	private int total_pages_result;
	private ArrayList<Bookshare_Periodical_Edition_Bean> results;

	Bookshare_Periodical_Edition_Bean result_bean;

	public PeriodicalEditionSAXHandler()
	{
		super();
		results = new ArrayList<Bookshare_Periodical_Edition_Bean>();
	}

	public boolean isTotal_pages_count_known()
	{
		return total_pages_count_known;
	}

	public int getTotal_pages_result()
	{
		return total_pages_result;
	}

	public ArrayList<Bookshare_Periodical_Edition_Bean> getResults()
	{
		return results;
	}

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
