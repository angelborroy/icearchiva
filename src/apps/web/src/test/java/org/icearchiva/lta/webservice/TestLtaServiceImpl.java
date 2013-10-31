package org.icearchiva.lta.webservice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;

import org.icearchiva.lta.ws.v1.ArchiveData;
import org.icearchiva.lta.ws.v1.ArchiveData.Element;
import org.icearchiva.lta.ws.v1.ArchiveRequest;
import org.icearchiva.lta.ws.v1.ArchiveResponse;
import org.icearchiva.lta.ws.v1.DeleteRequest;
import org.icearchiva.lta.ws.v1.DeleteResponse;
import org.icearchiva.lta.ws.v1.ExportRequest;
import org.icearchiva.lta.ws.v1.ExportResponse;
import org.icearchiva.lta.ws.v1.ListIdsRequest;
import org.icearchiva.lta.ws.v1.ListIdsResponse;
import org.icearchiva.lta.ws.v1.LtaPortType;
import org.icearchiva.lta.ws.v1.MetaData;
import org.icearchiva.lta.ws.v1.MetaItem;
import org.icearchiva.lta.ws.v1.StatusInformation;
import org.icearchiva.lta.ws.v1.VerifyRequest;
import org.icearchiva.lta.ws.v1.VerifyResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class TestLtaServiceImpl {

    public static final String NAMESPACE = "http://icearchiva.org/lta/ws/v1/";
    
    private static ClassPathXmlApplicationContext context;
    
    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[] {"context/client-context.xml"});
    }
    
    @Test
    public void wholeInOne() throws Exception {
    	
    	List<String> uuidsAdmin = new ArrayList<String>();
    	List<String> uuidsEntidad = new ArrayList<String>();

    	System.out.println("ARCHIVE");
    	String uuid = testArchiveOperationSimpleAdmin();
    	System.out.println("\tArchived " + uuid);
    	uuidsAdmin.add(uuid);
    	uuid = testArchiveOperationOneFileNoMetadata();
    	System.out.println("\tArchived " + uuid);
    	uuidsAdmin.add(uuid);
    	uuid = testArchiveOperationOneFile();
    	System.out.println("\tArchived " + uuid);
    	uuidsAdmin.add(uuid);
    	uuid = testArchiveOperationSeveralFiles();
    	System.out.println("\tArchived " + uuid);
    	uuidsAdmin.add(uuid);
    	uuid = testArchiveOperationBigData();
    	System.out.println("\tArchived " + uuid);
    	uuidsAdmin.add(uuid);
    	
    	uuid = testArchiveOperationSimpleEntidad();
    	System.out.println("\tArchived " + uuid);
    	uuidsEntidad.add(uuid);
    	
    	System.out.println("EXPORT");
    	testExportOperation(uuidsAdmin, "admin");
    	testExportOperation(uuidsEntidad, "entidad");

    	System.out.println("LISTIDS");
    	testListIds("admin");
    	testListIds("entidad");
    	
    	System.out.println("VERIFY");
    	testVerifyOperation(uuidsAdmin, "admin");
    	testVerifyOperation(uuidsEntidad, "entidad");
    	
    	System.out.println("DELETE");
    	testDeleteOperation(uuidsAdmin, "admin");
    	testDeleteOperation(uuidsEntidad, "entidad");
    	
    }

    public String testArchiveOperationSimpleAdmin() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");
        
        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "admin");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ArchiveRequest archiveRequest = new ArchiveRequest();
        archiveRequest.setPolicyId(5);
        archiveRequest.setTransactionId("1");
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());
        
        return archiveResponse.getReferenceId();
        
    }

    public String testArchiveOperationSimpleEntidad() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");
        
        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "entidad");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ArchiveRequest archiveRequest = new ArchiveRequest();
        archiveRequest.setPolicyId(1);
        archiveRequest.setTransactionId("1");
        archiveRequest.setVersion("1");
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());

        return archiveResponse.getReferenceId();

    }
    
    public String testArchiveOperationOneFileNoMetadata() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "admin");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ArchiveRequest archiveRequest = new ArchiveRequest();
        
        archiveRequest.setPolicyId(2);
        archiveRequest.setTransactionId("1");
        archiveRequest.setVersion("1");
        
        ArchiveData data = new ArchiveData();
        Element element = new Element();
        File resource = new File(this.getClass().getClassLoader().getResource("files/audit.log").getFile());
        element.setData(new DataHandler(new FileDataSource(resource)));
        data.getElement().add(element);
        archiveRequest.setData(data);
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());
        
        return archiveResponse.getReferenceId();
        
    }

    public String testArchiveOperationOneFile() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "admin");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ArchiveRequest archiveRequest = new ArchiveRequest();
        
        archiveRequest.setPolicyId(3);
        archiveRequest.setTransactionId("1");
        archiveRequest.setVersion("1");
        
        ArchiveData data = new ArchiveData();
        Element element = new Element();
        MetaData metadata = new MetaData();
        MetaItem metaitem = new MetaItem();
        metaitem.setType("metadata1");
        metaitem.getValues().add("data11");
        metaitem.getValues().add("data12");
        metadata.getMetaItem().add(metaitem);
        metaitem = new MetaItem();
        metaitem.setType("metadata2");
        metaitem.getValues().add("data21");
        metaitem.getValues().add("data22");
        metadata.getMetaItem().add(metaitem);
        element.setMetaData(metadata);
        File resource = new File(this.getClass().getClassLoader().getResource("files/audit.log").getFile());
        element.setData(new DataHandler(new FileDataSource(resource)));
        data.getElement().add(element);
        archiveRequest.setData(data);
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());
        
        return archiveResponse.getReferenceId();
        
    }

    public String testArchiveOperationSeveralFiles() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "admin");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ArchiveRequest archiveRequest = new ArchiveRequest();
        
        archiveRequest.setPolicyId(4);
        archiveRequest.setTransactionId("1");
        archiveRequest.setVersion("1");
        
        ArchiveData data = new ArchiveData();
        
        // 1
        Element element = new Element();
        MetaData metadata = new MetaData();
        MetaItem metaitem = new MetaItem();
        metaitem.setType("metadata1");
        metaitem.getValues().add("data11");
        metaitem.getValues().add("data12");
        metadata.getMetaItem().add(metaitem);
        metaitem = new MetaItem();
        metaitem.setType("metadata2");
        metaitem.getValues().add("data21");
        metaitem.getValues().add("data22");
        metadata.getMetaItem().add(metaitem);
        element.setMetaData(metadata);
        File resource = new File(this.getClass().getClassLoader().getResource("files/recomendaciones_correo.pdf").getFile());
        element.setData(new DataHandler(new FileDataSource(resource)));
        data.getElement().add(element);
        
        // 2
        Element element2 = new Element();
        MetaData metadata2 = new MetaData();
        MetaItem metaitem2 = new MetaItem();
        metaitem2.setType("meta2data1");
        metaitem2.getValues().add("da2ta11");
        metaitem2.getValues().add("da2ta12");
        metadata2.getMetaItem().add(metaitem2);
        metaitem2 = new MetaItem();
        metaitem2.setType("meta2data2");
        metaitem2.getValues().add("da2ta21");
        metaitem2.getValues().add("da2ta22");
        metadata2.getMetaItem().add(metaitem2);
        element2.setMetaData(metadata2);
        File resource2 = new File(this.getClass().getClassLoader().getResource("files/recomendaciones_correo.signed.pdf").getFile());
        element2.setData(new DataHandler(new FileDataSource(resource2)));
        data.getElement().add(element2);
        
        // 3
        Element element3 = new Element();
        MetaData metadata3 = new MetaData();
        MetaItem metaitem3 = new MetaItem();
        metaitem3.setType("meta2data1");
        metaitem3.getValues().add("da2ta11");
        metaitem3.getValues().add("da2ta12");
        metadata3.getMetaItem().add(metaitem3);
        metaitem3 = new MetaItem();
        metaitem3.setType("meta2data2");
        metaitem3.getValues().add("da2ta21");
        metaitem3.getValues().add("da2ta22");
        metadata3.getMetaItem().add(metaitem3);
        element3.setMetaData(metadata3);
        File resource3 = new File(this.getClass().getClassLoader().getResource("files/audit.log").getFile());
        element3.setData(new DataHandler(new FileDataSource(resource3)));
        data.getElement().add(element3);

        archiveRequest.setData(data);
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());
        
        return archiveResponse.getReferenceId();
        
    }

    public String testArchiveOperationBigData() throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");
        
        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "admin");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
       ArchiveRequest archiveRequest = new ArchiveRequest();
        
        archiveRequest.setPolicyId(6);
        archiveRequest.setTransactionId("1");
        archiveRequest.setVersion("1");
        
        ArchiveData data = new ArchiveData();
        Element element = new Element();
        MetaData metadata = new MetaData();
        MetaItem metaitem = new MetaItem();
        metaitem.setType("metadata1");
        metaitem.getValues().add("data11");
        metaitem.getValues().add("data12");
        metadata.getMetaItem().add(metaitem);
        metaitem = new MetaItem();
        metaitem.setType("metadata2");
        metaitem.getValues().add("data21");
        metaitem.getValues().add("data22");
        metadata.getMetaItem().add(metaitem);
        element.setMetaData(metadata);
        File resource = new File(this.getClass().getClassLoader().getResource("files/compressed.zip").getFile());
        element.setData(new DataHandler(new FileDataSource(resource)));
        data.getElement().add(element);
        archiveRequest.setData(data);
        
        ArchiveResponse archiveResponse = client.archiveOperation(archiveRequest);
        
        Assert.assertEquals(archiveResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        Assert.assertNotNull(archiveResponse.getReferenceId());
        
        return archiveResponse.getReferenceId();
        
    }
    
    public void testExportOperation(List<String> uuids, String entidad) throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, entidad);
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        for (String uuid : uuids) {
        	
        	System.out.println("\tExporting " + uuid);
        	
	        ExportRequest exportRequest = new ExportRequest();
	        exportRequest.setTransactionId("1");
	        exportRequest.setVersion("1");
	        exportRequest.setReferenceId(uuid);
	        ExportResponse exportResponse = client.exportOperation(exportRequest);
	        
	        Assert.assertEquals(exportResponse.getStatus().getStatus(), StatusInformation.GRANTED);
	        Assert.assertTrue(exportResponse.getEvidence().getData().getInputStream().read() > 0);
	        exportResponse.getEvidence().getData().getInputStream().close();
	        
	        for (Element e : exportResponse.getData().getElement()) {
	            Assert.assertTrue(e.getData().getInputStream().read() > 0);
	            e.getData().getInputStream().close();
	        }
        }
        
    }
    
    public void testListIds(String entidad) throws Exception {

    	LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, entidad);
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        ListIdsRequest listIdsRequest = new ListIdsRequest();
        listIdsRequest.setTransactionId("1");
        listIdsRequest.setVersion("1");
        
        ListIdsResponse listIdsResponse = client.listIdsOperation(listIdsRequest);
        
        Assert.assertTrue(listIdsResponse.getListIds().getId().size() > 0);
        
    }

    public void testVerifyOperation(List<String> uuids, String entidad) throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, entidad);
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        for (String uuid : uuids) {
        	
        	System.out.println("\tVerifying " + uuid);
        	
	        VerifyRequest verifyRequest = new VerifyRequest();
	        verifyRequest.setReferenceId(uuid);
	        verifyRequest.setTransactionId("1");
	        verifyRequest.setVersion("1");
	        VerifyResponse verifyResponse = client.verifyOperation(verifyRequest);
	        Assert.assertEquals(verifyResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        }
        
    }
    
    public void testDeleteOperation(List<String> uuids, String entidad) throws Exception {
    	
        LtaPortType client = (LtaPortType) context.getBean("ltaClient");

        BindingProvider prov = (BindingProvider)client;
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, entidad);
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "admin");
        
        for(String uuid : uuids) {
        	
        	System.out.println("\tDeleting " + uuid);
        	
	        DeleteRequest deleteRequest = new DeleteRequest();
	        deleteRequest.setTransactionId("1");
	        deleteRequest.setVersion("1");
	        deleteRequest.setReferenceId(uuid);
	        DeleteResponse deleteResponse = client.deleteOperation(deleteRequest);
	        Assert.assertEquals(deleteResponse.getStatus().getStatus(), StatusInformation.GRANTED);
        }
        
    }
    
}
