package org.jbpm.persistence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.drools.definition.process.Process;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class PersistenceTest {
    
    private PoolingDataSource ds = new PoolingDataSource();
    private EntityManagerFactory emf;
    
    @Before
    public void setUp() throws Exception {
        ds.setUniqueName("jdbc/testDS1");
        ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("user", "sa");
        ds.getDriverProperties().put("password", "");
        ds.getDriverProperties().put("url", "jdbc:h2:tcp://localhost/runtime/jbpm");
        ds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        ds.init();
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
    }
    
    @After
    public void tearDown() {
        // clean up db for another test run
        try {
            UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
            ut.begin();
            emf.createEntityManager().createNativeQuery("delete from processinstanceinfo").executeUpdate();
            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        ds.close();
    }

    @Test
    public void test() throws Exception {
       
        
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        ut.begin();
        
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        em.persist(new ProcessInstanceInfo(new ProcessInstance() {
            
            public void signalEvent(String type, Object event) {
                // TODO Auto-generated method stub
                
            }
            
            public String[] getEventTypes() {
                // TODO Auto-generated method stub
                return null;
            }
            
            public int getState() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            public String getProcessName() {
                // TODO Auto-generated method stub
                return null;
            }
            
            public String getProcessId() {
                
                return "test";
            }
            
            public Process getProcess() {
                // TODO Auto-generated method stub
                return null;
            }
            
            public long getId() {
                // TODO Auto-generated method stub
                return 0;
            }
        }));
        
        ut.rollback();
        
        em.close();
        em = null;
        
        em = emf.createEntityManager();
        
        List<ProcessInstanceInfo> result = em.createQuery("from ProcessInstanceInfo").getResultList();
        assertEquals(0, result.size());
    }

}
