package com.vsplc.android.poc.linkedin.fragments;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.linkedin_api.interfaces.Callback;
import com.vsplc.android.poc.linkedin.linkedin_api.interfaces.DownloadObserver;
import com.vsplc.android.poc.linkedin.linkedin_api.model.EasyLinkedIn;
import com.vsplc.android.poc.linkedin.linkedin_api.utils.Config;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;
import com.vsplc.android.poc.linkedin.networking.ResponseManager;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

@SuppressLint("NewApi")
public class LoginFragment extends Fragment implements OnClickListener, OnBackStackChangedListener{
    
	private FragmentActivity mFragActivityContext;
	private Button btnLogin;
	private TextView tvRights, tvHelp;
	
	private FragmentManager fragmentManager;
	
	private EasyLinkedIn _EasyLinkedIn;
	private ProgressDialog progressDialog;
	
	private boolean isAlertDialogShown = false;
	
	private LinkedinApplication application; 
	Dao<SignedLinkedinUser, Integer> signedLinkedinUserDao;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

    	mFragActivityContext = getActivity();
    	
    	application = (LinkedinApplication) mFragActivityContext.getApplication();	
    	
    	if (application != null) {
    		signedLinkedinUserDao = application.getSignedLinkedinUserDao();
//    		Toast.makeText(mFragActivityContext, "Application", Toast.LENGTH_SHORT).show();
		}else{
//			Toast.makeText(mFragActivityContext, "Application is NULL", Toast.LENGTH_SHORT).show();
		}
    	
    	
    	
        _EasyLinkedIn = EasyLinkedIn.getInstance(mFragActivityContext, Config.LINKEDIN_CONSUMER_KEY, Config.LINKEDIN_CONSUMER_SECRET, 
    			"https://www.linkedin.com", "", "");
    	
    	// Inflate the layout for this fragment
    	View view = inflater.inflate(R.layout.login_fragment, container, false);

    	btnLogin = (Button) view.findViewById(R.id.btn_login);
    	btnLogin.setOnClickListener(this);
    	
    	tvRights = (TextView) view.findViewById(R.id.tv_rights);
    	tvHelp = (TextView) view.findViewById(R.id.tv_help);
    	
    	tvRights.setTypeface(FontUtils.getLatoLightTypeface(mFragActivityContext));
    	tvHelp.setTypeface(FontUtils.getLatoLightTypeface(mFragActivityContext));
    	
    	tvRights.setOnClickListener(this);
    	tvHelp.setOnClickListener(this);
    	
        // showing progress dialog while performing heavy tasks..
		progressDialog = new ProgressDialog(MethodUtils.getContextWrapper(mFragActivityContext));	
		progressDialog.setCancelable(false);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
        
