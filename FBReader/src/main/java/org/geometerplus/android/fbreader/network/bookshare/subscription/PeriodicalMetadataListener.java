
package org.geometerplus.android.fbreader.network.bookshare.subscription;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;

/**
 * @author thushan
 * 
 */
public interface PeriodicalMetadataListener {

	public void onPeriodicalMetadataResponse(
			Bookshare_Edition_Metadata_Bean result);
}