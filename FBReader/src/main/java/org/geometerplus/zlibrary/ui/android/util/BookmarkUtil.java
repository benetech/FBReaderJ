package org.geometerplus.zlibrary.ui.android.util;

import android.content.Context;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Bookmark;

import java.util.List;

/**
 * Created by GreatArcantos on 12/19/2016.
 */

public class BookmarkUtil {

    public static void addBookmark(List<Bookmark> bookBookbarks, List<Bookmark> allBookmarks, Context context) {
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        final Bookmark bookmark = fbreader.addBookmark(20, true);
        if (bookBookbarks.contains(bookmark)) {
            final VoiceableDialog finishedDialog = new VoiceableDialog(context);
            String message = context.getResources().getString(R.string.message_bookmark_already_exists);
            finishedDialog.popup(message, 2000);

            return;
        }

        if (bookmark != null) {
            bookmark.save();
            bookBookbarks.add(0, bookmark);
            allBookmarks.add(0, bookmark);

            final VoiceableDialog finishedDialog = new VoiceableDialog(context);
            String msg = context.getResources().getString(R.string.bookmark_added, bookmark.getPageNumber());
            finishedDialog.popup(msg, 3000);
        }
    }
}
