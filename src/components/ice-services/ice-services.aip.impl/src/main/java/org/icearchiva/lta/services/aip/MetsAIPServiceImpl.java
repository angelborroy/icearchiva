package org.icearchiva.lta.services.aip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.icearchiva.lta.service.AIPService;
import org.icearchiva.lta.service.model.IAIPArchiveRequest;
import org.icearchiva.lta.service.model.IAIPFile;
import org.icearchiva.lta.service.model.IAIPMetadata;
import org.icearchiva.lta.service.model.IDescriptorDetails;
import org.icearchiva.lta.service.model.IFileInfo;
import org.icearchiva.lta.service.model.IUser;
import org.icearchiva.lta.ws.v1.ArchiveData.Element;
import org.icearchiva.lta.ws.v1.ArchiveRequest;
import org.icearchiva.lta.ws.v1.MetaItem;
import org.nubarchiva.commons.standards.mets.DivType;
import org.nubarchiva.commons.standards.mets.FileType;
import org.nubarchiva.commons.standards.mets.FileType.FLocat;
import org.nubarchiva.commons.standards.mets.MdSecType;
import org.nubarchiva.commons.standards.mets.MdSecType.MdWrap;
import org.nubarchiva.commons.standards.mets.MdSecType.MdWrap.XmlData;
import org.nubarchiva.commons.standards.mets.Mets;
import org.nubarchiva.commons.standards.mets.MetsType.FileSec;
import org.nubarchiva.commons.standards.mets.MetsType.FileSec.FileGrp;
import org.nubarchiva.commons.standards.mets.MetsType.MetsHdr;
import org.nubarchiva.commons.standards.mets.MetsType.MetsHdr.Agent;
import org.nubarchiva.commons.standards.mets.StructMapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MetsAIPServiceImpl implements AIPService {
	
	private static final Logger log = LoggerFactory.getLogger(MetsAIPServiceImpl.class);

	private static final String METS_XSD_URL = "http://www.loc.gov/standards/mets/mets.xsd";
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String XML_ROOT_ELEMENT_ID = "METS";

	private static final String METS_FILENAME = "mets.xml";
	private static final String METS_MIMETYPE = "application/xml";
	
	private static final String METS_RECORDSTATUS_COMPLETE = "Complete";
	private static final String DEFAULT_TYPE = "ORGANIZATION";
	private static final String DEFAULT_ROLE = "CREATOR";
	private static final String DMD_PREFIX = "DMD-";
	
	private boolean validationEnabled;
	private String aipId;

	@Override
	public boolean isValidationEnabled() {
		return validationEnabled;
	}

	public void setValidationEnabled(boolean validationEnabled) {
		this.validationEnabled = validationEnabled;
	}
	
	@Override
	public String getFileName() {
		return METS_FILENAME;
	}
	
	@Override
	public String getMimeType() {
		return METS_MIMETYPE;
	}
	
	@Override
	public String getXMLRootElementId() {
		return XML_ROOT_ELEMENT_ID;
	}
	
	@Override
	public String getPackageId() {
		return aipId;
	}

	@Override
	public InputStream getPackage(IAIPArchiveRequest iAipArchiveRequest) {
		
		aipId = iAipArchiveRequest.getAipId();
		IUser user = iAipArchiveRequest.getUser();
		ArchiveRequest archiveRequest = iAipArchiveRequest.getArchiveRequest();
		List<IFileInfo> fileInfoList = iAipArchiveRequest.getFileInfoList(); 
		
		Mets mets = new Mets();
		
		// Info
		mets.setID(XML_ROOT_ELEMENT_ID);
		mets.setOBJID(aipId);
		mets.setLabel1(archiveRequest.getTransactionId());
		mets.setTYPE(Integer.toString(archiveRequest.getPolicyId()));
		mets.setPROFILE(archiveRequest.getVersion());
		
		// Header
		MetsHdr metsHdr = new MetsHdr();
		try {
			metsHdr.setCREATEDATE(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		} catch (DatatypeConfigurationException dce) {
			throw new RuntimeException(dce);
		}
		metsHdr.setRECORDSTATUS(METS_RECORDSTATUS_COMPLETE);
		Agent agent = new Agent();
		agent.setROLE(DEFAULT_ROLE);
		agent.setTYPE(DEFAULT_TYPE);
		agent.setName(user.getName());
		metsHdr.getAgent().add(agent);
		
		mets.setMetsHdr(metsHdr);

		// Several files for every package
		if (archiveRequest.getData() != null) {
			
			for (Element e : archiveRequest.getData().getElement()) {
				
				// Descriptive data - Request metadata
				MdSecType dmdSec = new MdSecType();
				dmdSec.setID(DMD_PREFIX + e.getData().getDataSource().getName());
				if (e.getMetaData() != null) {
					MdWrap mdWrap = new MdWrap();
					mdWrap.setMIMETYPE("text/xml");
					mdWrap.setMDTYPE("OTHER");
					mdWrap.setLabel8("AIP metatada");
					XmlData xmlData = new XmlData();
					try {
	                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	                    org.w3c.dom.Element rootElement = doc.createElementNS("http://org.icearchiva.lta.metadata", "icemeta");
						for (MetaItem mi : e.getMetaData().getMetaItem()) {
							for (String value : mi.getValues()) {
						        org.w3c.dom.Element xmlContent = doc.createElementNS("http://org.icearchiva.lta.metadata", "icemeta:" + mi.getType());
						        xmlContent.setTextContent(value);
						        rootElement.appendChild(xmlContent);
							}
						}
	                    doc.appendChild(rootElement);
				        xmlData.getAny().add((org.w3c.dom.Element) doc.getFirstChild());
					} catch (ParserConfigurationException pce) {
						log.warn("METS metadata not assigned!");
					}
					mdWrap.setXmlData(xmlData);
					dmdSec.setMdWrap(mdWrap);
				}
				
				mets.getDmdSec().add(dmdSec);
	
			}
			
			// Several files for every package
			FileSec fileSec = new FileSec();
			fileSec.setID("FILESEC");
			FileGrp fileGroup = new FileGrp();
			fileGroup.setID("CONTENT");
			for (IFileInfo fileInfo : fileInfoList) {
				
				FileType file = new FileType();
                file.setID(fileInfo.getName());
				file.setMIMETYPE(fileInfo.getMimeType());
				file.setSIZE(fileInfo.getSize());
				file.setCHECKSUM(fileInfo.getHash());
				file.setCHECKSUMTYPE(fileInfo.getHashType());
				FLocat flocat = new FLocat();
				flocat.setLOCTYPE("URL");
				flocat.setHref("file://" + iAipArchiveRequest.getContentsPath() + "/" + fileInfo.getName());
				file.getFLocat().add(flocat);
				fileGroup.getFile().add(file);
			}
			
			fileSec.getFileGrp().add(fileGroup);
			mets.setFileSec(fileSec);
		
			
		}
		
		// Empty struct map
		StructMapType structMap = new StructMapType();
		DivType div = new DivType();
		structMap.setDiv(div);
		mets.getStructMap().add(structMap);
		
		// METS file to String
		ByteArrayInputStream bais = null;
		try {
			
			StringWriter sw = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(Mets.class);
			Marshaller msh = context.createMarshaller();
			if (validationEnabled) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(new URL(METS_XSD_URL)); 
				msh.setSchema(schema);
			}
			msh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			msh.setProperty(Marshaller.JAXB_ENCODING, DEFAULT_CHARSET);
			msh.marshal(mets, sw);
			bais = new ByteArrayInputStream(Charset.forName(DEFAULT_CHARSET).encode(sw.toString()).array());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return bais;
		
	}
	
	@Override
	public IDescriptorDetails getDetails(InputStream is) {
		
		MetsDescriptorDetailsImpl details = new MetsDescriptorDetailsImpl();
		
		Mets mets = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Mets.class);
			Unmarshaller umsh = context.createUnmarshaller();
			mets = (Mets) umsh.unmarshal(is);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
		
		this.aipId = mets.getOBJID();
		details.setPolicyId(mets.getTYPE());
		details.setReferenceId(mets.getOBJID());
		
		Map<String, IAIPMetadata> metadataMap = new HashMap<String, IAIPMetadata>();
		for (MdSecType dmd : mets.getDmdSec()) {
			
			String fileId = dmd.getID();
			if (fileId.startsWith(DMD_PREFIX)) {
				fileId = fileId.substring(DMD_PREFIX.length());
			} else {
				log.warn("METS file ID " + fileId + " has no " + DMD_PREFIX + " prefix. Metadata will not be mapped!");
			}
			
			MetsMetadataImpl mmi = new MetsMetadataImpl();
			if (dmd.getMdWrap() != null) {
				for(Object any : dmd.getMdWrap().getXmlData().getAny()) {
					org.w3c.dom.Element xmlElement = (org.w3c.dom.Element) any;
					NodeList nodeList = xmlElement.getChildNodes();
					String currentTypeNode = "";
					List<String> metaItemValues = null;
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						String metaItemType = node.getLocalName();
						NodeList metaNodeList = node.getChildNodes();
						if (!currentTypeNode.equals(metaItemType)) {
							metaItemValues = new ArrayList<String>();
							currentTypeNode = metaItemType;
						}
						for (int j = 0; j < metaNodeList.getLength(); j++) {
							metaItemValues.add(metaNodeList.item(j).getTextContent());
						}
						mmi.addMetaItem(metaItemType, metaItemValues);
					}
				}
			}
			
			metadataMap.put(fileId, mmi);
			
		}
		details.setMetadataMap(metadataMap);
		
		List<IAIPFile> files = new ArrayList<IAIPFile>();
		if (mets.getFileSec() != null && mets.getFileSec().getFileGrp() != null) {
			for (FileGrp fileGroup : mets.getFileSec().getFileGrp()) {
				for (FileType file : fileGroup.getFile()) {
					MetsFile mf = new MetsFile();
					mf.setFileName(file.getID());
					mf.setMimeType(file.getMIMETYPE());
					mf.setChecksum(file.getCHECKSUM());
					mf.setChecksumType(file.getCHECKSUMTYPE());
					mf.setSize(file.getSIZE());
					// Only first location read - if more locations are available, they are not returned (!)
					mf.setLocType(file.getFLocat().get(0).getLOCTYPE());
					mf.setHref(file.getFLocat().get(0).getHref());
					files.add(mf);
				}
			}
		}
		details.setFiles(files);
		
		return details;
	}

}
