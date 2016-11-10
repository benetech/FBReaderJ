/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.DictionaryUtil;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ColorProfile;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.ScrollingPreferences;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.fbreader.tips.TipsManager;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

public class PreferenceActivity extends ZLPreferenceActivity {

    public PreferenceActivity() {
        super("Preferences");
    }

    private Toolbar toolbar;

    @Override
    protected void init(Intent intent) {
        final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
        final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
        final ColorProfile profile = fbReader.getColorProfile();

        final Screen appearanceScreen = createPreferenceScreen("appearance");

        appearanceScreen.addPreference(new ZLStringChoicePreference(
                this, appearanceScreen.Resource, "screenOrientation",
                androidLibrary.OrientationOption, androidLibrary.allOrientations()
        ));

        final int myLevel = androidLibrary.ScreenBrightnessLevelOption.getValue();
        appearanceScreen.addPreference(new SeekBarPreference(this, null, 1, myLevel) {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    final int DARKEST_PROGRESS_SETTING = 1;
                    progress = DARKEST_PROGRESS_SETTING;
                }

                androidLibrary.ScreenBrightnessLevelOption.setValue(progress);
            }
        });

        appearanceScreen.addPreference(new BatteryLevelToTurnScreenOffPreference(
                this,
                androidLibrary.BatteryLevelToTurnScreenOffOption,
                appearanceScreen.Resource,
                "dontTurnScreenOff"
        ));
		/*
		appearanceScreen.addPreference(new ZLBooleanPreference(
			this,
			androidLibrary.DontTurnScreenOffDuringChargingOption,
			appearanceScreen.Resource,
			"dontTurnScreenOffDuringCharging"
		));
		*/
        appearanceScreen.addOption(androidLibrary.ShowStatusBarOption, "showStatusBar");

        final Screen textScreen = createPreferenceScreen("text");
        final ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
        final ZLTextBaseStyle baseStyle = collection.getBaseStyle();
        textScreen.addPreference(new FontOption(
                this, textScreen.Resource, "font",
                baseStyle.FontFamilyOption, false
        ));

        textScreen.addPreference(new ZLFontSizeListPreference(this, textScreen.Resource, baseStyle.FontSizeOption, "fontSize"));

        textScreen.addPreference(new FontStylePreference(
                this, textScreen.Resource, "fontStyle",
                baseStyle.BoldOption, baseStyle.ItalicOption
        ));
        final ZLIntegerRangeOption spaceOption = baseStyle.LineSpaceOption;
        final String[] spacings = new String[spaceOption.MaxValue - spaceOption.MinValue + 1];
        for (int i = 0; i < spacings.length; ++i) {
            final int val = spaceOption.MinValue + i;
            spacings[i] = (char)(val / 10 + '0') + "." + (char)(val % 10 + '0');
        }
        textScreen.addPreference(new ZLChoicePreference(
                this, textScreen.Resource, "lineSpacing",
                spaceOption, spacings
        ));
        textScreen.addPreference(new MarginsPreference(this, Resource,"margins", fbReader.LeftMarginOption,
                fbReader.RightMarginOption,  fbReader.TopMarginOption, fbReader.BottomMarginOption));

        final String[] alignments = { "left", "right", "center", "justify" };
        textScreen.addPreference(new ZLChoicePreference(
                this, textScreen.Resource, "alignment",
                baseStyle.AlignmentOption, alignments
        ));
        textScreen.addOption(androidLibrary.AllowPinchZoomOption, "allowPinchZoom");


        final ZLPreferenceSet footerPreferences = new ZLPreferenceSet();
        final ZLPreferenceSet bgPreferences = new ZLPreferenceSet();

        final Screen colorsScreen = createPreferenceScreen("colors");
        bgPreferences.add(colorsScreen.addOption(profile.BackgroundOption, "backgroundColor"));
            /*
            colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
            */
        colorsScreen.addOption(profile.RegularTextOption, "text");
        colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
        colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
        colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
        colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");

