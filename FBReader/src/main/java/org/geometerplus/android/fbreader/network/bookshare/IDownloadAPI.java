
package org.geometerplus.android.fbreader.network.bookshare;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.network.bookshare.subscription.SubscribedDbPeriodicalEntity;

public interface IDownloadAPI{

	public ArrayList<SubscribedDbPeriodicalEntity> isNewDownloadsAvailable(SubscribedDbPeriodicalEntity entity);
	public Bookshare_Periodical_Edition_Bean downloadPeriodical(SubscribedDbPeriodicalEntity entity);
}