package org.geometerplus.android.fbreader.subscription;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class PeriodicalSharedPrefs {

	public static final String SHARED_PREF_NAME = "subscribed_periodicals";
	public static final String PREF_KEY = "periodicals_ids";
	
	/*public static Set<String> loadSubscribedPeriodicals(Context context){
		SharedPreferences periodicalIdPref=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
		//Set<String> periodicalIds=periodicalIdPref.getStringSet(PeriodicalSharedPrefs.SHARED_PREF_NAME, null);
		return periodicalIds;		
	}*/

	public static void saveSubscribedPeriodicals(Context context, Set<String> periodicalIds){
		SharedPreferences mSubscribePref=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = mSubscribePref.edit();
		//edit.putString(PeriodicalSharedPrefs.SHARED_PREF_NAME, periodicalIds);
		edit.commit();
	}
}
