package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;

public class HTPCShip {
	
	private String to  , company  , addressOne  , addressTwo  ,	addressThree  , city  , state  , zip  ,
			country  , phone  , email = Misc.EMPTY_STRING;
	
	private boolean residential;
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = Misc.replaceNullWithEmptyString(to);
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = Misc.replaceNullWithEmptyString(company);
	}
	public String getAddressOne() {
		return addressOne;
	}
	public void setAddressOne(String addressOne) {
		this.addressOne = Misc.replaceNullWithEmptyString(addressOne);
	}
	public String getAddressTwo() {
		return addressTwo;
	}
	public void setAddressTwo(String addressTwo) {
		this.addressTwo = Misc.replaceNullWithEmptyString(addressTwo);
	}
	public String getAddressThree() {
		return addressThree;
	}
	public void setAddressThree(String addressThree) {
		this.addressThree = Misc.replaceNullWithEmptyString(addressThree);
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = Misc.replaceNullWithEmptyString(city);
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = Misc.replaceNullWithEmptyString(state);
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = Misc.replaceNullWithEmptyString(zip);
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = Misc.replaceNullWithEmptyString(country);
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = Misc.replaceNullWithEmptyString(phone);
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = Misc.replaceNullWithEmptyString(email);
	}
	public boolean isResidential() {
		return residential;
	}
	public void setResidential(boolean residential) {
		this.residential = residential;
	}
	
	
	
}
