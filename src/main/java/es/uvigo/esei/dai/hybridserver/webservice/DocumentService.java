package es.uvigo.esei.dai.hybridserver.webservice;


import java.util.List;
import java.util.Set;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;


@WebService
public interface DocumentService {

	@WebMethod
	public List<String> getHtmlUuids();
	@WebMethod
	public List<String> getXmlUuids();
	@WebMethod
	public List<String> getXsdUuids();
	@WebMethod
	public List<String> getXsltUuids();
	
	@WebMethod
	public String getHtmlContent(String htmlUuid);
	@WebMethod
	public String getXmlContent(String xmlUuid);
	@WebMethod
	public String getXsdContent(String xsdUuid);
	@WebMethod
	public String getXsltContent(String xsltUuid);

	@WebMethod
	public String getAssociatedXsdUuid(String xsltUuid);

}