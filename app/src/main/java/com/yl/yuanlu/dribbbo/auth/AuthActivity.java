package com.yl.yuanlu.dribbbo.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/12/2017.
 */

public class AuthActivity extends AppCompatActivity {

    public static final String KEY_CODE = "code";

    @BindView(R.id.web_view) WebView webView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Login to Dribbble");

        progressBar.setMax(100);

        webView.setWebViewClient(new WebViewClient() {
            //this callback function is called when the WebView is trying to load a URL
            //which in our case is the redirect URL
            //we utilize this function to catch the temporary token responded by Dribble API
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //must check because when authURL is loaded, the function will also be called
                if(url.startsWith(Auth.REDIRECT_URI)) {
                    //Use Uri to get the temporary token after "code=" easily
                    //parse the URL first, then call getQueryParameter() with parameter "code"
                    Uri uri = Uri.parse(url);
                    //going back to loginActivity, loginActivity used startActivityForResult() to start AuthActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(KEY_CODE, uri.getQueryParameter(KEY_CODE));
                    setResult(RESULT_OK, resultIntent);
                    //clear the cookie to disable auto login after logout
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().removeAllCookies(null);
                    }
                    finish();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            //setup the proper progressBar display
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        //showing the login page in WebView
        String authURL = Auth.getAuthURL();
        webView.loadUrl(authURL);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(webView!=null) webView.destroy();
        super.onDestroy();
    }
}
