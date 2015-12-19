package com.vsplc.android.poc.linkedin;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.vsplc.android.poc.linkedin.adapter.NavDrawerListAdapter;
import com.vsplc.android.poc.linkedin.fragments.ConnectionFragment;
import com.vsplc.android.poc.linkedin.fragments.GoogleMapFragment;
import com.vsplc.android.poc.linkedin.fragments.IndustriesFragment;
import com.vsplc.android.poc.linkedin.fragments.LoginFragment;
import com.vsplc.android.poc.linkedin.fragments.MessageFragment;
import com.vsplc.android.poc.linkedin.fragments.ProfileFragment;
import com.vsplc.android.poc.linkedin.linkedin_api.interfaces.DownloadObserver;
import com.vsplc.android.poc.linkedin.linkedin_api.model.EasyLinkedIn;
import com.vsplc.android.poc.linkedin.linkedin_api.utils.Config;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.City;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.model.NavDrawerItem;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;
import com.vsplc.android.poc.linkedin.networking.ResponseManager;
import com.vsplc.android.poc.linkedin.utils.CircleTransform;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

public class BaseActivity extends FragmentActivity implements View.OnClickListener {
	
	private String LOG_TAG = BaseActivity.class.getSimpleName();
	
	// slide menu items
	private String[] navMenuTitles;
	private String[] navMenuItemTags;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	// Navigation drawer header
	private ImageView mImageProfile;
	private TextView mProfileName, mProfileID;	
	
	private Context mContext;
	
	//Linkedin Auth variables
	private EasyLinkedIn _EasyLinkedIn;
	
	ArrayList<LinkedinUser> listLinkedinUsers;
	private ProgressDialog pDialog;
		
	private boolean isConnectionsWorkCompleted = false;
	
	private boolean isConnectionsRequested = false;
	private boolean isGoogleMapRequested = false;
	private boolean isIndustriesListRequested = false;
	private boolean isSendMessageRequested = false;
	
	private boolean isGetDataFirstTime = false;
	
	FragmentManager fragmentManager;
	
	private LinkedinApplication application; 
	public Dao<LinkedinUser, Integer> connectionsDao;
//	public Dao<SignedLinkedinUser, Integer> signedLinkedinUserDao;
	public Dao<City, Integer> cityInfoDao;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.base_activity);
	
		 mContext = BaseActivity.this;
		 application = (LinkedinApplication) getApplication();
		 
		 connectionsDao = application.getLinkedInconnectionsDao();
