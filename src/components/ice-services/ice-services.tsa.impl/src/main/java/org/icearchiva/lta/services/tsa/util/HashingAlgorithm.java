package org.icearchiva.lta.services.tsa.util;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class HashingAlgorithm {
	
	public static final String MD2 = "MD2";
	public static final String MD5 = "MD5";
	public static final String SHA1 = "SHA-1";
	public static final String SHA256 = "SHA-256";
	public static final String SHA512 = "SHA-512";
	public static final String SHA384 = "SHA-384";
	
	private static HashMap<String, String> mapOIDs = new HashMap<String, String>();
	private static HashMap<String, String> mapReverseOIDs = new HashMap<String, String>();

	public static final String getDefault() {
		return "SHA-256";
	}

	public static String getOID(String hashingAlgorithm)
			throws NoSuchAlgorithmException {
		String oid = (String) mapOIDs.get(hashingAlgorithm);
		if (oid == null) {
			throw new NoSuchAlgorithmException("Hashing algorithm '" + hashingAlgorithm + "' does not exists!");
		}

		return oid;
	}

	public static final String getDefaultOID() {
		try {
			return getOID(getDefault());
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static String getAlgorithmName(String oid)
			throws NoSuchAlgorithmException {
		String nombre = (String) mapReverseOIDs.get(oid);
		if (nombre == null) {
			throw new NoSuchAlgorithmException("OID '" + oid + "' does not exists!");
		}

		return nombre;
	}

	static {
		mapOIDs.put("MD2", "1.3.14.7.2.2.1");
		mapOIDs.put("MD5", "1.2.840.113549.2.5");
		mapOIDs.put("SHA-1", "1.3.14.3.2.26");
		mapOIDs.put("SHA-256", "2.16.840.1.101.3.4.2.1");
		mapOIDs.put("SHA-384", "2.16.840.1.101.3.4.2.2");
		mapOIDs.put("SHA-512", "2.16.840.1.101.3.4.2.3");

		mapReverseOIDs.put("1.3.14.7.2.2.1", "MD2");
		mapReverseOIDs.put("1.2.840.113549.2.5", "MD5");
		mapReverseOIDs.put("1.3.14.3.2.26", "SHA-1");
		mapReverseOIDs.put("2.16.840.1.101.3.4.2.1", "SHA-256");
		mapReverseOIDs.put("2.16.840.1.101.3.4.2.2", "SHA-384");
		mapReverseOIDs.put("2.16.840.1.101.3.4.2.3", "SHA-512");
	}
}