package com.vsplc.android.poc.linkedin.model;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.Comparator;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * LinkedinUser
 * @author VSPLC
 */
@SuppressWarnings("serial")
@DatabaseTable(tableName = "connections")
public class LinkedinUser implements Serializable {

	@DatabaseField(generatedId = true)
	public int _id;	
	
	@DatabaseField
	public String id;	
	@DatabaseField
	public String fname;	
	@DatabaseField
	public String lname;
	@DatabaseField
	public String industry;
	@DatabaseField
	public String country_code;
	@DatabaseField
	public String location;
	@DatabaseField
	public String profilepicture;
	@DatabaseField
	public String profileurl;
	@DatabaseField
	public String headline;

	public LinkedinUser() {
		// TODO Auto-generated constructor stub
	}
	
	public LinkedinUser(String id, String fname, String lname, String industry,
			String country_code, String location, String profilepicture,
			String profileurl, String headline) {
		
		this.id = id;
		this.fname = fname;
		this.lname = lname;
		this.industry = industry;
		this.country_code = country_code;
		this.location = location;
		this.profilepicture = profilepicture;
		this.profileurl = profileurl;
		this.headline = headline;
	}

	@Override
	public String toString() {
		String divider = "\n -----------------\n";
		String str = " ID : " + this.id + "\n  FName : " + this.fname
				+ "\n  LName : " + this.lname + "\n  Industry : "
				+ this.industry + "\n  Country Code : " + this.country_code
				+ "\n  Location : " + this.location + "\n  Profile Picture : "
				+ this.profilepicture + "\n  Profile URL : " + this.profileurl
				+ "\n Headline : " + this.headline;
		return divider + str + divider;
	}
	
	// Comparator for sorting the user list by Linkedin User Name
    public static Comparator<LinkedinUser> LinkedinUserNameComparator = new Comparator<LinkedinUser>() {

	@SuppressLint("DefaultLocale")
	public int compare(LinkedinUser user1, LinkedinUser user2) {
	   String userName1 = user1.fname.toUpperCase();
	   String userName2 = user2.fname.toUpperCase();

	   //ascending order
	   return userName1.compareTo(userName2);

	   //descending order
	   //return StudentName2.compareTo(StudentName1);
    }};
}
