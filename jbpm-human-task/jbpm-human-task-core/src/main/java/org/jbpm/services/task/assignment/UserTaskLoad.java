package org.jbpm.services.task.assignment;

import java.io.Serializable;

import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.User;

public class UserTaskLoad implements Serializable, Comparable<UserTaskLoad> {
	private static final long serialVersionUID = 19630331L;
	private String calculatorIdentifier;
	private User user;
	private Double calculatedLoad;

	public UserTaskLoad(String calculatorIdentifier, User user, Double calculatedLoad) {
		super();
		this.calculatorIdentifier = calculatorIdentifier;
		this.user = user;
		this.calculatedLoad = calculatedLoad;
	}
	
	public UserTaskLoad(String calculatorIdentifier, String user, Double calculatedLoad) {
		super();
		this.calculatorIdentifier = calculatorIdentifier;
		this.user = new UserImpl(user);
		this.calculatedLoad = calculatedLoad;
	}
	
	public UserTaskLoad(String calculatorIdentifier, User user) {
		super();
		this.calculatorIdentifier = calculatorIdentifier;
		this.user = user;
		this.calculatedLoad = Double.NaN;
	}
	
	public UserTaskLoad(String calculatorIdentifier, String user) {
		super();
		this.calculatorIdentifier = calculatorIdentifier;
		this.user = new UserImpl(user);
		this.calculatedLoad = Double.NaN;
	}

	public String getCalculatorIdentifier() {
		return calculatorIdentifier;
	}
	public void setCalculatorIdentifier(String calculatorIdentifier) {
		this.calculatorIdentifier = calculatorIdentifier;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Double getCalculatedLoad() {
		return calculatedLoad;
	}
	public void setCalculatedLoad(Double calculatedLoad) {
		this.calculatedLoad = calculatedLoad;
	}

	@Override
	public int compareTo(UserTaskLoad o) {
		if (o == null) {
			throw new IllegalArgumentException("Illegal attempt to compare UserTaskLoad with a null object");
		}
		if (this.calculatedLoad == Double.NaN || o.calculatedLoad == Double.NaN) {
			throw new IllegalStateException("Uninitialized UserTaskLoad encountered during UserTaskLoad comparison");
		}
		
		return this.calculatedLoad.compareTo(o.calculatedLoad);
	}
	
}
