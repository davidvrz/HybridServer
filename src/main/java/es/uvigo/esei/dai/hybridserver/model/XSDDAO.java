package es.uvigo.esei.dai.hybridserver.model;

import java.util.List;

public interface XSDDAO {
	public void addSchema(String uuid, String xsd);
	public boolean containsSchema(String uuid);
	public void deleteSchema(String uuid);
 	public String getSchema(String uuid);
	public List<String> listSchemas();

}
