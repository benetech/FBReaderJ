package org.geometerplus.android.fbreader;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by GreatArcantos on 02/06/2017.
 */

public class ReadingListAllowanceHelper {
    public static final String JSON_CODE_ALLOWS = "allows";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    public static Set<String> allowsFromJson(JSONArray allows) {
        Set<String> allowsSet = new HashSet<>();

        if(allows != null) {
            try {
                for (int i = 0; i < allows.length(); i++) {
                    allowsSet.add(allows.get(i).toString());
                }
            } catch (JSONException e) {
                Log.e(ReadingListAllowanceHelper.class.getSimpleName(), e.getMessage(), e);
            }
        }
        return allowsSet;
    }
}
