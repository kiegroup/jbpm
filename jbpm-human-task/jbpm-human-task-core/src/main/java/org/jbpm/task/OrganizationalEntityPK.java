package org.jbpm.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class OrganizationalEntityPK implements Externalizable {
	
	private static final long serialVersionUID = 7011482498840319585L;
	
	private String id;
	@Column(name="type", insertable=false,updatable=false)
	private String dtype;
	
	public OrganizationalEntityPK() {
		
	}

	public OrganizationalEntityPK(String id, String type) {
		this.id = id;
		this.dtype = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((dtype == null) ? 0 : dtype.hashCode());
        return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof OrganizationalEntityPK) ) return false;
        OrganizationalEntityPK other = (OrganizationalEntityPK) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        
        if ( dtype == null ) {
            if ( other.dtype != null ) return false;
        } else if ( !dtype.equals( other.dtype ) ) return false;
        
        return true;
	}
	
	public String toString() {
        return "[" + dtype + ":'" + id + "']";
    }

	public void writeExternal(ObjectOutput out) throws IOException {
		if( id == null ) { 
	          id = "";
	      }
		out.writeUTF(id);
		
		if( dtype == null ) { 
	          dtype = "";
	      }
		out.writeUTF(dtype);
		
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = in.readUTF();
		
		dtype = in.readUTF();
		
	}
	
	
}
