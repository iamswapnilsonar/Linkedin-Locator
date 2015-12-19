package com.vsplc.android.poc.linkedin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.SQLException;
import android.preference.PreferenceManager;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.database.DatabaseHelper;
import com.vsplc.android.poc.linkedin.model.City;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;

/**
 * This application class have ACRA integration which send a app crash report to specified email address.  
 * @Date 23 Jan, 2015
 * @author VSPLC
 */
@ReportsCrashes(
		formKey = "", // will not be used
        mailTo = "swapnil.sonar@vsplc.com",
        // customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },                
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
        
)

public class LinkedinApplication extends Application { 

	public static List<LinkedinUser> listGlobalConnections = new ArrayList<LinkedinUser>();
	public static Set<String> setOfGlobalCountries = new HashSet<String>();
	public static Set<String> setOfGlobalIndustryNames = new HashSet<String>();
	public static Map<String, List<LinkedinUser>> mapCountrywiseConnections = new HashMap<String, List<LinkedinUser>>();
	
	public static int iConnectionCount = 0;
	
	public static Set<String> cities = new HashSet<String>();
	public static Hashtable<String, City> hashTableOfCityInfo = new Hashtable<String, City>();
	
	public static SignedLinkedinUser signedLinkedinUser;
	
	private SharedPreferences preferences;
	private DatabaseHelper databaseHelper = null;

	private Dao<LinkedinUser, Integer> linkedinConnectionsDAO = null;
	private Dao<SignedLinkedinUser, Integer> signedLinkedinUserDAO = null;
	private Dao<City, Integer> citiesInfoDAO = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		databaseHelper = new DatabaseHelper(this);
		
	}
			
	public SharedPreferences getPreferences() {
		return preferences;
	}
	
	public Dao<SignedLinkedinUser, Integer> getSignedLinkedinUserDao() throws SQLException {
		
		if (signedLinkedinUserDAO == null) {

			try {
				signedLinkedinUserDAO = databaseHelper.getDao(SignedLinkedinUser.class);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return signedLinkedinUserDAO;
	}	

	public Dao<LinkedinUser, Integer> getLinkedInconnectionsDao() throws SQLException {
		
		if (linkedinConnectionsDAO == null) {

			try {
				linkedinConnectionsDAO = databaseHelper.getDao(LinkedinUser.class);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return linkedinConnectionsDAO;
	}
	
	public Dao<City, Integer> getCityInfoDao() throws SQLException {
		
		if (citiesInfoDAO == null) {

			try {
				citiesInfoDAO = databaseHelper.getDao(City.class);
			} catch (java.sql.SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return citiesInfoDAO;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}
