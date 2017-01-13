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
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.RadioGroup;

import org.benetech.android.R;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class MarginsPreference extends DialogPreference {

	private final ZLIntegerRangeOption myLeftOption;
	private final ZLIntegerRangeOption myRightOption;
	private final ZLIntegerRangeOption myTopOption;
	private final ZLIntegerRangeOption myBottomOption;
	private final ZLResource myResource;

	private RadioGroup myRadio;
	private int valueSelected;

	MarginsPreference(Context context, ZLResource resource, String resourceKey,
					  ZLIntegerRangeOption left, ZLIntegerRangeOption right,
					  ZLIntegerRangeOption top, ZLIntegerRangeOption bottom) {
		super(context, null);
		myLeftOption = left;
		myRightOption = right;
		myTopOption = top;
		myBottomOption = bottom;

		myResource = resource.getResource(resourceKey);
		final String title = myResource.getValue();
		setTitle(title);
		setDialogTitle(title);
		setDialogLayoutResource(R.layout.custom_dialog_preference);
		setSummary(getStringResForValue(myTopOption.getValue()));
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	@Override
	protected void onBindDialogView(View view) {
	myRadio = (RadioGroup) view.findViewById(R.id.radio_group);
		int value = myTopOption.getValue();
		if(value >= 0 && value <= 10){
			myRadio.check(R.id.radio_narrow);
		}else if(value > 10 && value <= 60){
			myRadio.check(R.id.radio_medium);
		}else if(value > 60){
			myRadio.check(R.id.radio_extrawide);
		}
		super.onBindDialogView(view);
	}

	private int getStringResForValue(int value){
		if(value >= 0 && value <= 10){
			return R.string.margins_small;
		} else if(value > 10 && value <= 60){
			return R.string.margins_medium;
		} else{// value > 60
			return R.string.margins_large;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			int radioButtonID = myRadio.getCheckedRadioButtonId();
			View radioButton = myRadio.findViewById(radioButtonID);
			switch(radioButton.getId()) {
				case R.id.radio_narrow:
					valueSelected = 10;
					break;
				case R.id.radio_medium:
					valueSelected = 60;
					break;
				case R.id.radio_extrawide:
					valueSelected = 120;
					break;
			}
			myLeftOption.setValue(valueSelected);
			myRightOption.setValue(valueSelected);
			myTopOption.setValue(valueSelected);
			myBottomOption.setValue(valueSelected);
			setSummary(getStringResForValue(valueSelected));
		}
	}

}
