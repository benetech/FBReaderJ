package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import org.benetech.android.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebActivity extends Activity{

	WebView wView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_view);
		String url=getIntent().getStringExtra("URL");
		
		wView=(WebView)findViewById(R.id.webView1);
		wView.getSettings().setJavaScriptEnabled(true);
        wView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if( url.contains("http://www.thushantwitterexample.com"))
                {
                    Uri uri = Uri.parse( url );
                    String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
                    setResult(RESULT_OK, getIntent().putExtra("verifier", oauthVerifier));
                    finish();
                    return true;
                }
                return false;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
            	super.onPageFinished(view, url);
            	if(url.contains("www.thushantwitterexample.com")){
            		Uri uri = Uri.parse( url );
                    String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
                    setResult(RESULT_OK, getIntent().putExtra("verifier", oauthVerifier));
                    finish();
                    
            	}
            }
        });
        wView.loadUrl(url);
        
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
