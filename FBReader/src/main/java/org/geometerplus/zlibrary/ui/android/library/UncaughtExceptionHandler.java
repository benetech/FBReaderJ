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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;

import android.app.Activity;
import android.content.*;
import android.os.Process;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.bugsense.trace.BugSenseHandler;

public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
	private final Context myContext;

	public UncaughtExceptionHandler(Context context) {
		myContext = context;
	}

	public void uncaughtException(Thread thread, Throwable exception) {
        boolean justShowAlert = false;
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		System.err.println(stackTrace);

        if (exception instanceof Exception) {
            SharedPreferences userInfo = PreferenceManager.getDefaultSharedPreferences(myContext);
            String user = userInfo.getString(Bookshare_Webservice_Login.USER, "");

            HashMap<String, String> extraData = new HashMap<String,String>();
            // only report email if user is logged into bookshare
            if (null != user && user.length() > 1) {
                extraData.put("email", user);
            }
            final Book currentBook = Library.getRecentBook();
            if (null != currentBook) {
                extraData.put("book", currentBook.getTitle());
            }
	        BugSenseHandler.addCrashExtraMap(extraData);
            //BugSenseHandler.log("FBR", extraData, (Exception)exception);
            justShowAlert = true;
        }
        
        Intent intent = new Intent(
            "android.fbreader.action.CRASH",
            new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
        );
        try {
            myContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(myContext, BugReportActivity.class);
            intent.putExtra(BugReportActivity.STACKTRACE, stackTrace.toString());
            intent.putExtra(BugReportActivity.JUST_SHOW_ALERT, true);
            myContext.startActivity(intent);
        }

		if (myContext instanceof Activity) {
			((Activity)myContext).finish();
		}

		Process.killProcess(Process.myPid());
		System.exit(10);
	}
}