//		 signedLinkedinUserDao = application.getSignedLinkedinUserDao();
		 cityInfoDao = application.getCityInfoDao();
		 
		 fragmentManager = getSupportFragmentManager();
		 
		 initUI();
		  
		 if(MethodUtils.isNetworkAvailable(mContext)){
			Toast.makeText(mContext, "Network is available..!!", Toast.LENGTH_SHORT).show(); 
		 }else{
			 MethodUtils.noNetworkConnectionDialog(mContext);
		 }
		 
		 // showing progress dialog while performing heavy tasks..
		 pDialog = new ProgressDialog(MethodUtils.getContextWrapper(mContext));	
		 pDialog.setCancelable(false);
		 pDialog.setCancelable(false);
		 pDialog.setCanceledOnTouchOutside(false);
		 
		 listLinkedinUsers = new ArrayList<LinkedinUser>();
		 
		 _EasyLinkedIn = EasyLinkedIn.getInstance(mContext, Config.LINKEDIN_CONSUMER_KEY, Config.LINKEDIN_CONSUMER_SECRET, 
	    			"https://www.linkedin.com", "", "");
		 
		 FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		 		 
		 if (EasyLinkedIn.hasAccessToken()) {
			 // User Access Token
			 Logger.vLog("Access Token", EasyLinkedIn.getAccessToken());
			 Logger.vLog("Access Secret", EasyLinkedIn.getAccessSecret());
			 
			 if (!isGetDataFirstTime) {
				 
				 Logger.vLog("App has token", "isGetDataFirstTime : "+isGetDataFirstTime);
				 
				 isGetDataFirstTime = true;
				 startFetchingTheConnectionsByAsyncTask();
				 
				 LinkedinApplication.signedLinkedinUser = MethodUtils.getObject(mContext);
				 Logger.vLog("BaseActivity", LinkedinApplication.signedLinkedinUser.toString());

				 // updating navigation drawer..
				 updateNavigationDrawer(LinkedinApplication.signedLinkedinUser);
			 }
			 			 
			 // Create fragment and give it an argument for the selected article
			 ProfileFragment profileFragment = (ProfileFragment) Fragment.instantiate(mContext, 
					 				ConstantUtils.PROFILE_FRAGMENT);
			 
			 Bundle bundle = new Bundle();
			 bundle.putString("profile_type", "AppUser");
			 bundle.putSerializable("user", LinkedinApplication.signedLinkedinUser);
			 profileFragment.setArguments(bundle);

			 // Replace whatever is in the fragment_container view with this fragment,
			 // and add the transaction to the back stack so the user can navigate back
			 transaction.replace(R.id.fragment_container, profileFragment, "profile");
			 transaction.addToBackStack("NavigationProfileFragment");
			 
		 }else{
			 
			 transaction.replace(R.id.fragment_container, Fragment.instantiate(mContext, ConstantUtils.LOGIN_FRAGMENT), "login");
			 transaction.addToBackStack("NavigationLoginFragment");
		 }
		 
		 	 
		 transaction.commit();
		
		 
		 fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
				
				@Override
				public void onBackStackChanged() {
					// TODO Auto-generated method stub
					
					int count = fragmentManager.getBackStackEntryCount();
					Logger.vLog("ProfileFragement", "Backstack Count : "+count);
					for (int i = 0; i < count; i++) {
						
						FragmentManager.BackStackEntry backStackEntry = (BackStackEntry) fragmentManager.getBackStackEntryAt(i);
						String str = backStackEntry.getName();
						Logger.vLog("ProfileFragement", "Fragment : "+str);
					}
				}
			});
	}

	private void initUI() {
		// TODO Auto-generated method stub
		
		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		navMenuItemTags = getResources().getStringArray(R.array.nav_drawer_tags);
		
		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		mImageProfile = (ImageView) findViewById(R.id.iv_profile);
		mProfileName = (TextView) findViewById(R.id.tv_profile_name);
		mProfileID = (TextView) findViewById(R.id.tv_profile_email);
		
		mProfileName.setTypeface(FontUtils.getLatoBlackTypeface(mContext));
		mProfileID.setTypeface(FontUtils.getLatoRegularTypeface(mContext));		
		
		navDrawerItems = new ArrayList<NavDrawerItem>();

		for (int i = 0; i < navMenuTitles.length; i++) {
			// adding nav drawer items to array
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1), false, "22", navMenuItemTags[i]));
		}
		
		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);
	}

	public void updateNavigationDrawer(SignedLinkedinUser signedLinkedinUser){
		
		mImageProfile = (ImageView) findViewById(R.id.iv_profile);
		mProfileName = (TextView) findViewById(R.id.tv_profile_name);
		mProfileID = (TextView) findViewById(R.id.tv_profile_email);
		
		Picasso picasso = Picasso.with(mContext);
		RequestCreator creator = picasso.load(signedLinkedinUser.profilepicture);
		creator.resize(120, 120);
		creator.centerCrop();
		creator.placeholder(R.drawable.btn_viewprofile_pressed);
		creator.error(R.drawable.btn_viewprofile_pressed);
		creator.transform(new CircleTransform());
		creator.into(mImageProfile);
		
		mProfileName.setText(signedLinkedinUser.fname+" "+signedLinkedinUser.lname);
		mProfileID.setText(signedLinkedinUser.email);
		
	}
	
	/**
	 * Slide menu item click listener
	 **/
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

