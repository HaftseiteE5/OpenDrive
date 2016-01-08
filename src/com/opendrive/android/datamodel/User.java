package com.opendrive.android.datamodel;

public class User {
	public String _username;
	public String _password;

	public User(String username, String password) {
		_username = username;
		_password = password;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}
}


