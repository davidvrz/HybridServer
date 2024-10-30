package es.uvigo.esei.dai.hybridserver.model;
/**
 *
 * @author Usuario
 */
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PagesMapDAO {
	
	private final Map<String, String> content;
	
	public PagesMapDAO() {
		content = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			UUID uuid = UUID.randomUUID();
			content.put(uuid.toString(), "<html><body>Pagina " + i + "</body></html>");			
		}
	}
	
	public PagesMapDAO(Map<String, String> content) {
		this.content = content;
	}
	
	public boolean contains(String uuid) {
        return content.containsKey(uuid);
	}
	
	public String getContent(String uuid) {
		return content.get(uuid);
	}
	
	public String toString() {
		return content.toString();
	}

	public void add(String uuid, String html) {
		content.put(uuid, html);
	}
	
	public void delete(String uuid) {
		content.remove(uuid);		
	}

	public Set<String> list() {
		return content.keySet();
	}

}