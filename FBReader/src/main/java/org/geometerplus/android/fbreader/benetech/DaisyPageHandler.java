package org.geometerplus.android.fbreader.benetech;

import java.util.LinkedHashMap;
import java.util.Set;

import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

/**
 * @author roms
 *
 * Paging handler for DAISY books which uses the DAISY pageTarget tags
 */
public class DaisyPageHandler implements PageHandler {
    
    private FBReaderApp fbreader;
    private LinkedHashMap<String, Integer> pageMap;
    private LinkedHashMap<Integer, String> paragraphToPage;

    public DaisyPageHandler(FBReaderApp fbreader, LinkedHashMap<String, Integer> pageMap) {
        this.fbreader = fbreader;
        this.pageMap = pageMap;
        paragraphToPage = new LinkedHashMap<Integer, String>(pageMap.size());
        
        Set<String> pages = pageMap.keySet();
        for (String page : pages) {
            paragraphToPage.put(pageMap.get(page), page);
        }
    }

    @Override
    /*
     * return true if all the page numbers are numeric
     */
    public boolean isNumeric() {
        return fbreader.isAllDaisyPagesIntegers();
    }

    @Override
    /*
     * get the page number user is currently listening to/viewing
     */
    public String getCurrentPage() {

        ApiServerImplementation myApi = new ApiServerImplementation();
        int currentParagraph = myApi.getPageStart().ParagraphIndex;

        String currentPage;
        currentPage = paragraphToPage.get(currentParagraph);
        while (null == currentPage) {
            currentPage = paragraphToPage.get(--currentParagraph);
            if (currentParagraph < 2) {
                return "1";
            }
        }

        return currentPage;
    }

    @Override
    /*
     * get the last page number for this book
     */
    public String getLastPage() {
        return fbreader.getLastDaisyPage();
    }

    @Override
    /*
     * go to the specified page in the book
     * return true if the page exists in the book, false otherwise
     */
    public boolean gotoPage(String page) {
        boolean validPage = false;
        final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
        //fbreader.addInvisibleBookmark();
        Integer paragraphNumber = pageMap.get(page);
        if (null == paragraphNumber) {
            String upperCasePage = page.toUpperCase();
            paragraphNumber = pageMap.get(upperCasePage);
        }
        if (null == paragraphNumber) {
            String lowerCasePage = page.toLowerCase();
            paragraphNumber = pageMap.get(lowerCasePage);
        }
        if (null != paragraphNumber) {
            fbreader.BookTextView.gotoPosition(paragraphNumber, 0, 0);
            fbreader.showBookTextView();
            FBReaderApp.Instance().getViewWidget().reset();
            FBReaderApp.Instance().getViewWidget().repaint();
            validPage = true;
        }
        return validPage;
    }
}
