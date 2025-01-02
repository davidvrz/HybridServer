package es.uvigo.esei.dai.hybridserver.webservice;

import java.util.List;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.model.HTMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSDDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSLTDBDAO;
import jakarta.jws.WebService;




@WebService(
		endpointInterface = "es.uvigo.esei.dai.webservice.WebServiceInterface",
		serviceName = "HybridServerService",
		targetNamespace = "http://hybridserver.dai.esei.uvigo.es/"
)
public class ControllerService implements WebServiceInterface {

	String DB_URL, DB_PASSWORD, DB_USER;
	
	public ControllerService (String DB_URL, String DB_PASSWORD, String DB_USER) {
		this.DB_URL = DB_URL;
		this.DB_PASSWORD = DB_PASSWORD;
		this.DB_USER = DB_USER;
	}

	public List<String> getHtmlUuids() {
		HTMLDBDAO dbDao = new HTMLDBDAO();
		return dbDao.listDocuments();
	}

	public List<String> getXmlUuids() {
		XMLDBDAO dbDao = new XMLDBDAO();
		return dbDao.listDocuments();
	}


	public List<String> getXsdUuids() {
		XSDDBDAO dbDao = new XSDDBDAO();
		return dbDao.listSchemas();
	}


	public List<String> getXsltUuids() {
		XSLTDBDAO dbDao = new XSLTDBDAO();
		return dbDao.listStylesheets();
	}


	public String getHtmlContent(String htmlUuid) {
		HTMLDBDAO dbDao = new HTMLDBDAO();
		return dbDao.getDocument(htmlUuid);
	}


	public String getXmlContent(String xmlUuid) {
		XMLDBDAO dbDao = new XMLDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getDocument(xmlUuid);
	}


	public String getXsdContent(String xsdUuid) {
		XSDDBDAO dbDao = new XSDDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getSchema(xsdUuid);
	}


	public String getXsltContent(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getXsd(xsltUuid);
	}


	public String getAssociatedXsdUuid(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_PASSWORD, DB_USER);
		return dbDao.getXsd(xsltUuid);
	}

}
