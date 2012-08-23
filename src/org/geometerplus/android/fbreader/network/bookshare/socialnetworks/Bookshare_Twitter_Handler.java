package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Periodical_Edition_Details;

import android.content.res.Resources;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

public class Bookshare_Twitter_Handler {

	public final static int BOOKSHARE_TWITTER_REQUEST = 2;
	private Activity mActivity;
	private ProgressDialog progressBar;
	//private SharedPreferences mSharedPreferences;
	private String callbackURL;

	private Twitter twitter;
	private Button updateStatus;
	private AccessToken accessToken;

	private RequestToken r;
    private Resources resources;

	public static final String SUCCESS_STRING = "SUCCESS";
	public static final String FAILURE_STRING = "FAILURE";
	
	public Bookshare_Twitter_Handler(Activity mActivity) {
		this.mActivity = mActivity;

		progressBar = new ProgressDialog(mActivity);
		progressBar.setMessage("Loading ...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(SocialNetworkKeys.TWITTER_CONSUMER_KEY,
				SocialNetworkKeys.TWITTER_CONSUMER_SECRET);
        resources = mActivity.getApplicationContext().getResources();

	}

	public void setUpTwitterForPosting() {
		if (r == null) {
			new GetAuthURLTask().execute(new String[] {});
		}

	}

	public class GetAuthURLTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			r = null;
			try {
				r = twitter.getOAuthRequestToken();

			} catch (TwitterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return r.getAuthenticationURL();
		}

		@Override
		protected void onPostExecute(String result) {
			progressBar.hide();
			Intent i = new Intent(mActivity, TwitterWebActivity.class);
			i.putExtra("URL", r.getAuthenticationURL());
			mActivity
			.startActivityForResult(
					i,BOOKSHARE_TWITTER_REQUEST);
		}
	}

	public class GetAccessTokenTask extends
	AsyncTask<TwitterAccessTokenListener, String, String> {
		private String verifier;
		private TwitterAccessTokenListener callback;

		public String getVerifier() {
			return verifier;
		}

		public void setVerifier(String verifier) {
			this.verifier = verifier;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.show();
		}

		@Override
		protected String doInBackground(TwitterAccessTokenListener... callbacks) {
			callback = callbacks[0];
			mActivity.setProgressBarIndeterminateVisibility(true);
			String status = null;
			try {
				accessToken = twitter.getOAuthAccessToken(r, verifier);
				status = SUCCESS_STRING;
			} catch (TwitterException e) {
				status = FAILURE_STRING;
				e.printStackTrace();
			}

			return status;
		}

		@Override
		protected void onPostExecute(String result) {
			progressBar.hide();

			callback.onAccessTokenFound(result,accessToken.getToken(),
					accessToken.getTokenSecret());

		}

	}

	public class UpdateStatusTask extends AsyncTask<String, Void, String> {
		String accessToken;
		String accessTokenSecret;
		Twitter t;
		String message;
		String status;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();


		}

		@Override
		protected String doInBackground(String... tokens) {
			accessToken = tokens[0];
			accessTokenSecret = tokens[1];
			message = tokens[2];

			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(SocialNetworkKeys.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(SocialNetworkKeys.TWITTER_CONSUMER_SECRET);
			builder.setOAuthAccessToken(accessToken);
			builder.setOAuthAccessTokenSecret(accessTokenSecret);
			twitter4j.conf.Configuration conf = builder.build();
			t = new TwitterFactory(conf).getInstance();

			if (accessToken != null & accessTokenSecret != null) {

				
						try {
							t.updateStatus(message);
							status = SUCCESS_STRING;
						} catch (TwitterException e) {
							e.printStackTrace();
							status = FAILURE_STRING;
						}
					
			}
			return status;
		}

		@Override
		protected void onPostExecute(String result) {
			
			if(SUCCESS_STRING.equals(result)){
                final String msg = resources.getString(R.string.twitter_successful_post);
                final VoiceableDialog finishedDialog = new VoiceableDialog(mActivity);
                finishedDialog.popup(msg, 1000);
			}
		}
	}

}
