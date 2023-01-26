package org.jbpm.test.domain;
public class OrderDetails implements java.io.Serializable {

	static final long serialVersionUID = 1L;

	@org.kie.api.definition.type.Label(value = "status")
	private java.lang.String status;

	public OrderDetails() {
	}

	public java.lang.String getStatus() {
		return this.status;
	}

	public void setStatus(java.lang.String status) {
		this.status = status;
	}

	public OrderDetails(java.lang.String status) {
		this.status = status;
	}

}