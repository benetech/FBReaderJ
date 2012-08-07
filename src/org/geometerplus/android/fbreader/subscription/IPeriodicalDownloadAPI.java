package org.geometerplus.android.fbreader.subscription;

import java.util.Vector;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;

public interface IPeriodicalDownloadAPI {

	public boolean downloadPeriodical(Bookshare_Edition_Metadata_Bean bean);
	public Vector<Bookshare_Periodical_Edition_Bean> getUpdates(String id);
	public Bookshare_Edition_Metadata_Bean getDetails(Bookshare_Periodical_Edition_Bean bean);
}