//			try{

				String listItemTAG = navDrawerItems.get(position).getTag();
				Logger.vLog("SlideMenuClickListener - NAV ", listItemTAG);

				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
				String fragmentTAG = fragment.getTag();
				Logger.vLog("SlideMenuClickListener - FRAGMENT", fragmentTAG);

				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
				
				// If we are at Profile fragment and want to go to Profile again..
				if (listItemTAG.equals(fragmentTAG)) {
					// NOP
				}else{

					FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
					Fragment targetFragment = null;
					String tagFragment = null;
					String strBackStackEntry = null;

					if (EasyLinkedIn.hasAccessToken()) {

						// User Access Token
						Logger.vLog("Access Token", EasyLinkedIn.getAccessToken());
						Logger.vLog("Access Secret", EasyLinkedIn.getAccessSecret());

						if (position == 0) {
																			
							// Create fragment and give it an arguments if any
							targetFragment = (ProfileFragment) Fragment.instantiate(mContext, ConstantUtils.PROFILE_FRAGMENT);

							Bundle bundle = new Bundle();
							bundle.putString("profile_type", "AppUser");
							
//							LinkedinUser user = MethodUtils.getObject(mContext);
							Logger.vLog("BaseActivity", LinkedinApplication.signedLinkedinUser.toString());
							 
							bundle.putSerializable("user", LinkedinApplication.signedLinkedinUser);
							targetFragment.setArguments(bundle);

							tagFragment = "profile";							
							strBackStackEntry = "NavigationProfileFragment";
							
						}else if(position == 1){
							
							isConnectionsRequested = true;

							Logger.vLog("Nav Drawer : ", "isConnectionsRequested : "+isConnectionsRequested);
							Logger.vLog("Nav Drawer : ", "isConnectionsWorkCompleted : "+isConnectionsWorkCompleted);
							
							if (isConnectionsWorkCompleted) {

								Logger.vLog("Nav Drawer : ", "Progress Dialog : "+pDialog);
								pDialog.dismiss();

								// Create fragment and give it an arguments if any
								targetFragment = (ConnectionFragment) Fragment.instantiate(mContext, ConstantUtils.CONNECTION_FRAGMENT);
								tagFragment = "connections";
								strBackStackEntry = "NavigationConnectionsFragment";
								
								Bundle bundle = new Bundle();
								DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections);
								bundle.putSerializable("connection_list", dataWrapper);
								bundle.putString("callingFrom","NavigationDrawer");
								targetFragment.setArguments(bundle);

							}else{
								
								if (pDialog != null) {
									
									// Make webservice call to fetch all the connnction data
									pDialog.setMessage("Fetching Connections..");
									pDialog.show();
									
								}else{
									Logger.vLog("BaseActivity", "pDialog value is null");
								}

							}
						}else if (position == 2){
							
							isGoogleMapRequested = true;

							if (isConnectionsWorkCompleted) {

								pDialog.dismiss();

								// Create fragment and give it an arguments if any
								targetFragment = (GoogleMapFragment) Fragment.instantiate(BaseActivity.this, ConstantUtils.GOOGLE_MAP_FRAGMENT);
								tagFragment = "googlemap";
								strBackStackEntry = "NavigationGoogleMapFragment";
								
					            Bundle bundle = new Bundle();
					            String[] mArr = LinkedinApplication.cities.toArray(new String[LinkedinApplication.cities.size()]);	            
					            bundle.putStringArray("city_markers", mArr);
					            bundle.putString("marker_type", "MapAll");
					            bundle.putString("callingFrom","NavigationDrawer");
					            
					            DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections);
								bundle.putSerializable("connection_list", dataWrapper);
								
								targetFragment.setArguments(bundle);

							}else{
								// Make webservice call to fetch all the connnction data
								pDialog.setMessage("Preparing Google Map..");
								pDialog.show();
							}
							
						}else if(position == 3){
							
							isIndustriesListRequested = true;

							if (isConnectionsWorkCompleted) {

								pDialog.dismiss();

								// Create fragment and give it an arguments if any
								targetFragment = (IndustriesFragment) Fragment.instantiate(mContext, ConstantUtils.INDUSTRIES_FRAGMENT);
								tagFragment = "industries";
								strBackStackEntry = "NavigationIndustriesFragment";

							}else{
								// Make webservice call to fetch all the connnction data
								pDialog.setMessage("Fetching Connections..");
								pDialog.show();
							}
						}else if(position == 4){
							
							isSendMessageRequested = true;

							if (isConnectionsWorkCompleted) {

								pDialog.dismiss();

								// Create fragment and give it an arguments if any
								targetFragment = (MessageFragment) Fragment.instantiate(mContext, ConstantUtils.MESSAGE_FRAGMENT);								
								tagFragment = "message";
								strBackStackEntry = "NavigationSendMessageFragment";
								
					            Bundle bundle = new Bundle();
					            bundle.putString("callingFrom","NavigationDrawer");					         							
								targetFragment.setArguments(bundle);

							}else{
								// Make webservice call to fetch all the connnction data
								pDialog.setMessage("Fetching Connections..");
								pDialog.show();
							}
							
						}else if(position == 7){
							
							EasyLinkedIn.getSharedPreferenceEditor().clear().commit();
							
							LinkedinApplication.iConnectionCount = 0;
							LinkedinApplication.hashTableOfCityInfo.clear();							
							LinkedinApplication.listGlobalConnections.clear();
							LinkedinApplication.mapCountrywiseConnections.clear();
							LinkedinApplication.setOfGlobalCountries.clear();
							LinkedinApplication.setOfGlobalIndustryNames.clear();							
							
							if(EasyLinkedIn.hasAccessToken()){
								
								Logger.vLog("Access Token", EasyLinkedIn.getAccessToken());
								Logger.vLog("Access Secret", EasyLinkedIn.getAccessSecret());
								
							}else{
								Logger.vLog("BaseActivity", "AccessToken not available..");
							}
							
							Toast.makeText(mContext, "Signout Successfully..", Toast.LENGTH_SHORT).show();
							
							// Create fragment and give it an arguments if any
							targetFragment = (LoginFragment) Fragment.instantiate(mContext, ConstantUtils.LOGIN_FRAGMENT);
							tagFragment = "login";
							strBackStackEntry = "NavigationLoginFragment";
							
						}
						
					}else{
						// Create fragment and give it an arguments if any
						targetFragment = (LoginFragment) Fragment.instantiate(mContext, ConstantUtils.LOGIN_FRAGMENT);
						tagFragment = "login";
						strBackStackEntry = "NavigationLoginFragment";
					}

					
					Logger.vLog("BaseActivity", "targetFragment : "+targetFragment+"tagFragment : "+tagFragment);
					
					if (targetFragment != null && tagFragment != null && strBackStackEntry != null) {
						
						// Replace whatever is in the fragment_container view with this fragment,
						// and add the transaction to the back stack so the user can navigate back
						transaction.replace(R.id.fragment_container, targetFragment, tagFragment);

						transaction.addToBackStack(strBackStackEntry);		 
						transaction.commit();
						
					}

				}
				
