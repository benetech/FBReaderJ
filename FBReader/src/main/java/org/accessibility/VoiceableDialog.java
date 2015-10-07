package org.accessibility;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;

/**
 * An 'accessible' dialog that pops up for a specified time and is voiced by TalkBack
 * @author roms
 */
public class VoiceableDialog extends Dialog {

    public VoiceableDialog(Context context) {
        super(context);
    }
    
    public void popup(final String message, final int wait) {
        setTitle(message);
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
