package com.vsplc.android.poc.linkedin.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.adapter.ConnectionListAdapter;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.City;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

@SuppressLint("NewApi")
public class ConnectionFragment extends Fragment implements OnClickListener, OnBackStackChangedListener{

	private FragmentActivity mFragActivityContext;

	private ListView list;
	private ConnectionListAdapter adapter;
	private ArrayList<LinkedinUser> listLinkedinUsers;
	
	private Button btnLeft, btnMessageToChecked, btnMapToChecked;
	private TextView tvMapAll;
	private EditText editSearch;
	
	private CheckBox checkAllConnections;
	
	private boolean isCitysWorkCompleted = false;
	private boolean isGoogleMapRequested = false;
	
	@SuppressWarnings("unused")
	private boolean isSendMessageRequested = false;
	
	private ProgressDialog progressDialog;
	
	public static Set<String> checkedSetCities = new HashSet<String>();
	public static ArrayList<LinkedinUser> checkedArrayListLinkedinUsers = new ArrayList<LinkedinUser>();	
	
	public static boolean[] checkBoxState;
	
	FragmentManager fragmentManager;
		
	String callingFrom;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mFragActivityContext = getActivity();
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.connection_fragment, container, false);
		
		list = (ListView) view.findViewById(R.id.list);
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvMapAll = (TextView) view.findViewById(R.id.tv_map_all);
		
		checkAllConnections = (CheckBox) view.findViewById(R.id.checkbox_all);
		btnMessageToChecked = (Button) view.findViewById(R.id.btn_message_all);
		btnMapToChecked = (Button) view.findViewById(R.id.btn_map_connections);
		
		editSearch = (EditText) view.findViewById(R.id.edt_search); 
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle args = getArguments(); 

		fragmentManager = mFragActivityContext.getSupportFragmentManager();
		
		if (args != null) { // load web link received through bundle
			//NOP
			
			DataWrapper dataWrapper = (DataWrapper) args.getSerializable("connection_list");
			ArrayList<LinkedinUser> mConnections = dataWrapper.getList();
			
			listLinkedinUsers = mConnections;
			
			callingFrom = args.getString("callingFrom");
			
		}else{
			listLinkedinUsers = (ArrayList<LinkedinUser>)LinkedinApplication.listGlobalConnections;
		}
		
		if (callingFrom.equals("NavigationDrawer")) {
			btnLeft.setBackgroundResource(R.drawable.btn_list_tap_effect);
		}else{
			btnLeft.setBackgroundResource(R.drawable.btn_back_tap_effect);
		}
		
		// Sort all connections in ascending order..
		Collections.sort(listLinkedinUsers, LinkedinUser.LinkedinUserNameComparator);
		
		new AsyncTaskForCities().execute(listLinkedinUsers);
		
		// Getting adapter by passing xml data ArrayList
		adapter = new ConnectionListAdapter(this, mFragActivityContext, listLinkedinUsers, list);
		list.setAdapter(adapter);
		
		// View linkedin user profile on webview..
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
								
				Logger.vLog("ConnectionFragment ", "Size : "+ConnectionListAdapter.data.size());
				
				LinkedinUser user = (LinkedinUser) ConnectionListAdapter.data.get(position);				
				Logger.vLog("ConnectionFragment ", "User : "+user.toString());		
											
				hideKeyboardAndClearSearchText();
				
				cleanAndResetAllTheFlags();
				
				// Create fragment and give it an argument for the selected article
	            ProfileFragment profileFragment = (ProfileFragment) Fragment.instantiate(mFragActivityContext, 
	            						ConstantUtils.PROFILE_FRAGMENT);	           
	            
	            Bundle bundle = new Bundle();
				bundle.putString("profile_type", "ConnectionUser");
				bundle.putSerializable("user", user);
				profileFragment.setArguments(bundle);
	            
	            FragmentTransaction transaction = mFragActivityContext.getSupportFragmentManager().beginTransaction();

	            // Replace whatever is in the fragment_container view with this fragment,
	            // and add the transaction to the back stack so the user can navigate back
	            transaction.replace(R.id.fragment_container, profileFragment, "profile");
	            transaction.addToBackStack(null);

	            // Commit the transaction
	            transaction.commit();
			}
		});
		
		btnLeft.setOnClickListener(this);
		tvMapAll.setOnClickListener(this);
		
		btnMapToChecked.setOnClickListener(this);
		btnMessageToChecked.setOnClickListener(this);
		
		progressDialog = new ProgressDialog(MethodUtils.getContextWrapper(mFragActivityContext));	
		progressDialog.setCancelable(false);		
		progressDialog.setCanceledOnTouchOutside(false);
		
		
		list.setTextFilterEnabled(true);
                 
		editSearch.setTypeface(FontUtils.getLatoRegularTypeface(mFragActivityContext));
        editSearch.addTextChangedListener(new TextWatcher() {
        
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					System.out.println("Text ["+s+"] - Start ["+start+"] - Before ["+before+"] - Count ["+count+"]");
					
					if (count < before) {
						// We're deleting char so we need to reset the adapter data
						adapter.resetData();
					}
						
					adapter.getFilter().filter(s.toString());
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				}
			});
        
        editSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				if (hasFocus) {
					editSearch.setHint("");
				}else{
					editSearch.setHint("Browse Connections");
				}
				
			}
		});
        
        checkAllConnections.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
				adapter.selectedAll(isChecked);
				
			}
		});
        		
		fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				// TODO Auto-generated method stub
				
				/*Toast.makeText(mFragActivityContext, "Backstack Changed..", Toast.LENGTH_SHORT).show();
				
				int count = fragmentManager.getBackStackEntryCount();
				Logger.vLog("ProfileFragement", "Backstack Count : "+count);
				for (int i = 0; i < count; i++) {
					
					FragmentManager.BackStackEntry backStackEntry = (BackStackEntry) fragmentManager.getBackStackEntryAt(i);
					String str = backStackEntry.getName();
					Logger.vLog("ProfileFragement", "Fragment : "+str);
				}*/
			}
		});
        
	}

	public void hideKeyboardAndClearSearchText() {   
	    
		editSearch.clearFocus();
		editSearch.setText("");
		
		// Check if no view has focus:			
	    View view = mFragActivityContext.getCurrentFocus();
	    if (view != null) {
	        InputMethodManager inputManager = (InputMethodManager) mFragActivityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		switch (key) {
		
		case R.id.btn_left:	
			
			if (callingFrom.equals("NavigationDrawer")) {
				((BaseActivity) getActivity()).showHideNevigationDrawer();
			}else{
				getActivity().onBackPressed();
			}
			
			break;

		case R.id.tv_map_all:
			
			isGoogleMapRequested = true;

			if (isCitysWorkCompleted) {
				// fragment is ready to open..
				
				hideKeyboardAndClearSearchText();				
				cleanAndResetAllTheFlags();
				
				// Create fragment and give it an argument for the selected article
	            GoogleMapFragment mapFragment = (GoogleMapFragment) Fragment.instantiate(mFragActivityContext, 
	            						ConstantUtils.GOOGLE_MAP_FRAGMENT);	           
	            
	            Bundle bundle = new Bundle();
	            
	            String[] mArr = LinkedinApplication.cities.toArray(new String[LinkedinApplication.cities.size()]);
	            bundle.putStringArray("city_markers", mArr);
	            bundle.putString("marker_type", "MapAll");
	            bundle.putString("callingFrom","ConnectionFragment");
	            
	            DataWrapper dataWrapper = new DataWrapper(listLinkedinUsers);
				bundle.putSerializable("connection_list", dataWrapper);
	            
	            mapFragment.setArguments(bundle);
	            
	            FragmentTransaction transaction = fragmentManager.beginTransaction();

	            // Replace whatever is in the fragment_container view with this fragment,
	            // and add the transaction to the back stack so the user can navigate back
	            transaction.replace(R.id.fragment_container, mapFragment, "googlemap");
	            transaction.addToBackStack(null);

	            // Commit the transaction
	            transaction.commit();
				
				
			} else{
				progressDialog.setMessage("Preparing Google Map..");
				progressDialog.show();
			}
			
			break;
			
			
		case R.id.btn_message_all:
						
			hideKeyboardAndClearSearchText();
			
			checkedSetCities.clear();
			checkedArrayListLinkedinUsers.clear();
			
			for (int i = 0; i < ConnectionListAdapter.data.size(); i++) {
				
				LinkedinUser linkedinUser = (LinkedinUser) ConnectionListAdapter.data.get(i);
				
				if (checkBoxState[i]) {
										
					@SuppressWarnings("unused")
					String str = linkedinUser.fname + " " + linkedinUser.lname;
					
			    	ConnectionFragment.checkedArrayListLinkedinUsers.add(linkedinUser);		    				    	
				}else{
					//NOP
					ConnectionFragment.checkedArrayListLinkedinUsers.remove(linkedinUser);
					
//					ViewGroup row = (ViewGroup) buttonView.getParent();
//			    	row.setBackgroundResource(R.color.dark_blue_color_code);
				}
				
			}
			
			for (LinkedinUser user : checkedArrayListLinkedinUsers) {
				
				String str = (user.fname+" "+user.lname);
				Logger.vLog("btn_map_connections", str);
			}
			
			Logger.vLog("checkedArrayListLinkedinUsers", "Size : "+checkedArrayListLinkedinUsers.size());
			
			if (checkedArrayListLinkedinUsers.size() > 0 && checkedArrayListLinkedinUsers.size() <= 50) {

				// fragment is ready to open..
				progressDialog.dismiss();

				cleanAndResetAllTheFlags();

				// Create fragment and give it an argument for the selected article
				MessageFragment messageFragment = (MessageFragment) Fragment.instantiate(mFragActivityContext, 
						ConstantUtils.MESSAGE_FRAGMENT);           

				Bundle bundle = new Bundle();            

				DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)checkedArrayListLinkedinUsers);
				bundle.putSerializable("connection_list", dataWrapper);			
				bundle.putString("callingFrom","ConnectionFragment");
				messageFragment.setArguments(bundle);

				FragmentTransaction transaction = fragmentManager.beginTransaction();

				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack so the user can navigate back
				transaction.replace(R.id.fragment_container, messageFragment, "message");
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();

			}else{
				Toast.makeText(mFragActivityContext, "Check the recipents size. Minimum is one and max is 50 recipent.", Toast.LENGTH_SHORT).show();
			}
			
			break;
		
		case R.id.btn_map_connections:
			
			hideKeyboardAndClearSearchText();
			
			isGoogleMapRequested = true;
			
			checkedSetCities.clear();
			checkedArrayListLinkedinUsers.clear();
			
			for (int i = 0; i < checkBoxState.length; i++) {
				
				LinkedinUser linkedinUser = (LinkedinUser) ConnectionListAdapter.data.get(i);
				
				if (checkBoxState[i]) {
										
					@SuppressWarnings("unused")
					String str = linkedinUser.fname + " " + linkedinUser.lname;
			    	ConnectionFragment.checkedArrayListLinkedinUsers.add(linkedinUser);		    				    	
				}else{
					//NOP
					ConnectionFragment.checkedArrayListLinkedinUsers.remove(linkedinUser);
				}
				
			}
			
			for (LinkedinUser user : checkedArrayListLinkedinUsers) {
				
				String str = (user.fname+" "+user.lname);
				Logger.vLog("btn_map_connections", str);
			}
			
			for (LinkedinUser user : checkedArrayListLinkedinUsers) {
				
				if (user.fname.equals("private") || user.lname.equals("private")) {
					//NOP
				}else{
					
					String str = (user.fname+" "+user.lname);
					Logger.vLog("btn_map_connections", str);

					String mCity = MethodUtils.getCityNameFromLocation(user.location, user.country_code);

					if (ConnectionFragment.checkedSetCities.contains(mCity)){
						//NOP
					}else{
						ConnectionFragment.checkedSetCities.add(mCity);
					}
					
				}
			}
			
			for (String city : checkedSetCities) {
				Logger.vLog("btn_map_connections", "city : "+city);
			}
			
			if (checkedSetCities.size() > 0) {
				
				if (isCitysWorkCompleted) {

					// fragment is ready to open..
					progressDialog.dismiss();					
					cleanAndResetAllTheFlags();
					
					// Create fragment and give it an argument for the selected article
					GoogleMapFragment mapFragment = (GoogleMapFragment) Fragment.instantiate(mFragActivityContext, 
							ConstantUtils.GOOGLE_MAP_FRAGMENT);	           

					Bundle bundle = new Bundle();
					String[] mArr = checkedSetCities.toArray(new String[checkedSetCities.size()]);	            
					bundle.putStringArray("city_markers", mArr);
					bundle.putString("marker_type", "MapAll");
					bundle.putString("callingFrom","ConnectionFragment");

					DataWrapper dataWrapper = new DataWrapper(checkedArrayListLinkedinUsers);
					bundle.putSerializable("connection_list", dataWrapper);

					mapFragment.setArguments(bundle);

					FragmentTransaction transaction = fragmentManager.beginTransaction();

					// Replace whatever is in the fragment_container view with this fragment,
					// and add the transaction to the back stack so the user can navigate back
					transaction.replace(R.id.fragment_container, mapFragment, "googlemap");
					transaction.addToBackStack(null);

					// Commit the transaction
					transaction.commit();
				}
			}else{
				Toast.makeText(mFragActivityContext, "Select atleast one connection..", Toast.LENGTH_SHORT).show();
			}
			
			break;
			
		default:
			break;
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

							String address = mCity + "," + mCountry;
							String mLatitude, mLongitude;
							
							if (Geocoder.isPresent()) {

								LatLng latLng = MethodUtils.getLatLngFromGivenAddressGeoCoder(mFragActivityContext, address);

								mLatitude = String.valueOf(latLng.latitude);
								mLongitude = String.valueOf(latLng.longitude);

							}else{

								LatLng latLng = MethodUtils.getLatLongFromGivenAddress(address);

								mLatitude = String.valueOf(latLng.latitude);
								mLongitude = String.valueOf(latLng.longitude);
							}

							City city = new City(mCity, mCountry, mLatitude, mLongitude);

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
			
			isCitysWorkCompleted = true;
			
			if (isGoogleMapRequested && isCitysWorkCompleted && result.equals("Success")) {
				
				hideKeyboardAndClearSearchText();
				
				// fragment is ready to open..
				progressDialog.dismiss();				
				cleanAndResetAllTheFlags();
				
				// Create fragment and give it an argument for the selected article
	            GoogleMapFragment mapFragment = (GoogleMapFragment) Fragment.instantiate(mFragActivityContext, 
	            						ConstantUtils.GOOGLE_MAP_FRAGMENT);	           

	            Bundle bundle = new Bundle();
	            String[] mArr = LinkedinApplication.cities.toArray(new String[LinkedinApplication.cities.size()]);	            
	            bundle.putStringArray("city_markers", mArr);
	            bundle.putString("marker_type", "MapAll");
	            bundle.putString("callingFrom","ConnectionFragment");
	            
	            DataWrapper dataWrapper = new DataWrapper(listLinkedinUsers);
				bundle.putSerializable("connection_list", dataWrapper);
	            
				mapFragment.setArguments(bundle);
				
	            FragmentTransaction transaction = fragmentManager.beginTransaction();
	            
	            // Replace whatever is in the fragment_container view with this fragment,
	            // and add the transaction to the back stack so the user can navigate back
	            transaction.replace(R.id.fragment_container, mapFragment, "googlemap");
	            transaction.addToBackStack(null);

	            // Commit the transaction
	            transaction.commit();
	            
			}
			
			
		}

	}

	@SuppressWarnings("unused")
	private class AyscGettingCityInfo extends AsyncTask<Void, Integer, String> {

		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(Void... params) {

			for (String city : LinkedinApplication.cities) {
				
				City cityObject = LinkedinApplication.hashTableOfCityInfo.get(city);
				Logger.vLog("AyscGettingCityInfo : City ", cityObject.name+" Lat : "+cityObject.latitude+" Long : "+cityObject.longitude);				
				
				if (cityObject.latitude.equals("NA") && cityObject.longitude.equals("NA")) {

					String address = cityObject.name + "," + cityObject.country;
					if (Geocoder.isPresent()) {
						
						LatLng latLng = MethodUtils.getLatLngFromGivenAddressGeoCoder(mFragActivityContext, address);
						
						String lat = String.valueOf(latLng.latitude);
						String lng = String.valueOf(latLng.longitude);
						
						cityObject.setLatAndLong(lat, lng);										
					}else{
						
						LatLng latLng = MethodUtils.getLatLongFromGivenAddress(address);
						
						String lat = String.valueOf(latLng.latitude);
						String lng = String.valueOf(latLng.longitude);
						
						cityObject.setLatAndLong(lat, lng);
					}
					
					LinkedinApplication.hashTableOfCityInfo.put(cityObject.name, cityObject);
					
				}else{
					// NOP
				}
				
			}

			return "Completed";
		}

		@Override
		protected void onPostExecute(String result) {
			
			isCitysWorkCompleted = true;
			
			if (isGoogleMapRequested && isCitysWorkCompleted && result.equals("Completed")) {
				
				hideKeyboardAndClearSearchText();
				
				// fragment is ready to open..
				progressDialog.dismiss();
				cleanAndResetAllTheFlags();
				
				// Create fragment and give it an argument for the selected article
	            GoogleMapFragment mapFragment = (GoogleMapFragment) Fragment.instantiate(mFragActivityContext, 
	            						ConstantUtils.GOOGLE_MAP_FRAGMENT);	           

	            Bundle bundle = new Bundle();
	            String[] mArr = LinkedinApplication.cities.toArray(new String[LinkedinApplication.cities.size()]);
	            bundle.putStringArray("city_markers", mArr);
	            bundle.putString("marker_type", "MapAll");
	            bundle.putString("callingFrom","ConnectionFragment");
	            
	            DataWrapper dataWrapper = new DataWrapper(listLinkedinUsers);
				bundle.putSerializable("connection_list", dataWrapper);
	            
				mapFragment.setArguments(bundle);
				
	            FragmentTransaction transaction = fragmentManager.beginTransaction();
	            
	            // Replace whatever is in the fragment_container view with this fragment,
	            // and add the transaction to the back stack so the user can navigate back
	            transaction.replace(R.id.fragment_container, mapFragment, "googlemap");
	            transaction.addToBackStack(null);

	            // Commit the transaction
	            transaction.commit();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}

	@Override
	public void onBackStackChanged() {
		// TODO Auto-generated method stub
	}
	
	public void cleanAndResetAllTheFlags(){
		
		isCitysWorkCompleted = false;
		isGoogleMapRequested = false;
		isSendMessageRequested = false;
	}
}