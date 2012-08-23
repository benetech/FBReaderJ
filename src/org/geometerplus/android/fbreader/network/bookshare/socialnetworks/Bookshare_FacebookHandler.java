package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Metadata_Bean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class Bookshare_FacebookHandler {

	private Activity authActivity;
	private Context context;
	SharedPreferences mPrefs;
	private Facebook facebook;

	public Bookshare_FacebookHandler(Activity activity) {
		authActivity = activity;
		context = authActivity.getBaseContext();
		facebook = new Facebook(SocialNetworkKeys.FACEBOOK_APP_ID);
	}

    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
        facebook.authorizeCallback(requestCode, resultCode, data);
    }

	public void ssoInitialAuth() {
		facebook.authorize(authActivity, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {

            }

            @Override
            public void onFacebookError(FacebookError error) {

                Toast.makeText(context, "Couldn't connect to Facebook, " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(DialogError e) {
                Toast.makeText(context, "Error during initial SSO Auth, " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {

                Toast.makeText(context, "Connection terminated forcibly",
                        Toast.LENGTH_LONG).show();
            }

        });
	}

	// get permission to access users information. When the user provide
	// permission
	// the application will automatically do the rest of the work
	public void getFBPermission() {
		facebook.authorize(authActivity, new String[]{"publish_stream"},

                new DialogListener() {
                    @Override
                    public void onComplete(Bundle values) {
                    }

                    @Override
                    public void onFacebookError(FacebookError error) {
                        Toast.makeText(context, "Facebook Error getting publish permission, " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(DialogError e) {
                        Toast.makeText(context, "Error getting publish permission, " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Connection terminated forcibly on permission",
                            Toast.LENGTH_LONG).show();
                    }
                });
	}

	// Access token is needed in order to give access to the program for what is
	// called
	// a session
	public void getAccessToken() {
		mPrefs = authActivity.getPreferences(authActivity.MODE_PRIVATE);
		String accessToken = mPrefs.getString("access_token", null);
		long tokenExpires = mPrefs.getLong("access_expires", 0);
		if (accessToken != null) {
			facebook.setAccessToken(accessToken);
		}
		if (tokenExpires != 0) {
			facebook.setAccessExpires(tokenExpires);
		}

		if (!facebook.isSessionValid()) {
			facebook.authorize(authActivity, new String[] {}, new DialogListener() {

				@Override
				public void onFacebookError(FacebookError e) {
					Toast.makeText(
							context,
							"Request could not be fulfilled. Please try again.",
							Toast.LENGTH_LONG).show();
				}

				@Override
				public void onError(DialogError e) {
					Toast.makeText(context, "Error has occured.",
							Toast.LENGTH_LONG).show();
				}

				@Override
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires", facebook.getAccessExpires());
					editor.commit();
				}

				@Override
				public void onCancel() {

				}
			});
		}
	}

	private void postOnWall(Bundle parameters) {

		parameters.putString("link", "http://www.bookshare.org/");
		parameters.putString("name", "Bookshare");
		parameters
				.putString(
						"picture",
						"http://i935.photobucket.com/albums/ad197/thushv/Benetech-FBReaderJ/bookshare_small.jpg");

		String response = null;
		try {
			response = facebook.request("me/feed", parameters, "POST");
			// String response = facebook.request("me/home", parameters, "POST");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null
					&& !response.equals("")
					&& !response.equals("false")
					&& (!response.contains("error") || !response
							.contains("OAuthException"))) {
				Toast.makeText(context, "Successfully posted on your wall",
						Toast.LENGTH_SHORT).show();

			}

		}

	}

	public void postBookOnWall(Bookshare_Metadata_Bean bean) {
		Bundle parameters = new Bundle();
		// message to post on the wall
		parameters.putString("message", SocialNetowkPosts.getBookPost(bean));
		postOnWall(parameters);
	}

	public void postPeriodicalOnWall(Bookshare_Edition_Metadata_Bean bean) {
		Bundle parameters = new Bundle();
		parameters.putString("message",
				SocialNetowkPosts.getPeriodicalPost(bean));
		postOnWall(parameters);
	}

}
