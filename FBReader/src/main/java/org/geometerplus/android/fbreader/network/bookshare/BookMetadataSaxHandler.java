package org.geometerplus.android.fbreader.network.bookshare;

import android.util.Log;

import org.geometerplus.android.fbreader.FBReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

/**
 * Created by animal@martus.org on 2/2/17.
 */ // Class that applies parsing logic
public class BookMetadataSaxHandler extends DefaultHandler {

    private String LOG_TAG = FBReader.LOG_LABEL;
    private boolean metadata = false;
    private boolean contentId = false;
    private boolean daisy = false;
    private boolean brf = false;
    private boolean downloadFormats = false;
    private boolean images = false;
    private boolean isbn = false;
    private boolean authors = false;
    private boolean title = false;
    private boolean publishDate = false;
    private boolean publisher = false;
    private boolean copyright = false;
    private boolean language = false;
    private boolean briefSynopsis = false;
    private boolean completeSynopsis = false;
    private boolean quality = false;
    private boolean category = false;
    private boolean bookshareId = false;
    private boolean freelyAvailable = false;
    private boolean availableToDownload = false;
    private boolean authorElementVisited = false;
    private boolean downloadFormatElementVisited = false;
    private boolean titleElementVisited = false;
    private boolean categoryElementVisited = false;
    private boolean briefSynopsisElementVisited = false;
    private boolean completeSynopsisElementVisited = false;
    private Vector<String> vector_author;
    private Vector<String> vector_downloadFormat;
    private Vector<String> vector_category;
    private Vector<String> vector_briefSynopsis;
    private Vector<String> vector_completeSynopsis;
    private Vector<String> vector_title;
    private Bookshare_Metadata_Bean metadata_bean;

