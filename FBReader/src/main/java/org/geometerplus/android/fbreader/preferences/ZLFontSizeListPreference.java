package org.geometerplus.android.fbreader.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.common.primitives.Ints;

import org.geometerplus.zlibrary.core.options.ZLIntegerArrayOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by animal@martus.org on 10/8/15.
 */
public class ZLFontSizeListPreference extends ZLStringListPreference {

    private static LinkedHashMap<Integer, String> fontSizeToLabelMap;
    private ZLIntegerArrayOption fontSizeOption;

    public ZLFontSizeListPreference(Context context, ZLResource rootResource, ZLIntegerArrayOption fontSizeOption, String resourceKey) {
        super(context, rootResource, resourceKey);

        fillFontSizeToLabelMap();
        this.fontSizeOption = fontSizeOption;
        setList(getFontSizeLabels());
        setInitialValue(getFontSizeToLabelMap().get(fontSizeOption.getValue()));
    }

    private static void fillFontSizeToLabelMap() {
        fontSizeToLabelMap = new LinkedHashMap<>();
        fontSizeToLabelMap.put(12, "X Small");
        fontSizeToLabelMap.put(18, "Small");
        fontSizeToLabelMap.put(24, "Regular");
        fontSizeToLabelMap.put(30, "Large");
        fontSizeToLabelMap.put(45, "X Large");
        fontSizeToLabelMap.put(70, "Huge");
    }

    private static LinkedHashMap<Integer, String> getFontSizeToLabelMap() {
        if (fontSizeToLabelMap == null)
            fillFontSizeToLabelMap();

        return fontSizeToLabelMap;
    }

    private String[] getFontSizeLabels() {
        Collection<String> values = getFontSizeToLabelMap().values();
        return values.toArray(new String[0]);
    }

    @Override
    protected void onDialogClosed(boolean result) {
        super.onDialogClosed(result);

        if (result) {
            int keyForFontSize = findKey(getEntry().toString());
            fontSizeOption.setValue(keyForFontSize);
        }
    }

    private int findKey(String value) {
        Set<Map.Entry<Integer, String>> entries = getFontSizeToLabelMap().entrySet();
        for (Map.Entry<Integer, String> entry : entries) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return -1;
    }

    public static int[] getFontSizes() {
        Set<Integer> fontSizesSet = getFontSizeToLabelMap().keySet();

        return Ints.toArray(fontSizesSet);
    }

    public static int getConvertedDpFontSize(float staticFontSize) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, staticFontSize, displayMetrics);
    }
}
