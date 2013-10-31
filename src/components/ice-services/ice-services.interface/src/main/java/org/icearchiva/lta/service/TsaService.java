package org.icearchiva.lta.service;

import java.util.Date;

public interface TsaService {
	
	public Date getCaducityFromTimeStamp(byte[] timeStampContentB64);

	public Date getCaducityFromXadesA(byte[] xadesAContentB64);
	
	public byte[] stampSignature(byte[] signature);
	
}
