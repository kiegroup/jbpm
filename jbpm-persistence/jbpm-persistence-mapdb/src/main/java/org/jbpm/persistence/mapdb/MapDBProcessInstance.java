package org.jbpm.persistence.mapdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.PersisterHelper;
import org.drools.core.marshalling.impl.ProcessMarshallerWriteContext;
import org.drools.core.marshalling.impl.ProtobufMarshaller;
import org.drools.persistence.mapdb.MapDBTransformable;
import org.jbpm.marshalling.impl.JBPMMessages;
import org.jbpm.marshalling.impl.ProcessInstanceMarshaller;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.marshalling.impl.ProtobufRuleFlowProcessInstanceMarshaller;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.ProcessInstance;
import org.mapdb.DB;

public class MapDBProcessInstance implements PersistentProcessInstance, MapDBTransformable {

	private Long id;
	private ProcessInstance processInstance;
	private String processId;
	private Date startDate;
	private Date lastModificationDate;
	private int state;
	private Set<String> eventTypes = new HashSet<>();
	private byte[] processInstanceByteArray;
	private boolean deleted = false;
	
	private Environment env;

	public MapDBProcessInstance(ProcessInstance processInstance, Environment environment) {
		this.processInstance = processInstance;
        this.processId = processInstance.getProcessId();
        this.startDate = new Date();
        this.env = environment;
	}

	public MapDBProcessInstance() {
	}

	@Override
	public void transform() {
		if (deleted == false) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ProcessMarshallerWriteContext context = new ProcessMarshallerWriteContext( 
						baos, null, null, null, null, this.env );
				context.setProcessInstanceId(processInstance.getId());
				context.setState(processInstance.getState() == ProcessInstance.STATE_ACTIVE ? 
						ProcessMarshallerWriteContext.STATE_ACTIVE:ProcessMarshallerWriteContext.STATE_COMPLETED);
            
				String processType = ((ProcessInstanceImpl) processInstance).getProcess().getType();
				context.stream.writeUTF( processType );
				ProcessInstanceMarshaller marshaller = ProcessMarshallerRegistry.INSTANCE.getMarshaller( processType );
            
				Object result = marshaller.writeProcessInstance( context,
                                                             processInstance);
				if( marshaller instanceof ProtobufRuleFlowProcessInstanceMarshaller && result != null ) {
					JBPMMessages.ProcessInstance _instance = (JBPMMessages.ProcessInstance)result;
					PersisterHelper.writeToStreamWithHeader( context, _instance );
				}
				context.close();
			} catch ( IOException e ) {
				throw new IllegalArgumentException( "IOException while storing process instance "
						+ processInstance.getId() + ": " + e.getMessage(), e );
			}
			byte[] newByteArray = baos.toByteArray();
			this.state = processInstance.getState();
			this.lastModificationDate = new Date();
			this.processInstanceByteArray = newByteArray;
			this.eventTypes.clear();
			for ( String type : processInstance.getEventTypes() ) {
				eventTypes.add( type );
			}
			this.processId = processInstance.getProcessId();
			((WorkflowProcessInstanceImpl) processInstance).setPersisted(true);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public int getState() {
		if (state == 0 && processInstance != null) {
			state = processInstance.getState();
		}
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Set<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(Set<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public byte[] getProcessInstanceByteArray() {
		return processInstanceByteArray;
	}

	public void setProcessInstanceByteArray(byte[] processInstanceByteArray) {
		this.processInstanceByteArray = processInstanceByteArray;
	}

	public void updateLastReadDate() {
		this.lastModificationDate = new Date();
	}

	public ProcessInstance getProcessInstance(InternalKnowledgeRuntime kruntime, Environment environment, boolean readOnly) {
		this.env = environment;
        if ( processInstance == null ) {        	
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream( processInstanceByteArray );
                MarshallerReaderContext context = new MarshallerReaderContext( bais,
                                                                               (InternalKnowledgeBase) kruntime.getKieBase(),
                                                                               null,
                                                                               null,
                                                                               ProtobufMarshaller.TIMER_READERS,
                                                                               this.env
                                                                              );
                String processInstanceType = context.stream.readUTF();
                ProcessInstanceMarshaller marshaller = ProcessMarshallerRegistry.INSTANCE.getMarshaller( processInstanceType );
            	context.wm = ((StatefulKnowledgeSessionImpl) kruntime).getInternalWorkingMemory();
                processInstance = marshaller.readProcessInstance(context);
                ((WorkflowProcessInstanceImpl) processInstance).setPersisted(false);
                if (readOnly) {
                    ((WorkflowProcessInstanceImpl) processInstance).disconnect();
                }
                context.close();
            } catch ( IOException e ) {
                e.printStackTrace();
                throw new IllegalArgumentException( "IOException while loading process instance: " + e.getMessage(),
                                                    e );
            }
        }
        return processInstance;
	}

	public Environment getEnvironment() {
		return env;
	}
	
	@Override
	public void setEnvironment(Environment env) {
		this.env = env;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	@Override
	public String getMapKey() {
		return "process";
	}

	@Override
	public boolean updateOnMap(DB db, ObjectStoringStrategy[] strategies) {
		new ProcessIndexService(db, strategies).update(this);
		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapDBProcessInstance other = (MapDBProcessInstance) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
