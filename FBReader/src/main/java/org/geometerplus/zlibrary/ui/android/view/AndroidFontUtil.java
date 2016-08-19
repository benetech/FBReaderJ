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

package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.graphics.Typeface;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.util.ZLTTFInfoDetector;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public final class AndroidFontUtil {
	private static Method ourFontCreationMethod;
	static {
		try {
			ourFontCreationMethod = Typeface.class.getMethod("createFromFile", File.class);
		} catch (NoSuchMethodException e) {
			ourFontCreationMethod = null;
		}
	}

	public static boolean areExternalFontsSupported() {
		return ourFontCreationMethod != null;
	}

	public static Typeface createFontFromFile(File file) {
		if (ourFontCreationMethod == null) {
			return null;
		}
		try {
			return (Typeface)ourFontCreationMethod.invoke(null, file);
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	private static Map<String,File[]> ourFontMap;
	private static File[] ourFileList;
	private static long myTimeStamp;
	public static Map<String,File[]> getFontMap(boolean forceReload) {
		final long timeStamp = System.currentTimeMillis();
		if (forceReload && timeStamp < myTimeStamp + 1000) {
			forceReload = false;
		}
		myTimeStamp = timeStamp;
		if (ourFontMap == null || forceReload) {
			boolean rebuildMap = ourFontMap == null;
			if (ourFontCreationMethod == null) {
				if (rebuildMap) {
					ourFontMap = new HashMap<String,File[]>();
				}
			} else {
				final File[] fileList = new File(Paths.FontsDirectoryOption().getValue()).listFiles(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.startsWith(".")) {
								return false;
							}
							final String lcName = name.toLowerCase();
							return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
						}
					}
				);
				if (fileList == null) {
					if (ourFileList != null) {
						ourFileList = null;
						rebuildMap = true;
					}
				}
				if (fileList != null && !fileList.equals(ourFileList)) {
					ourFileList = fileList;
					rebuildMap = true;
				}
				if (rebuildMap) {
					ourFontMap = new ZLTTFInfoDetector().collectFonts(fileList);
				}
			}
		}
		return ourFontMap;
	}

	public static String realFontFamilyName(String fontFamily) {
		for (String name : getFontMap(false).keySet()) {
			if (name.equalsIgnoreCase(fontFamily)) {
				return name;
			}
		}
		if ("serif".equalsIgnoreCase(fontFamily) || "droid serif".equalsIgnoreCase(fontFamily)) {
			return "serif";
		}
		if ("sans-serif".equalsIgnoreCase(fontFamily) || "sans serif".equalsIgnoreCase(fontFamily) || "droid sans".equalsIgnoreCase(fontFamily)) {
			return "sans-serif";
		}
		if ("monospace".equalsIgnoreCase(fontFamily) || "droid mono".equalsIgnoreCase(fontFamily)) {
			return "monospace";
		}
		if ("Open Dyslexic Mono".equalsIgnoreCase(fontFamily)) {
			return "Open Dyslexic Mono";
		}
		if ("Free Sans".equalsIgnoreCase(fontFamily)) {
			return "Free Sans";
		}
		if ("Times New Roman".equalsIgnoreCase(fontFamily)) {
			return "Times New Roman";
		}
		if ("Verdana".equalsIgnoreCase(fontFamily)) {
			return "Verdana";
		}
		return "sans-serif";
	}

	public static boolean isCustomFont(String fontFamily) {
		return fontFamily.equalsIgnoreCase("Open Dyslexic Mono")
				|| fontFamily.equalsIgnoreCase("Free Sans")
				|| fontFamily.equalsIgnoreCase("Times New Roman")
				|| fontFamily.equalsIgnoreCase("Verdana");
	}

	public static String getCustomFontDirectory(String fontFamily) {
		switch (fontFamily) {
			case "Open Dyslexic Mono":
				return "fonts/OpenDyslexicMono-Regular.otf";
			case "Free Sans":
				return "fonts/FreeSans.otf";
			case "Times New Roman":
				return "fonts/TimesTenLTStd-Roman.otf";
			case "Verdana":
				return "fonts/Verdana.ttf";
			default:
				return null;
		}
	}

	public static void fillFamiliesList(ArrayList<String> families, boolean forceReload) {
		final TreeSet<String> familySet = new TreeSet<String>(getFontMap(forceReload).keySet());
		familySet.add("Droid Sans");
		familySet.add("Droid Serif");
		familySet.add("Droid Mono");
		familySet.add("Open Dyslexic Mono");
		familySet.add("Free Sans");
		familySet.add("Times New Roman");
		familySet.add("Verdana");
		families.addAll(familySet);
	}

	private static HashMap<String,Typeface[]> myTypefaces = new HashMap<String,Typeface[]>();


	public static Typeface typefaceForFontFamilyWithStyle(Context context, String fam, int style){
		String family = realFontFamilyName(fam);
		Typeface[] typefaces = myTypefaces.get(family);
		if (typefaces == null) {
			typefaces = new Typeface[4];
			myTypefaces.put(family, typefaces);
		}
		Typeface tf = typefaces[style];
		if (tf == null) {
			File[] files = AndroidFontUtil.getFontMap(false).get(family);
			if (files != null) {
				try {
					if (files[style] != null) {
						tf = AndroidFontUtil.createFontFromFile(files[style]);
					} else {
						for (int i = 0; i < 4; ++i) {
							if (files[i] != null) {
								tf = (typefaces[i] != null) ?
										typefaces[i] : AndroidFontUtil.createFontFromFile(files[i]);
								typefaces[i] = tf;
								break;
							}
						}
					}
				} catch (Throwable e) {
				}
			}
			if (tf == null) {
				if (AndroidFontUtil.isCustomFont(family)) {
					tf = Typeface.createFromAsset(context.getAssets(),
							AndroidFontUtil.getCustomFontDirectory(family));
				} else {
					tf = Typeface.create(family, style);
				}
			}
			typefaces[style] = tf;
		}
		return tf;
	}
}
