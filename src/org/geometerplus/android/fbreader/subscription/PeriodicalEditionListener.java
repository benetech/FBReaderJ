package org.geometerplus.android.fbreader.subscription;

import java.util.Vector;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;

public interface PeriodicalEditionListener {

	public void onPeriodicalEditionListResponse(
			Vector<Bookshare_Periodical_Edition_Bean> results);
}
