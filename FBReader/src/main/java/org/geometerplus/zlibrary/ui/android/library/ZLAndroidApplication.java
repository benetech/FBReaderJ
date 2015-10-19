/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.library;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.benetech.android.R;
import org.geometerplus.zlibrary.core.sqliteconfig.ZLSQLiteConfig;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

public class ZLAndroidApplication extends Application {
	public ZLAndroidApplicationWindow myMainWindow;
	private Tracker mTracker;


	@Override
	public void onCreate() {
		super.onCreate();
		new ZLSQLiteConfig(this);
		new ZLAndroidImageManager();
		new ZLAndroidLibrary(this);
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		MultiDex.install(this);
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}

	public void trackGoogleAnalyticsEvent(String eventCategory, String eventAction, String eventLabel) {
		getDefaultTracker().send(new HitBuilders.EventBuilder()
				.setCategory(eventCategory)
				.setAction(eventAction)
				.setLabel(eventLabel)
				.build());

	}

	public void stopTracker(Activity activity) {
		GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
	}

	public void startTracker(Activity activity) {
		GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
	}

}
