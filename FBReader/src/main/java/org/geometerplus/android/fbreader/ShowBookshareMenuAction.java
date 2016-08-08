package org.geometerplus.android.fbreader;

import android.content.Intent;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

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
        Intent intent = new Intent(getBaseActivity().getApplicationContext(), Bookshare_Webservice_Login.class);
        getBaseActivity().startActivityForResult(intent, FBReader.LOGIN_CODE);
    }
}
