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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.WindowManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class ZLStringPreference extends EditTextPreference {
	private String myValue;

	ZLStringPreference(Context context, ZLResource rootResource, String resourceKey) {
		super(context);

		ZLResource resource = rootResource.getResource(resourceKey);
		setTitle(resource.getValue());
	}

	protected void setValue(String value) {
		setSummary(value);
		setText(value);
		myValue = value;
	}

	protected final String getValue() {
		return myValue;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Dialog dialog = this.getDialog();
		if(dialog.isShowing()){
			dialog.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
			dialog.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		if (result) {
			setValue(getEditText().getText().toString());
		}
		super.onDialogClosed(result);
	}
}
