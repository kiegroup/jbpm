/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.jbpm.services.task.identity.DBUserGroupCallbackImpl.DS_JNDI_NAME;
import static org.jbpm.services.task.identity.DBUserGroupCallbackImpl.PRINCIPAL_QUERY;
import static org.jbpm.services.task.identity.DBUserGroupCallbackImpl.ROLES_QUERY;
import static org.jbpm.services.task.identity.DBUserGroupCallbackImpl.USER_ROLES_QUERY;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DBUserGroupCallbackImplTest {

    protected enum Configuration {
        PROGRAMMATICALLY, DECLARATIVELY
    }
    
    protected static final String DATASOURCE_PROPERTIES = "/datasource.properties";
    private PoolingDataSourceWrapper pds;
    private Properties props;
    
    private Configuration config;
    private DBUserGroupCallbackImpl callback;
    
    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 { Configuration.PROGRAMMATICALLY }, { Configuration.DECLARATIVELY }
           });
    }
    
    public DBUserGroupCallbackImplTest(Configuration config) {
        this.config = config;
    }

    @Before
    public void setup() {
        Properties dsProps = loadDataSourceProperties();
        pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", dsProps);

        prepareDb();

        configureProps();
    }

    private void configureProps() {
        switch (config) {
            case PROGRAMMATICALLY:
                configurePropsProgrammatically();
                break;

            case DECLARATIVELY:
                System.setProperty("jbpm.usergroup.callback.properties", "/jbpm.usergroup.callback.db.properties");
                callback = new DBUserGroupCallbackImpl(true);
                break;

            default:
                throw new IllegalArgumentException("unknown config type");
        }
    }

    private void configurePropsProgrammatically() {
        props = new Properties();
        props.setProperty(DS_JNDI_NAME, "jdbc/jbpm-ds");
        props.setProperty(PRINCIPAL_QUERY, "select userId from Users where userId = ?");
        props.setProperty(ROLES_QUERY, "select groupId from UserGroups where groupId = ?");
        props.setProperty(USER_ROLES_QUERY, "select groupId from UserGroups where userId = ?");
        
        callback = new DBUserGroupCallbackImpl(props);
    }

    protected Properties loadDataSourceProperties() {

        InputStream propsInputStream = getClass().getResourceAsStream(DATASOURCE_PROPERTIES);

        Properties dsProps = new Properties();
        if (propsInputStream != null) {
            try {
                dsProps.load(propsInputStream);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return dsProps;
    }

    protected void prepareDb() {
        try {
            Connection conn = pds.getConnection();
            String createUserTableSql = "create table Users (userId varchar(255))";
            PreparedStatement st = conn.prepareStatement(createUserTableSql);
            st.execute();

            String createGroupTableSql = "create table UserGroups (groupId varchar(255), userId varchar(255))";
            st = conn.prepareStatement(createGroupTableSql);
            st.execute();

            // insert user rows
            String insertUser = "insert into Users (userId) values (?)";
            st = conn.prepareStatement(insertUser);
            st.setString(1, "john");
            st.execute();

            // insert group rows
            String insertGroup = "insert into UserGroups (groupId, userId) values (?, ?)";
            st = conn.prepareStatement(insertGroup);
            st.setString(1, "PM");
            st.setString(2, "john");
            st.execute();


            st.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    protected void cleanDb() {
        try {
            Connection conn = pds.getConnection();
            String dropUserTableSql = "drop table Users";
            PreparedStatement st = conn.prepareStatement(dropUserTableSql);
            st.execute();

            String dropGroupTableSql = "drop table UserGroups";
            st = conn.prepareStatement(dropGroupTableSql);

            st.execute();

            st.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @After
    public void cleanup() {
        System.clearProperty("jbpm.usergroup.callback.properties");
        cleanDb();
        pds.close();
    }

    @Test
    public void testUserExists() {
        boolean exists = callback.existsUser("john");
        assertTrue(exists);
    }

    @Test
    public void testGroupExists() {
        boolean exists = callback.existsGroup("PM");
        assertTrue(exists);
    }

    @Test
    public void testUserGroups() {
        List<String> groups = callback.getGroupsForUser("john");
        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertEquals("PM", groups.get(0));
    }

    @Test
    public void testUserNotExists() {
        boolean exists = callback.existsUser("mike");
        assertFalse(exists);
    }

    @Test
    public void testGroupNotExists() {
        boolean exists = callback.existsGroup("HR");
        assertFalse(exists);
    }

    @Test
    public void testNoUserGroups() {
        List<String> groups = callback.getGroupsForUser("mike");
        assertNotNull(groups);
        assertEquals(0, groups.size());

    }

    @Test
    public void testInvalidConfiguration() {
        assertThatThrownBy(this::configureWrongProps)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("All properties must be given ("+ DS_JNDI_NAME + ","
                  + PRINCIPAL_QUERY +"," + ROLES_QUERY +"," +USER_ROLES_QUERY +")");
    }
    
    private void configureWrongProps() {
        switch (config) {
            case PROGRAMMATICALLY:
                callback = new DBUserGroupCallbackImpl(new Properties());
                break;

            case DECLARATIVELY:
                System.setProperty("jbpm.usergroup.callback.properties", "/fake.properties");
                callback = new DBUserGroupCallbackImpl(true);
                break;

            default:
                throw new IllegalArgumentException("unknown config type");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgument() {
        callback.getGroupsForUser(null);
        fail("Should fail as it does not have valid configuration");

    }
}