    public BookMetadataSaxHandler(Bookshare_Metadata_Bean metadata_bean) {
        this.metadata_bean = metadata_bean;
    }

    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) {

        if (qName.equalsIgnoreCase("metadata")) {
            Log.i(LOG_TAG, "******* metadata visited");
            metadata = true;

            authorElementVisited = false;
            downloadFormatElementVisited = false;
            titleElementVisited = false;
            categoryElementVisited = false;
            briefSynopsisElementVisited = false;
            completeSynopsisElementVisited = false;
            vector_author = new Vector<String>();
            vector_downloadFormat = new Vector<String>();
            vector_category = new Vector<String>();
            vector_briefSynopsis = new Vector<String>();
            vector_completeSynopsis = new Vector<String>();
            vector_title = new Vector<String>();
        }
        if (qName.equalsIgnoreCase("content-id")) {
            contentId = true;
        }
        if (qName.equalsIgnoreCase("available-to-download")) {
            availableToDownload = true;
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
        if (qName.equalsIgnoreCase("isbn10") || qName.equalsIgnoreCase("isbn13")) {
            isbn = true;
        }
        if (qName.equalsIgnoreCase("author")) {
            authors = true;
            if (!authorElementVisited) {
                authorElementVisited = true;
            }
        }
        if (qName.equalsIgnoreCase("title")) {
            title = true;
            if (!titleElementVisited) {
                titleElementVisited = true;
            }
        }
        if (qName.equalsIgnoreCase("publish-date")) {
            publishDate = true;
        }
        if (qName.equalsIgnoreCase("publisher")) {
            publisher = true;
        }
        if (qName.equalsIgnoreCase("copyright")) {
            copyright = true;
        }
        if (qName.equalsIgnoreCase("language")) {
            language = true;
        }
        if (qName.equalsIgnoreCase("brief-synopsis")) {
            briefSynopsis = true;
            if (!briefSynopsisElementVisited) {
                briefSynopsisElementVisited = true;
            }
        }
        if (qName.equalsIgnoreCase("complete-synopsis")) {
            completeSynopsis = true;
            if (!completeSynopsisElementVisited) {
                completeSynopsisElementVisited = true;
            }
        }
        if (qName.equalsIgnoreCase("freely-available")) {
            freelyAvailable = true;
        }
        if (qName.equalsIgnoreCase("quality")) {
            quality = true;
        }
        if (qName.equalsIgnoreCase("bookshare-id")) {
            bookshareId = true;
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
        if (qName.equalsIgnoreCase("available-to-download")) {
            availableToDownload = false;
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
        if (qName.equalsIgnoreCase("isbn10") || qName.equalsIgnoreCase("isbn13")) {
            isbn = false;
        }
        if (qName.equalsIgnoreCase("author")) {
            authors = false;
        }
        if (qName.equalsIgnoreCase("title")) {
            title = false;
        }
        if (qName.equalsIgnoreCase("publish-date")) {
            publishDate = false;
        }
        if (qName.equalsIgnoreCase("publisher")) {
            publisher = false;
        }
        if (qName.equalsIgnoreCase("copyright")) {
            copyright = false;
        }
        if (qName.equalsIgnoreCase("language")) {
            language = false;
        }
        if (qName.equalsIgnoreCase("brief-synopsis")) {
            briefSynopsis = false;
        }
        if (qName.equalsIgnoreCase("complete-synopsis")) {
            completeSynopsis = false;
        }
        if (qName.equalsIgnoreCase("freely-available")) {
            freelyAvailable = false;
        }
        if (qName.equalsIgnoreCase("quality")) {
            quality = false;
        }
        if (qName.equalsIgnoreCase("category")) {
            category = false;
        }
        if (qName.equalsIgnoreCase("bookshare-id")) {
            bookshareId = false;
        }
    }

    public void characters(char[] c, int start, int length) {

        if (metadata) {
            if (contentId) {
                metadata_bean.setContentId(new String(c, start, length));
            }
            if (availableToDownload) {
                metadata_bean.setAvailableToDownload(new String(c, start, length));
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
                // for(int i=0;i<vector_downloadFormat.size();i++)
                final String temp = new String(c, start, length);
                Log.i(LOG_TAG, "formats" + temp);
            }
            if (images) {
                metadata_bean.setImages(new String(c, start, length));
            }
            if (isbn) {
                metadata_bean.setIsbn(new String(c, start, length));
            }

            if (authors) {
                vector_author.add(new String(c, start, length));
                metadata_bean.setAuthors(vector_author.toArray(new String[0]));
            }
            if (title) {
                vector_title.add(new String(c, start, length));
                metadata_bean.setTitle(vector_title.toArray(new String[0]));
            }
            if (publishDate) {
                metadata_bean.setPublishDate(new String(c, start, length));
            }
            if (publisher) {
                metadata_bean.setPublisher(new String(c, start, length));
            }
            if (copyright) {
                metadata_bean.setCopyright(new String(c, start, length));
            }
            if (language) {
                metadata_bean.setLanguage(new String(c, start, length));
            }
            if (briefSynopsis) {
                vector_briefSynopsis.add(new String(c, start, length));
                metadata_bean.setBriefSynopsis(vector_briefSynopsis.toArray(new String[0]));
            }
            if (completeSynopsis) {
                vector_completeSynopsis.add(new String(c, start, length));
                metadata_bean.setCompleteSynopsis(vector_completeSynopsis.toArray(new String[0]));
            }
            if (quality) {
                metadata_bean.setQuality(new String(c, start, length));
            }
            if (category) {
                vector_category.add(new String(c, start, length));
                metadata_bean.setCategory(vector_category.toArray(new String[0]));
                Log.i(LOG_TAG, "metadata_bean.getCategory() = " + metadata_bean.getCategory());

            }
            if (bookshareId) {
                metadata_bean.setBookshareId(new String(c, start, length));
            }
            if (freelyAvailable) {
                metadata_bean.setFreelyAvailable(new String(c, start, length));
            }
        }
    }
}
