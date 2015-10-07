package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

/**
 * @author roms
 * Action to bring up full screen accessible menu
 */
public class ShowBookshareMenuAction extends FBAndroidAction {
    ShowBookshareMenuAction(FBReader baseActivity, FBReaderApp fbreader) {
    		super(baseActivity, fbreader);
    	}

    @Override
    protected void run(Object... params) {
        Intent intent = new Intent(BaseActivity.getApplicationContext(), Bookshare_Webservice_Login.class);
        BaseActivity.startActivity(intent);
    }
}
