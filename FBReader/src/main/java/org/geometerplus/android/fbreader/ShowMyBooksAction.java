package org.geometerplus.android.fbreader;

import android.content.Intent;

import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class ShowMyBooksAction extends FBAndroidAction{

    public ShowMyBooksAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object ... params) {
        final BookModel model = Reader.Model;
        Intent intent = new Intent(getBaseActivity().getApplicationContext(), LibraryActivity.class);
        if (model != null && model.Book != null) {
            intent.putExtra(LibraryActivity.SELECTED_BOOK_PATH_KEY, model.Book.File.getPath());
        }
        getBaseActivity().startActivity(intent);
    }
}
