/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

abstract class ZLPreferenceActivity extends SettingsPreferencesActivity {
	public static String SCREEN_KEY = "screen";

	private Toolbar toolbar;
	private final HashMap<String,Screen> myScreenMap = new HashMap<String,Screen>();

	protected class Screen {
		public final ZLResource Resource;
		private final PreferenceScreen myScreen;

		private Screen(ZLResource root, String resourceKey) {
			Resource = root.getResource(resourceKey);
			myScreen = getPreferenceManager().createPreferenceScreen(ZLPreferenceActivity.this);
			myScreen.setTitle(Resource.getValue());
			myScreen.setSummary(Resource.getResource("summary").getValue());
		}

		public void setSummary(CharSequence summary) {
			myScreen.setSummary(summary);
		}

		public Screen createPreferenceScreen(String resourceKey) {
			Screen screen = new Screen(Resource, resourceKey);
			myScreen.addPreference(screen.myScreen);
			return screen;
		}

		public Preference addPreference(Preference preference) {
			myScreen.addPreference(preference);
			return preference;
		}

		public Preference addOption(ZLBooleanOption option, String resourceKey) {
			return addPreference(
				new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey)
			);
		}

		public Preference addOption(ZLStringOption option, String resourceKey) {
			return addPreference(
				new ZLStringOptionPreference(ZLPreferenceActivity.this, option, Resource, resourceKey)
			);
		}

		public Preference addOption(ZLColorOption option, String resourceKey) {
			return addPreference(
				new ZLColorPreference(ZLPreferenceActivity.this, Resource, resourceKey, option)
			);
		}

		public <T extends Enum<T>> Preference addOption(ZLEnumOption<T> option, String resourceKey) {
			return addPreference(
				new ZLEnumPreference<T>(ZLPreferenceActivity.this, option, Resource, resourceKey)
			);
		}
	}

	private PreferenceScreen myScreen;
	final ZLResource Resource;

	ZLPreferenceActivity(String resourceKey) {
		Resource = ZLResource.resource("dialog").getResource(resourceKey);
	}

	Screen createPreferenceScreen(String resourceKey) {
		final Screen screen = new Screen(Resource, resourceKey);
		myScreenMap.put(resourceKey, screen);
		myScreen.addPreference(screen.myScreen);
		return screen;
	}

	public Preference addPreference(Preference preference) {
		myScreen.addPreference((Preference)preference);
		return preference;
	}

	public Preference addOption(ZLBooleanOption option, String resourceKey) {
		ZLBooleanPreference preference =
			new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey);
		myScreen.addPreference(preference);
		return preference;
	}

	/*
	protected Category createCategory() {
		return new CategoryImpl(myScreen, Resource);
	}
	*/

	protected abstract void init(Intent intent);

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.settings);
		setupActionBar();

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		myScreen = getPreferenceManager().createPreferenceScreen(this);

		final Intent intent = getIntent();
		init(intent);
		final Screen screen = myScreenMap.get(intent.getStringExtra(SCREEN_KEY));
		setPreferenceScreen(screen != null ? screen.myScreen : myScreen);
	}

	private void setupActionBar() {
		toolbar = (Toolbar)findViewById(R.id.toolbar);
		//Toolbar will now take on default Action Bar characteristics
		toolbar.setTitle("Settings");

		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);

		// If the user has clicked on a preference screen, set up the screen
		if (preference instanceof PreferenceScreen) {
			setUpNestedScreen((PreferenceScreen) preference);
		}

		return false;
	}

	public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
		final Dialog dialog = preferenceScreen.getDialog();

		Toolbar bar;

		LinearLayout root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
		bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.sub_settings, root, false);
		root.addView(bar, 0); // insert at top

		bar.setTitle(preferenceScreen.getTitle());

		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.toolbar_overflow_menu, menu);

		changeLoginState(menu);

		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.refreshMenu();

		return true;
	}

	private void changeLoginState(Menu menu) {
		MenuItem loginMenuItem = menu.findItem(R.id.menu_item_login_bookshare);
		MenuItem logoutMenuItem = menu.findItem(R.id.menu_item_logout_bookshare);

		final boolean isLoggedintoBookshare = isLoggedintoBookshare();
		if(isLoggedintoBookshare) {
			String title = String.format(getString(R.string.signout_button_title_pattern), getCurrentLoggedUsername());
			logoutMenuItem.setTitle(title);
		}
		loginMenuItem.setVisible(!isLoggedintoBookshare);
		logoutMenuItem.setVisible(isLoggedintoBookshare);
	}

	protected boolean isLoggedintoBookshare() {
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(this);
		String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
		String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");
		if (username == null || username.isEmpty())
			return false;

		if (password == null || password.isEmpty())
			return false;

		return true;
	}

	protected String getCurrentLoggedUsername() {
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(this);
		String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
		return username;
	}
}
