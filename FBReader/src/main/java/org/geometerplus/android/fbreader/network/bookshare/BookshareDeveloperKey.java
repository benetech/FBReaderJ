package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * This file reads the developer key from the developer.properties file in
 * assets/developerSpecific folder. It will be read by other Bookshare classes.
 * You can obtain a developer key from http://developer.bookshare.org
 */
public class BookshareDeveloperKey {

	public static String DEVELOPER_KEY = "";
	public static String BUGSENSE_KEY = "";
	public static boolean OPT_OUT_GOOGLE_ANALYTICS = false;

	public static void initialize(Context applicationContext) {
		// TODO Auto-generated method stub
		final Context mcontext = applicationContext;
		getProperties(mcontext);

	}

	private static void getProperties(Context mcontext) {
		// TODO Auto-generated method stub
		Resources resources = mcontext.getResources();
		AssetManager assetManager = resources.getAssets();

		// Read from the /assets directory
		try {
			InputStream inputStream = assetManager
					.open("developerSpecific/developer.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			System.out.println("The properties are now loaded");
			System.out.println("properties: " + properties);
			BUGSENSE_KEY = properties.getProperty("bugSenseKey");
			DEVELOPER_KEY = properties.getProperty("developerKey");
			if (properties.getProperty("optOutGoogleAnalytics")
					.equalsIgnoreCase("false")) {
				OPT_OUT_GOOGLE_ANALYTICS = false;
			} else {
				OPT_OUT_GOOGLE_ANALYTICS = true;
			}

		} catch (IOException e) {
			System.err.println("Failed to open developer.property file");
			e.printStackTrace();
		}
	}

}
