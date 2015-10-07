package org.geometerplus.android.fbreader.benetech;


import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;

/**
 * @author roms
 */
public class DefaultPageHandler implements PageHandler {
    
    private FBReaderApp fbreader;
    private ZLTextView.PagePosition pagePosition;

    public DefaultPageHandler(FBReaderApp fbreader) {
        this.fbreader = fbreader;
        final ZLTextView textView = (ZLTextView) fbreader.getCurrentView();
        pagePosition = textView.pagePosition();
    }

    @Override
    /*
     * return true if all the page numbers are numeric
     */
    public boolean isNumeric() {
        return true;
    }

    /*
     * get the page number user is currently listening to/viewing
     */
    @Override
    public String getCurrentPage() {
        return String.valueOf(pagePosition.Current);
    }

    @Override
    /*
     * get the last page number for this book
     */
    public String getLastPage() {
        return String.valueOf(pagePosition.Total);
    }

    @Override
    /*
     * go to the specified page in the book
     * return true if the page exists in the book, false otherwise
     */
    public boolean gotoPage(String pageStr) {

        int page;
        try {
            page = Integer.parseInt(pageStr);
        } catch (NumberFormatException nfe) {
            return false;
        }

        final ZLTextView view = (ZLTextView)fbreader.getCurrentView();

        if (page == 1) {
            view.gotoHome();
        } else {
            view.gotoPage(page);
        }
        fbreader.getViewWidget().reset();
        fbreader.getViewWidget().repaint();

        return true;
    }
}
