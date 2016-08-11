package org.geometerplus.zlibrary.ui.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.AbstractTitleListRowItem;

import java.util.Comparator;

/**
 * Created by Miguel Villalobos on 8/10/2016.
 */

public class SortUtil {
    public static final String SORT_BY_PREF = "SORT_BY_PREF";

    public enum SORT_ORDER {

        SORT_BY_TITLE(0),
        SORT_BY_AUTHOR(1),
        SORT_BY_DATE(2);

        private int value;

        SORT_ORDER (int val){
            value = val;
        }

        public int getValue(){
            return value;
        }

        public int getId(){
            switch (value){
                case 1:
                    return R.id.sort_by_author;
                default:
                    return R.id.sort_by_title;
            }
        }
        static public SORT_ORDER fromInt(int i){
            switch (i){
                case 2:
                    return SORT_BY_DATE;
                case 1:
                    return SORT_BY_AUTHOR;
                default:
                    return SORT_BY_TITLE;
            }
        }
        static public SORT_ORDER fromId(int i){
            switch (i){
                case R.id.sort_by_author:
                    return SORT_BY_AUTHOR;
                default:
                    return SORT_BY_TITLE;
            }
        }

    }

    static public SORT_ORDER getSortOrderPreference(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return SORT_ORDER.fromInt(prefs.getInt(SORT_BY_PREF, 0));
    }

    static public void saveSortPreference(Context context, SORT_ORDER sortOrder){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putInt(SORT_BY_PREF, sortOrder.getValue())
                .commit();
    }

    static public Comparator<AbstractTitleListRowItem> getComparator(Context context){
        final SORT_ORDER order = SortUtil.getSortOrderPreference(context);
        return new Comparator<AbstractTitleListRowItem>() {
            @Override
            public int compare(AbstractTitleListRowItem item1, AbstractTitleListRowItem item2) {
                switch(order){
                    case SORT_BY_AUTHOR:
                        return item1.getAuthors().compareTo(item2.getAuthors());
                    case SORT_BY_DATE:
                    default :
                        return item1.getBookTitle().compareTo(item2.getBookTitle());
                }
            }
        };
    }



}
