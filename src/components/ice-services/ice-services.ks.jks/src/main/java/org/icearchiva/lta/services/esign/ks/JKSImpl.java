package org.icearchiva.lta.services.esign.ks;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;

import org.icearchiva.lta.service.KeyStoreService;
import org.springframework.beans.factory.InitializingBean;

public class JKSImpl implements KeyStoreService, InitializingBean {
	
	private String jksPath;
	private String jksPassword;
	private static KeyStore ks;
	
	@Override
	public PrivateKeyEntry getCertificatePrivateKey(String certAlias, String certPassword) {
		KeyStore.Entry key = null;
		try {
			key = ks.getEntry(certAlias, new KeyStore.PasswordProtection(certPassword.toCharArray()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			e.printStackTrace();
		}
		return (KeyStore.PrivateKeyEntry) key;
	}

	public String getJksPath() {
		return jksPath;
	}

	public void setJksPath(String jksPath) {
		this.jksPath = jksPath;
	}

	public String getJksPassword() {
		return jksPassword;
	}

	public void setJksPassword(String jksPassword) {
		this.jksPassword = jksPassword;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		InputStream is = JKSImpl.class.getResourceAsStream(jksPath);
		ks = KeyStore.getInstance("jks");
		ks.load(is, jksPassword.toCharArray());
	}
	
}
