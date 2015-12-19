package com.vsplc.android.poc.linkedin.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * SignedLinkedinUser, cuurently loggedin user.
 * @author VSPLC
 */
@SuppressWarnings("serial")
@DatabaseTable(tableName = "SignedLinkedinUser")
public class SignedLinkedinUser implements Serializable {

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
	@DatabaseField
	public String connectionsCount;
	@DatabaseField
	public String email;
	@DatabaseField
	public String summary;
	
	@DatabaseField
	public String skills;	
	
	@DatabaseField
	public String languages;	
	
//	@DatabaseField
//	public String[] skills;	
//	@DatabaseField
//	public String[] languages;

	public SignedLinkedinUser() {
		// TODO Auto-generated constructor stub
	}
	
	public SignedLinkedinUser(String id, String fname, String lname, String industry,
			String country_code, String location, String profilepicture,
			String profileurl, String headline, String email, String summary,
			String skills, String languages, String connectionsCount) {
		
		this.id = id;
		this.fname = fname;
		this.lname = lname;
		this.industry = industry;
		this.country_code = country_code;
		this.location = location;
		this.profilepicture = profilepicture;
		this.profileurl = profileurl;
		this.headline = headline;
		
		this.email = email;
		this.summary = summary;
		this.skills = skills;
		this.languages = languages;
		this.connectionsCount = connectionsCount;
	}

	@Override
	public String toString() {
		String divider = "\n -----------------\n";
		String str = " ID : " + this.id + "\n  FName : " + this.fname
				+ "\n  LName : " + this.lname + "\n  Industry : "
				+ this.industry + "\n  Country Code : " + this.country_code
				+ "\n  Location : " + this.location + "\n  Profile Picture : "
				+ this.profilepicture + "\n  Profile URL : " + this.profileurl
				+ "\n Headline : " + this.headline + "\n Email : "+this.email
				+ "\n Summary : "+summary + "\n Connections : "+connectionsCount;
		return divider + str + divider;
	}
}
