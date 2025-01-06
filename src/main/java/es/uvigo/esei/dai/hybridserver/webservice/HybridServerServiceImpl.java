package es.uvigo.esei.dai.hybridserver.webservice;

import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.model.HTMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSDDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSLTDBDAO;
import jakarta.jws.WebService;


@WebService(
		endpointInterface = "es.uvigo.esei.dai.hybridserver.webservice.HybridServerService",
		serviceName = "HybridServerService",
		targetNamespace = "http://hybridserver.dai.esei.uvigo.es/"
)
public class HybridServerServiceImpl implements HybridServerService {	
	
	String DB_URL, DB_USER, DB_PASSWORD;
	
	public HybridServerServiceImpl (String dbUrl, String dbUser, String dbPassword) {
		this.DB_URL = dbUrl;
		this.DB_USER = dbUser;
		this.DB_PASSWORD = dbPassword;
	}

	public List<String> getHtmlUuids() {
		HTMLDBDAO dbDao = new HTMLDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.listDocuments();
	}

	public List<String> getXmlUuids() {
		XMLDBDAO dbDao = new XMLDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.listDocuments();
	}


	public List<String> getXsdUuids() {
		XSDDBDAO dbDao = new XSDDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.listSchemas();
	}


	public List<String> getXsltUuids() {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.listStylesheets();
	}


	public String getHtmlContent(String htmlUuid) {
		HTMLDBDAO dbDao = new HTMLDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.getDocument(htmlUuid);
	}


	public String getXmlContent(String xmlUuid) {
		XMLDBDAO dbDao = new XMLDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.getDocument(xmlUuid);
	}


	public String getXsdContent(String xsdUuid) {
		XSDDBDAO dbDao = new XSDDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.getSchema(xsdUuid);
	}


	public String getXsltContent(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.getStylesheet(xsltUuid);
	}


	public String getAssociatedXsdUuid(String xsltUuid) {
		XSLTDBDAO dbDao = new XSLTDBDAO(DB_URL, DB_USER, DB_PASSWORD);
		return dbDao.getXsd(xsltUuid);
	}

}
