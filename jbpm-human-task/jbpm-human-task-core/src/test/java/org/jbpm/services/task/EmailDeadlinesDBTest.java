/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.identity.DBUserInfoImpl;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.kie.internal.task.api.InternalTaskService;


public class EmailDeadlinesDBTest extends EmailDeadlinesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(EmailDeadlinesDBTest.class);
    private static final String CREATE_USERS = "create table Users (entityId varchar(255), email varchar(255), lang varchar(255), name varchar(255))";
    private static final String CREATE_GROUPS = "create table UserGroups (groupId varchar(255), entityId varchar(255), email varchar(255))";
    private static final String INSERT_INTO_USERS = "insert into Users (entityId, email, lang, name) values (?, ?, ?, ?)";
    private static final String DROP_USERS = "drop table Users";
    private static final String DROP_GROUPS = "drop table UserGroups";
    
    private PoolingDataSourceWrapper pds;
    private EntityManagerFactory emf;
    
    
    @Before
    public void setup() {
        pds = setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );
        super.setup();
        
        executeStatement(CREATE_USERS);
        executeStatement(CREATE_GROUPS);
        executeStatement(INSERT_INTO_USERS, "Darth Vader", "darth@domain.com", "en-UK", "Darth Vader");
        executeStatement(INSERT_INTO_USERS, "Tony Stark", "tony@domain.com", "en-UK", "Tony Stark");
                 
        Properties props = new Properties();
        props.setProperty(DBUserInfoImpl.DS_JNDI_NAME, "jdbc/jbpm-ds");
        props.setProperty(DBUserInfoImpl.NAME_QUERY, "select name from Users where entityId = ?");
        props.setProperty(DBUserInfoImpl.EMAIL_QUERY, "select email from Users where entityId = ?");
        props.setProperty(DBUserInfoImpl.LANG_QUERY, "select lang from Users where entityId = ?");
        props.setProperty(DBUserInfoImpl.MEMBERS_QUERY, "select entityId from UserGroups where groupId = ?");

        DBUserInfoImpl userInfo = new DBUserInfoImpl(props);
                
        this.taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
                                                .entityManagerFactory(emf)
                                                .userInfo(userInfo)
                                                .getTaskService();
    }
    
    @After
    public void clean() {
        TaskDeadlinesServiceImpl.reset();
        super.tearDown();
        executeStatement(DROP_USERS);
        executeStatement(DROP_GROUPS);
        if (emf != null) {
            emf.close();
        }
        if (pds != null) {
            pds.close();
        }
    }
    
    private void executeStatement(String sql, String... params) {
        try (Connection conn = pds.getConnection();
            PreparedStatement st = conn.prepareStatement(sql);){
            if (params.length != 0)
                setParameters(st, params);
            st.execute();
        } catch (SQLException e) {
            logger.error("Unexpected exception: ", e);
        }
    }
    
    private void setParameters(PreparedStatement ps, String... params) throws SQLException {
        for (int i=0; i < params.length; i++) {
            ps.setString(i+1, params[i]);
        }
    }

}