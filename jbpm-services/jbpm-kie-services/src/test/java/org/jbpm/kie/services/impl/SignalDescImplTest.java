package org.jbpm.kie.services.impl;

import java.util.Collection;
import java.util.HashSet;

import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.kie.services.impl.model.SignalDescImpl;
import org.jbpm.services.api.model.SignalDesc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SignalDescImplTest {

    @Test
    public void testEqualsHashCode() {
        SignalDesc signal1 = SignalDescImpl.from(new Signal("pepe", "pepe", "pepe"));
        SignalDesc signal2 = SignalDescImpl.from(new Signal("pepe", "pepe", "pepe"));
        Message message = new Message("pepe");
        message.setName("pepe");
        message.setType("pepe");
        SignalDesc signal3 = SignalDescImpl.from(message);

        Collection<SignalDesc> signals = new HashSet<>();
        signals.add(signal1);
        signals.add(signal2);
        signals.add(signal3);
        assertEquals(2, signals.size());
    }
}
