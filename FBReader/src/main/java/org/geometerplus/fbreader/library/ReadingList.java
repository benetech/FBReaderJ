package org.geometerplus.fbreader.library;

import android.util.Log;

import org.geometerplus.android.fbreader.PermissionConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by animal@martus.org on 3/2/16.
 */
public class ReadingList {

    private static final String JSON_CODE_ID = "id";
    private static final String JSON_CODE_NAME = "readingListName";
    private static final String JSON_CODE_BOOKSJSON = "readingListJson";
    private static final String JSON_CODE_TITLES = "titles";
    private static final String JSON_BOOKSHARE_ID = "readingListId";


    private Long id;
    private String readingListName;

    private String bookshareId;
    private ArrayList<ReadingListBook> readingListBooks;
    private Set<String> allows;
    private JSONObject readingListJson;

    public ReadingList() {
        readingListBooks = new ArrayList<>();
    }

    public ReadingList(JSONObject jsonToLoadFrom) throws Exception{
        id = jsonToLoadFrom.getLong(JSON_CODE_ID);
        readingListName = jsonToLoadFrom.optString(JSON_CODE_NAME);
        readingListBooks = new ArrayList<>();
        setReadingListJson(jsonToLoadFrom.optString(JSON_CODE_BOOKSJSON));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getBookshareId() {
        return bookshareId;
    }

    public void setReadingListName(String readingListName) {
        this.readingListName = readingListName;
    }

    public String getReadingListName() {
        return readingListName;
    }

    public int getBookCount() {
        return readingListBooks.size();
    }

    public void addBook(ReadingListBook readingListBookToAdd) {
        if (!readingListBooks.contains(readingListBookToAdd))
            readingListBooks.add(readingListBookToAdd);
    }

    public ArrayList<ReadingListBook> getReadingListBooks() {
        return new ArrayList<>(readingListBooks);
    }

    public void setReadingListJson(String readingListJsonAsString) throws Exception {
        readingListJson = new JSONObject(readingListJsonAsString);
        bookshareId = readingListJson.optString(JSON_BOOKSHARE_ID);
        JSONArray titlesArray = readingListJson.optJSONArray(JSON_CODE_TITLES);
        for (int index = 0; index < titlesArray.length(); ++index) {
            JSONObject titleJson = titlesArray.getJSONObject(index);
            readingListBooks.add(new ReadingListBook(titleJson));
        }
        allows = new HashSet<>();
        JSONArray allowsArray = readingListJson.optJSONArray(PermissionConstants.JSON_CODE_ALLOWS);
        for (int index = 0; index < allowsArray.length(); ++index) {
            allows.add(allowsArray.get(0).toString());
        }

    }

    public JSONObject toJSONObject(){
        JSONObject ans = new JSONObject();
        try {
            ans.put(JSON_CODE_ID, id);
            ans.put(JSON_CODE_NAME, readingListName);
            if(readingListJson != null) {
                ans.put(JSON_CODE_BOOKSJSON, readingListJson);
            }
            else {
                JSONObject json = new JSONObject();
                JSONArray titles = new JSONArray();
                for(ReadingListBook title : readingListBooks){
                    titles.put(title.toJSONObject());
                }
                json.put(JSON_CODE_TITLES, titles);
                ans.put(JSON_CODE_BOOKSJSON, json.toString());
            }
        } catch (JSONException e){
            Log.e("JSONEXCEPTION", "error creating json representation of ReadingList", e);


        }
        return ans;
    }
}
