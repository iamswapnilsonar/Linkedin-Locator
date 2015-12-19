package com.vsplc.android.poc.linkedin.networking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;

/**
 * This is JSON parsing class which parse the response received from Linkedin Server.
 * 
 * @Date 19 Sept, 2014
 * @author VSPLC
 */
@SuppressWarnings("unused")
public class ResponseManager {

	// response status values
	private static final String TAG_CONNECTION_COUNT = "_count";
	private static final String TAG_CONNECTION_START = "_start";
	private static final String TAG_CONNECTION_TOTAL = "_total";
	private static final String TAG_CONNECTION_VALUES = "values";

	// users details
	private static final String TAG_LK_USER_ID = "id";
	private static final String TAG_LK_USER_FIRST_NAME = "firstName";
	private static final String TAG_LK_USER_LAST_NAME = "lastName";
	private static final String TAG_LK_USER_INDUSTRY = "industry";
	private static final String TAG_LK_USER_LOCATION = "location";
	
	private static final String TAG_LK_USER_EMAIL = "emailAddress";
	private static final String TAG_LK_USER_LANGUAGES = "languages";
	private static final String TAG_LK_USER_SKILLS = "skills";
	private static final String TAG_LK_USER_SUMMARY = "summary";
	private static final String TAG_TOTAL = "_total";
	private static final String TAG_LK_USER_LANGUAGE = "language";
	private static final String TAG_LK_USER_SKILL = "skill";
	private static final String TAG_LK_USER_CONNECTION_COUNT = "numConnections";	

	// location parameters
	private static final String TAG_LK_USER_LOCATION_COUNTRY = "country";
	private static final String TAG_LK_USER_LOCATION_COUNTRY_CODE = "code";
	private static final String TAG_NAME = "name";

	private static final String TAG_LK_USER_HEADLINE = "headline";
	private static final String TAG_LK_USER_PICTURE_URL = "pictureUrl";	
	private static final String TAG_LK_USER_PROFILE_URL = "publicProfileUrl";
	
	
	private static final String TAG_RESPONSE_CODE = "errorCode";	
	private static final String TAG_RESPONSE_MESSAGE = "message";
	private static final String TAG_RESPONSE_REQ_ID = "requestId";
	private static final String TAG_RESPONSE_SATUS = "status";
	private static final String TAG_RESPONSE_TIMESTAMP = "timestamp";

