
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;
import org.geometerplus.fbreader.fbreader.FBReaderApp.AutomaticDownloadType;

/**
 * Automatic download API
 * 
 * @author thushan
 * 
 */
public interface IPeriodicalDownloadAPI {

	public boolean downloadPeriodical(Bookshare_Edition_Metadata_Bean bean);

	public void getUpdates(AutomaticDownloadType downType, String id);

	public Bookshare_Edition_Metadata_Bean getDetails(
			Bookshare_Periodical_Edition_Bean bean);
}