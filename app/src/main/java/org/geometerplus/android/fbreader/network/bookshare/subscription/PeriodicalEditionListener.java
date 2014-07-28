
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.util.ArrayList;
import java.util.Vector;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Bean;

/**
 * @author thushan
 * 
 */
public interface PeriodicalEditionListener {

	public void onPeriodicalEditionListResponse(
			ArrayList<Bookshare_Periodical_Edition_Bean> results);
}