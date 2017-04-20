package org.jbpm.services.task.assignment.impl;

import org.jbpm.services.task.assignment.LoadCalculator;

public abstract class LoadCalculatorImpl implements LoadCalculator {
	protected String loadCalculatorIdentifier;
	
	public LoadCalculatorImpl(String loadCalculatorIdentifier) {
		this.loadCalculatorIdentifier = loadCalculatorIdentifier;
	}
	
	@Override
	public String getIdentifier() {
		return this.loadCalculatorIdentifier;
	}
}
