package org.geometerplus.android.fbreader.benetech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ScreenUnlockReceiver extends BroadcastReceiver{

	
		public static boolean wasScreenunlocked = true;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				wasScreenunlocked = false;
				Toast.makeText(context, "receiver scrren off", Toast.LENGTH_LONG).show();
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				wasScreenunlocked = true;
				Toast.makeText(context, "receiver screen on", Toast.LENGTH_LONG).show();
			}
		}

}
