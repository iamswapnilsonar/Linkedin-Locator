package com.vsplc.android.poc.linkedin.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.City;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

@SuppressLint({ "InflateParams", "NewApi" })
public class GoogleMapFragment extends Fragment implements OnClickListener, OnInfoWindowClickListener {
	
	private GoogleMap map;
	private SupportMapFragment fragment;
	private Marker marker;
	
	private Button btnLeft;
	private TextView tvListAll;
	
	static final LatLng INDIA = new LatLng(21.0000, 78.0000);
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    
    private FragmentActivity mFragActivityContext;
    
	private double minLatitude = Integer.MAX_VALUE;
	private double maxLatitude = Integer.MIN_VALUE;
	private double minLongitude = Integer.MAX_VALUE;
	private double maxLongitude = Integer.MIN_VALUE;
	
	private ProgressDialog progressDialog;
	
	private String[] arrOfCities;
	private List<LinkedinUser> mConnections = new ArrayList<LinkedinUser>();
	
	FragmentManager fragmentManager;
	
	private String markerType;
	private LinkedinUser linkedinUser;	
	private LatLng mLinkedinUserPosition = new LatLng(0.0, 0.0);
	
	private String callerFragment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.vLog("onCreateView", "I am in onCreateView");
		
		mFragActivityContext = getActivity();
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.map_fragment, container, false);
		
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvListAll = (TextView) view.findViewById(R.id.tv_show_list);
		
        return view;
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
		Logger.vLog("onActivityCreated", "I am in onActivityCreated");
		
		FragmentManager fm = getChildFragmentManager();
		fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
		
		if (fragment == null) {
			
			Logger.vLog("onActivityCreated", "Fragment is null. Creating new instance of google map.");
			
			fragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map, fragment).commit();
		}
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Logger.vLog("onStart", "I am in onStart");
		
		fragmentManager = mFragActivityContext.getSupportFragmentManager();
				
		Bundle args = getArguments();
		
		if (args != null) {
			
			markerType = args.getString("marker_type");
			
			if (markerType.equals("MapAll")) {
				
				arrOfCities = args.getStringArray("city_markers");
								
				DataWrapper dataWrapper = (DataWrapper) args.getSerializable("connection_list");
				mConnections = dataWrapper.getList();
				Logger.vLog("GoogleMapFragment", "mConnections : "+mConnections.size());	
			}else{
				
				linkedinUser = (LinkedinUser) args.getSerializable("user");				
			}
			
			callerFragment = args.getString("callingFrom");
		}
		
		if (callerFragment.equals("NavigationDrawer")) {
			btnLeft.setBackgroundResource(R.drawable.btn_list_tap_effect);
		}else{
			btnLeft.setBackgroundResource(R.drawable.btn_back_tap_effect);
		}
		
		btnLeft.setOnClickListener(this);
		tvListAll.setOnClickListener(this);
		
		fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				// TODO Auto-generated method stub
				
