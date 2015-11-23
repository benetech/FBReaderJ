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

package org.geometerplus.android.fbreader;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class SelectionPopup extends ButtonsPopupPanel {
	final static String ID = "SelectionPopup";

	SelectionPopup(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControlPanel(FBReader activity, ViewGroup root, PopupWindow.Location location) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}

		myWindow = new PopupWindow(activity, root, location, false);

        addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.selection_copy);
        addButton(ActionCode.SELECTION_SHARE, true, R.drawable.selection_share);
        addButton(ActionCode.SELECTION_TRANSLATE, true, R.drawable.selection_translate);
        addButton(ActionCode.SELECTION_BOOKMARK, true, R.drawable.selection_bookmark);
        addButton(ActionCode.SELECTION_CLEAR, true, R.drawable.selection_close);
    }
    
    public void move(int selectionStartY, int selectionEndY) {
		if (myWindow == null) {
			return;
		}

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
		);

        myWindow.setLayoutParams(layoutParams);
    }
}
