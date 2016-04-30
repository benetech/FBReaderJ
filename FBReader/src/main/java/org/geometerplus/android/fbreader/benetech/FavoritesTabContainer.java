package org.geometerplus.android.fbreader.benetech;

import android.support.annotation.NonNull;

/**
 * Created by animal@martus.org on 4/25/16.
 */
public class FavoritesTabContainer extends AbstractReadingListTabContainer {

    @Override
    @NonNull
    protected String getReadingListName() {
        return "My Favorites";
    }
}
