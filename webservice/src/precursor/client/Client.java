/************************************************************************
* IBM Confidential
* OCO Source Materials
* *** IBM Security Identity Manager ***
*
* (C) Copyright IBM Corp. 2015  All Rights Reserved.
*
* The source code for this program is not published or otherwise  
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office.
*************************************************************************/
package precursor.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSOrganizationalContainerServiceService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSPersonServiceService;
import com.ibm.itim.ws.services.WSServiceService;
import com.ibm.itim.ws.services.WSServiceServiceService;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSSessionService_Service;

public abstract class Client {

    static Logger m_logger = Logger.getLogger(Client.class.getSimpleName());
    
	// Command line argument names prefixed by "-"
	public static final String OPERATION_NAME = "operationName";
	public static final String OPERATION_PARAMETERS = "parameters";
	public static final String PARAM_ITIM_CREDENTIAL = "credential";
	public static final String PARAM_ITIM_PRINCIPAL = "principal";
	public static final String[][] utilParams = new String[][] { { OPERATION_NAME,	"No operationName specified" } };

	static String HOST = null;
	static String PORT = null;
	static final String WS_CONFIG_FILE = "ws_config.properties";
	static final String WS_SUFFIX = "/itim/services/";
	static final String WS_WSDL_LOCATION = "/WEB-INF/wsdl/";
	static final String namespaceURI = "http://services.ws.itim.ibm.com";
	static boolean isCustomHostAndPortProvided = true;
	
	/*
	 * The static block below reads the ws_example_config.properties file 
	 * and sets the values of HOST and PORT */
	static {		
		java.util.Properties properties = new java.util.Properties();		
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
		} catch (FileNotFoundException e1) {
			System.out.println("The properties file ("+ WS_CONFIG_FILE +"), used for configuring host and port was not found");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String host = properties.getProperty("host");
		if( host!=null && !host.isEmpty()){
			HOST = host;
			String port = properties.getProperty("port");
			if( port!=null && !port.isEmpty()){
				PORT = port;
			} else {
				isCustomHostAndPortProvided = false;			
			}
		} else {
			isCustomHostAndPortProvided = false;
		}		
	}

	/**
	 * @param serviceName
	 *            Name of the service
	 * @param serviceWSDLFileName
	 *            Name of WSDL file of service
	 * @return URL of the service specified in the input argument
	 */
	private static URL generateServiceURL(String serviceName, String serviceWSDLFileName){
	
		String strURL = "https://" + HOST + ":" + PORT + WS_SUFFIX + serviceName + WS_WSDL_LOCATION + serviceWSDLFileName;		
		URL url = null;
		try {
			url = new URL(strURL);		
		} catch (MalformedURLException e) {
			System.out.println("Failed to generate the wsdl resource URL");
			e.printStackTrace();
			System.exit(-1);
		}
		return url;
	}
	
	/**
	 * @return A Session service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSSessionService getSessionService() {
		
		WSSessionService_Service service = null;
		URL url = null;		
		String serviceName = "WSSessionService";
		
		if(!isCustomHostAndPortProvided){
			m_logger.info("if url : " + url);
			service = new WSSessionService_Service();	
		} else if ( (url = generateServiceURL(serviceName, "WSSessionService.wsdl") ) != null) {
			m_logger.info("else if url : " + url);
			m_logger.info("else if namespaceURI : " + namespaceURI);
			m_logger.info("else if serviceName : " + serviceName);
			service = new WSSessionService_Service(url, new QName(namespaceURI, serviceName));
			
			System.out.println(" getSessionService / service ==> " + service );
			
		} else {			
			return null;
		}
		WSSessionService proxy = service.getWSSessionServicePort();
		
		System.out.println(" getSessionService / Proxy ==> " + proxy);
		
		return proxy;			
	}

	
	/**
	 * @return A Person Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSPersonService getPersonService() { // API 사용해서 PersonService 가져오기
		
		// Person과 Object 관련 메서드 제공
		// 생성, 수정, 일시 중지, 복원, 삭제 및 기타 간단한 개인 작업 제공
		// Person 검색
		// 본인의 Person Object 검색
		
		WSPersonServiceService service = null;
		URL url = null;
		String serviceName = "WSPersonServiceService";
		
		System.out.println(" serviceName ==> " + serviceName );

		if (!isCustomHostAndPortProvided) {
			
			service = new WSPersonServiceService(); //여기안탐 로그찍어봄 
			
		} else if ((url = generateServiceURL(serviceName,"WSPersonService.wsdl")) != null) {
			
			service = new WSPersonServiceService(url, new QName(namespaceURI,serviceName));
			
			System.out.println(" getPersonServcie / service ==> " + service);
			
		} else {
			return null;
		}
		WSPersonService proxy = service.getWSPersonService();
		
		System.out.println(" getPersonService / Proxy ==>  " +proxy );
		
		return proxy;
	}

	
	/**
	 * @return An Organizational Container Service port on which the service's APIs can be
	 *         invoked.
	 */
	// 조직 컨테이너 서비스
	protected static WSOrganizationalContainerService getOrganizationalContainerService() {
		
		WSOrganizationalContainerServiceService service = null;
		URL url = null;
		String serviceName = "WSOrganizationalContainerServiceService";
		
		System.out.println(" serviceName ==> " + serviceName);

		if (!isCustomHostAndPortProvided) { //isCustomHostAndPortProvided 는 true 이다 (초기값)
			service = new WSOrganizationalContainerServiceService(); //false일 경우 service라는 
		} else if ((url = generateServiceURL(serviceName,
				"WSOrganizationalContainerService.wsdl")) != null) {
			service = new WSOrganizationalContainerServiceService(url,
					new QName(namespaceURI, serviceName));
		} else {
			return null;
		}
		WSOrganizationalContainerService proxy = service
				.getWSOrganizationalContainerService();
		return proxy;
	}
	/**
	 * @return A Service service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSServiceService getServiceService() {
		
		WSServiceServiceService service = null;
		URL url = null;
		String serviceName = "WSServiceServiceService";
		if(!isCustomHostAndPortProvided){	

			m_logger.info("if url : " + url);
			service = new WSServiceServiceService();			
		} else if ( (url = generateServiceURL(serviceName, "WSServiceService.wsdl") ) != null) {

			m_logger.info("else if url : " + url);
			service = new WSServiceServiceService(url, new QName(namespaceURI, serviceName));			
		} else {
			
			return null;
		}	
		
		WSServiceService proxy = service.getWSServiceService();
		return proxy;			
	}
	
	
}
