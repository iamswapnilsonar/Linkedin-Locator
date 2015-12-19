package com.vsplc.android.poc.linkedin.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * City Object
 * @author VSPLC
 */
@DatabaseTable(tableName = "cityInfo")
public class City {
	
	@DatabaseField(generatedId = true)
	public int _id;	
	
	@DatabaseField
	public String name;
	@DatabaseField
	public String country;
	@DatabaseField
	public String latitude = "NA";
	@DatabaseField
	public String longitude = "NA";
	
	public City() {
		// TODO Auto-generated constructor stub
	}
	
	public City(String name){
		this.name = name;
	}
	
	public City getCityObject(){
		return this;
	}
	
	public City setLatAndLong(String latitude, String longitude){
		this.latitude = latitude;
		this.longitude = longitude;
		return this;
	}
	
	public City(String name, String country, String latitude, String longitude){
		this.name = name;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		
		String divider = "\n -----------------\n";
		String str = " Name : " + this.name + "\n  Country : " + this.country;
		str += " Lat : " + this.latitude + " Long : " + this.longitude;
		
		return divider + str + divider;
	}
}
