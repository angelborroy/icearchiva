package org.icearchiva.commons.logging.aop;

import java.io.ByteArrayOutputStream;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;

import org.aspectj.lang.JoinPoint;
import org.icearchiva.lta.audit.IAuditSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBParametersLoggingAspect {
	
	private static final Logger log = LoggerFactory.getLogger(JAXBParametersLoggingAspect.class);
	
	private IAuditSystem auditSystem;

	public IAuditSystem getAuditSystem() {
		return auditSystem;
	}

	public void setAuditSystem(IAuditSystem auditSystem) {
		this.auditSystem = auditSystem;
	}

	public void logBefore(JoinPoint joinPoint) {

		StringBuffer logMessage = new StringBuffer();
		logMessage.append(joinPoint.getTarget().getClass().getName());
		logMessage.append(".");
		logMessage.append(joinPoint.getSignature().getName() + "\n");
		Object[] args = joinPoint.getArgs();
		try {
			for (int i = 0; i < args.length; i++) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JAXBContext context = JAXBContext.newInstance(args[i].getClass());
				Marshaller msh = context.createMarshaller();
				msh.setAttachmentMarshaller(new DataSourceSkipperAttachmentMarshaller());
				msh.setProperty("jaxb.formatted.output", Boolean.TRUE);
				msh.marshal(args[i], baos);
				logMessage.append(args[i]).append(baos.toString());
			}
		} catch (Exception e) {
			log.warn("Request info not audited!", e);
		}
		logMessage.append(")");
		auditSystem.logRequest(logMessage.toString());

	}

	public void logAfterReturning(JoinPoint joinPoint, Object result) {

		StringBuffer logMessage = new StringBuffer();
		logMessage.append(joinPoint.getTarget().getClass().getName());
		logMessage.append(".");
		logMessage.append(joinPoint.getSignature().getName() + "\n");
		try {			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBContext context = JAXBContext.newInstance(result.getClass());
			Marshaller msh = context.createMarshaller();
			msh.setAttachmentMarshaller(new DataSourceSkipperAttachmentMarshaller());
			msh.setProperty("jaxb.formatted.output", Boolean.TRUE);
			msh.marshal(result, baos);
			logMessage.append(result).append(baos.toString());
		} catch (Exception e) {
			log.warn("Response info not audited!", e);
		}
		logMessage.append(")");
		auditSystem.logResponse(logMessage.toString());

	}
	
	public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
		auditSystem.logResponse("Exception: " + error.getMessage());
	}
	
	class DataSourceSkipperAttachmentMarshaller extends AttachmentMarshaller {

        @Override
        public boolean isXOPPackage() {
           return true;
        }

        @Override
        public String addMtomAttachment(DataHandler data, String elementNamespace, String elementLocalName) {
            return data.getName();
        }

        @Override
        public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNamespace, String elementLocalName) {
            return "attachMtom";
        }

        @Override
        public String addSwaRefAttachment(DataHandler data) {
            return "attachSwA";
        }

    }
	
}
