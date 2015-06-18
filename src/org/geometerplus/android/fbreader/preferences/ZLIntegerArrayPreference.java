/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

import android.content.Context;
import android.preference.ListPreference;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLIntegerArrayOption;

class ZLIntegerArrayPreference extends ListPreference {
	private final ZLIntegerArrayOption myOption;

	ZLIntegerArrayPreference(Context context, ZLResource resource, ZLIntegerArrayOption option) {
		super(context);
		myOption = option;
		setTitle(resource.getValue());
		String[] entries = new String[option.Values.length];
		for (int i = 0; i < entries.length; ++i) {
			entries[i] = ((Integer) option.Values[i]).toString();
			if (option.getValue() == option.Values[i]) {
				setValueIndex(i);
			}
		}
		setEntries(entries);
		setEntryValues(entries);
		setSummary(getValue());
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			setSummary(value);
			myOption.setValue(myOption.Values[findIndexOfValue(value)]);
		}
	}
}