//			}catch (Exception ex) {
//				// TODO: handle exception
//				Logger.vLog("Nav-Drawer", ex.toString());
//				Toast.makeText(mContext, "Unable to Open..", Toast.LENGTH_SHORT).show();
//			}
		}
	}

	public void showHideNevigationDrawer(){
		
		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		} else {
			mDrawerLayout.openDrawer(Gravity.LEFT); 
		} 
		
	}
	
	public void disableNavigationDrawer(){
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	public void startFetchingTheConnectionsByAsyncTask(){
		
		AsyncGetAllConnections asyncConnections = new AsyncGetAllConnections();
        
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
        	asyncConnections.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
        	asyncConnections.execute((Void[])null);
	}
	
	public class AsyncGetAllConnections extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {			
			
			Logger.vLog("AsyncGetAllConnections","doInBackground");						
			_EasyLinkedIn.getConnections(mContext, getUserInfoDownloadObserver, ConstantUtils.USER_INFO_FEILDS);			
			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {
			
			Logger.vLog("AsyncGetAllConnections","onPostExecute");	
			
			if (isConnectionsRequested && isConnectionsWorkCompleted && result.equals("Success")) {
				
				// Open up connection list layout
				pDialog.dismiss();
				
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				// Create fragment and give it an arguments if any
				ConnectionFragment targetFragment = (ConnectionFragment) Fragment.instantiate(mContext, ConstantUtils.CONNECTION_FRAGMENT);
				
				Bundle bundle = new Bundle();
				DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections);
				bundle.putSerializable("connection_list", dataWrapper);
				bundle.putString("callingFrom","NavigationDrawer");
				targetFragment.setArguments(bundle);
				
				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack so the user can navigate back
				transaction.replace(R.id.fragment_container, targetFragment, "connections");

				transaction.addToBackStack("NavigationConnectionsFragment");		 
				transaction.commit();
		        
			}else{
				// NOP				
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}
	
	private DownloadObserver getUserInfoDownloadObserver = new DownloadObserver() {

    	@Override
    	public void onDownloadingStart() {}

    	@SuppressLint("NewApi")
		@Override
    	public void onDownloadingComplete(Object data) {
    		Logger.vLog("onDownloadingComplete : ", ""+data.toString());
    		
    		ResponseManager manager = new ResponseManager();
    		
    		try {
    			
    			@SuppressWarnings("unused")
				int connection_count = manager.parseConnectionCount(data);
    			
    			// result jo mil raha h wo compare karo with stored result
    			if (LinkedinApplication.listGlobalConnections.size() > 0) {
					
    				isConnectionsWorkCompleted = true;
    				openFrgamentIfUserRequested();
    				
				}else{
					
					DoingLengthyTask asyncLengthyTask = new DoingLengthyTask();
					
					if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
						asyncLengthyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object)data);
					else
						asyncLengthyTask.execute((Object)data);					
				}
    			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    		
    	}

    	@Override
    	public void onDownloadFailure(Object errorData) {}
    };
    
	private class DoingLengthyTask extends AsyncTask<Object, Void, String> {

		@Override
		protected String doInBackground(Object... params) {

			Logger.vLog("DoingLengthyTask","doInBackground");	
			
			Object data = params[0];

			try {

				List<LinkedinUser> temp = new ResponseManager().parse(data);
				
				// Saving to database
				for (LinkedinUser user : temp) {
					
					try {
						connectionsDao.create(user);				
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					
				}
				
				try {
					Log.v(LOG_TAG, "Total Connections : "+ connectionsDao.countOf());
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				listLinkedinUsers.clear();
				listLinkedinUsers.addAll(temp);		
				
				// save the connections information globally. 
				LinkedinApplication.listGlobalConnections = listLinkedinUsers;
				Log.v("MainActivity : ", "Total Connection Size : "+listLinkedinUsers.size());
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("MainActivity : ", "Exception : "+e.toString());
			}
			
			return "All Connection are fetched..!!";
		}

		@Override
		protected void onPostExecute(String result) {
			Logger.vLog("DoingLengthyTask","onPostExecute");	
			new AsyncTaskForCities().execute(LinkedinApplication.listGlobalConnections);			
		}
		
	}
    
	private class AsyncTaskForCities extends AsyncTask<Object, Void, String> {

		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(Object... params) {
			// TODO Auto-generated method stub

			Logger.vLog("LinkedinApplication.listCityInfo : ", "Size : "+LinkedinApplication.hashTableOfCityInfo.size());
			
			@SuppressWarnings("unchecked")
			ArrayList<LinkedinUser> connections = (ArrayList<LinkedinUser>) params[0];
			LinkedinApplication.cities.clear();
			
			for (int i = 0; i < connections.size(); i++) {

				LinkedinUser user = connections.get(i);

				if (user.location != null && user.country_code != null) {

					String mCity = MethodUtils.getCityNameFromLocation(user.location, user.country_code);

					// Prepared how many city connections available..
					if (mCity != null && LinkedinApplication.cities.add(mCity)) {

						if (LinkedinApplication.hashTableOfCityInfo.containsKey(mCity)) {
							//NOP
						}else{
							
							String mCountry = MethodUtils.getISOCountryNameFromCC(user.country_code);

							@SuppressWarnings("unused")
							String address = mCity + "," + mCountry;
							/*String mLatitude, mLongitude;
							
							if (Geocoder.isPresent()) {

								LatLng latLng = MethodUtils.getLatLngFromGivenAddressGeoCoder(BaseActivity.this, address);

								mLatitude = String.valueOf(latLng.latitude);
								mLongitude = String.valueOf(latLng.longitude);

							}else{

								LatLng latLng = MethodUtils.getLatLongFromGivenAddress(address);

								mLatitude = String.valueOf(latLng.latitude);
								mLongitude = String.valueOf(latLng.longitude);
							}*/

							City city = new City(mCity, mCountry, "NA", "NA");

							LinkedinApplication.hashTableOfCityInfo.put(mCity, city);
						}
					
					} else {
						Logger.vLog("hashTableOfCityInfo", mCity + " already present.");
					}

				}

			}

			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {
			
			isConnectionsWorkCompleted = true;
			openFrgamentIfUserRequested();
		}		

	}
	
	private void openFrgamentIfUserRequested() {
		// TODO Auto-generated method stub
		
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		Fragment targetFragment = null;
		String tagFragment = null;
		String strBackStackEntry = null;
				
		if (isConnectionsWorkCompleted) {
			
			if (isGoogleMapRequested) {
				
				// fragment is ready to open..
				pDialog.dismiss();
				
				// Create fragment and give it an argument for the selected article
				targetFragment = (GoogleMapFragment) Fragment.instantiate(BaseActivity.this, 
	            						ConstantUtils.GOOGLE_MAP_FRAGMENT);	           

	            Bundle bundle = new Bundle();
	            String[] mArr = LinkedinApplication.cities.toArray(new String[LinkedinApplication.cities.size()]);	            
	            bundle.putStringArray("city_markers", mArr);
	            bundle.putString("marker_type", "MapAll");
	            bundle.putString("callingFrom","NavigationDrawer");
	            
	            DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections);
				bundle.putSerializable("connection_list", dataWrapper);
	            
				targetFragment.setArguments(bundle);				
	            
	            tagFragment = "googlemap";
	    		strBackStackEntry = "NavigationGoogleMapFragment";
	            
			}else if(isConnectionsRequested){
				
				// fragment is ready to open..
				pDialog.dismiss();
				
				// Create fragment and give it an arguments if any
				targetFragment = (ConnectionFragment) Fragment.instantiate(mContext, ConstantUtils.CONNECTION_FRAGMENT);
				
				Bundle bundle = new Bundle();
				DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections);
				bundle.putSerializable("connection_list", dataWrapper);
				bundle.putString("callingFrom","NavigationDrawer");
				targetFragment.setArguments(bundle);
	            
	            tagFragment = "connections";
	    		strBackStackEntry = "NavigationConnectionsFragment";
				
			}else if(isIndustriesListRequested){
				
				// fragment is ready to open..
				pDialog.dismiss();
				
				// Create fragment and give it an arguments if any
				targetFragment = (IndustriesFragment) Fragment.instantiate(mContext, ConstantUtils.INDUSTRIES_FRAGMENT);	       
	            
	            tagFragment = "industries";
	    		strBackStackEntry = "NavigationIndustriesFragment";
	            
			}else if(isSendMessageRequested){
				
				// fragment is ready to open..
				pDialog.dismiss();
				
				// Create fragment and give it an arguments if any
				targetFragment = (MessageFragment) Fragment.instantiate(mContext, ConstantUtils.MESSAGE_FRAGMENT);								
				
	            Bundle bundle = new Bundle();
	            bundle.putString("callingFrom","NavigationDrawer");					         							
				targetFragment.setArguments(bundle);
				
	            tagFragment = "message";
	    		strBackStackEntry = "NavigationSendMessageFragment";            
	            
			}
			
		}
		
		if (targetFragment != null && tagFragment != null && strBackStackEntry != null) {
			
			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.fragment_container, targetFragment, tagFragment);

			transaction.addToBackStack(strBackStackEntry);		 
			transaction.commit();
			
		}
		
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		switch (key) {
			
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
				
		int count = fragmentManager.getBackStackEntryCount();
		Logger.vLog("ProfileFragement", "Backstack Count : "+count);

		if (count > 1) {
			
			FragmentManager.BackStackEntry backStackEntry = (BackStackEntry) fragmentManager.getBackStackEntryAt(count-1);
			String str = backStackEntry.getName();
			Logger.vLog("ProfileFragement", "Fragment : "+str);

			String backStackEntryForNavigationProfile = "NavigationProfileFragment";
			String backStackEntryForNavigationLogin = "NavigationLoginFragment";
			
			if(backStackEntryForNavigationProfile.equals(str) || backStackEntryForNavigationLogin.equals(str)){
				
				// clear all the flags and temp data..
				cleanUpAndCloseApplication();			
				this.finish();
				
			}else{
				super.onBackPressed();
			}
			
		}else if(count == 1){
			
			// clear all the flags and temp data..
			cleanUpAndCloseApplication();			
			this.finish();
		}
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		
		if (LinkedinApplication.hashTableOfCityInfo.size() > 0) {

			for (String cityName : LinkedinApplication.cities) {

				City city = LinkedinApplication.hashTableOfCityInfo.get(cityName);

				try {
					cityInfoDao.create(city);				
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}

			}

			try {
				Log.v(LOG_TAG, "Total Connections : "+ cityInfoDao.countOf());
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
				
		super.onPause();
	}
	
	private void cleanUpAndCloseApplication() {
		// TODO Auto-generated method stub
		
		isGetDataFirstTime = false;
		
		isConnectionsWorkCompleted = false;		
		isConnectionsRequested = false;
		isGoogleMapRequested = false;
		isIndustriesListRequested = false;
		isSendMessageRequested = false;
	}
}
