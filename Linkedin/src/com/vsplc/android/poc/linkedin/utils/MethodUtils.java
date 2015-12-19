package com.vsplc.android.poc.linkedin.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;

public class MethodUtils {

	public static LatLng getLatLngFromGivenAddressGeoCoder(Context context, String youraddress) {
		
		LatLng position;
		
		double latitude = 0;
		double longitude = 0;
		
		Geocoder geoCoder = new Geocoder(context);		
		
		try {
			
			List<Address> addresses = geoCoder.getFromLocationName(youraddress, 1); 
			
			if (addresses.size() >  0) {
				latitude = addresses.get(0).getLatitude(); 
				longitude = addresses.get(0).getLongitude(); 
			}

		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace(); 
			
			LatLng latLng = MethodUtils.getLatLongFromGivenAddress(youraddress);
			
			latitude = latLng.latitude;
			longitude = latLng.longitude;
			
		}

		Logger.vLog("getLatLngFromGivenAddressGeoCoder", "Address : "+youraddress+" Latitude : "+
									latitude+" Longitude : "+longitude);
		
		position = new LatLng(latitude, longitude);
		
		return position;
	}

	public static LatLng getLatLongFromGivenAddress(String youraddress) {

		double lat = 0;
		double lng = 0;

		youraddress = youraddress.replaceAll("\\s", "%20");
		
		String uri = "http://maps.google.com/maps/api/geocode/json?address="
				+ youraddress + "&sensor=false";

		HttpGet httpGet = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());

			lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");

			Logger.vLog("latitude", "" + lat);
			Logger.vLog("longitude", "" + lng);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Logger.vLog("getLatLongFromGivenAddress", "Address : "+youraddress+" Latitude : "+lat+" Longitude : "+lng);
		
