package org.icearchiva.lta.service;

import java.security.KeyStore.PrivateKeyEntry;

public interface KeyStoreService {
	
	public PrivateKeyEntry getCertificatePrivateKey(String certAlias, String certPassword);

}
