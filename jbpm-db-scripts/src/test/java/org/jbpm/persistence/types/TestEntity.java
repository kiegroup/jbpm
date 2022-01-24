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

package org.jbpm.persistence.types;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/*
 * create sequence hibernate_sequence start 1 increment 1;
 * create table testentity(id int8 not null, processInstanceByteArray bytea, errorInfo text, primary key(id)); 
 */

@Entity
@Table(name = "testentity")
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Lob
    @Column(length = 2147483647)
    private byte[] processInstanceByteArray;

    @Lob
    @Column(length = 65535)
    private String errorInfo;

    public TestEntity() {
    }

    public TestEntity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getProcessInstanceByteArray() {
        return processInstanceByteArray;
    }

    public void setProcessInstanceByteArray(byte[] processInstanceByteArray) {
        this.processInstanceByteArray = processInstanceByteArray;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

}