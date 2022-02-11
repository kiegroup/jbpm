/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.test.functional.event;
public class MyFact implements java.io.Serializable {

	static final long serialVersionUID = 1L;

	private java.lang.Integer id;
	private java.lang.String name;
	private boolean conditionA;
	private boolean conditionB;

	public MyFact() {
	}

	public java.lang.Integer getId() {
		return this.id;
	}

	public void setId(java.lang.Integer id) {
		this.id = id;
	}

	public java.lang.String getName() {
		return this.name;
	}

	public void setName(java.lang.String name) {
		this.name = name;
	}

	public boolean isConditionA() {
		return this.conditionA;
	}

	public void setConditionA(boolean conditionA) {
		this.conditionA = conditionA;
	}

	public boolean isConditionB() {
		return this.conditionB;
	}

	public void setConditionB(boolean conditionB) {
		this.conditionB = conditionB;
	}

	public MyFact(java.lang.Integer id, java.lang.String name,
			boolean conditionA, boolean conditionB) {
		this.id = id;
		this.name = name;
		this.conditionA = conditionA;
		this.conditionB = conditionB;
	}
	
	public String toString() {
	    return "MyFact[" + id + ", " + name + ", " + conditionA + ", " + conditionB + "]";
	}

}