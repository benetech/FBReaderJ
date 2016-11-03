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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
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
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	@Override
	protected void onBindDialogView(View view) {
	myRadio = (RadioGroup) view.findViewById(R.id.radio_group);
		int value = myTopOption.getValue();
		if(value == 0){
			myRadio.check(R.id.radio_none);
		}else if(value > 0 && value <= 7){
			myRadio.check(R.id.radio_narrow);
		}else if(value > 7 && value <= 15){
			myRadio.check(R.id.radio_medium);
		}else if(value > 15 && value <= 22){
			myRadio.check(R.id.radio_wide);
		}else if(value > 22){
			myRadio.check(R.id.radio_extrawide);
		}
		super.onBindDialogView(view);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			int radioButtonID = myRadio.getCheckedRadioButtonId();
			View radioButton = myRadio.findViewById(radioButtonID);
			switch(radioButton.getId()) {
				case R.id.radio_none:
					valueSelected = 0;
					break;
				case R.id.radio_narrow:
					valueSelected = 7;
					break;
				case R.id.radio_medium:
					valueSelected = 15;
					break;
				case R.id.radio_wide:
					valueSelected = 22;
					break;
				case R.id.radio_extrawide:
					valueSelected = 30;
					break;
			}

			myLeftOption.setValue(valueSelected);
			myRightOption.setValue(valueSelected);
			myTopOption.setValue(valueSelected);
			myBottomOption.setValue(valueSelected);
		}
	}

}
