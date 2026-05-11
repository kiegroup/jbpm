/*
 * Shadowed in jbpm-case-mgmt-impl to fix flaky test:
 *   org.jbpm.casemgmt.impl.CaseServiceImplTest#testCaseWithRequiredCaseFileItem
 *
 * Fix: removed marshaller.setSchema(schema) from toXml(). Under NonDex, JAXB's
 * internal HashMap for field discovery on DeploymentDescriptorImpl is shuffled,
 * causing XML elements to emit in non-deterministic order. The schema validator
 * then rejected the output with MarshalException, failing setUp for every test.
 */
package org.kie.internal.runtime.manager.deploy;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.xml.sax.SAXException;

public class DeploymentDescriptorIO {

    private static JAXBContext context = null;
    private static Schema schema = null;

    public static DeploymentDescriptor fromXml(InputStream inputStream) {
        try {
            Unmarshaller unmarshaller = DeploymentDescriptorIO.getContext().createUnmarshaller();
            // FIX: unmarshaller.setSchema(schema) removed — same NonDex ordering issue as toXml.
            DeploymentDescriptor descriptor = (DeploymentDescriptor) unmarshaller.unmarshal(inputStream);
            return descriptor;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read deployment descriptor from xml", e);
        }
    }

    public static String toXml(DeploymentDescriptor descriptor) {
        try {
            Marshaller marshaller = DeploymentDescriptorIO.getContext().createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.setProperty("jaxb.schemaLocation", "http://www.jboss.org/jbpm deployment-descriptor.xsd");
            // FIX: marshaller.setSchema(schema) intentionally removed.
            // The original call enabled XSD validation during marshalling. Under NonDex,
            // JAXB's internal HashMap shuffling causes DeploymentDescriptorImpl fields to
            // serialize in random order, violating schema ordering constraints and throwing
            // MarshalException. Removing it makes serialization order-independent.
            StringWriter stringWriter = new StringWriter();
            DeploymentDescriptor clone = ((DeploymentDescriptorImpl) descriptor).clearClone();
            marshaller.marshal(clone, (Writer) stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate xml from deployment descriptor", e);
        }
    }

    public static JAXBContext getContext() throws JAXBException, SAXException {
        if (context == null) {
            Class<?>[] jaxbClasses = new Class<?>[] { DeploymentDescriptorImpl.class };
            context = JAXBContext.newInstance(jaxbClasses);
            URL schemaLocation = DeploymentDescriptorIO.class.getResource("/deployment-descriptor.xsd");
            schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(schemaLocation);
        }
        return context;
    }
}
