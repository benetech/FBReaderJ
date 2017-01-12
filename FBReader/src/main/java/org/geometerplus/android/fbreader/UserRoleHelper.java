package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by avanticatechnologies on 1/12/17.
 */

public class UserRoleHelper {
    public static final String PREFERENCE_IS_IM = "isIM";
    public static final String PREFERENCE_IS_OM = "isOM";
    public static final String PREFERENCE_IS_SPONSOR = "isSponsor";

    private boolean isOM;
    private boolean isIM;
    private boolean isSponsor;

    public boolean isSponsor() {
        return isSponsor;
    }

    public boolean isIM() {
        return isIM;
    }

    public boolean isOM() {
        return isOM;
    }


    public UserRoleHelper(Context context){
        final SharedPreferences login_preference = PreferenceManager
                .getDefaultSharedPreferences(context);
        isIM = login_preference.getBoolean(PREFERENCE_IS_IM, false);
        isOM = login_preference.getBoolean(PREFERENCE_IS_OM, false);
        isSponsor = login_preference.getBoolean(PREFERENCE_IS_SPONSOR, false);

    }

    static public void storeRoles(SharedPreferences.Editor editor, boolean isIM, boolean isOM, boolean isSponsor){
        editor.putBoolean(PREFERENCE_IS_IM, isIM);
        editor.putBoolean(PREFERENCE_IS_OM, isOM);
        editor.putBoolean(PREFERENCE_IS_SPONSOR, isSponsor);
    }

}
