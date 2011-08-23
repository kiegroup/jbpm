package org.jbpm.formbuilder.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlRootElementProvider;
import org.jboss.resteasy.plugins.providers.jaxb.XmlJAXBContextFinder;

public class FormBuilderResteasy extends Application {

    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    
    private final Set<Object> singletons = new HashSet<Object>();
    
    public FormBuilderResteasy() {
        classes.add(XmlJAXBContextFinder.class);
        classes.add(JAXBXmlRootElementProvider.class);
        
        singletons.add(new RESTMenuService());
        singletons.add(new RESTFormService());
        singletons.add(new RESTIoService());
        singletons.add(new XmlJAXBContextFinder());
        singletons.add(new JAXBXmlRootElementProvider());
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
