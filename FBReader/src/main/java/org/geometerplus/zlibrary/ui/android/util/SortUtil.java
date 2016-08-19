package org.geometerplus.zlibrary.ui.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.AbstractTitleListRowItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Miguel Villalobos on 8/10/2016.
 */

public class SortUtil {
    public static final String SORT_BY_PREF = "SORT_BY_PREF";
    private static SORT_ORDER currentSortOrder = null;
    private static final ArrayList<SortChangesListener> registeredListeners = new ArrayList<>();
    public static SORT_ORDER getCurrentSortOrder(){
        if(currentSortOrder == null){
            Log.e("SORT ERROR", "Sort Util not initialized");
            return SORT_ORDER.SORT_BY_AUTHOR;
        }
        return currentSortOrder;
    }

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
                case 2:
                    return R.id.sort_by_date;
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
                case R.id.sort_by_date:
                    return SORT_BY_DATE;
                default:
                    return SORT_BY_TITLE;
            }
        }

    }

    static public SORT_ORDER initSortOrderFromPreference(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        currentSortOrder = SORT_ORDER.fromInt(prefs.getInt(SORT_BY_PREF, 0));
        notifyListeners();
        return currentSortOrder;
    }

    static public void saveSortPreference(Context context, SORT_ORDER sortOrder){
        currentSortOrder = sortOrder;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putInt(SORT_BY_PREF, sortOrder.getValue())
                .commit();
        notifyListeners();
    }

    static public void notifyListeners(){
        for(SortChangesListener listener : registeredListeners){
            listener.onSortChanged();
        }
    }

    static public Comparator<AbstractTitleListRowItem> getComparator(){
        return mComparator;
    }

    private static final Comparator<AbstractTitleListRowItem> mComparator = new Comparator<AbstractTitleListRowItem>() {
        @Override
        public int compare(AbstractTitleListRowItem item1, AbstractTitleListRowItem item2) {
            int ans = 0;
            switch(currentSortOrder){
                case SORT_BY_AUTHOR:
                    ans = item1.getAuthors().compareTo(item2.getAuthors());
                    break;
                case SORT_BY_DATE:
                    ans = compareDates(item1.getCompareDate(), item2.getCompareDate());
                    break;
                case SORT_BY_TITLE:
                    ans = item1.getBookTitle().compareTo(item2.getBookTitle());
                    break;
            }
            return ans;
        }
        private int compareDates(Date date1, Date date2){
            if(date1 != null && date2 != null){
                return -1 * date1.compareTo(date2);
            }
            else if(date1 != null){
                return 1;
            }
            else if(date2 != null){
                return -1;
            }
            else return 0;
        }
    };

    public static void registerForSortChanges(SortChangesListener listener){
        registeredListeners.add(listener);
    }
    public static void unregisterForSortChanges(SortChangesListener listener){
        registeredListeners.remove(listener);
    }

    public interface SortChangesListener {
        void onSortChanged();
    }

}
