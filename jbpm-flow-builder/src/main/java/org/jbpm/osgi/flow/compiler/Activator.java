package org.jbpm.osgi.flow.compiler;

import java.util.Hashtable;

import org.drools.Service;
import org.drools.compiler.ProcessBuilderFactoryService;
import org.jbpm.process.builder.ProcessBuilderFactoryServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator
    implements
    BundleActivator {

    private ServiceRegistration processBuilderReg;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void start(BundleContext bc) throws Exception {
        this.logger.debug("registering flow compiler services");
        this.processBuilderReg = bc.registerService( new String[]{ ProcessBuilderFactoryService.class.getName(), Service.class.getName()},
                                                                   new ProcessBuilderFactoryServiceImpl(),
                                                                   new Hashtable() );
        this.logger.debug("flow compiler services registered");
    }

    public void stop(BundleContext bc) throws Exception {
        this.processBuilderReg.unregister();
    }

}