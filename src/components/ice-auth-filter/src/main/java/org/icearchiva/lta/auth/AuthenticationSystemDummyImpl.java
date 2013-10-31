package org.icearchiva.lta.auth;


public class AuthenticationSystemDummyImpl implements IAuthenticationSystem<CredentialsUsernamePassword> {
	
	@Override
	public boolean checkUser(CredentialsUsernamePassword credentials) {
		return true;
	}
}
