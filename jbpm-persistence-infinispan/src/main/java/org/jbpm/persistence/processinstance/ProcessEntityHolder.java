package org.jbpm.persistence.processinstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.drools.persistence.info.EntityHolder;
import org.drools.persistence.info.SessionInfo;
import org.drools.persistence.info.WorkItemInfo;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.infinispan.util.Base64;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.correlation.CorrelationPropertyInfo;
import org.kie.internal.process.CorrelationProperty;

@Entity
@Indexed
public class ProcessEntityHolder extends EntityHolder {

	@Field
	private String processInstanceEventTypes;
	@Field
	private Long processInstanceId;
	@Field
	private Date processInstanceLastModificationDate;
	@Field
	private Date processInstanceLastReadDate;
	@Field
	private String processId;
	@Field
	private String processInstanceByteArray;
	@Field
	private Date processInstanceStartDate;
	@Field
	private Integer processInstanceState;
	@Field
	private Integer processInstanceVersion;
	@Field
	private long correlationKeyId;
	@Field
	private String correlationKeyName;
	@Field
	private String correlationKeyProperties;

	public ProcessEntityHolder(String key, SessionInfo sessionInfo) {
		super(key, sessionInfo);
	}

	public ProcessEntityHolder(String key, WorkItemInfo workItemInfo) {
		super(key, workItemInfo);
	}
	
	public ProcessEntityHolder(String key, ProcessInstanceInfo processInstanceInfo) {
		super(key, "processInstanceInfo");
		this.processInstanceEventTypes = generateString(processInstanceInfo.getEventTypes());
		this.processInstanceId = processInstanceInfo.getId();
		this.processInstanceLastModificationDate = processInstanceInfo.getLastModificationDate();
		this.processInstanceLastReadDate = processInstanceInfo.getLastReadDate();
		this.processId = processInstanceInfo.getProcessId();
		processInstanceInfo.update();
		this.processInstanceByteArray = Base64.encodeBytes(processInstanceInfo.getProcessInstanceByteArray());
		this.processInstanceStartDate = processInstanceInfo.getStartDate();
		this.processInstanceState = processInstanceInfo.getState();
		this.processInstanceVersion = processInstanceInfo.getVersion();
	}
	
	public ProcessEntityHolder(String key, CorrelationKeyInfo correlationKeyInfo) {
		super(key, "correlationInfo");
		this.correlationKeyId = correlationKeyInfo.getId() == 0 ? correlationKeyInfo.getProperties().size() : correlationKeyInfo.getId();
		this.correlationKeyName = correlationKeyInfo.getName();
		this.processInstanceId = correlationKeyInfo.getProcessInstanceId();
		this.correlationKeyProperties = generateString(correlationKeyInfo.getProperties());
	}

