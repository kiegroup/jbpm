package org.jbpm.services.task.query;

import static org.junit.Assert.fail;

import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.jbpm.services.task.HumanTaskServicesBaseTest;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl_;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * If this test *fails*, 
 * then that's a GOOD thing!
 * </p>
 * If this test failes, 
 * then please move the test classes in the jbpm-human-task-query-tests module
 * *back* into the jbpm-human-task-core module! 
 */
public class KeepQueryTestsInATheJbpmHumanTaskQueryTestsModuleTest extends HumanTaskServicesBaseTest {

    private PoolingDataSource pds;
    private EntityManagerFactory emf;
        
    @Before
    public void setup() {
        pds = setupPoolingDataSource();
        emf = Persistence.createEntityManagerFactory( "org.jbpm.services.task" );
    }
   
    @After
    public void clean() {
        super.tearDown();
        if (emf != null) {
            emf.close();
        }
        if (pds != null) {
            pds.close();
        }
    }

    private static void skipTestIfRedhatJarsAreInClasspath() { 
        CodeSource codeSource = HibernateEntityManagerFactory.class.getProtectionDomain().getCodeSource();
        if ( codeSource != null) {
            String jarLoc = codeSource.getLocation().toExternalForm();
            String jarName = jarLoc.replaceFirst(".*/hibernate-entitymanager-4", "hibernate-entitymanager-4");
            Assume.assumeFalse("Classpath contains '" + jarName + "', skipping test", jarName.contains("Final-redhat-"));
        }
    }

    
    /**
     * The issue here is the following: 
     * 
     * 1. The code for BPMSPL-165 works with Hib 4.2.19.Final-redhat-1, but not with Hib 4.2.9.Final.
     * 2. Hib 4.*3*.9.Final is the first community release that fixes this -- but it uses
     * JPA 2.1 (and 4.3.9.Final is *not* compatibile with JPA 2.0)
     * </p>
     * When this test fails, it means that we've upgraded to a Hib 4.3.9+ version and can move the tests back 
     * into this module (from the jbpm-human-task-query-tests module).
     */
    @Test
    public void testJPACriteriaNonEmbeddedTransitiveLeftJoin() {
        skipTestIfRedhatJarsAreInClasspath();
        
        EntityManager em = emf.createEntityManager();
       
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<TaskImpl> task = query.from(TaskImpl.class);
        List<String> groupIds = new ArrayList<String>();
        groupIds.add("Elan");
        
        // @formatter:off
        query.select(task.get(TaskImpl_.id));
        query.where(task
                .join(TaskImpl_.peopleAssignments)
                .join(PeopleAssignmentsImpl_.businessAdministrators)
                .get(OrganizationalEntityImpl_.id)
                .in(groupIds));
        // @formatter:on

        try { 
            em.createQuery(query).getResultList();
            fail("Please move the jbpm-human-task-query-tests test classes back into jbpm-human-task!");
        } catch( Exception t ) { 
            
        }
    }
}
