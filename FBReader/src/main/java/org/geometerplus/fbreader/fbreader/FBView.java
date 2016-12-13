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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextSelectionCursor;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;

import java.util.ArrayList;

public final class FBView extends ZLTextView {
	private FBReaderApp mFBReader;

	FBView(FBReaderApp reader) {
		super(reader);
		mFBReader = reader;
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		if (myFooter != null) {
			myFooter.resetTOCMarks();
		}
	}

	private int myStartY;
	private boolean myIsBrightnessAdjustmentInProgress;
	private int myStartBrightness;

	private String myZoneMapId;
	private TapZoneMap myZoneMap;

	private ZLTextRegion doubleTapSelectedRegion = null;

	private boolean didScroll = false;

	public ZLTextRegion getDoubleTapSelectedRegion(){
		return doubleTapSelectedRegion;
	}

	public ZLTextRegion getTopOfPageRegion(){
		return findRegion(25, 25, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
	}

	public boolean didScroll() {
		return didScroll;
	}

	public void resetDidScroll(){
		didScroll = false;
	}

	public void resetLatestLongPressSelectedRegion(){
		doubleTapSelectedRegion = null;
	}

	private TapZoneMap getZoneMap() {
		//final String id =
		//	ScrollingPreferences.Instance().TapZonesSchemeOption.getValue().toString();
		final String id =
			ScrollingPreferences.Instance().HorizontalOption.getValue()
				? "right_to_left" : "up";
		if (!id.equals(myZoneMapId)) {
			myZoneMap = new TapZoneMap(id);
			myZoneMapId = id;
		}
		return myZoneMap;
	}

	public boolean onFingerSingleTap(int x, int y) {
		if (super.onFingerSingleTap(x, y)) {
			return true;
		}

		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.HyperlinkFilter);
		if (region != null) {
			selectRegion(region);
			mFBReader.getViewWidget().reset();
			mFBReader.getViewWidget().repaint();
			mFBReader.doAction(ActionCode.PROCESS_HYPERLINK);
			return true;
		}
		String action = getZoneMap().getActionByCoordinates(
				x, y, myContext.getWidth(), myContext.getHeight(),
				isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
		);
		if(action == null){ //we're not catching any other events for this tap
			action = ActionCode.TOGGLE_BARS;
		}
		mFBReader.doAction(action, x, y);

		return true;
	}

	@Override
	public boolean isDoubleTapSupported() {
		return mFBReader.EnableDoubleTapOption.getValue();
	}

	@Override
	public boolean onFingerDoubleTap(int x, int y, boolean multitouch) {
		if (super.onFingerDoubleTap(x, y, multitouch)) {
			return true;
		}

		if(multitouch){
			doubleTapSelectedRegion = null;
			mFBReader.doAction(ActionCode.PLAY_OR_PAUSE);
		}
		else {
			doubleTapSelectedRegion = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
			mFBReader.doAction(ActionCode.SELECT_SENTENCE);
		}

		return true;
	}

