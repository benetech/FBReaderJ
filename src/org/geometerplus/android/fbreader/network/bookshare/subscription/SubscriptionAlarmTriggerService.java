package org.geometerplus.android.fbreader.network.bookshare.subscription;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.FBReader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Used in order to trigger the subscription service if the application is left
 * on for more than a day. AlarmMangaer will trigger the service after 1 days
 * time (If application is not closed)
 * 
 * @author thushan
 * 
 */
public class SubscriptionAlarmTriggerService extends Service {

	private AlarmManager alarmManager;
	PendingIntent pendingIntent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent serviceIntent = new Intent(this,
				Bookshare_Subscription_Download_Service.class);

		long daysTimeinMillis = 1000 * 60 * 60 * 24;

		ArrayList<String> ids = intent
				.getStringArrayListExtra(FBReader.SUBSCRIBED_PERIODICAL_IDS_KEY);
		String downloadTypeStr = intent
				.getStringExtra(FBReader.AUTOMATIC_DOWNLOAD_TYPE_KEY);

		serviceIntent.putStringArrayListExtra(
				FBReader.SUBSCRIBED_PERIODICAL_IDS_KEY, ids);
		serviceIntent.putExtra(FBReader.AUTOMATIC_DOWNLOAD_TYPE_KEY,
				downloadTypeStr);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		pendingIntent = PendingIntent.getService(getBaseContext(), 0,
				serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), daysTimeinMillis, pendingIntent);

		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (alarmManager != null && pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
		}
	}
}
