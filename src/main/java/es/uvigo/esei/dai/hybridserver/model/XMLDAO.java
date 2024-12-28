package es.uvigo.esei.dai.hybridserver.model;

import java.util.List;

public interface XMLDAO {
	public void addDocument(String uuid, String xml);
	public boolean containsDocument(String uuid);
	public void deleteDocument(String uuid);
	public String getDocument(String uuid);
	public List<String> listDocuments();
 	

}
