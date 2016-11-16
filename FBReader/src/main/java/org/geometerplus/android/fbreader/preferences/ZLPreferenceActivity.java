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

import java.util.HashMap;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.*;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.benetech.android.R;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class ZLPreferenceActivity extends SettingsPreferencesActivity {
	public static String SCREEN_KEY = "screen";


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

	protected abstract void setupActionBar();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setupActionBar();

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		myScreen = getPreferenceManager().createPreferenceScreen(this);

		final Intent intent = getIntent();
		init(intent);
		final Screen screen = myScreenMap.get(intent.getStringExtra(SCREEN_KEY));
		setPreferenceScreen(screen != null ? screen.myScreen : myScreen);
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
		if(dialog != null) {
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
	}


}
