package org.jbpm.persistence.processinstance.objects;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity
@Indexed
public class NonSerializableClass {
    
	@Id @DocumentId @Field
	private String id;
	@Field
	private String someString;
	@SuppressWarnings("unused")
	@Field
    private Date creationDate;

	public NonSerializableClass() { 
	    creationDate = new Date();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSomeString() {
		return someString;
	}

	public void setString(String someString) {
		this.someString = someString;
	}

}
