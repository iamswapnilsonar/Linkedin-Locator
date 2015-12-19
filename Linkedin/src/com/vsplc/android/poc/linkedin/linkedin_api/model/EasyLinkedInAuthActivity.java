package com.vsplc.android.poc.linkedin.linkedin_api.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.linkedin_api.interfaces.DownloadObserver;
import com.vsplc.android.poc.linkedin.linkedin_api.interfaces.EasyLinkedInConstants;
import com.vsplc.android.poc.linkedin.linkedin_api.webservices.AccessTokenWebService;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

/**
 * @Links 
 * 
 * https://developer.linkedin.com/apis
 * https://www.linkedin.com/secure/developer 
 * http://developer.linkedin.com/documents/profile-fields
 * https://developer.linkedin.com/forum/oauth-20-redirect-url-faq-invalid-redirecturi-error
 * 
 * @author VSPLC
 *
 */
public class EasyLinkedInAuthActivity extends Activity {

	private WebView _WebView = null;

	private ProgressDialog _Dialog = null;
	
	private boolean createdFirstTime = false;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.linkedin_auth_activity);
		initViews();
		setUpViews();

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		 super.onBackPressed();
	}
	
	private void initViews() {
		_WebView = (WebView) findViewById(R.id.LinkedInAuthActivity_web_view);
	}

	private void setUpViews() {

		String url = getOauthUrl(EasyLinkedIn.get_ConsumerKey(),
				EasyLinkedIn.get_CallBackUrl(), EasyLinkedIn.get_State(),
				EasyLinkedIn.get_Scope());
		
		configureWebView(_WebView.getSettings());
		_WebView.setWebViewClient(new MyClient());
		_WebView.loadUrl(url);

	}

	class MyClient extends WebViewClient {

		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d("Check", url);

			Uri uri = Uri.parse(url);
			Uri callBackUri = Uri.parse(EasyLinkedIn.get_CallBackUrl());

			String host = uri.getHost();
			String callBackHost = callBackUri.getHost();

			Logger.vLog("shouldOverrideUrlLoading", "Host : "+host);
			Logger.vLog("shouldOverrideUrlLoading", "callBackHost : "+callBackHost);
			
			if (host.equals(callBackHost)) {

				view.stopLoading();
				
				String code = uri.getQueryParameter("code");
				
				Logger.vLog("shouldOverrideUrlLoading", "code : "+code);
				
				try{
					
					AccessTokenWebService webservice = new AccessTokenWebService(
							EasyLinkedInAuthActivity.this,
							accessTokenDownloadObserver, getAccessTokenUrl(code,
									EasyLinkedIn.get_CallBackUrl(),
									EasyLinkedIn.get_ConsumerKey(),
									EasyLinkedIn.get_ConsumerSecretKey()),
							new JSONObject());
					
					webservice.execute();
					Log.d("Check", code);
					
				}catch(NullPointerException ex){
					// TODO: handle exception
					ex.printStackTrace();
					
					dismissDialog();
					finish();
					EasyLinkedIn.authCallback.onFailure();
					
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}							

			}

			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {

			super.onPageStarted(view, url, favicon);
			goneWebView();
			showDialog();

		}

		@Override
		public void onPageFinished(WebView view, String url) {

			super.onPageFinished(view, url);
			showWebView();
			dismissDialog();

		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void configureWebView(WebSettings webSettings) {

		webSettings.setJavaScriptEnabled(true);
		webSettings.setPluginState(PluginState.ON);
		webSettings.setRenderPriority(RenderPriority.HIGH);

	}

	private void showDialog() {
		
		if (!createdFirstTime) {
//			_Dialog = DialogBuilder.BuildDialog(this);
			_Dialog = new ProgressDialog(MethodUtils.getContextWrapper(EasyLinkedInAuthActivity.this));
			_Dialog.setMessage("Connecting to Linkedin Server..");
			_Dialog.setCancelable(false);		
			_Dialog.setCanceledOnTouchOutside(false);
			_Dialog.show();
			createdFirstTime = true;
		}else{
			_Dialog.show();
		}
		
	}

	private void dismissDialog() {

		_Dialog.dismiss();
	}

	private void showWebView() {

		_WebView.setVisibility(WebView.VISIBLE);

	}

	private void goneWebView() {

		_WebView.setVisibility(WebView.GONE);

	}

	public String getOauthUrl(String consumerKey, String redirectUri,
			String state, String scope) {

		return String
				.format(EasyLinkedInConstants.SERVER_NAME
						+ "uas/oauth2/authorization?response_type=code&client_id=%s&state=%s&redirect_uri=%s&scope=%s",
						consumerKey, state, redirectUri, scope);
	}

	public String getAccessTokenUrl(String authorizeCode, String redirectUri,
			String consumerKey, String consumerSecretKey) {

		String mAccessTokenUrl = String
				.format(EasyLinkedInConstants.SERVER_NAME
						+ "uas/oauth2/accessToken?grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
						authorizeCode, redirectUri, consumerKey,
						consumerSecretKey);
		
		Logger.vLog("getAccessTokenUrl : ", "Value : "+mAccessTokenUrl);
		
		return mAccessTokenUrl;
	}

	private DownloadObserver accessTokenDownloadObserver = new DownloadObserver() {

		@Override
		public void onDownloadingStart() {
			showDialog();
		}

		@Override
		public void onDownloadingComplete(Object data) {

			Log.d("accessTokenDownloadObserver Check", data.toString());
			
			try {
				
				Log.v("onDownloadingComplete", ""+data.toString());
				
				JSONObject jsonData = new JSONObject(data.toString());
				
				EasyLinkedIn
						.getSharedPreferenceEditor()
						.putString(EasyLinkedIn.EASY_LINKED_IN_ACCESS_TOKEN,
								jsonData.getString("access_token")).commit();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dismissDialog();
			finish();
			EasyLinkedIn.authCallback.onSucess("Success");
		}

		@Override
		public void onDownloadFailure(Object errorData) {

			Log.d("accessTokenDownloadObserver Check", errorData.toString());
			
			dismissDialog();
			finish();
			EasyLinkedIn.authCallback.onFailure();

		}
	};
}
