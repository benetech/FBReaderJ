package org.geometerplus.android.fbreader.benetech;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.fbreader.library.ReadingListBook;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by animal@martus.org on 4/6/16.
 */
public class ReadingListFragment extends TitleListFragmentWithContextMenu {

    public static final String ARG_SHOULD_ADD_FAVORITES = "shouldAddFavorites";
    public static final String PARAM_READINGLIST_JSON= "PARAM_READINGLIST_JSON";

    private ReadingList readingList;

    private boolean shouldAddFavorites = false;

    public void setReadingList(ReadingList readingListToUse) {
        readingList = readingListToUse;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            shouldAddFavorites = savedInstanceState.getBoolean(ARG_SHOULD_ADD_FAVORITES, false);
            try {
                String jsonString = savedInstanceState.getString(PARAM_READINGLIST_JSON);
                JSONObject json = new JSONObject(jsonString);
                readingList = new ReadingList(json);
            } catch (Exception e){
                readingList = new ReadingList();
            }
        }
        else if(getArguments() != null){
            shouldAddFavorites = getArguments().getBoolean(ARG_SHOULD_ADD_FAVORITES, false);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        String readingListJson = readingList.toJSONObject().toString();
        savedInstanceState.putString(PARAM_READINGLIST_JSON, readingListJson);
        savedInstanceState.putBoolean(ARG_SHOULD_ADD_FAVORITES, shouldAddFavorites);
    }

    @Override
    protected void fillListAdapter() {
        ArrayList<ReadingListBook> readingListBooks = readingList.getReadingListBooks();
        for (int index = 0; index < readingListBooks.size(); ++index) {
            ReadingListBook readingListBook = readingListBooks.get(index);
            final String readingListBookTitle = readingListBook.getTitle();
            final String readingListBookAuthors = readingListBook.getAllAuthorsAsString();
            final int bookshareId = readingListBook.getBookId();
            bookRowItems.add(new ReadingListTitleItem(bookshareId, readingListBookTitle, readingListBookAuthors, readingListBook.getDateAdded()));
        }

        if(shouldAddFavorites) {
            ArrayList<Book> favoriteTitelsOnDevice = getFavoritesOnDevice();
            for (Book favoriteBookOnDevice : favoriteTitelsOnDevice) {
                bookRowItems.add(new DownloadedTitleListRowItem(favoriteBookOnDevice));
            }
        }

        sortListItems();
        setListAdapter(new ReadingListBooksAdapter(getActivity(), bookRowItems));
    }

    private ArrayList<Book> getFavoritesOnDevice() {
        final BooksDatabase db = BooksDatabase.Instance();
        final Map<Long,Book> savedBooksByBookId = new HashMap<>();
        ArrayList<Book> favoriteBooksOnDevice = new ArrayList<>();
        for (long id : db.loadFavoritesIds()) {
            Book book = savedBooksByBookId.get(id);
            if (book == null) {
                book = Book.getById(id);
                if (book != null && !book.File.exists()) {
                    book = null;
                }
            }
            if (book != null) {
                favoriteBooksOnDevice.add(book);
            }
        }

        return favoriteBooksOnDevice;
    }

    public class ReadingListBooksAdapter extends ArrayAdapter<AbstractTitleListRowItem> {

        public ReadingListBooksAdapter(Context context, List<AbstractTitleListRowItem> items) {
            super(context, R.layout.reading_list_book_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.reading_list_book_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.readingListBook = (TextView) convertView.findViewById(R.id.bookTitle);
                viewHolder.readingListBookAuthors = (TextView) convertView.findViewById(R.id.bookAuthorsLabel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            AbstractTitleListRowItem item = getItem(position);
            viewHolder.readingListBook.setText(item.getBookTitle());
            viewHolder.readingListBookAuthors.setText(item.getAuthors());

            int userValue = ZLTextStyleCollection.Instance().getBaseStyle().FontSizeOption.getValue();
            userValue = Math.max(userValue, 18); //these values come from ZLFontSizeListPreference
            userValue = Math.min(userValue, 30);
            viewHolder.readingListBook.setTextSize(userValue);

            double lowerValue = userValue / 1.5;
            lowerValue = Math.max(lowerValue, 12d);
            viewHolder.readingListBookAuthors.setTextSize(Math.round(lowerValue));


            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView readingListBook;
        public TextView readingListBookAuthors;
    }
}
