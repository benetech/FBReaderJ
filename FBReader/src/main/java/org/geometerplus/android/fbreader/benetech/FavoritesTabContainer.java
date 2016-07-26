package org.geometerplus.android.fbreader.benetech;

import android.support.annotation.NonNull;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 4/25/16.
 */
public class FavoritesTabContainer extends AbstractReadingListTabContainer {

    @Override
    @NonNull
    protected String getReadingListName() {
        return "My Favorites";
    }

    @Override
    public int getTitleId() {
        return R.string.my_books_tab_favorites;
    }

    @Override
    public int getTabDrawableId() {
        return R.drawable.ic_favorite_white_24dp;
    }
}
