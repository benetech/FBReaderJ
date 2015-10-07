package org.geometerplus.android.fbreader;

import java.io.File;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

/**
 * @author roms
 * Action to bring up help
 */
public class ShowHelpAction extends FBAndroidAction {
    ShowHelpAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        File helpFile = new File(Paths.BooksDirectoryOption().getValue(), FBReader.USER_GUIDE_FILE);
        BaseActivity.startActivity(
            new Intent(BaseActivity.getApplicationContext(), FBReader.class)
                    .setAction(Intent.ACTION_VIEW)
                    .putExtra(FBReader.BOOK_PATH_KEY, helpFile.getPath())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
    }
}
