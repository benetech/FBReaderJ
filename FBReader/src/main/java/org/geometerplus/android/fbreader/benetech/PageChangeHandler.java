package org.geometerplus.android.fbreader.benetech;

import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;

/**
 * Created by animal@martus.org on 5/26/16.
 */

public class PageChangeHandler implements ViewPager.OnPageChangeListener {

    private SharedPreferences sharedPreferences;
    private String preferenceTag;

    public PageChangeHandler(SharedPreferences sharedPreferencesToUse, String preferenceTagToUse) {
        sharedPreferences = sharedPreferencesToUse;
        preferenceTag = preferenceTagToUse;
    }

    @Override
    public void onPageSelected(int position) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(preferenceTag, position);
        prefsEditor.commit();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
}