    	return view;		
	}

	@Override
	public void onStart() {
		super.onStart();

		fragmentManager = mFragActivityContext.getFragmentManager();
		fragmentManager.addOnBackStackChangedListener(this);
		
		((BaseActivity) getActivity()).disableNavigationDrawer();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		switch (key) {
		
		case R.id.btn_login:
			
			if (MethodUtils.isNetworkAvailable(mFragActivityContext)) {
				
				_EasyLinkedIn.authorize(mFragActivityContext, new Callback() {

			        @SuppressLint("NewApi")
					@Override
			        public void onSucess(Object data) {		        	
			        	Logger.vLog("onSucess : ", ""+data.toString());
			        	new AsyncGetUserProfileDetails().execute();
			        }

			        @Override
			        public void onFailure() {
			        	Logger.vLog("onFailure : ", "Autorization failure");
			        	
			        	if (!isAlertDialogShown) {		        		
						}
			        	
			        }			        
			        
			    });
				
			}else{
				MethodUtils.noNetworkConnectionDialog(mFragActivityContext);
			}					   
			
			break;
			
		case R.id.tv_rights:
			break;
		
		case R.id.tv_help:
			break;

		default:
			break;
		}
		
	}
	
	public class AsyncGetUserProfileDetails extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {			
			Logger.vLog("AsyncGetUserProfileDetails","doInBackground");			
			// Get user info
			_EasyLinkedIn.getUserInfo(mFragActivityContext, getUserDetailsDownloadObserver, ConstantUtils.SIGNED_USER_INFO_FEILDS);
			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {
			Logger.vLog("AsyncGetUserProfileDetails",result);				
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// Make webservice call to fetch all the user details
	        progressDialog.setMessage("Processing..");
	        progressDialog.show();
			
		}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}
	
	
	private class DoingLengthyTask extends AsyncTask<Object, Void, String> {

		@Override
		protected String doInBackground(Object... params) {

			Logger.vLog("DoingLengthyTask","doInBackground");	
			
			Object data = params[0];

    		ResponseManager manager = new ResponseManager();
    		
    		try {
    			
				SignedLinkedinUser user = manager.parseUserResponse(data);
				Logger.vLog("getUserDetailsDownloadObserver", user.toString());
					
				try {
					signedLinkedinUserDao.create(user);			
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}					

				try {
					Log.v("LoginFragment", "Total Connections : "+ signedLinkedinUserDao.countOf());
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
				MethodUtils.saveObject(mFragActivityContext, user);	
				
	            LinkedinApplication.signedLinkedinUser = MethodUtils.getObject(mFragActivityContext);
				Logger.vLog("getUserDetailsDownloadObserver", LinkedinApplication.signedLinkedinUser.toString());
				
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}    	
			
			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {
			
			if (result.equals("Success")) {
				
				// Open up connection list layout
				progressDialog.dismiss();
				
				Logger.vLog("onPostExecute", "Hello");
				
				((BaseActivity)mFragActivityContext).startFetchingTheConnectionsByAsyncTask();							
				
				// Create fragment and give it an argument for the selected article
	            ProfileFragment profileFragment = (ProfileFragment) Fragment.instantiate(mFragActivityContext, 
	            						ConstantUtils.PROFILE_FRAGMENT);
	            
	            SignedLinkedinUser signedLinkedinUser = MethodUtils.getObject(mFragActivityContext);
	            
	            // update the navigation drawer with user information..
	            ((BaseActivity)mFragActivityContext).updateNavigationDrawer(signedLinkedinUser);
	            
				Logger.vLog("getUserDetailsDownloadObserver", signedLinkedinUser.toString());
				
	            Bundle bundle = new Bundle();
	            bundle.putString("profile_type", "AppUser");
	            bundle.putSerializable("user", signedLinkedinUser);	            
	            profileFragment.setArguments(bundle);
	            
	            FragmentTransaction transaction = mFragActivityContext.getSupportFragmentManager().beginTransaction();

	            // Replace whatever is in the fragment_container view with this fragment,
	            // and add the transaction to the back stack so the user can navigate back
	            transaction.replace(R.id.fragment_container, profileFragment, "profile");
	            transaction.addToBackStack("NavigationProfileFragment");

	            // Commit the transaction
	            transaction.commitAllowingStateLoss();
		        
			}else{
				// NOP				
			}
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Downloading.."); 
		}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}
	
	
	private DownloadObserver getUserDetailsDownloadObserver = new DownloadObserver() {

    	@Override
    	public void onDownloadingStart() {}

    	@SuppressLint("NewApi")
		@Override
    	public void onDownloadingComplete(Object data) {
    		Log.v("onDownloadingComplete : ", ""+data.toString());	
    		new DoingLengthyTask().execute(data);
    	}

    	@Override
    	public void onDownloadFailure(Object errorData) {}
    };

	@Override
	public void onBackStackChanged() {
		// TODO Auto-generated method stub
		
		int count = fragmentManager.getBackStackEntryCount();
		
		for (int i = 0; i < count; i++) {
			
			FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
			String str = backStackEntry.getName();
			
			Toast.makeText(mFragActivityContext, str, Toast.LENGTH_SHORT).show();
			
		}
		
	}
    
}