	public boolean onFingerPress(int x, int y) {
		if (super.onFingerPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = findSelectionCursor(x, y, MAX_SELECTION_DISTANCE);
		if (cursor != ZLTextSelectionCursor.None) {
			mFBReader.doAction(ActionCode.SELECTION_HIDE_PANEL);
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		if (mFBReader.AllowScreenBrightnessAdjustmentOption.getValue() && x < myContext.getWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
			myStartBrightness = ZLibrary.Instance().getScreenBrightness();
			return true;
		}

		startManualScrolling(x, y);
		return true;
	}

	private boolean isFlickScrollingEnabled() {
		final ScrollingPreferences.FingerScrolling fingerScrolling =
			ScrollingPreferences.Instance().FingerScrollingOption.getValue();
		return
			fingerScrolling == ScrollingPreferences.FingerScrolling.byFlick ||
			fingerScrolling == ScrollingPreferences.FingerScrolling.byTapAndFlick;
	}

	private void startManualScrolling(int x, int y) {
		if (!isFlickScrollingEnabled()) {
			return;
		}

		final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
		final Direction direction = horizontal ? Direction.rightToLeft : Direction.up;
		mFBReader.getViewWidget().startManualScrolling(x, y, direction);
	}

	public boolean onFingerMove(int x, int y) {
		if (super.onFingerMove(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		synchronized (this) {
			if (myIsBrightnessAdjustmentInProgress) {
				if (x >= myContext.getWidth() / 5) {
					myIsBrightnessAdjustmentInProgress = false;
					startManualScrolling(x, y);
				} else {
// FBR-360: Disabled swiping up/down on left side of screen to brighten/darken
//					final int delta = (myStartBrightness + 30) * (myStartY - y) / myContext.getHeight();
//					ZLibrary.Instance().setScreenBrightness(myStartBrightness + delta);
					return true;
				}
			}

			if (isFlickScrollingEnabled()) {
				mFBReader.getViewWidget().scrollManuallyTo(x, y);
			}
		}
		return true;
	}

	public boolean onFingerRelease(int x, int y) {
		if (super.onFingerRelease(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		if (myIsBrightnessAdjustmentInProgress) {
			myIsBrightnessAdjustmentInProgress = false;
			return true;
		}

		if (isFlickScrollingEnabled()) {
			mFBReader.getViewWidget().startAnimatedScrolling(
				x, y, ScrollingPreferences.Instance().AnimationSpeedOption.getValue()
			);
			return true;
		}

		return true;
	}

	public boolean onFingerLongPress(int x, int y) {
		if (super.onFingerLongPress(x, y)) {
			return true;
		}

		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = true;
			if (soul instanceof ZLTextWordRegionSoul) {
				switch (mFBReader.WordTappingActionOption.getValue()) {
					case startSelecting:
						mFBReader.doAction(ActionCode.SELECTION_HIDE_PANEL);
						initSelection(x, y);
						final ZLTextSelectionCursor cursor = findSelectionCursor(x, y);
						if (cursor != ZLTextSelectionCursor.None) {
							moveSelectionCursorTo(cursor, x, y);
						}
						return true;
					case selectSingleWord:
					case openDictionary:
						doSelectRegion = true;
						break;
				}
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doSelectRegion =
					mFBReader.ImageTappingActionOption.getValue() !=
					FBReaderApp.ImageTappingAction.doNothing;
			} else if (soul instanceof ZLTextHyperlinkRegionSoul) {
				doSelectRegion = true;
			}

			if (doSelectRegion) {
				selectRegion(region);
				mFBReader.getViewWidget().reset();
				mFBReader.getViewWidget().repaint();
				return true;
			}
		}

		return false;
	}

	public boolean onFingerMoveAfterLongPress(int x, int y) {
		if (super.onFingerMoveAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextHyperlinkRegionSoul ||
				soul instanceof ZLTextWordRegionSoul) {
				if (mFBReader.WordTappingActionOption.getValue() !=
					FBReaderApp.WordTappingAction.doNothing) {
					region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
					if (region != null) {
						soul = region.getSoul();
						if (soul instanceof ZLTextHyperlinkRegionSoul
							 || soul instanceof ZLTextWordRegionSoul) {
							selectRegion(region);
							mFBReader.getViewWidget().reset();
							mFBReader.getViewWidget().repaint();
						}
					}
				}
			}
		}
		return true;
	}

	public boolean onFingerReleaseAfterLongPress(int x, int y) {
		if (super.onFingerReleaseAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		final ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();

			boolean doRunAction = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				doRunAction =
					mFBReader.WordTappingActionOption.getValue() ==
					FBReaderApp.WordTappingAction.openDictionary;
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doRunAction =
					mFBReader.ImageTappingActionOption.getValue() ==
					FBReaderApp.ImageTappingAction.openImageView;
			}

			if (doRunAction) {
				mFBReader.doAction(ActionCode.PROCESS_HYPERLINK);
				return true;
			}
		}

		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffX == 0 && diffY == 0) {
			return true;
		}

		final Direction direction = (diffY != 0) ?
			(diffY > 0 ? Direction.down : Direction.up) :
			(diffX > 0 ? Direction.leftToRight : Direction.rightToLeft);

		new MoveCursorAction(mFBReader, direction).run();
		return true;
	}


	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex) {
		super.onScrollingFinished(pageIndex);
		didScroll = true;
	}

	@Override
	public int getLeftMargin() {
		return mFBReader.LeftMarginOption.getValue();
	}

	@Override
	public int getRightMargin() {
		return mFBReader.RightMarginOption.getValue();
	}

	@Override
	public int getTopMargin() {
		return mFBReader.TopMarginOption.getValue();
	}

	@Override
	public int getBottomMargin() {
		return mFBReader.BottomMarginOption.getValue();
	}

	@Override
	public ZLFile getWallpaperFile() {
		final String filePath = mFBReader.getColorProfile().WallpaperOption.getValue();
		if ("".equals(filePath)) {
			return null;
		}

		final ZLFile file = ZLFile.createFileByPath(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	public ZLColor getBackgroundColor() {
		return mFBReader.getColorProfile().BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedBackgroundColor() {
		return mFBReader.getColorProfile().SelectionBackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedForegroundColor() {
		return mFBReader.getColorProfile().SelectionForegroundOption.getValue();
	}

	@Override
	public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = mFBReader.getColorProfile();
		switch (hyperlink.Type) {
			default:
			case FBHyperlinkType.NONE:
				return profile.RegularTextOption.getValue();
			case FBHyperlinkType.INTERNAL:
				return mFBReader.Model.Book.isHyperlinkVisited(hyperlink.Id)
					? profile.VisitedHyperlinkTextOption.getValue()
					: profile.HyperlinkTextOption.getValue();
			case FBHyperlinkType.EXTERNAL:
				return profile.HyperlinkTextOption.getValue();
		}
	}

	@Override
	public ZLColor getHighlightingColor() {
		return mFBReader.getColorProfile().HighlightingOption.getValue();
	}

	private class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				mFBReader.getViewWidget().repaint();
			}
		};

		private ArrayList<TOCTree> myTOCMarks;

		public int getHeight() {
			return mFBReader.FooterHeightOption.getValue();
		}

		public synchronized void resetTOCMarks() {
			myTOCMarks = null;
		}

		private final int MAX_TOC_MARKS_NUMBER = 100;
		private synchronized void updateTOCMarks(BookModel model) {
			myTOCMarks = new ArrayList<TOCTree>();
			TOCTree toc = model.TOCTree;
			if (toc == null) {
				return;
			}
			int maxLevel = Integer.MAX_VALUE;
			if (toc.getSize() >= MAX_TOC_MARKS_NUMBER) {
				final int[] sizes = new int[10];
				for (TOCTree tocItem : toc) {
					if (tocItem.Level < 10) {
						++sizes[tocItem.Level];
					}
				}
				for (int i = 1; i < sizes.length; ++i) {
					sizes[i] += sizes[i - 1];
				}
				for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
					if (sizes[maxLevel] < MAX_TOC_MARKS_NUMBER) {
						break;
					}
				}
			}
			for (TOCTree tocItem : toc.allSubTrees(maxLevel)) {
				myTOCMarks.add(tocItem);
			}
		}

		public synchronized void paint(ZLPaintContext context) {
			final FBReaderApp reader = mFBReader;
			if (reader == null) {
				return;
			}
			final BookModel model = reader.Model;
			if (model == null) {
				return;
			}

			//final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
			final ZLColor fillColor = reader.getColorProfile().FooterFillOption.getValue();

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			context.setFont(
				reader.FooterFontOption.getValue(),
				height <= 10 ? height + 3 : height + 1,
				height > 10, false, false
			);

			final PagePosition pagePosition = FBView.this.pagePosition();

			final StringBuilder info = new StringBuilder();
			if (reader.FooterShowProgressOption.getValue()) {
				info.append(pagePosition.Current);
				info.append("/");
				info.append(pagePosition.Total);
			}
			if (reader.FooterShowBatteryOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(reader.getBatteryLevel());
				info.append("%");
			}
			if (reader.FooterShowClockOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			final String infoString = info.toString();

			final int infoWidth = context.getStringWidth(infoString);
			final ZLFile wallpaper = getWallpaperFile();
			if (wallpaper != null) {
				context.clear(wallpaper, wallpaper instanceof ZLResourceFile);
			} else {
				context.clear(getBackgroundColor());
			}

			// draw info text
			context.setTextColor(fgColor);
			context.drawString(right - infoWidth, height - delta, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			myGaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * myGaugeWidth * pagePosition.Current / pagePosition.Total);

			context.setFillColor(fillColor);
			context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);

			if (reader.FooterShowTOCMarksOption.getValue()) {
				if (myTOCMarks == null) {
					updateTOCMarks(model);
				}
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						final int xCoord =
							left + 2 * lineWidth + (int)(1.0 * myGaugeWidth * refCoord / fullLength);
						context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
					}
				}
			}
		}

		// TODO: remove
		int myGaugeWidth = 1;
		/*public int getGaugeWidth() {
			return myGaugeWidth;
		}*/

		/*public void setProgress(int x) {
			// set progress according to tap coordinate
			int gaugeWidth = getGaugeWidth();
			float progress = 1.0f * Math.min(x, gaugeWidth) / gaugeWidth;
			int page = (int)(progress * computePageNumber());
			if (page <= 1) {
				gotoHome();
			} else {
				gotoPage(page);
			}
			mFBReader.getViewWidget().reset();
			mFBReader.getViewWidget().repaint();
		}*/
	}

	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		if (mFBReader.ScrollbarTypeOption.getValue() == SCROLLBAR_SHOW_AS_FOOTER) {
			if (myFooter == null) {
				myFooter = new Footer();
				mFBReader.addTimerTask(myFooter.UpdateTask, 15000);
			}
		} else {
			if (myFooter != null) {
				mFBReader.removeTimerTask(myFooter.UpdateTask);
				myFooter = null;
			}
		}
		return myFooter;
	}

	@Override
	protected void releaseSelectionCursor() {
		super.releaseSelectionCursor();
		if (getCountOfSelectedWords() > 0) {
			mFBReader.doAction(ActionCode.SELECTION_SHOW_PANEL);
		}
	}

	public String getSelectedText() {
		final TextBuildTraverser traverser = new TextBuildTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getText();
	}

	public int getCountOfSelectedWords() {
		final WordCountTraverser traverser = new WordCountTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getCount();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;

	@Override
	public int scrollbarType() {
		return mFBReader.ScrollbarTypeOption.getValue();
	}

	@Override
	public Animation getAnimationType() {
		return ScrollingPreferences.Instance().AnimationOption.getValue();
	}
}
