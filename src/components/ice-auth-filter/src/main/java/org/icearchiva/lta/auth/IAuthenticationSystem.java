package org.icearchiva.lta.auth;

public interface IAuthenticationSystem <T extends IUserCredentials> {
	
	public boolean checkUser(T credentials);
	
}
