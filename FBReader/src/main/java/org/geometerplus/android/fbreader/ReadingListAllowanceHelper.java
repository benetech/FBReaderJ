package org.geometerplus.android.fbreader;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public static Set<String> allowsFromJson(JSONObject base) throws Exception {
        Set<String> allowsSet = new HashSet<>();
        if(!base.has(ReadingListAllowanceHelper.JSON_CODE_ALLOWS)){
            return allowsSet;
        }

        JSONArray allows = base.optJSONArray(ReadingListAllowanceHelper.JSON_CODE_ALLOWS);
        for (int i = 0; i < allows.length(); i++) {
            allowsSet.add(allows.get(i).toString());
        }
        return allowsSet;
    }
}
