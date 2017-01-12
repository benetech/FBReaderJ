package org.geometerplus.android.fbreader;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

/**
 * Created by animal@martus.org on 11/3/15.
 */
public class LogoutFromBookshareAction extends FBAndroidAction {

    public LogoutFromBookshareAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    public static void clearBookshareLoginData(Context applicationContext) {
        SharedPreferences login = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences.Editor editor = login.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        UserRoleHelper.storeRoles(editor, false, false, false);
        editor.commit();
    }

    @Override
    protected void run(Object... params) {
        final Dialog confirmDialog = new Dialog(getBaseActivity());
        confirmDialog.setTitle(getBaseActivity().getResources().getString(R.string.accessible_alert_title));
        confirmDialog.setContentView(R.layout.accessible_alert_dialog);
        final TextView confirmation = (TextView)confirmDialog.findViewById(R.id.bookshare_confirmation_message);
        confirmation.setText(getBaseActivity().getResources().getString(R.string.logout_dialog_message));
        Button yesButton = (Button)confirmDialog.findViewById(R.id.bookshare_dialog_btn_yes);
        Button noButton = (Button) confirmDialog.findViewById(R.id.bookshare_dialog_btn_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Upon logout clear the stored login credentials
                Context applicationContext = getBaseActivity().getApplicationContext();
                clearBookshareLoginData(applicationContext);
                confirmDialog.dismiss();
                String toastMessage = getBaseActivity().getString(R.string.bks_menu_log_out);
                Toast.makeText(getBaseActivity(), toastMessage, Toast.LENGTH_SHORT).show();
                getBaseActivity().invalidateOptionsMenu();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
            }
        });
        confirmDialog.show();
    }
}
