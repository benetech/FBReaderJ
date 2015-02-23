/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import junit.framework.Assert;

import org.geometerplus.android.fbreader.network.bookshare.BookshareDeveloperKey;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.test.ActivityInstrumentationTestCase2;

public class FBReaderTest extends ActivityInstrumentationTestCase2<FBReader> {
	private FBReader mActivity;
	
	public FBReaderTest() {
		super(FBReader.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Launches the activity.
		mActivity = getActivity();
	}
	
	public void testBookshareDeveloperKey_initialized() {
		Assert.assertFalse("Developer key is not specified.", 
				"".equals(BookshareDeveloperKey.DEVELOPER_KEY));
		Assert.assertFalse("Bugsense key is not specified.", 
				"".equals(BookshareDeveloperKey.BUGSENSE_KEY));
	}
	
	public void testOnCreate_enablesRequiredActions() {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		Assert.assertTrue(fbReader.isActionEnabled(ActionCode.SPEAK));
		Assert.assertTrue(fbReader.isActionEnabled(ActionCode.
				SET_SCREEN_ORIENTATION_LANDSCAPE));		
	}
}
