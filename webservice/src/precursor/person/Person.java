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

package precursor.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSInvalidLoginException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSSessionService;

//import examples.ws.Person;
//import examples.ws.Person;
//import examples.ws.Person;
import precursor.client.Client;
import precursor.client.Utils;
import precursor.client.Utils2;

public class Person extends Client {

	private static final String PARAM_JUSTIFICATION = "justification";
	private static final String PARAM_ORG_CONTAINER = "orgContainer";
	private static final String PARAM_PERSON_FILTER = "(preimkey=9999)";
	
    static Logger m_logger = Logger.getLogger(Person.class.getSimpleName());
    
    
    //
	public boolean createPerson(Map<String, Object> mpParams) throws Exception
	{
		boolean executedSuccessfully = false;
		mpParams.remove(OPERATION_NAME); // 기업 명 제거
		String principal = (String) mpParams.get(PARAM_ITIM_PRINCIPAL); // 아이디 가져오는 부분 main에서 mpParams.put(); 으로 아이디를 넣어준다.
		String credential = (String) mpParams.get(PARAM_ITIM_CREDENTIAL); // 비밀번호 가져오는 부분 main에서 mpParams.put(); 으로 비밀번호를 넣어준다.
		String justification = (String)mpParams.get(PARAM_JUSTIFICATION); // 
		mpParams.remove(PARAM_ITIM_PRINCIPAL); //삭제
		mpParams.remove(PARAM_ITIM_CREDENTIAL); //삭제
		mpParams.remove(PARAM_JUSTIFICATION); //삭제
		System.out.println(" session" );
		WSSession session = loginIntoITIM(principal, credential); // 로그인 증명하는 메서드 호출
		WSPersonService personService = getPersonService();  //proxy 리턴 
		System.out.println(" session ==> " + session); // 생성한 세션 출력
		
		String containerName = (String) mpParams.get(PARAM_ORG_CONTAINER); //precursor 컨테이너 가져옴 (Person 명을 가져오는 것)
		System.out.println(" containerName ==> " + containerName); // 가져온 Person 명 출력
		
		mpParams.remove(PARAM_ORG_CONTAINER); // 삭제
		System.out.println(" PARAM_ORG_CONTAINER ==> " + PARAM_ORG_CONTAINER);

		// Dependency on OrganizationalContainerservice.
		WSOrganizationalContainerService port = getOrganizationalContainerService();
		List<WSOrganizationalContainer> lstWSOrgContainers = port.getOrganizations(session);
		System.out.println(lstWSOrgContainers);
		
		/*
		 * List<WSOrganizationalContainer> lstWSOrgContainers =
		 * port.searchContainerByName( session, null, "Organization", containerName);
		 */
		
		
		System.out.println(session +"\n"+ containerName);
		
		
		if (lstWSOrgContainers != null && !lstWSOrgContainers.isEmpty()) {
			WSOrganizationalContainer wsContainer = lstWSOrgContainers.get(0);
			WSPerson wsPerson = createWSPersonFromAttributes(mpParams);
			XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
			boolean isCreatePersonAllowed = personService.isCreatePersonAllowed(session);
			if(isCreatePersonAllowed){
				Utils.printMsg(Person.class.getName(), "execute", "createPerson", "The user " + PARAM_ITIM_PRINCIPAL + " is authorized to create a person");
				WSRequest wsRequest = personService.createPerson(session, wsContainer, wsPerson, date,justification);
				Utils.printWSRequestDetails("create person", wsRequest);
			}else{
				Utils.printMsg(Person.class.getName(), "execute", "createPerson", "The user " + PARAM_ITIM_PRINCIPAL + " is not authorized to create a person");
			}
			executedSuccessfully = true;
		} else {
			System.out.println("No container found matching " + containerName);
		}
		
		System.out.println(" executedSuccessfully ==> " + executedSuccessfully);
		
		return executedSuccessfully;
	}
	
	
	

	
	WSPerson createWSPersonFromAttributes(Map<String, Object> mpParams) { //Person에 맞게 속성 넣어주는 메서드 (생성)
		WSPerson wsPerson = new WSPerson();
		//wsPerson.setProfileName(ObjectProfileCategory.PERSON);
		wsPerson.setProfileName("preperson"); //Person 명 입력
		java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
		WSAttribute wsAttr = null;
		List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();

		while (itrParams.hasNext()) {
			String paramName = itrParams.next();
			Object paramValue = mpParams.get(paramName);
			wsAttr = new WSAttribute();
			wsAttr.setName(paramName);
			ArrayOfXsdString arrStringValues = new ArrayOfXsdString();

			if (paramValue instanceof String) {
				arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
			} else if (paramValue instanceof Vector) {
				Vector paramValues = (Vector) paramValue;
				arrStringValues.getItem().addAll(paramValues);
			} else {
				Utils.printMsg(Person.class.getName(), "createWSPersonFromAttributes", "CREATEPERSON", "The parameter value datatype is not supported.");
				//System.out.println(" The parameter value datatype is not supported.");
			}
			wsAttr.setValues(arrStringValues);
			lstWSAttributes.add(wsAttr);
		}
		ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
		attrs.getItem().addAll(lstWSAttributes);
		wsPerson.setAttributes(attrs);

		return wsPerson;
	}
	/**
	 * @param args
	 */
	
	
	// 로그인 할 때 쓰는 메서드 
	static WSSession loginIntoITIM(String principal, String credential) throws WSInvalidLoginException, WSLoginServiceException{
		
		WSSessionService proxy = getSessionService();
		WSSession session = proxy.login(principal, credential); // 로그인 체크 (자격증명)
		if(session != null)
			Utils.printMsg(Person.class.getName(), "loginIntoITIM", "loginIntoITIM", "session id is " + session.getSessionID());
		else
			Utils.printMsg(Person.class.getName(), "loginintoITIM", "loginIntoITIM", "Invalid session");
		
		return session; 
	}
	
	
	// delete person
	public boolean deletePerson(Map<String, Object> mpParams) throws Exception {
		//루트에서 person 검색함. 사용자로 부터 cn 또는 sn 필터 기준을 가져온다. 
		String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
		System.out.println(sFilterParam); // 가져온 필터 값.
		if(sFilterParam == null){
			Utils.printMsg(Person.class.getName(), "deletePerson", "DELETEPERSON", "No Filter parameter passed to search for the person to be deleted.");
			//String usage = this.getUsage("");
			//System.out.println(usage);
			return false;
		}
			
		
		//searchPerson API를 호출하여 Person을 검색.
		String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
		String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//삭제해야 할 Person이 있다는 뜻입니다. Person이 두 명 이상인 경우 첫 번째 사람을 선택하고 삭제합니다
			WSPerson personToBeDeleted = lstWSPersons.get(0);
			String personDN = personToBeDeleted.getItimDN();
			Utils.printMsg(Person.class.getName(), "deletePerson", "DELETEPERSON", "Deleting the person " + personToBeDeleted.getName() + " having DN " + personToBeDeleted.getItimDN());
			WSRequest wsRequest = personService.deletePerson(wsSession, personDN, date,justification);
			Utils.printWSRequestDetails("delete person", wsRequest);
			return true;
		}else{
			//필터 기준과 일치하는 사람을 찾지 못했다는 메시지를 출력.
			Utils.printMsg(Person.class.getName(), "deletePerson", "DELETEPERSON", "No person found matching the filter : " + sFilterParam);
			return false;
		}
		
		
	}
	
	
	
	//modifyPerson
	public boolean modifyPerson(Map<String, Object> mpParams) throws Exception{
		//루트에서 person 검색함. 사용자로 부터 cn 또는 sn 필터 기준을 가져온다. 
		String sFilterParam = (String)mpParams.get(PARAM_PERSON_FILTER);
		System.out.println(sFilterParam); // 가져온 필터 출력
		if(sFilterParam == null){
			Utils2.printMsg(Person.class.getName(), "modifyPerson", "MODIFYPERSON", "삭제할 사람을 검색하기 위해 필터 매개변수가 전달되지 않았습니다.");
			//String usage = this.getUsage("");
			//System.out.println(usage);
			return false;
		}
			
		
		//searchPerson API를 호출하여 사람을 검색.
		String principal = (String)mpParams.get(PARAM_ITIM_PRINCIPAL);
		String credential = (String)mpParams.get(PARAM_ITIM_CREDENTIAL);
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils2.long2Gregorian(new Date().getTime());
		
		System.out.println("modifyPerson date : "+date);
		
		String justification = (String)mpParams.get(PARAM_JUSTIFICATION);
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//이 내용은 수정해야 할 사람이 있다 라는 뜻 
			//사람이 두 명 이상인 경우 첫 번째 사람을 선택하고 수정한다.
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			
			System.out.println("modifyPerson personDN : " +personDN);
			
			Utils2.printMsg(Person.class.getName(), "modifyPerson", "MODIFYPERSON", "Modifying person  " + person.getName());
			//수정할 person attributes를 전달할 수 있도록 Map mpParams에서 불필요한 속성을 제거한다.
			mpParams.remove(OPERATION_NAME);
			mpParams.remove(PARAM_ITIM_PRINCIPAL);
			mpParams.remove(PARAM_ITIM_CREDENTIAL);
			mpParams.remove(PARAM_PERSON_FILTER);
			
			List<WSAttribute> lstWSAttributes = Utils2.getWSAttributesList(mpParams,"MODIFYPERSON");
			
//			System.out.println("modifyPerson lstWSAttributes : " + lstWSAttributes.get(0).getName());
//			System.out.println("modifyPerson lstWSAttributes : " + lstWSAttributes.get(1).getName());
			
			if(lstWSAttributes != null && lstWSAttributes.size() > 0){
				WSRequest wsRequest = personService.modifyPerson(wsSession, personDN, lstWSAttributes, date, null);
				Utils2.printWSRequestDetails("modify person", wsRequest);
			}else{
				Utils2.printMsg(Person.class.getName(), "modifyPerson", "MODIFYPERSON", "No modify parameters passed to the modifyPerson operation.");
				//Utils.printMsg(Person.class.getName(), "modifyPerson", "MODIFYPERSON", "\n" + this.getUsage(""));
			}
			
			
			return true;
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils2.printMsg(Person.class.getName(), "modifyPerson", "MODIFYPERSON", "No person found matching the filter : " + sFilterParam);
			return false;
		}
	}
	

	
	
	// 메인
	public static void main(String[] args)
	{
		Person person = new Person();
		Map<String, Object> mpParams = new HashMap<String, Object>();

		mpParams.put(PARAM_ITIM_PRINCIPAL, "itim manager");//SIM Console Login ID
		mpParams.put(PARAM_ITIM_CREDENTIAL, "P@ssw0rd!");//SIM Console Login PWD
//		mpParams.put(PARAM_ORG_CONTAINER, "precursor");//Organization of Person
		
		mpParams.put(PARAM_PERSON_FILTER, "(preimkey=9999)"); // key값 중 9999를 가져와라.
		
//		mpParams.put("cn", "샘플 cn");//Person's CN
//		mpParams.put("sn", "샘플입니다만 sn");//Person's SN
//		mpParams.put("uid", "샘플이지요 uid");//Person's uid
//		mpParams.put("preimkey", "9999");//Person's key
//		mpParams.put("preimflag", "Y");//Person's flag
//		mpParams.put("preimstatus", "Y");//Person's status
		
		mpParams.put("preimusernamekr", "이름이름");
		
		
		

		try {
			//person.createPerson(mpParams);
			//person.deletePerson(mpParams);// 해당 조건에 맞는 사람 삭제.
			person.modifyPerson(mpParams);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
