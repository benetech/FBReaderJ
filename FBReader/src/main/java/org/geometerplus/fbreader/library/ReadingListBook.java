package org.geometerplus.fbreader.library;

import org.json.JSONObject;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class ReadingListBook {

    private int bookId;
    private String title;
    private static final String JSON_CODE_TITLE = "title";
    private static final String JSON_CODE_BOOKSHARE_ID = "bookshareId";

    public ReadingListBook(JSONObject jsonToFillFrom) throws Exception {
        bookId = jsonToFillFrom.getInt(JSON_CODE_BOOKSHARE_ID);
        title = jsonToFillFrom.optString(JSON_CODE_TITLE);

        System.out.println(jsonToFillFrom);
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }
}
