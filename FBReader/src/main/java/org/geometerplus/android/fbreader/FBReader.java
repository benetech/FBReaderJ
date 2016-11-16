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

package org.geometerplus.android.fbreader;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.analytics.GoogleAnalytics;

import org.apache.commons.io.FileUtils;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.benetech.FBReaderWithNavigationBar;
import org.geometerplus.android.fbreader.library.KillerCallback;
import org.geometerplus.android.fbreader.network.bookshare.BookshareDeveloperKey;
import org.geometerplus.android.fbreader.network.bookshare.subscription.BooksharePeriodicalDataSource;
import org.geometerplus.android.fbreader.network.bookshare.subscription.MainPeriodicalDownloadService;
import org.geometerplus.android.fbreader.network.bookshare.subscription.PeriodicalEntity;
import org.geometerplus.android.fbreader.network.bookshare.subscription.PeriodicalsSQLiteHelper;
import org.geometerplus.android.fbreader.tips.TipsActivity;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.tips.TipsManager;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivityforActionBar;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.util.SortUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FBReader extends ZLAndroidActivityforActionBar {
	public static final String PREFS_USER_MANUAL_VERSION = "bks_userManualVersion";
    public static final String USER_GUIDE_FILE = "User-Guide.epub";
	public static final String FONTS_ASSET_FOLDER = "fonts";
	public static final String MINI_HELP_FILE_NAME = "MiniHelp.en.fb2";
    public static final String LOG_LABEL = "GoRead";

	protected final static int REPAINT_CODE = 1;
	protected final static int CANCEL_CODE = 2;
	protected final static int AUTO_SPEAK_CODE = 3;
	protected final static int LOGIN_CODE = 4;

	private int myFullScreenFlag;
	//private InputAccess inputAccess = new InputAccess(this, true);

	private BooksharePeriodicalDataSource dataSource;

    public static final String SUBSCRIBED_PERIODICAL_IDS_KEY = "subscribed_periodical_ids";
    public static final String AUTOMATIC_DOWNLOAD_TYPE_KEY = "download_type";

	protected boolean isFirstTimeRunningThisVersion = false;

	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					int index = 0;
					while (index < myPluginActions.size()) {
						fbReader.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						fbReader.addAction(
							PLUGIN_ACTION_PREFIX + index++,
							new RunPluginAction(FBReader.this, fbReader, info.getId())
						);
					}
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		BookshareDeveloperKey.initialize(getApplicationContext());
		Log.i(LOG_LABEL,BookshareDeveloperKey.BUGSENSE_KEY);
		Log.i(LOG_LABEL,BookshareDeveloperKey.DEVELOPER_KEY);
		Log.i(LOG_LABEL,String.valueOf(BookshareDeveloperKey.OPT_OUT_GOOGLE_ANALYTICS));
		BugSenseHandler.initAndStartSession(this, BookshareDeveloperKey.BUGSENSE_KEY);

        //todo:
		//inputAccess.onCreate();
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
        
        accessibilityManager = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
		myFullScreenFlag = zlibrary.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		if (fbReader.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(fbReader);
		}
		if (fbReader.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(fbReader);
		}
		if (fbReader.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(fbReader);
		}

        fbReader.addAction(ActionCode.BOOKSHARE, new ShowBookshareMenuAction(this, fbReader));
        fbReader.addAction(ActionCode.ACCESSIBLE_NAVIGATION, new ShowAccessiblePageNavigateAction(this, fbReader));
        fbReader.addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this, fbReader));
        fbReader.addAction(ActionCode.SHOW_ACCESSIBILITY_SETTINGS, new ShowAccessibilitySettingsAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_MY_BOOKS, new ShowMyBooksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, fbReader));
		fbReader.addAction(ActionCode.SYNC_WITH_BOOKSHARE, new SyncReadingListsWithBookshareAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, fbReader));
		
		fbReader.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));

		fbReader.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, fbReader));

		fbReader.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, fbReader));

		fbReader.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, fbReader));

		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			fbReader.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, fbReader, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}

		fbReader.addAction(ActionCode.ABOUT_GOREAD, new ShowAboutGoReadAction(this, fbReader));
		fbReader.addAction(ActionCode.LOGOUT_BOOKSHARE, new LogoutFromBookshareAction(this, fbReader));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int currentVersion = zlibrary.getVersionCode();
        int userManualVersion = prefs.getInt(PREFS_USER_MANUAL_VERSION, 0);

        if (userManualVersion != currentVersion) {
			isFirstTimeRunningThisVersion = true;
            copyManual();
			//userManualVersion is ultimately a packageversion check. Should be good placing this here.
			copyFontsFromAssetsIntoPhoneStorage();
			SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putInt(PREFS_USER_MANUAL_VERSION, currentVersion);
            prefsEditor.commit();
        }
		SortUtil.initSortOrderFromPreference(getApplicationContext());
        //Activating subscription download
        //activateSubscriptionDownload(prefs);
	}

    public void activateSubscriptionDownload(SharedPreferences prefs) {

        PeriodicalsSQLiteHelper dbHelper = new PeriodicalsSQLiteHelper(getApplicationContext());
        SQLiteDatabase periodicalDb = dbHelper.getWritableDatabase();
        dataSource = BooksharePeriodicalDataSource
                .getInstance(getApplicationContext());

        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");
        boolean OM = prefs.getBoolean("isOM", false);

        ArrayList<String> ids = getSubscribedPeriodicalIds(periodicalDb);

        if (!OM && username != null && password != null
                && !TextUtils.isEmpty(username)) {

            Intent downloadService = new Intent(FBReader.this, MainPeriodicalDownloadService.class);
            downloadService.putStringArrayListExtra(SUBSCRIBED_PERIODICAL_IDS_KEY, ids);
            startService(downloadService);

        }
    }

    private ArrayList<String> getSubscribedPeriodicalIds(SQLiteDatabase db) {
    		ArrayList<String> ids = new ArrayList<String>();
    		final ArrayList<PeriodicalEntity> entities = new ArrayList<PeriodicalEntity>();
    		entities.addAll(dataSource.getAllEntities(db,
					PeriodicalsSQLiteHelper.TABLE_SUBSCRIBED_PERIODICALS));
    		for (PeriodicalEntity entity : entities) {
    			ids.add(entity.getId());
    		}
    		return ids;
    	}

	@Override
	protected void onNewIntent(Intent intent) {
		final Uri data = intent.getData();
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())
					&& data != null && "fbreader-action".equals(data.getScheme())) {
			fbReader.doAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup)fbReader.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					fbReader.TextSearchPatternOption.setValue(pattern);
					if (fbReader.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								fbReader.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
            if (accessibilityManager.isEnabled()) {
                ZLApplication.Instance().doAction(ActionCode.SPEAK);
            }
		}
	}

	@Override
	public void onStart() {
		super.onStart();
        GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(BookshareDeveloperKey.OPT_OUT_GOOGLE_ANALYTICS);
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();

		final int fullScreenFlag =
			zlibrary.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, getClass()));
		}

		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ViewGroup root = (ViewGroup)findViewById(R.id.root_view);
		((PopupPanel)fbReader.getPopupById(TextSearchPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
		((PopupPanel)fbReader.getPopupById(NavigationPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
		((PopupPanel)fbReader.getPopupById(SelectionPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Floating);

		synchronized (myPluginActions) {
			int index = 0;
			while (index < myPluginActions.size()) {
				fbReader.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(
			new Intent(PluginApi.ACTION_REGISTER),
			null,
			myPluginInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);

		final TipsManager manager = TipsManager.Instance();
		switch (manager.requiredAction()) {
			case Initialize:
				startActivity(new Intent(TipsActivity.INITIALIZE_ACTION, null, this, TipsActivity.class));
				break;
			case Show:
				startActivity(new Intent(TipsActivity.SHOW_TIP_ACTION, null, this, TipsActivity.class));
				break;
			case Download:
				manager.startDownloading();
				break;
			case None:
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			sendBroadcast(new Intent(getApplicationContext(), KillerCallback.class));
		} catch (Throwable t) {
		}
		PopupPanel.restoreVisibilities(FBReaderApp.Instance());
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
        if (!accessibilityManager.isEnabled()) {
            setApplicationTitle();
        }
	}

	@Override
	public void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(FBReaderApp.Instance(), this);
		super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final FBReaderApp.PopupPanel popup = fbreader.getActivePopup();
		fbreader.hideActivePopup();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					fbreader.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	public void showSelectionPanel() {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ZLTextView view = fbReader.getTextView();
		((SelectionPopup)fbReader.getPopupById(SelectionPopup.ID))
			.move(view.getSelectionStartY(), view.getSelectionEndY());
		fbReader.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final FBReaderApp.PopupPanel popup = fbReader.getActivePopup();
		if (popup != null && popup.getId().equals(SelectionPopup.ID)) {
			FBReaderApp.Instance().hideActivePopup();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        if (resultCode == FBReaderWithNavigationBar.SPEAK_BACK_PRESSED) {
            //fbreader.doAction(ActionCode.SHOW_CANCEL_MENU);
            fbreader.closeWindow();
            return;
        }
		switch (requestCode) {
			case REPAINT_CODE:
			{
				final BookModel model = fbreader.Model;
				if (model != null) {
					final Book book = model.Book;
					if (book != null) {
						book.reloadInfoFromDatabase();
						ZLTextHyphenator.Instance().load(book.getLanguage());
					}
				}
				fbreader.clearTextCaches();
				fbreader.getViewWidget().repaint();
				break;
			}
			case CANCEL_CODE:
				fbreader.runCancelAction(resultCode - 1);
				break;
		}
	}

	public void navigate() {
        ((NavigationPopup)FBReaderApp.Instance().getPopupById(NavigationPopup.ID)).runNavigation();
	}
    
    /** 
     * If book is available, add it to application title.
     */
    private void setApplicationTitle() {
        final Book currentBook = Library.getRecentBook();
        
        if (currentBook != null) {
            setTitle(currentBook.getTitle());
        }
    }


	/**
	 * Adds to internal storage all the fonts that come packaged with the app.
	 * This is so AndroidFontUtil.java can find them in the getFontMap() call
	 */
	private void copyFontsFromAssetsIntoPhoneStorage() {

		// create Fonts directory if it doesn't already exist
		final File fontsDir = new File(Paths.FontsDirectoryOption().getValue());
		if (!fontsDir.exists()) {
			fontsDir.mkdirs();
		}

		//Going over every font packaged in assets
		InputStream from = null;
		try {
			for(String asset : getAssets().list(FONTS_ASSET_FOLDER)) {
				from = getAssets().open(String.format("%s/%s",FONTS_ASSET_FOLDER,asset));
				File outFile = new File(Paths.FontsDirectoryOption().getValue(), asset);
				if(!outFile.exists()) {
					//If it's not already there add it
					FileUtils.copyInputStreamToFile(from, outFile);
				}
				from.close();
			}
		} catch (Exception e) {
			Log.w("FBR", e.getMessage());
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

	private void copyManual() {

        // create books directory if it doesn't already exist
        final File booksDir = new File(Paths.BooksDirectoryOption().getValue());
        if (!booksDir.exists()) {
            booksDir.mkdirs();
        } else {
            // remove existing user manual
            final File oldFile = new File(Paths.BooksDirectoryOption().getValue(), USER_GUIDE_FILE);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        InputStream from = null;
        FileOutputStream to = null;
        try {
            from = getAssets().open(USER_GUIDE_FILE);
            File outFile = new File(Paths.BooksDirectoryOption().getValue(), USER_GUIDE_FILE);
            to = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytes_read;
            while ((bytes_read = from.read(buffer)) > 0)
                to.write(buffer, 0, bytes_read);
        } catch (Exception e) {
            Log.w("FBR", e.getMessage());
        } finally {
              if (from != null)
                try {
                  from.close();
                } catch (IOException e) {
                  // do nothing
                }
              if (to != null)
                try {
                  to.close();
                } catch (IOException e) {
                  // do nothing
                }
        }
    }

}
