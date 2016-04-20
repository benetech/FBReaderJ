package org.geometerplus.android.fbreader;

import android.content.Intent;

import org.geometerplus.android.fbreader.benetech.MyBooksActivity;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * Created by animal@martus.org on 4/4/16.
 */
public class ShowMyBooksAction extends FBAndroidAction {

    public ShowMyBooksAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object ... params) {
        Intent intent = new Intent(getBaseActivity().getApplicationContext(), MyBooksActivity.class);
        getBaseActivity().startActivity(intent);
    }
}
