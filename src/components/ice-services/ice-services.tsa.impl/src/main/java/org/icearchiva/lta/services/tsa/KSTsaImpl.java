package org.icearchiva.lta.services.tsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.encoders.Base64;
import org.icearchiva.lta.service.TsaService;
import org.icearchiva.lta.services.tsa.util.HashingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class KSTsaImpl implements TsaService {
	
	private static final Logger logger = LoggerFactory.getLogger(KSTsaImpl.class);

	private static final String BOUNCY_CASTLE_PROVIDER = "BC";
	private static final String XADES_A_ENCAPSULATED_TIMESTAMP_TAG_NAME = "EncapsulatedTimeStamp";
	private static final String XADES_A_ARCHIVE_TIMESTAMP_TAG_NAME = "ArchiveTimeStamp";
	
	List<String> tsaUrl;
	TimeStampToken timeStamp;
	
	@Override
    public Date getCaducityFromXadesA(byte[] xadesAContentB64) {
		
		Date caducityDate = null;
		
		try {
			
			List<Date> caducityDates = new ArrayList<Date>();
		    Document doc = loadXMLFromByteArray(Base64.decode(xadesAContentB64));
		    NodeList atsList = doc.getElementsByTagName(XADES_A_ARCHIVE_TIMESTAMP_TAG_NAME);
		    
		    // One or more archive timestamps nodes
		    for (int i = 0; i < atsList.getLength(); i++) {
		    	NodeList atsNodes = atsList.item(i).getChildNodes();
		    	for (int j = 0; j < atsNodes.getLength(); j++) {
		    		if (atsNodes.item(j).getLocalName().equals(XADES_A_ENCAPSULATED_TIMESTAMP_TAG_NAME)) {
		    			caducityDates.add(new KSTsaImpl().getCaducityFromTimeStamp(atsNodes.item(j).getTextContent().getBytes()));
		    		}
		    	}
		    }
		    
		    // Last caducity date
		    caducityDate = caducityDates.get(caducityDates.size() - 1);
		    
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return caducityDate;

	}


	@Override
	public Date getCaducityFromTimeStamp(byte[] timeStampContentB64) {
		
		Date caducityDate = null;

		try {

			timeStamp = new TimeStampToken(new CMSSignedData(getDER(Base64.decode(timeStampContentB64)).getDEREncoded()));
			X509Certificate certificate = getSignatureCertificate();
			caducityDate = certificate.getNotAfter();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return caducityDate;
	}
	
	@Override
    public byte[] stampSignature(byte[] signature) {
		
    	try {
    		
    		MessageDigest digest = MessageDigest.getInstance(HashingAlgorithm.getDefault());
    		byte[] hash = digest.digest(signature);
    	
    	    TimeStampRequestGenerator tsqGenerator = new TimeStampRequestGenerator();
    	    tsqGenerator.setCertReq(true);

    	    BigInteger nonce = BigInteger.valueOf(System.currentTimeMillis());
    	    TimeStampRequest request = tsqGenerator.generate(HashingAlgorithm.getDefaultOID(), hash, nonce);
    	    byte[] requestBytes = null;
    	    requestBytes = request.getEncoded();

    	    Hashtable<String, String> reqProperties = new Hashtable<String, String>();
    	    reqProperties.put("Content-Type", "application/timestamp-query");
    	    reqProperties.put("Content-Transfer-Encoding", "binary");
    	    
    	    TimeStampResponse response = null;
	    	for (String currentTsaUrl : tsaUrl) {
	    	    try {
    	            response = connect(new URL(currentTsaUrl), reqProperties, requestBytes);
    	            // Out on first valid TSA response
    	            break;
	    	    } catch (Exception e) {
	    	    	logger.warn(currentTsaUrl + " TSA unavailable!", e);
	    	    }
	    	}

    	    if (response.getTimeStampToken() == null)
    	    {
    	    	
    	      if (response.getFailInfo() == null) {
    	        throw new RuntimeException("Unknown reason!");
    	      }

    	      switch (response.getFailInfo().intValue()) {
	    	      case 4:
	      	        throw new RuntimeException("Data to be signed has incorrect format");
	    	      case 32:
	    	        throw new RuntimeException("TSA does not understand the request");
	    	      case 128:
	      	        throw new RuntimeException("Hash algorithm not supported by TSA");
	    	      case 512:
	    	        throw new RuntimeException("TSA unavailable");
	    	      case 256:
	    	        throw new RuntimeException("Policy not supported by TSA");
	    	      case 8388608:
	    	        throw new RuntimeException("Extension not supported by TSA");
	    	      case 4194304:
	    	        throw new RuntimeException("Extra information not supported by TSA");
	    	      case 1073741824:
	    	        throw new RuntimeException("TSA internal error");
    	      }
    	      throw new RuntimeException("Unknown reason");
    	    }

    	    return Base64.encode(response.getTimeStampToken().getEncoded());
    	    
	    } catch (Exception e) {
	        throw new RuntimeException(e);	
	    }
    	
    }

	@SuppressWarnings("rawtypes")
	private static TimeStampResponse connect(URL serverTimeStampURL, Hashtable<String, String> reqProperties, byte[] requestBytes) throws Exception {

		OutputStream printout = null;
		DataInputStream input = null;

		URLConnection urlConn = serverTimeStampURL.openConnection();

		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);

		Iterator iter = reqProperties.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			urlConn.setRequestProperty((String) entry.getKey(),
					(String) entry.getValue());
		}

		try {
			printout = urlConn.getOutputStream();
			printout.write(requestBytes);
			printout.flush();
		} finally {
			if (printout != null)
				try {
					printout.close();
				} catch (IOException e) {
				}
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			input = new DataInputStream(urlConn.getInputStream());

			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0)
				baos.write(buffer, 0, bytesRead);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
				}
		}
		try {
			return new TimeStampResponse(baos.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    private static DERObject getDER(byte[] bytesDER) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytesDER);
		return toDER(bais);
	}

	@SuppressWarnings("resource")
	private static DERObject toDER(InputStream isDER) throws IOException {
		DERObject lObjRes = null;
		ASN1InputStream lObjDerOut = new ASN1InputStream(isDER);
		lObjRes = lObjDerOut.readObject();
		return lObjRes;
	}

	@SuppressWarnings("rawtypes")
	private X509Certificate getSignatureCertificate() throws Exception {

        Provider bcProvider = Security.getProvider(BOUNCY_CASTLE_PROVIDER);
    	if (bcProvider == null) {
    		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());	
    	}
    	
    	X509Certificate certificate = null;
    	
    	CertStore certs = timeStamp.getCertificatesAndCRLs("Collection", BOUNCY_CASTLE_PROVIDER);
	    
	    Iterator iteratorIssuer = certs.getCertificates(null).iterator();
	    Iterator iteratorSubject = certs.getCertificates(null).iterator();

	    List<String> lIssuers = new ArrayList<String>();

	    while (iteratorIssuer.hasNext()) {
	    	certificate = (X509Certificate) iteratorIssuer.next();
	    	lIssuers.add(certificate.getIssuerDN().getName());
	    }

	    while (iteratorSubject.hasNext()) {
	    	certificate = (X509Certificate) iteratorSubject.next();
	        if (!lIssuers.contains(certificate.getSubjectDN().getName())) {
	          return certificate;
	        }

	    }
	    
	    return certificate;

    }	
	    
	private Document loadXMLFromByteArray(byte[] xml) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(xml));
	}


	public List<String> getTsaUrl() {
		return tsaUrl;
	}


	public void setTsaUrl(List<String> tsaUrl) {
		this.tsaUrl = tsaUrl;
	}
	
}
