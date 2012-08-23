package org.geometerplus.android.fbreader.benetech;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * LastReadPageOfCurrentBook keeps up with the last read page of any book that the 
 * user is reading. The class heavily relies on the FBReader's ZLTextParagraphCursor.
 *  
 * @see {@link android.app.backup.BackupAgent}
 * */
public final class LastReadPageOfCurrentBook {
	private static final String LAST_PAGE_PREFS = "last_page_preferences";

	/**
	 * Where the last page of the current book is saved (referenced with a bookId).
	 * 
	 * @see {@link org.geometerplus.fbreader.library.Book}
     **/
	public static void saveLocationOfLastReadPage(Context zla) {

		// RETRIEVE FBReaderApp RESOURCES AND PREFERENCES EDITOR
		final FBReaderApp fbReader = (FBReaderApp) FBReaderApp.Instance();
		final Editor ed = zla.getSharedPreferences(LAST_PAGE_PREFS,
				Context.MODE_PRIVATE).edit();

		// RETRIEVE POSITION, PARAGRAPH INDEX AND BOOK ID
		final ZLTextPosition zlp = fbReader.Model.Book.getStoredPosition();
		final int paragraphIndex = zlp.getParagraphIndex();
		final long bookId = fbReader.Model.Book.getId();

		// COMMIT PARAGRAPH INDEX INTO PREFERENCES UNDER BOOK ID
		ed.putLong("lastBook", bookId);
		ed.putInt("" + bookId, paragraphIndex).commit();

	}

	/**
	 * The last page of the current book is loaded (using a bookId).
	 * 
	 * @see {@link org.geometerplus.fbreader.library.Book} */
	public static void loadLocationOfLastReadPage(Context zla) {

		// RETRIEVE APPLICATION FBReaderApp RESOURCES AND PREFERENCES
		final FBReaderApp fbReader = (FBReaderApp) FBReaderApp.Instance();
		final SharedPreferences sp = zla.getSharedPreferences(LAST_PAGE_PREFS,Context.MODE_PRIVATE);

		// RETRIEVE BOOK
		final long lastBook = sp.getLong("lastBook", -1);
		Book book = Book.getById(lastBook);

		// RETRIEVE BOOK MODEL
		final FBView view = fbReader.getTextView();
		final ZLTextModel model = view.getModel();

		if (book == null && fbReader.Model != null)
			book = fbReader.Model.Book;

		if (book == null) return; // TODO SET UP THE BOOK HERE IF NEEDED

		// RETRIEVE PARAGRAPH INDEX
		final long bookId = book.getId();
		final int paragraphIndex = sp.getInt("" + bookId, -1);

		// VALIDATE NEEDED DATA IS AVAILABLE
		if (model == null || paragraphIndex == -1) return;

		// RETRIEVE PARAGRAPH/WORD CURSORS FOR LAST PAGE POSITION
		final ZLTextParagraphCursor parag = ZLTextParagraphCursor.cursor(model,paragraphIndex);
		final ZLTextWordCursor cursor = new ZLTextWordCursor(parag);

		if (cursor.isNull()) return;

		// RETRIEVE BOOK AND OPEN TO ITS LAST PAGE POSITION
		fbReader.openBook(book, new Bookmark(fbReader.Model.Book, 
			view.getModel().getId(), cursor, 0, false));
	}
}