//				Toast.makeText(mFragActivityContext, "Backstack Changed..", Toast.LENGTH_SHORT).show();
//				
//				int count = fragmentManager.getBackStackEntryCount();
//				Logger.vLog("ProfileFragement", "Backstack Count : "+count);
//				for (int i = 0; i < count; i++) {
//					
//					FragmentManager.BackStackEntry backStackEntry = (BackStackEntry) fragmentManager.getBackStackEntryAt(i);
//					String str = backStackEntry.getName();
//					Logger.vLog("ProfileFragement", "Fragment : "+str);
//				}
			}
		});
		
		
		
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Logger.vLog("onResume", "I am in onResume");

		if (map == null) {
			map = fragment.getMap();
			
//			map.setMyLocationEnabled(true);
//	        LocationManager locationManager = (LocationManager) mFragActivityContext.getSystemService(Context.LOCATION_SERVICE);
//	        Criteria criteria = new Criteria();
//	        
//	        String bestProvider = locationManager.getBestProvider(criteria, true);
//	        Location location = locationManager.getLastKnownLocation(bestProvider);
//	        
//	        MyLocationListener listener = new MyLocationListener();
//	        
//	        if (location != null) {
//	            listener.onLocationChanged(location);
//	        }
//	        
//	        locationManager.requestLocationUpdates(bestProvider, 20000, 0, listener);
			
//			map.setMapType(GoogleMap.);
		}
		
		if (markerType.equals("MapAll")) {
			
			// use google map for mulitple markers
			new AyscTaskForSettingOfMarkers().execute(arrOfCities);
			map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			map.setOnInfoWindowClickListener(this);
//			tvListAll.setVisibility(View.INVISIBLE);
			
		}else{			

			tvListAll.setVisibility(View.INVISIBLE);
			btnLeft.setBackgroundResource(R.drawable.btn_back_tap_effect);
			
			new AsyncTaskForShowingSingleMarker().execute(linkedinUser);
			
		}

	}
	
	private class AsyncTaskForShowingSingleMarker extends AsyncTask<Object, Void, String>{

		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(Object... params) {
			// TODO Auto-generated method stub
			
			String result;
			
			linkedinUser = (LinkedinUser) params[0];
			
			// try indivisual marker
			if (linkedinUser.location != null && linkedinUser.country_code != null) {

				String mCity = MethodUtils.getCityNameFromLocation(linkedinUser.location, linkedinUser.country_code);
				String mCountry = MethodUtils.getISOCountryNameFromCC(linkedinUser.country_code);

				String address;

				if (mCity.equals("NA"))
					address = mCountry;
				else
					address = mCity + "," + mCountry;

				if (Geocoder.isPresent())
					mLinkedinUserPosition = MethodUtils.getLatLngFromGivenAddressGeoCoder(mFragActivityContext, address);
				else
					mLinkedinUserPosition = MethodUtils.getLatLongFromGivenAddress(address);

				result = "Success";
			}else{
				result = "Failure";
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result.equals("Success") && map != null) {

				@SuppressWarnings("unused")
				Marker linkedinUserMarker = map.addMarker(new MarkerOptions()
				.position(mLinkedinUserPosition)
				.title(linkedinUser.fname +" "+linkedinUser.lname)
				.snippet(linkedinUser.location+", "+MethodUtils.getISOCountryNameFromCC(linkedinUser.country_code))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.img_marker)));
				
				// Move the camera instantly to hamburg with a zoom of 15.
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLinkedinUserPosition, 15));

				// Zoom in, animating the camera.
				map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

			}else{
				Toast.makeText(mFragActivityContext, "Failed in getting geo-cordinates.", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	private class AyscTaskForSettingOfMarkers extends AsyncTask<String[], Object, String> {

		@SuppressLint("NewApi")
		@Override
		protected String doInBackground(String[]... params) {

			String[] arrCityMarkers = params[0];
			
			Logger.vLog("doInBackground", "Array of Cities : "+arrCityMarkers.length);

			for (int i = 0; i < arrCityMarkers.length; i++) {

				String cityName = arrCityMarkers[i];

				if (LinkedinApplication.hashTableOfCityInfo.containsKey(cityName)) {
					
					// If city is available					
					Logger.vLog("onProgressUpdate - City ", cityName);
					
					City city = LinkedinApplication.hashTableOfCityInfo.get(cityName);
					Logger.vLog("AyscGettingCityInfo : City ", city.name);

					if (city.latitude.equals("NA") && city.longitude.equals("NA")) {
						String address = city.name + "," + city.country;
						
						if (Geocoder.isPresent()) {
							
							LatLng latLng = MethodUtils.getLatLngFromGivenAddressGeoCoder(mFragActivityContext, address);
							
							city.latitude = String.valueOf(latLng.latitude);
							city.longitude = String.valueOf(latLng.longitude);
							
						}else{
							
							LatLng latLng = MethodUtils.getLatLongFromGivenAddress(address);
							
							city.latitude = String.valueOf(latLng.latitude);
							city.longitude = String.valueOf(latLng.longitude);
							
						}
						
					}
					
					publishProgress(city);
				}
			}

			return "Completed";
		}

		@SuppressLint("NewApi")
		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
//			String mCity = values[0];
			
			City city = (City) values[0];
			
//			Logger.vLog("onProgressUpdate - City ", mCity);
			
//			City city = LinkedinApplication.hashTableOfCityInfo.get(mCity);
			Logger.vLog("AyscGettingCityInfo : City ", city.name);

			/*if (city.latitude.equals("NA") && city.longitude.equals("NA")) {
				String address = city.name + "," + city.country;
				
				if (Geocoder.isPresent()) {
					
					LatLng latLng = MethodUtils.getLatLngFromGivenAddressGeoCoder(mFragActivityContext, address);
					
					city.latitude = String.valueOf(latLng.latitude);
					city.longitude = String.valueOf(latLng.longitude);
					
				}else{
					
					LatLng latLng = MethodUtils.getLatLongFromGivenAddress(address);
					
					city.latitude = String.valueOf(latLng.latitude);
					city.longitude = String.valueOf(latLng.longitude);
					
				}
				
			}	*/				
			
			if (map != null) {

				try {
										
					Double mLat = Double.parseDouble(city.latitude);
					Double mLong = Double.parseDouble(city.longitude);
					
					LatLng latLng = new LatLng(mLat, mLong);
					
					Logger.vLog("AyscGettingCityInfo : ", "City - Latitude "+ mLat);
					Logger.vLog("AyscGettingCityInfo : ", "City - Longitude "+ mLong);

					maxLatitude = Math.max(mLat, maxLatitude);
					minLatitude = Math.min(mLat, minLatitude);
					maxLongitude = Math.max(mLong, maxLongitude);
					minLongitude = Math.min(mLong, minLongitude);

					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(latLng);
					markerOptions.title(city.name);
					markerOptions.snippet(city.country);
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.img_marker));
					
					Marker marker = map.addMarker(markerOptions);

					Logger.vLog("AyscTaskForSettingOfMarkers", "Marker "+ marker.getTitle().toString() + "added");
				} catch (Exception ex) {
					Logger.vLog("AyscTaskForSettingOfMarkers","Error while creating markers"+ex.toString());
				}

			}else{
				Logger.vLog("AyscTaskForSettingOfMarkers","Google Map is NULL");
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
	
			progressDialog.dismiss();
			
			/**
			 * Display all the pin center of screen. All pins are visible.
			 * 
			 * @Date 15 December, 2015
			 * @author Swapnil
			 */
			if (map != null) {

				map.setOnCameraChangeListener(new OnCameraChangeListener() {

					@Override
					public void onCameraChange(CameraPosition cameraPosition) {

						try{

							Logger.vLog("setOnCameraChangeListener :", "minLatitude : "+minLatitude+" \n minLongitude : "+minLongitude+
									"maxLatitude : "+maxLatitude+" \n maxLongitude : "+maxLongitude);

							// Move camera.						
							map.moveCamera(CameraUpdateFactory
									.newLatLngBounds(new LatLngBounds(new LatLng(minLatitude, minLongitude),
											new LatLng(maxLatitude, maxLongitude)), 15));

//							// Move the camera instantly to hamburg with a zoom of 15.
//							map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLinkedinUserPosition, 15));

							// Zoom in, animating the camera.
							map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
							
							// Remove listener to prevent position reset on camera move.
							map.setOnCameraChangeListener(null);

						}catch(Exception ex){
							Logger.vLog("Exception :", ex.toString());
						}

					}
				});

			}
			
//			// Move the camera instantly to hamburg with a zoom of 15.
//			map.moveCamera(CameraUpdateFactory.newLatLngZoom(INDIA, 7));
//
//			// Zoom in, animating the camera.
//			map.animateCamera(CameraUpdateFactory.zoomTo(5), 2000, null);

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			 progressDialog = new ProgressDialog(MethodUtils.getContextWrapper(mFragActivityContext));
			 progressDialog.setMessage("Preparing GoogleMap..");
			 progressDialog.setCancelable(false);
			 progressDialog.setCanceledOnTouchOutside(false);
			 progressDialog.show();
		}
		
	}


	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		switch (key) {
		case R.id.btn_left:
			
			if (callerFragment.equals("NavigationDrawer")) {
				((BaseActivity) getActivity()).showHideNevigationDrawer();
			}else{
				getActivity().onBackPressed();
			}
			
			break;

		case R.id.tv_show_list:
			            
			if (callerFragment.equals("NavigationDrawer")) {
								
				FragmentTransaction transaction = mFragActivityContext.getSupportFragmentManager().beginTransaction();
				
				// Create fragment and give it an arguments if any
				ConnectionFragment targetFragment = (ConnectionFragment) Fragment.instantiate(mFragActivityContext, ConstantUtils.CONNECTION_FRAGMENT);
				
				Bundle bundle = new Bundle();
				
				DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)mConnections);
				bundle.putSerializable("connection_list", dataWrapper);
				bundle.putString("callingFrom","GoogleMapFragment");
				targetFragment.setArguments(bundle);
				
				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack so the user can navigate back
				transaction.replace(R.id.fragment_container, targetFragment, "connections");

				transaction.addToBackStack(null);		 
				transaction.commit();
				
			}else{
				// NOP
				getActivity().onBackPressed();
			}	
			
			break;
			
		default:
			break;
		}
		
	}
	
	private class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private View view;

		public CustomInfoWindowAdapter() {
			view = mFragActivityContext.getLayoutInflater().inflate(R.layout.marker_infowindow_layout, null);
		}

		@Override
		public View getInfoContents(Marker marker) {

			if (GoogleMapFragment.this.marker != null
					&& GoogleMapFragment.this.marker.isInfoWindowShown()) {
				
				GoogleMapFragment.this.marker.hideInfoWindow();
			}
			return null;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			
			GoogleMapFragment.this.marker = marker;
			
			TextView titleUi = ((TextView) view.findViewById(R.id.tv_marker_location));
			TextView tvShowConnection = ((TextView) view.findViewById(R.id.tv_show_connection));
			
			titleUi.setTypeface(FontUtils.getLatoRegularTypeface(mFragActivityContext));
			tvShowConnection.setTypeface(FontUtils.getLatoRegularTypeface(mFragActivityContext));
			
			if (marker.getTitle() != null && marker.getSnippet() != null) {
				
				final String text = marker.getTitle() +","+marker.getSnippet();
				
				if (text != null) {
					titleUi.setText(text);
				}
				
			}else{
				titleUi.setText("Your Current Location");	
				tvShowConnection.setVisibility(View.INVISIBLE);
			}
			
			return view;
		}
	}


	@Override
	public void onInfoWindowClick(final Marker marker) {
		// TODO Auto-generated method stub
			
		if (marker.isInfoWindowShown()) {
			marker.hideInfoWindow();
		}
		
		if (marker.getTitle() != null && marker.getSnippet() != null) {
			
			FragmentTransaction transaction = fragmentManager.beginTransaction();

			// Create fragment and give it an argument for the selected article
			ConnectionFragment connectionFragment = (ConnectionFragment) Fragment.instantiate(mFragActivityContext, 
					ConstantUtils.CONNECTION_FRAGMENT);
			
			Bundle bundle = new Bundle();
			
			Logger.vLog("onInfoWindowClick","City : "+marker.getTitle());
			Logger.vLog("onInfoWindowClick","Country : "+marker.getSnippet());
			
			if (mConnections.size() > 0) {
				
				ArrayList<LinkedinUser> mConns = MethodUtils.getCitywiseConnections(mConnections, marker.getTitle(), marker.getSnippet());
				
				DataWrapper dataWrapper = new DataWrapper(mConns);
				bundle.putSerializable("connection_list", dataWrapper);
				
			}
			
			bundle.putString("callingFrom","GoogleMapFragment");
			connectionFragment.setArguments(bundle);

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.fragment_container, connectionFragment, "connections");
			
			transaction.addToBackStack(null);		 
			transaction.commit();
			
		}
		
	}
	
	class MyLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
			double latitude = location.getLatitude();
	        double longitude = location.getLongitude();
	        
	        LatLng latLng = new LatLng(latitude, longitude);
	        map.addMarker(new MarkerOptions().position(latLng));	      
	        
//	        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//	        map.animateCamera(CameraUpdateFactory.zoomTo(15));
	        
	     // Move the camera instantly to hamburg with a zoom of 15.
//			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//
//			// Zoom in, animating the camera.
//			map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	        
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
