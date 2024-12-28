package es.uvigo.esei.dai.hybridserver.model;

import java.util.List;

public interface XSLTDAO {

	public void addStylesheet(String uuid, String xslt, String xsd);
	public boolean containsStylesheet(String uuid);
	public void deleteStylesheet(String uuid);
 	public String getStylesheet(String uuid);
	public List<String> listStylesheets();
 	public String getXsd(String uuid);

}
