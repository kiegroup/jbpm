package org.jbpm.integration.console.shared.model;

/**
 * This class holds the information returned by the guvnor /rest/packages call.
 * </p>
 * The information is primarily used to determine whether a package has been deleted and recreated. 
 */
public class GuvnorPackage {

    private String title;
    private String uuid;
    private Boolean archived;
  
    public GuvnorPackage() { 
       // default 
    }
    
    public GuvnorPackage(String title, boolean archived, String uuid) { 
       this.title = title;
       this.archived = archived;
       this.uuid = uuid;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getArchived() {
        return archived;
    }
    
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
 
    public String toString() {
       return this.title + " [UUID: " + this.uuid + ", ARCHIVED: " + this.archived + "]";
    }
}