package org.geometerplus.android.fbreader.network.bookshare;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

/**
 * Created by animal@martus.org on 5/25/16.
 */ // Class containing the logic for parsing the response of search results
public class BookshareApiResultSaxHandler extends DefaultHandler {

    private boolean result = false;
    private StringBuilder stringBuilder = new StringBuilder();
    private Vector<String> vector_author;
    private Vector<String> vector_downloadFormat;
    private Bookshare_Result_Bean result_bean;
    private Vector<Bookshare_Result_Bean> vectorResults;
    private int total_pages_result;

    public BookshareApiResultSaxHandler() {
        vectorResults = new Vector<>();
    }

    public int getTotal_pages_result() {
        return total_pages_result;
    }

    public Vector<Bookshare_Result_Bean> getVectorResults() {
        return vectorResults;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

        stringBuilder = new StringBuilder();

        if (qName.equalsIgnoreCase("result")) {
            result = true;
            result_bean = new Bookshare_Result_Bean();
            vector_author = new Vector<String>();
            vector_downloadFormat = new Vector<String>();
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (result) {
            if (qName.equalsIgnoreCase("result")) {
                result = false;
                result_bean.setAuthor(vector_author.toArray(new String[0]));
                result_bean.setDownloadFormats(vector_downloadFormat.toArray(new String[0]));
                vectorResults.add(result_bean);
                result_bean = null;
            } else if (qName.equalsIgnoreCase("id")) {
                result_bean.setId(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("title")) {
                result_bean.setTitle(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("author")) {
                vector_author.add(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("download-format")) {
                vector_downloadFormat.add(stringBuilder.toString());
            }else if (qName.equalsIgnoreCase("download-date")) {
                result_bean.setDownloadDateString(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("images")) {
                result_bean.setImages(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("freely-available")) {
                result_bean.setFreelyAvailable(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("available-to-download")) {
                result_bean.setAvailableToDownload(stringBuilder.toString());
            } else if (qName.equalsIgnoreCase("num-pages")) {
                total_pages_result = Integer.parseInt(stringBuilder.toString());
            }
        }
    }

    public void characters(char[] c, int start, int length) {
        stringBuilder.append(c, start, length);
    }
}
