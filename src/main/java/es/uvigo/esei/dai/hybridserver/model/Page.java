package es.uvigo.esei.dai.hybridserver.model;


import java.util.Objects;
import java.util.UUID;

public class Page {
    private UUID uuid;
    private String content;

    public Page(UUID uuid, String content) {
        this.uuid = uuid;
        this.content = content;
    }

    public Page(String content) {
        this.uuid = UUID.randomUUID();
        this.content = content;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
   
    @Override
    public String toString() {
        return "Page{" +
                "uuid=" + uuid +
                ", content='" + content.substring(0, Math.min(30, content.length())) + "...'" +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Page page = (Page) obj;
        return Objects.equals(uuid, page.uuid);
    }
}