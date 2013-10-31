package org.icearchiva.lta.web.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.icearchiva.commons.session.ICurrentSessionIdentifierSetter;
import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierSetter;
import org.icearchiva.lta.auth.CredentialsUsernamePassword;
import org.icearchiva.lta.auth.IAuthenticationSystem;
import org.icearchiva.lta.auth.IUserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlBasicAuthFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(HtmlBasicAuthFilter.class);

	private IAuthenticationSystem<IUserCredentials> authenticationSystem;
	private ICurrentTenantIdentifierSetter multiTenantContextSetter;
	private ICurrentSessionIdentifierSetter sessionSetter;

	public IAuthenticationSystem<IUserCredentials> getAuthenticationSystem() {
		return this.authenticationSystem;
	}
	
	public void setAuthenticationSystem(IAuthenticationSystem<IUserCredentials> authenticationSystem) {
		this.authenticationSystem = authenticationSystem;
	}

	public ICurrentTenantIdentifierSetter getMultiTenantContextSetter() {
		return multiTenantContextSetter;
	}

	public void setMultiTenantContextSetter(
			ICurrentTenantIdentifierSetter multiTenantContextSetter) {
		this.multiTenantContextSetter = multiTenantContextSetter;
	}
	
	public ICurrentSessionIdentifierSetter getSessionSetter() {
		return sessionSetter;
	}

	public void setSessionSetter(ICurrentSessionIdentifierSetter sessionSetter) {
		this.sessionSetter = sessionSetter;
	}

	private static final String HTTP_BASIC_AUTH_PREFIX = "Basic ";
	private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
	private static final String WSDL_RESOURCE_SUFFIX = "wsdl";

    public static String getLocalHostMacAddress() throws UnknownHostException, SocketException {
    	
        InetAddress addr = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
        if (ni == null)
            return null;

        byte[] mac = ni.getHardwareAddress();
        if (mac == null)
            return null;

        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac) {
            if (sb.length() > 0)
                sb.append('-');
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
        
    }
    
	// UUID cluster safe
    private String getUUID() {
    	String uuid = UUID.randomUUID().toString();
    	try {
    	    uuid = uuid + "-" + getLocalHostMacAddress();
    	} catch (Exception e) {
    		uuid = uuid + "-localhost";
    	}
    	return uuid;
    }
	

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		if (excludeFromFilter(request.getQueryString())) {
			
			filterChain.doFilter(servletRequest,  servletResponse);
			
		} else {

			String header = request.getHeader(HTTP_HEADER_AUTHORIZATION);
			
			if (header != null && header.substring(0, 6).equals(HTTP_BASIC_AUTH_PREFIX)) {
				
				String basicAuthEncoded = header.substring(6);
				String basicAuthAsString = new String(
						Base64.decodeBase64(basicAuthEncoded));
				String user = basicAuthAsString.substring(0, basicAuthAsString.indexOf(":"));
				String password = basicAuthAsString.substring(basicAuthAsString.indexOf(":") + 1);
				CredentialsUsernamePassword cup = new CredentialsUsernamePassword();
				cup.setUsername(user);
				cup.setPassword(password);
				if (authenticationSystem.checkUser(cup)) {
					
					String uuid = getUUID();
					multiTenantContextSetter.setCurrentTenantId(user);
					sessionSetter.setCurrentSessionId(uuid);
					
					org.apache.log4j.MDC.put("UUID", uuid);
					org.apache.log4j.MDC.put("USER", user);
					
					filterChain.doFilter(servletRequest, servletResponse);
					
					org.apache.log4j.MDC.remove("UUID");
					org.apache.log4j.MDC.remove("USER");
					
				} else {
					log.warn("User (" + user + ") with password (" + password + ") access allowed");
					response.sendError(HttpServletResponse.SC_FORBIDDEN,
	                        "User (" + user + ") not allowed or incorrect password");
				}
			} else {
				// Prompt for user credentials
		        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        response.setHeader("WWW-Authenticate", "basic realm=\"Auth (" + new Date() + ")\"" );
			}
			
		}
	}
	
	private boolean excludeFromFilter(String requestQueryString) {
		return requestQueryString != null && requestQueryString.endsWith(WSDL_RESOURCE_SUFFIX);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}