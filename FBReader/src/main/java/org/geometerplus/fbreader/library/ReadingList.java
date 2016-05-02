package org.geometerplus.fbreader.library;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 3/2/16.
 */
public class ReadingList {

    private Long id;
    private String readingListName;
    private ArrayList<ReadingListBook> readingListBooks;
    private JSONObject readingListJson;

    public ReadingList() {
        readingListBooks = new ArrayList<>();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

        JSONArray titlesArray = readingListJson.optJSONArray("titles");
        for (int index = 0; index < titlesArray.length(); ++index) {
            JSONObject titleJson = titlesArray.getJSONObject(index);
            readingListBooks.add(new ReadingListBook(titleJson));
        }
    }
}
