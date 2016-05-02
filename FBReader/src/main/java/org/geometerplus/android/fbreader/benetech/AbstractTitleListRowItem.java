package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

/**
 * Created by animal@martus.org on 5/2/16.
 */
abstract public class AbstractTitleListRowItem {

    abstract public String getBookFilePath();

    abstract public ZLFile getBookZlFile();

    abstract public Book getBook();

    abstract public String getBookTitle();

    abstract public String getAuthors();

    abstract public boolean isDownloadedBook();

    abstract public int getBookId();
}
