package org.geometerplus.android.fbreader.network.bookshare.subscription;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Metadata_Bean;

/**
 * Created by animal@martus.org on 2/3/17.
 */

public interface BookDetailsFetechedResultsHandler {

    public void onResultsFetched(Bookshare_Metadata_Bean metadata_bean);
}