	private void set(Object obj, String fieldName, Object value) {
		java.lang.reflect.Field field;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			throw new RuntimeException("Cant set field " + fieldName, e);
		}
	}
	
	public ProcessInstanceInfo getProcessInstanceInfo() {
		ProcessInstanceInfo info = new ProcessInstanceInfo();
		info.setId(this.processInstanceId);
		set(info, "lastModificationDate", this.processInstanceLastModificationDate);
		set(info, "lastReadDate", this.processInstanceLastReadDate);
		set(info, "processId", this.processId);
		set(info, "processInstanceByteArray", Base64.decode(this.processInstanceByteArray));
		set(info, "startDate", this.processInstanceStartDate);
		set(info, "state", this.processInstanceState);
		set(info, "version", this.processInstanceVersion);
		set(info, "eventTypes", toSet(this.processInstanceEventTypes));
		return info;
	}
	
	public CorrelationKeyInfo getCorrelationKeyInfo() {
		CorrelationKeyInfo info = new CorrelationKeyInfo();
		info.setName(this.correlationKeyName);
		info.setProcessInstanceId(this.processInstanceId);
		List<CorrelationPropertyInfo> props = toProperties(this.correlationKeyProperties);
		for (CorrelationPropertyInfo prop : props) {
			info.addProperty(prop);
		}
		set(info, "id", this.correlationKeyId);
		return info;
	}

	public static String generateString(List<CorrelationProperty<?>> properties) {
		StringBuilder sb = new StringBuilder();
		if (properties != null) {
			for (Iterator<CorrelationProperty<?>> iter = properties.iterator(); iter.hasNext();) {
				CorrelationProperty<?> cp = iter.next();
				sb.append(cp.getName()).append("=").append(cp.getValue());
				if (iter.hasNext()) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	
	public static String generateString(Set<String> setOfStrings) {
		StringBuilder sb = new StringBuilder();
		if (setOfStrings != null) {
			for (Iterator<String> iter = setOfStrings.iterator(); iter.hasNext(); ) {
				sb.append(iter.next());
				if (iter.hasNext()) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

	public static Set<String> toSet(String setOfStringsString) {
		if (setOfStringsString != null) {
			String[] splitted = setOfStringsString.split(",");
			return new HashSet<String>(Arrays.asList(splitted));
		} else {
			return new HashSet<String>();
		}
	}
	
	public static List<CorrelationPropertyInfo> toProperties(String properties) {
		String[] props = properties.split(",");
		List<CorrelationPropertyInfo> retval = new ArrayList<CorrelationPropertyInfo>(props.length);
		for (String prop : props) {
			String[] sub = prop.split("=");
			String key = sub[0];
			String value = sub[1];
			retval.add(new CorrelationPropertyInfo(key, value));
		}
		return retval;
	}

	public String getProcessInstanceEventTypes() {
		return processInstanceEventTypes;
	}

	public void setProcessInstanceEventTypes(String processInstanceEventTypes) {
		this.processInstanceEventTypes = processInstanceEventTypes;
	}

	public Date getProcessInstanceLastModificationDate() {
		return processInstanceLastModificationDate;
	}

	public void setProcessInstanceLastModificationDate(
			Date processInstanceLastModificationDate) {
		this.processInstanceLastModificationDate = processInstanceLastModificationDate;
	}

	public Date getProcessInstanceLastReadDate() {
		return processInstanceLastReadDate;
	}

	public void setProcessInstanceLastReadDate(Date processInstanceLastReadDate) {
		this.processInstanceLastReadDate = processInstanceLastReadDate;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getProcessInstanceByteArray() {
		return processInstanceByteArray;
	}

	public void setProcessInstanceByteArray(String processInstanceByteArray) {
		this.processInstanceByteArray = processInstanceByteArray;
	}

	public Date getProcessInstanceStartDate() {
		return processInstanceStartDate;
	}

	public void setProcessInstanceStartDate(Date processInstanceStartDate) {
		this.processInstanceStartDate = processInstanceStartDate;
	}

	public Integer getProcessInstanceState() {
		return processInstanceState;
	}

	public void setProcessInstanceState(Integer processInstanceState) {
		this.processInstanceState = processInstanceState;
	}

	public Integer getProcessInstanceVersion() {
		return processInstanceVersion;
	}

	public void setProcessInstanceVersion(Integer processInstanceVersion) {
		this.processInstanceVersion = processInstanceVersion;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}
	
	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public long getCorrelationKeyId() {
		return correlationKeyId;
	}

	public void setCorrelationKeyId(long correlationKeyId) {
		this.correlationKeyId = correlationKeyId;
	}

	public String getCorrelationKeyName() {
		return correlationKeyName;
	}

	public void setCorrelationKeyName(String correlationKeyName) {
		this.correlationKeyName = correlationKeyName;
	}

	public String getCorrelationKeyProperties() {
		return correlationKeyProperties;
	}

	public void setCorrelationKeyProperties(String correlationKeyProperties) {
		this.correlationKeyProperties = correlationKeyProperties;
	}
}