	/**
	 * @Date : 29 August, 2013 
	 * This is the function to parse Data (where Data is in JSON format)
	 * @author VSPLC
	 * @param res response from web service
	 * @param Url Category for web service 
	 **/
	public List<LinkedinUser> parse(Object jsonResponseObject) throws Exception { 

		List<LinkedinUser> listLinkedinUsers = new ArrayList<LinkedinUser>();
		
		JSONObject jsonObject = (JSONObject) jsonResponseObject;

		try {

			int iConnectionCount = jsonObject.getInt(TAG_CONNECTION_COUNT);
			Logger.vLog("Connection Count : ", "" + iConnectionCount);
			LinkedinApplication.iConnectionCount = iConnectionCount;
			
			int iConnectionStart = jsonObject.getInt(TAG_CONNECTION_START);
			int iConnectionTotal = jsonObject.getInt(TAG_CONNECTION_TOTAL);

			// contacts JSONArray
			JSONArray arrLKConnectionValues = jsonObject.getJSONArray(TAG_CONNECTION_VALUES);

			// when error found at server side may this will come as NULL
			if (arrLKConnectionValues != null) {

				// looping through all connections
				for (int i = 0; i < arrLKConnectionValues.length(); i++) {

					String fname = null, lname = null, id = null, industry = null, country_code = null, location = null;
					String profilepicture = null, profileurl = null, headline = null;
					
					LinkedinUser linkedinUser;

					JSONObject jsonObjectLKUser = arrLKConnectionValues.getJSONObject(i);
					fname = jsonObjectLKUser.getString(TAG_LK_USER_FIRST_NAME);

					id = jsonObjectLKUser.getString(TAG_LK_USER_ID);

					try {
						headline = jsonObjectLKUser.getString(TAG_LK_USER_HEADLINE);						
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}
					
					try {						
						industry = jsonObjectLKUser.getString(TAG_LK_USER_INDUSTRY);						
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}

					try {
						lname = jsonObjectLKUser.getString(TAG_LK_USER_LAST_NAME);
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}

					try {

						JSONObject jsonObjectLKUserLocation = jsonObjectLKUser.getJSONObject(TAG_LK_USER_LOCATION);

						try {

							JSONObject jsonObjectLKUserLocationCountry = jsonObjectLKUserLocation
									.getJSONObject(TAG_LK_USER_LOCATION_COUNTRY);
							
							country_code = jsonObjectLKUserLocationCountry
									.getString(TAG_LK_USER_LOCATION_COUNTRY_CODE);

							try {
								location = jsonObjectLKUserLocation.getString(TAG_NAME);
							} catch (JSONException ex_json) {
								ex_json.printStackTrace();
							}

						} catch (JSONException ex_json) {
							ex_json.printStackTrace();
						}

					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}
					
					try {
						profilepicture = jsonObjectLKUser.getString(TAG_LK_USER_PICTURE_URL);
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}

					try {
						profileurl = jsonObjectLKUser.getString(TAG_LK_USER_PROFILE_URL);
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}

					linkedinUser = new LinkedinUser(id, fname, lname, industry, country_code, location, profilepicture, profileurl, headline);
					
					// Add the LinkedinUser to list
					listLinkedinUsers.add(linkedinUser);
					
					// Prepared how many country connections available..
					if (country_code != null) {
						LinkedinApplication.setOfGlobalCountries.add(country_code);
					}
					
					// how many industry connections available..
					if (industry != null) {
						LinkedinApplication.setOfGlobalIndustryNames.add(industry);
					}
					
				}

			}

		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}
		
		return listLinkedinUsers;
	}// end of parse() method
	
	
	/**
	 * @Date : 23 December, 2014 
	 * @author VSPLC
	 **/
	public SignedLinkedinUser parseUserResponse(Object jsonResponseObject) throws Exception { 
		
		JSONObject jsonObject = (JSONObject) jsonResponseObject;

		try {
			
			int iResponseCode = jsonObject.getInt(TAG_RESPONSE_CODE);
			Logger.vLog("ResponseCode : ", "" + iResponseCode);
			return null;
			
		} catch (JSONException jsonException) {
			
			String fname = null, lname = null, id = null, industry = null, country_code = null, location = null;
			String profilepicture = null, profileurl = null, headline = null;
						
			String email = null, summary = null, connections = null;
			String[] languages = new String[0], skills = new String[0];			

			SignedLinkedinUser signedLinkedinUser;
			
			fname = jsonObject.getString(TAG_LK_USER_FIRST_NAME);

			id = jsonObject.getString(TAG_LK_USER_ID);

			try {
				headline = jsonObject.getString(TAG_LK_USER_HEADLINE);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {
				industry = jsonObject.getString(TAG_LK_USER_INDUSTRY);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {
				lname = jsonObject.getString(TAG_LK_USER_LAST_NAME);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {

				JSONObject jsonObjectLKUserLocation = jsonObject.getJSONObject(TAG_LK_USER_LOCATION);

				try {

					JSONObject jsonObjectLKUserLocationCountry = jsonObjectLKUserLocation
							.getJSONObject(TAG_LK_USER_LOCATION_COUNTRY);

					country_code = jsonObjectLKUserLocationCountry.getString(TAG_LK_USER_LOCATION_COUNTRY_CODE);

					try {
						location = jsonObjectLKUserLocation.getString(TAG_NAME);
					} catch (JSONException ex_json) {
						ex_json.printStackTrace();
					}

				} catch (JSONException ex_json) {
					ex_json.printStackTrace();
				}

			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {
				profilepicture = jsonObject.getString(TAG_LK_USER_PICTURE_URL);				
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {
				profileurl = jsonObject.getString(TAG_LK_USER_PROFILE_URL);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}

			try {
				email = jsonObject.getString(TAG_LK_USER_EMAIL);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}
			
			try {
				summary = jsonObject.getString(TAG_LK_USER_SUMMARY);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}
			
			try {
				int count = jsonObject.getInt(TAG_LK_USER_CONNECTION_COUNT);
				connections = String.valueOf(count);
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}
			
			try {
				JSONObject languageObject = jsonObject.getJSONObject(TAG_LK_USER_LANGUAGES);
				
				int lang_count = languageObject.getInt(TAG_TOTAL);
				languages = new String[lang_count];
								
				JSONArray languageJSONArray = languageObject.getJSONArray(TAG_CONNECTION_VALUES);
				
				// when error found at server side may this will come as NULL
				if (languageJSONArray != null) {

					// looping through all connections
					for (int i = 0; i < languageJSONArray.length(); i++) {
						
						JSONObject langObject = languageJSONArray.getJSONObject(i);						
						languages[i] = langObject.getJSONObject(TAG_LK_USER_LANGUAGE).getString(TAG_NAME);						
						
					}
				}
				
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}
			
			try {
				JSONObject skillsObject = jsonObject.getJSONObject(TAG_LK_USER_SKILLS);
				
				int count = skillsObject.getInt(TAG_TOTAL);
				skills = new String[count];
								
				JSONArray skillsJSONArray = skillsObject.getJSONArray(TAG_CONNECTION_VALUES);
				
				// when error found at server side may this will come as NULL
				if (skillsJSONArray != null) {

					// looping through all connections
					for (int i = 0; i < skillsJSONArray.length(); i++) {
						
						JSONObject skillObject = skillsJSONArray.getJSONObject(i);						
						skills[i] = skillObject.getJSONObject(TAG_LK_USER_SKILL).getString(TAG_NAME);						
						
					}
				}
				
			} catch (JSONException ex_json) {
				ex_json.printStackTrace();
			}
			
			
			ArrayList<String> arrListSkills = new ArrayList<String>(Arrays.asList(skills));			
			JSONObject skillJsonObject = new JSONObject();
			skillJsonObject.put("skillArrays", new JSONArray(arrListSkills));
			String strSkills = skillJsonObject.toString();
			
			ArrayList<String> arrListLaugauges = new ArrayList<String>(Arrays.asList(languages));
			JSONObject langsJsonObject = new JSONObject();
			langsJsonObject.put("skillArrays", new JSONArray(arrListLaugauges));
			String strLangs = langsJsonObject.toString();
			
			Logger.vLog("ResponseManager", "strSkills : "+strSkills);
			Logger.vLog("ResponseManager", "strLangs : "+strLangs);
			
			signedLinkedinUser = new SignedLinkedinUser(id, fname, lname, industry, 
					country_code, location, profilepicture, profileurl, headline, email, summary,
					strSkills, strLangs, connections);
			
			return signedLinkedinUser;
			
		}
	}// end of parse() method
	
	
	public int parseConnectionCount(Object jsonResponseObject) throws Exception { 

		int iCount = 0;	
		JSONObject jsonObject = (JSONObject) jsonResponseObject;

		try {

			int iConnectionCount = jsonObject.getInt(TAG_CONNECTION_COUNT);
			Logger.vLog("Connection Count : ", "" + iConnectionCount);
			iCount = iConnectionCount;

		} catch (JSONException jsonException) {
			// TODO: handle exception
			iCount = -1;
		}
		
		return iCount;
	}// end of parseConnectionCount() method
	
	
	public LatLng parseGeocoderWebserviceResult(String result){
		
		double lat = 0;
		double lng = 0;

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(result);

			lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");

			lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			Logger.vLog("latitude", "" + lat);
			Logger.vLog("longitude", "" + lng);

		} catch (JSONException e) {
			Logger.vLog("JSONException", e.toString());
			e.printStackTrace();
		}

		return new LatLng(lat, lng);
		
	}
}
