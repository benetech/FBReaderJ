package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import java.util.Set;

import org.benetech.android.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebActivity extends Activity {

	private final String VERIFIER_PARAM = "oauth_verifier";
	public static final String VERIFIER_EXTRA = "verifier";

	WebView wView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_view);
		String url = getIntent().getStringExtra("URL");

		wView = (WebView) findViewById(R.id.webView1);
		wView.getSettings().setJavaScriptEnabled(true);
		wView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				final Uri uri = Uri.parse(url);
				final Set<String> queryParams = uri.getQueryParameterNames();
				if (queryParams.contains(VERIFIER_PARAM)) {
					getVerifier(uri);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (url.contains(SocialNetworkKeys.TWITTER_CALLBACK_URL)) {
					Uri uri = Uri.parse(url);
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					setResult(RESULT_OK,
							getIntent().putExtra("verifier", oauthVerifier));
					finish();

				}
			}

			private void getVerifier(Uri uri) {
				String oauthVerifier = uri.getQueryParameter(VERIFIER_PARAM);
				setResult(RESULT_OK,
						getIntent().putExtra(VERIFIER_EXTRA, oauthVerifier));
				finish();
			}
		});
		wView.loadUrl(url);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
