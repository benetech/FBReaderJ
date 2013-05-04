package org.accessibility;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;

/**
 * An 'accessible' dialog that pops up for a specified time and is voiced by TalkBack
 * @author roms
 */
public class VoiceableDialog extends AlertDialog {

    public VoiceableDialog(Context context) {
        super(context);
    }
    
    public void popup(final String message, final int wait) {
        setMessage(message);
        show();

        // Close the dialog after a short wait
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
             public void run() {
                  cancel();
             }
        }, wait);
    }
}
