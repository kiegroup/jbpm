package org.jbpm.process;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.junit.Test;
import org.kie.api.runtime.process.EventListener;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class FlowTypesTest {

    private static final Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.jbpm"),
            new TypeAnnotationsScanner(),
            new FieldAnnotationsScanner(), new SubTypesScanner());

    @Test
    public void eventListenerTypesTest() {
       Set<Class<? extends EventListener>> eventListenerImpls = reflections.getSubTypesOf(EventListener.class);

       for( Class<? extends EventListener> eventListenerType : eventListenerImpls ) {
          assertTrue( eventListenerType.getSimpleName(),
                  ProcessImplementationPart.class.isAssignableFrom(eventListenerType) );
       }
    }
}
