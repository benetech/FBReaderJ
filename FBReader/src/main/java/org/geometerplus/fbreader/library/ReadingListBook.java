package org.geometerplus.fbreader.library;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class ReadingListBook {

    private int bookId;
    private String title;
    private ArrayList<String> authors;
    private static final String JSON_CODE_TITLE = "title";
    private static final String JSON_CODE_AUTHORS = "authors";
    private static final String JSON_CODE_BOOKSHARE_ID = "bookshareId";
    private static final String JSON_CODE_LAST_NAME = "lastName";
    private static final String JSON_CODE_FIRST_NAME = "firstName";

    public ReadingListBook(JSONObject jsonToFillFrom) throws Exception {
        bookId = jsonToFillFrom.getInt(JSON_CODE_BOOKSHARE_ID);
        title = jsonToFillFrom.optString(JSON_CODE_TITLE);

        fillAuthorsList(jsonToFillFrom.optJSONArray(JSON_CODE_AUTHORS));
    }

    private void fillAuthorsList(JSONArray authorsAsJson) throws Exception {
        if (authorsAsJson == null)
            return;

        authors = new ArrayList<>();
        for (int index = 0; index < authorsAsJson.length(); ++index) {
            JSONObject authorAsJsonObject = authorsAsJson.getJSONObject(index);
            String surname = authorAsJsonObject.optString(JSON_CODE_LAST_NAME);
            String firstName = authorAsJsonObject.optString(JSON_CODE_FIRST_NAME);
            final String SINGLE_SPACE = " ";
            authors.add(firstName + SINGLE_SPACE + surname);
        }
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAllAuthorsAsString() {
        return getAllAuthorsAsString(this.authors);
    }

    @NonNull
    public static String getAllAuthorsAsString(List<String> authors) {
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < authors.size(); ++index) {
            if (index > 0)
                buffer.append(", ");

            buffer.append(authors.get(index));
        }

        return buffer.toString();
    }
}