		return new LatLng(lat, lng);

	}
	
	public static void printSet(Set<String> set) {

		// Get an iterator
		Iterator<String> iterator = set.iterator();

		// Display elements
		while (iterator.hasNext()) {
			Logger.vLog("printSet", "Value : " + iterator.next());
		}

	}

	public static Map<String, List<LinkedinUser>> getCountrywiseConnections(
			List<LinkedinUser> listConnections) {

		Map<String, List<LinkedinUser>> mapCountrywiseConnections = new HashMap<String, List<LinkedinUser>>();
		Iterator<String> iterator = LinkedinApplication.setOfGlobalCountries
				.iterator();

		while (iterator.hasNext()) {
			String code = iterator.next();

			List<LinkedinUser> list = new ArrayList<LinkedinUser>();

			for (int i = 0; i < listConnections.size(); i++) {
				LinkedinUser user = listConnections.get(i);

				if (user.country_code != null && user.country_code.equals(code)) {
					// NOP
					list.add(user);
				}
			}

			Logger.vLog("Users", "" + list.size());
			mapCountrywiseConnections.put(code, list);
		}

		return mapCountrywiseConnections;
	}

	public static List<LinkedinUser> getTotalCountrywiseConnections(String country,
			List<LinkedinUser> listConnections) {

		List<LinkedinUser> list = new ArrayList<LinkedinUser>();

		for (int i = 0; i < listConnections.size(); i++) {
			LinkedinUser user = listConnections.get(i);

			if (user.country_code != null && user.country_code.equals(country)) {
				// NOP
				list.add(user);
			}
		}

		Logger.vLog("Users list : ", "" + list.size());
		return list;
	}
	
	public static ArrayList<LinkedinUser> getIndustrywiseConnections(
			String industry, List<LinkedinUser> connections) {

		ArrayList<LinkedinUser> filteredConnections = new ArrayList<LinkedinUser>();

		for (int i = 0; i < connections.size(); i++) {
			LinkedinUser user = connections.get(i);

			if (user.industry != null && user.industry.equals(industry)) {
				// NOP
				filteredConnections.add(user);
			}
		}

		Logger.vLog(industry + "Users", "" + filteredConnections.size());

		for (LinkedinUser user : filteredConnections)
			Logger.vLog("Print", "Value : " + user.toString());
		return filteredConnections;
	}

	public static ArrayList<LinkedinUser> getCitywiseConnections(String city, String country) {

		List<LinkedinUser> connections = LinkedinApplication.listGlobalConnections;
		Logger.vLog("City : ", city);
		
		ArrayList<LinkedinUser> filteredConnections = new ArrayList<LinkedinUser>();

		for (int i = 0; i < connections.size(); i++) {
			LinkedinUser user = connections.get(i);

			if (user.location != null && user.location.contains(city)) {
				// NOP
				filteredConnections.add(user);
			}
		}
		
		return filteredConnections;
	}
	
	
	public static ArrayList<LinkedinUser> getCitywiseConnections(List<LinkedinUser> mConnections, String city, String country) {

		List<LinkedinUser> connections =  mConnections;
		Logger.vLog("City : ", city);
		
		ArrayList<LinkedinUser> filteredConnections = new ArrayList<LinkedinUser>();

		for (int i = 0; i < connections.size(); i++) {
			LinkedinUser user = connections.get(i);

			if (user.location != null && user.location.contains(city)) {
				// NOP
				filteredConnections.add(user);
			}
		}
		
		return filteredConnections;
	}
	
	public static void printMap(Map<String, List<LinkedinUser>> map) {

		for (Entry<String, List<LinkedinUser>> entry : map.entrySet()) {

			String country_code = entry.getKey();
			Logger.vLog("CC", country_code);

			List<LinkedinUser> list = entry.getValue();
			Logger.vLog("Users", "" + list.size());

			for (LinkedinUser user : list)
				Logger.vLog("Print", "Value : " + user.toString());
		}
	}

	/**
	 * get city from location area.
	 * 
	 * @param location
	 * @param countryCode
	 * @return
	 */
	public static String getCityNameFromLocation(String location,
			String countryCode) {

		String str = location;

		String strWordArea = "Area";
		String strWordCountry = getISOCountryNameFromCC(countryCode);
		String strWordSemiColon = ",";

		if (str.equalsIgnoreCase(strWordCountry)) {
			// NOP
			str = "NA";
		} else {
			if (str.contains(strWordArea)) {
				str = str.replace(" " + strWordArea, "");
				if (str.contains(strWordCountry)) {
					str = str.replace(" " + strWordCountry, "");
					if (str.contains(strWordSemiColon)) {
						str = str.replace(strWordSemiColon, "");

					}
				}
			}
		}

		// System.out.println(str);
		return str;
	}

	/**
	 * get ISO Country name from Country Code.
	 * 
	 * @param countryCode
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String getISOCountryNameFromCC(String countryCode) {
		Locale obj = new Locale("", countryCode.toUpperCase());
		String strCountryName = obj.getDisplayCountry();
		return strCountryName;
	}
	
	
    public static SignedLinkedinUser getObject(Context context){
    	SharedPreferences mPrefs = ((BaseActivity) context).getPreferences(Context.MODE_PRIVATE);
    	Gson gson = new Gson();
        String json = mPrefs.getString("SignedLinkedinUser", "");
        SignedLinkedinUser user = gson.fromJson(json, SignedLinkedinUser.class);
        return user;
    }
    
    public static void saveObject(Context context, SignedLinkedinUser object){

    	SharedPreferences mPrefs = ((BaseActivity) context).getPreferences(Context.MODE_PRIVATE);
    	Editor prefsEditor = mPrefs.edit();

    	Gson gson = new Gson();
    	String json = gson.toJson(object);
    	prefsEditor.putString("SignedLinkedinUser", json);
    	prefsEditor.commit();

    	Logger.vLog("saveObject", "Object Saved..");
    }
    
    public static boolean isBetween(int x, int lower, int upper) {
		return lower <= x && x <= upper;
	}
    
    @SuppressLint("InlinedApi")
	public static ContextWrapper getContextWrapper(Context context){

    	int API_LEVEL = Integer.valueOf(android.os.Build.VERSION.SDK_INT);

    	ContextWrapper wrapper;

    	if (MethodUtils.isBetween(API_LEVEL, 8, 10)) {

    		Logger.vLog("TimePickerFragment", "API Level is : "+API_LEVEL);
    		wrapper = new ContextThemeWrapper(context, android.R.style.Theme_Light);

    	} else if (MethodUtils.isBetween(API_LEVEL, 11, 13)) {

    		Logger.vLog("TimePickerFragment", "API Level is : "+API_LEVEL);
    		wrapper = new ContextThemeWrapper(context, android.R.style.Theme_Holo);

    	} else {
    		Logger.vLog("TimePickerFragment", "API Level is : "+API_LEVEL);
    		wrapper = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light); 
    	}		    	

    	return wrapper;
    }
    
    @SuppressWarnings("deprecation")
	public static boolean isNetworkAvailable(Context context) {

    	try {

    		ConnectivityManager connec = (ConnectivityManager) context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);

    		if (connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null
    				&& connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
    			return true;

    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    		if (tm.getDataState() == TelephonyManager.DATA_CONNECTED || tm.getDataState() == TelephonyManager.DATA_CONNECTING)
    			return true;

    		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {

    			if (connec.getNetworkInfo(ConnectivityManager.TYPE_WIMAX) != null
    					&& connec.getNetworkInfo(ConnectivityManager.TYPE_WIMAX).getState() == NetworkInfo.State.CONNECTED)
    				return true;
    		}

    	} catch (Exception ex) {
    		ex.printStackTrace(); 
    		return true;
    	} catch (Error ex) {
    		ex.printStackTrace();
    		return true;
    	}
    	
    	return false;		
	}
    
    public static void noNetworkConnectionDialog(Context context) {
		
    	AlertDialog dialog = new AlertDialog.Builder(getContextWrapper(context))
				.setTitle("Error").setMessage("It seems you're not connected to the internet!")
				.setIcon(android.R.drawable.stat_notify_error)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				}).create();
		
		dialog.show();
	}
}
