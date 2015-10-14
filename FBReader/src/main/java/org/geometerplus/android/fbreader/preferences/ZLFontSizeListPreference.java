package org.geometerplus.android.fbreader.preferences;

import android.content.Context;

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
        fontSizeToLabelMap = new LinkedHashMap<Integer, String>();
        fontSizeToLabelMap.put(10, "Tiny");
        fontSizeToLabelMap.put(20, "Small");
        fontSizeToLabelMap.put(30, "Medium");
        fontSizeToLabelMap.put(50, "Large");
        fontSizeToLabelMap.put(80, "Huge");
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
}
