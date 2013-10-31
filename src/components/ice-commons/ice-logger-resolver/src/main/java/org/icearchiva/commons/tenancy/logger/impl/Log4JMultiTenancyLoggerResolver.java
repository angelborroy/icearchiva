package org.icearchiva.commons.tenancy.logger.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierResolver;
import org.icearchiva.commons.tenancy.logger.IMultiTenancyLoggerResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4JMultiTenancyLoggerResolver implements IMultiTenancyLoggerResolver {
	
    private static final Logger log = LoggerFactory.getLogger(Log4JMultiTenancyLoggerResolver.class);
	
    private String prefix;
    private String threshold;
    private String rootPath;
    private String appender;
    private String datePattern;
    private String layout;
    private String layoutConversionPattern;
	
    private ICurrentTenantIdentifierResolver multiTenantContextResolver;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public ICurrentTenantIdentifierResolver getMultiTenantContextResolver() {
		return multiTenantContextResolver;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getAppender() {
		return appender;
	}

	public void setAppender(String appender) {
		this.appender = appender;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getLayoutConversionPattern() {
		return layoutConversionPattern;
	}

	public void setLayoutConversionPattern(String layoutConversionPattern) {
		this.layoutConversionPattern = layoutConversionPattern;
	}

	public void setMultiTenantContextResolver(ICurrentTenantIdentifierResolver multiTenantContextResolver) {
		this.multiTenantContextResolver = multiTenantContextResolver;
	}
	
	private static Map<String, Logger> currentLoggers = new HashMap<String, Logger>();
	
	@Override
	public Logger resolveCurrentTenantLogger() {
		
		if (currentLoggers.containsKey(multiTenantContextResolver.resolveCurrentTenantIdentifier())) {
			return currentLoggers.get(multiTenantContextResolver.resolveCurrentTenantIdentifier());
		}
		
		try {
			
			Class<?> appenderClass = Class.forName(appender);
			Appender appenderObject = (Appender)appenderClass.newInstance();
			
			if (appenderObject instanceof DailyRollingFileAppender) {
				
				DailyRollingFileAppender drfa = new DailyRollingFileAppender();
				
				if (!rootPath.endsWith("/")) rootPath = rootPath + "/";
                String logFileName = rootPath + multiTenantContextResolver.resolveCurrentTenantIdentifier();
                File logFile = new File(logFileName);
                if (!logFile.exists()) logFile.mkdir();
				drfa.setFile(logFileName + "/audit.log");
				
				drfa.setDatePattern(datePattern);
				PatternLayout layoutObject = null;
				try {
				    layoutObject = (PatternLayout) Class.forName(layout).newInstance();
				} catch (ClassCastException cce) {
					throw new Exception(layout + " log4j layout not supported!");
				}
				layoutObject.setConversionPattern(layoutConversionPattern);
				drfa.setLayout(layoutObject);

				drfa.setAppend(true);
				drfa.setName(prefix + "." + multiTenantContextResolver.resolveCurrentTenantIdentifier());
				drfa.setThreshold(Level.toLevel(threshold));
				
				drfa.activateOptions();
				
				org.apache.log4j.Logger myLogger = org.apache.log4j.LogManager.getLogger(prefix + "." + multiTenantContextResolver.resolveCurrentTenantIdentifier());
				myLogger.addAppender(drfa);
	
				Logger logger = new Log4jLoggerAdapter(myLogger);
				currentLoggers.put(multiTenantContextResolver.resolveCurrentTenantIdentifier(), logger);
				return logger;
				
			} else {
				throw new Exception(appenderClass.getName() + " log4j appender not supported!");
			}
			
		} catch (Exception e) {
			log.warn("Multitenant logger not initialized for " + prefix + "." + multiTenantContextResolver.resolveCurrentTenantIdentifier(), e);
		}
		
		return null;
		
	}

}
