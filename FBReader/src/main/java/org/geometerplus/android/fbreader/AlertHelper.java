package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.widget.TextView;

import org.benetech.android.R;

/**
 * Created by GreatArcantos on 1/13/2017.
 */

public class AlertHelper {

    public  static void popupAlert(final Context context, String message){
        final AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(null)
                .setMessage(message)
                .create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                float px = context.getResources().getDimension(R.dimen.alert_font_size);
                TextView textView = (TextView) alert.findViewById(android.R.id.message);
                if(textView != null) textView.setTextSize(px);
            }
        });
        alert.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    alert.dismiss();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 10000);
    }
}
