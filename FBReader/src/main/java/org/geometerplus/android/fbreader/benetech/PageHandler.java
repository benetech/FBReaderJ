package org.geometerplus.android.fbreader.benetech;

/**
 * @author roms
 *         
 */
public interface PageHandler {

    /*
     * get the page number user is currently listening to/viewing
     */
    public String getCurrentPage();

    /*
     * get the last page number for this book
     */
    public String getLastPage();

    /*
     * go to the specified page in the book
     * return true if the page exists in the book, false otherwise
     */
    public boolean gotoPage(String page);

    /*
     * return true if all the page numbers are numeric
     */
    public boolean isNumeric();
}