//        final Screen marginsScreen = createPreferenceScreen("margins");
//        marginsScreen.addPreference(new ZLIntegerRangePreference(
//                this, marginsScreen.Resource.getResource("left"),
//                fbReader.LeftMarginOption
//        ));
//        marginsScreen.addPreference(new ZLIntegerRangePreference(
//                this, marginsScreen.Resource.getResource("right"),
//                fbReader.RightMarginOption
//        ));
//        marginsScreen.addPreference(new ZLIntegerRangePreference(
//                this, marginsScreen.Resource.getResource("top"),
//                fbReader.TopMarginOption
//        ));
//        marginsScreen.addPreference(new ZLIntegerRangePreference(
//                this, marginsScreen.Resource.getResource("bottom"),
//                fbReader.BottomMarginOption
//        ));

        final Screen statusLineScreen = createPreferenceScreen("scrollBar");

        final String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
        statusLineScreen.addPreference(new ZLChoicePreference(
                this, statusLineScreen.Resource, "scrollbarType",
                fbReader.ScrollbarTypeOption, scrollBarTypes
        ) {
            @Override
            protected void onDialogClosed(boolean result) {
                super.onDialogClosed(result);
                footerPreferences.setEnabled(
                        findIndexOfValue(getValue()) == FBView.SCROLLBAR_SHOW_AS_FOOTER
                );
            }
        });

        footerPreferences.add(statusLineScreen.addPreference(new ZLIntegerRangePreference(
                this, statusLineScreen.Resource.getResource("footerHeight"),
                fbReader.FooterHeightOption
        )));
        footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowTOCMarksOption, "tocMarks"));

        footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowClockOption, "showClock"));
        footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowBatteryOption, "showBattery"));
        footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowProgressOption, "showProgress"));
        footerPreferences.add(statusLineScreen.addPreference(new FontOption(
                this, statusLineScreen.Resource, "font",
                fbReader.FooterFontOption, false
        )));
        footerPreferences.setEnabled(
                fbReader.ScrollbarTypeOption.getValue() == FBView.SCROLLBAR_SHOW_AS_FOOTER
        );

		/*
		final Screen colorProfileScreen = createPreferenceScreen("colorProfile");
		final ZLResource resource = colorProfileScreen.Resource;
		colorProfileScreen.setSummary(ColorProfilePreference.createTitle(resource, fbreader.getColorProfileName()));
		for (String key : ColorProfile.names()) {
			colorProfileScreen.addPreference(new ColorProfilePreference(
				this, fbreader, colorProfileScreen, key, ColorProfilePreference.createTitle(resource, key)
			));
		}
		*/

        final ScrollingPreferences scrollingPreferences = ScrollingPreferences.Instance();

        final ZLKeyBindings keyBindings = fbReader.keyBindings();

        final Screen scrollingScreen = createPreferenceScreen("scrolling");
        scrollingScreen.addOption(scrollingPreferences.FingerScrollingOption, "fingerScrolling");
        scrollingScreen.addOption(fbReader.EnableDoubleTapOption, "enableDoubleTapDetection");

        final ZLPreferenceSet volumeKeysPreferences = new ZLPreferenceSet();
        scrollingScreen.addPreference(new ZLCheckBoxPreference(
                this, scrollingScreen.Resource, "volumeKeys"
        ) {
            {
                setChecked(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));
            }

            @Override
            protected void onClick() {
                super.onClick();
                if (isChecked()) {
                    keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
                    keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
                } else {
                    keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, FBReaderApp.NoAction);
                    keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, FBReaderApp.NoAction);
                }
                volumeKeysPreferences.setEnabled(isChecked());
            }
        });
        volumeKeysPreferences.setEnabled(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));

        scrollingScreen.addOption(scrollingPreferences.HorizontalOption, "horizontal");

        final Screen dictionaryScreen = createPreferenceScreen("dictionary");
        dictionaryScreen.addPreference(new DictionaryPreference(
                this,
                dictionaryScreen.Resource,
                "dictionary",
                DictionaryUtil.singleWordTranslatorOption()
        ));
        dictionaryScreen.addPreference(new DictionaryPreference(
                this,
                dictionaryScreen.Resource,
                "translator",
                DictionaryUtil.multiWordTranslatorOption()
        ));
        dictionaryScreen.addPreference(new ZLBooleanPreference(
                this,
                fbReader.NavigateAllWordsOption,
                dictionaryScreen.Resource,
                "navigateOverAllWords"
        ));

        dictionaryScreen.addOption(fbReader.WordTappingActionOption, "tappingAction");

        //final Screen subscriptionScreen = createPreferenceScreen("subscriptionSettings");
        //subscriptionScreen.addOption(, resourceKey)


        final Screen imagesScreen = createPreferenceScreen("images");
        imagesScreen.addOption(fbReader.ImageTappingActionOption, "tappingAction");
        imagesScreen.addOption(fbReader.ImageViewBackgroundOption, "backgroundColor");

        final Screen cancelMenuScreen = createPreferenceScreen("cancelMenu");
        cancelMenuScreen.addOption(fbReader.ShowPreviousBookInCancelMenuOption, "previousBook");
        cancelMenuScreen.addOption(fbReader.ShowPositionsInCancelMenuOption, "positions");
        final String[] backKeyActions =
                { ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU };
        cancelMenuScreen.addPreference(new ZLStringChoicePreference(
                this, cancelMenuScreen.Resource, "backKeyAction",
                keyBindings.getOption(KeyEvent.KEYCODE_BACK, false), backKeyActions
        ));
        final String[] backKeyLongPressActions =
                { ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU, FBReaderApp.NoAction };
        cancelMenuScreen.addPreference(new ZLStringChoicePreference(
                this, cancelMenuScreen.Resource, "backKeyLongPressAction",
                keyBindings.getOption(KeyEvent.KEYCODE_BACK, true), backKeyLongPressActions
        ));

        final Screen tipsScreen = createPreferenceScreen("tips");
        tipsScreen.addOption(TipsManager.Instance().ShowTipsOption, "showTips");
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((ZLAndroidApplication) getApplication()).startTracker(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ZLAndroidApplication) getApplication()).stopTracker(this);
    }

    @Override
    protected void setupActionBar() {
        setContentView(R.layout.settings);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        //Toolbar will now take on default Action Bar characteristics
        toolbar.setTitle(ZLResource.resource("menu").getResource("preferences").getValue());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary) ZLibrary.Instance();
        if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        String action = findActionForMenuItem(item.getItemId());
        Object[] params = findParamsForMenuItemAction(item.getItemId());
        if(params == null){
            ZLApplication.Instance().doAction(action);
        }
        else {
            ZLApplication.Instance().doAction(action, params);
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private String findActionForMenuItem(int itemId) {
        if (itemId == R.id.menu_item_settings)
            return ActionCode.SHOW_PREFERENCES;

        if (itemId == R.id.menu_item_sync_with_bookshare) {
            if (isLoggedintoBookshare())
                return ActionCode.SYNC_WITH_BOOKSHARE;

            return ActionCode.BOOKSHARE;
        }

        if (itemId == R.id.menu_item_help)
            return ActionCode.SHOW_HELP;

        if (itemId == R.id.menu_item_about_goread)
            return ActionCode.ABOUT_GOREAD;

        if (itemId == R.id.menu_item_logout_bookshare)
            return ActionCode.LOGOUT_BOOKSHARE;

        if (itemId == R.id.menu_item_login_bookshare)
            return ActionCode.BOOKSHARE;
        return "";
    }

    private Object[] findParamsForMenuItemAction(int itemId){
        if (itemId == R.id.menu_item_sync_with_bookshare)
            return new Object[]{SyncReadingListsWithBookshareAction.SyncType.USER_ACTIVATED};
        return null;
    }
}
